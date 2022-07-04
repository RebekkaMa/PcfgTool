package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.utils.Tuple3
import evaluators.LexiconExpressionParser
import evaluators.OutsideScoreParser
import evaluators.RulesExpressionParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import model.DeductiveParser
import model.Grammar
import model.getSignature
import kotlin.system.exitProcess

class Parse : CliktCommand() {
    override val commandHelp = "Liest eine Sequenz natürlichsprachiger Sätze von der Standardeingabe und gibt die" +
            " zugehörigen besten Parsebäume im PTB-Format bzw. (NOPARSE <Satz>) auf der Standardausgabe aus. RULES" +
            " und LEXICON sind die Dateinamen der PCFG."

    init {
        eagerOption("-k", "--kbest") {
            exitProcess(22)
        }
    }

    val paradigma by option(
        "-p",
        "--paradigma",
        help = "Parserparadigma (cyk[nicht implementiert] oder deductive)."
    ).choice("cyk", "deductive").default("deductive")
    val initialNonterminal by option(
        "-i",
        "--initial-nonterminal",
        help = "Definiere N als Startnichtterminal."
    ).default("ROOT")
    val numberOfParallelParsers by option("-c", "--number-parallel-parsers").int().default(2)
        .check("value must be greater than null") { it > 0 }
    val unking by option("-u", "--unking", help = "Ersetze unbekannte Wörter durch UNK.").flag(default = false)
    val smoothing by option(
        "-s", "--smoothing", help = "Ersetze unbekannte Wörter gemäß der Smoothing-Implementierung."
    ).flag(default = false)
    val astar by option(
        "-a", "--astar", help = "Führe A*-Suche durch. Lade die Outside weights aus der Datei PATH."
    ).file(mustExist = true)

    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)

    private val thresholdBeam by option(
        "-t",
        "--threshold-beam",
        help = "Führe Beam-Search durch mit Threshold."
    ).double().check { it in 0.0..1.0 }
    private val rankBeam by option(
        "-r",
        "--rank-beam",
        help = "Führe Beam-Search durch mit Beam konstanter Größe."
    ).int().check { it in 1..Int.MAX_VALUE }

    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {

                if (paradigma == "cyk") {
                    exitProcess(22)
                }

                val getRulesFromRulesFile = async { getRulesFromFile(rules, RulesExpressionParser()) }
                val getRulesFromLexiconFile = async { getRulesFromFile(lexicon, LexiconExpressionParser()) }

                val grammar = Grammar.create(
                    initialNonterminal,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )

                val (accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal, lexiconByInt, lexiconByString, numberNonTerminals) = grammar.getGrammarDataStructuresForParsing()

                val outsideScoreParser = OutsideScoreParser()
                var outsideScores: MutableMap<Int, Double>? = null

                try {
                    astar?.apply {
                        outsideScores = mutableMapOf()
                        this.forEachLine {
                            val (nonTerminal, score) = outsideScoreParser.parseToEnd(it)
                            val nonTerminalAsInt = lexiconByString[nonTerminal]
                                ?: throw Exception("Missing Nonterminal in Outside Score File")
                            outsideScores!![nonTerminalAsInt] = score
                        }
                    }
                } catch (e: ParseException) {
                    System.err.println("Ungültige Eingabe! Bitte geben Sie Outside Scores im richtigen Format ein!")
                    exitProcess(5)
                }
                val producer = produceStringsFromInput()

                launch {
                    repeat(numberOfParallelParsers) {
                        produceParseTreesAsStrings(
                            producer,
                            grammar.initial,
                            accessRulesBySecondNtOnRhs,
                            accessRulesByFirstNtOnRhs,
                            accessChainRulesByNtRhs,
                            accessRulesByTerminal,
                            lexiconByInt,
                            lexiconByString,
                            numberNonTerminals,
                            outsideScores
                        )

                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }
                printTreesInOrder(outputChannel)
            }
        } catch (e: ParseException) {
            System.err.println("Ungültige Grammatik! Bitte verwenden Sie eine binarisierte PCFG!")
            throw ProgramResult(5)
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            throw ProgramResult(1)
        }
    }

    private fun CoroutineScope.produceParseTreesAsStrings(
        channel: ReceiveChannel<Pair<Int, String>>,
        initial: String,
        accessRulesBySecondNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
        accessRulesByFirstNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
        accessChainRulesByNtRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
        accessRulesByTerminal: MutableMap<Int, MutableList<Tuple3<Int, IntArray, Double>>>,
        lexiconByInt: Map<Int, String>,
        lexiconByString: Map<String, Int>,
        numberNonTerminals: Int,
        outsideScores: Map<Int, Double>?,
    ) =
        launch {
            for ((sentenceNumber, sentence) in channel) {
                val tokensAsString = sentence.split(" ")
                val tokensAsInt = replaceTokensByInts(lexiconByString, tokensAsString, unking, smoothing)

                if (-1 in tokensAsInt) {
                    outputChannel.send(sentenceNumber to "(NOPARSE ${sentence})")
                    continue
                }

                val (_, parsedTreeItem) = DeductiveParser(
                    lexiconByString[initial]
                        ?: throw Exception("Parse: produceParseTreesAsStrings -> Initial ist nicht im Lexikon"),
                    accessRulesBySecondNtOnRhs,
                    accessRulesByFirstNtOnRhs,
                    accessChainRulesByNtRhs,
                    accessRulesByTerminal,
                    outsideScores,
                    thresholdBeam = thresholdBeam,
                    rankBeam = rankBeam,
                    (numberNonTerminals * tokensAsInt.size * 0.21).toInt(),
                ).weightedDeductiveParsing(tokensAsInt)
                outputChannel.send(
                    sentenceNumber to (parsedTreeItem?.getBacktraceAsString(tokensAsString, lexiconByInt)
                        ?: "(NOPARSE ${sentence})")
                )
            }
        }

    companion object {
        fun replaceTokensByInts(
            lexiconByString: Map<String, Int>,
            tokensAsString: List<String>,
            unking: Boolean,
            smoothing: Boolean
        ): IntArray {
            return tokensAsString.mapIndexed { index, word ->
                val wordAsInt = lexiconByString[word]
                return@mapIndexed when {
                    wordAsInt != null -> wordAsInt //TODO
                    smoothing -> lexiconByString[getSignature(word, index + 1)] ?: -1
                    unking -> lexiconByString["UNK"] ?: -1
                    else -> -1
                }
            }.toIntArray()
        }
    }
}