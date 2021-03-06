package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.parser.ParseException
import expressionParser.LexiconExpressionParser
import expressionParser.RulesExpressionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import model.Grammar

class Outside : CliktCommand() {
    override val commandHelp = "Berechnet Viterbi Outside weights für jedes Nichtterminal " +
            "der Grammatik und gibt diese auf der Standardausgabe aus. " +
            "Wird das optionale Argument GRAMMAR angegeben, dann werden " +
            "die Outside weights in die Datei GRAMMAR.outside " +
            "gespeichert."


    val initial by option("-i", "--initial-nonterminal", help = "Definiere N als Startnichtterminal.").default("ROOT")
    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)
    private val outputFileName by argument(name = "grammar").optional()


    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val getRulesFromRulesFile = async { getRulesFromFile(rules, RulesExpressionParser()) }
                val getRulesFromLexiconFile = async { getRulesFromFile(lexicon, LexiconExpressionParser()) }

                val grammar = Grammar.create(
                    initial,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )
                val outSideWeights = grammar.getViterbiOutsideScores()
                if (outputFileName.isNullOrEmpty()) {
                    outSideWeights.forEach {(nonTerminal, value) ->
                        println("$nonTerminal $value")
                    }
                } else {
                    writeOutsideScoreToFiles(outSideWeights, outputFileName!!)
                }
            }
        } catch (e: ParseException) {
            System.err.println("Ungültige Grammatik! Bitte verwenden Sie eine binarisierte PCFG!")
            throw ProgramResult(5)
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}