import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import evaluators.ExpressionEvaluator
import evaluators.LexiconExpressionEvaluator
import evaluators.RulesExpressionEvaluator
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import model.DeductiveParser
import model.Grammar
import model.Rule
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    PcfgTool().subcommands(Induce(), Parse(), Binarise(), Debinarise(), Unk(), Smooth(), Outside()).main(args)
}

class PcfgTool : CliktCommand() {
    override val commandHelp = """
        Tools zum PCFG-basierten Parsing natürlichsprachiger Sätze
    """

    override fun run() = Unit
}

class Induce : CliktCommand() {
    override val commandHelp = """
        Liest eine Sequenz Konstituentenbäume von der Standardeingabe und gibt eine aus diesen Bäumen induzierte PCFG auf der Standardausgabe aus.
    """

    private val grammar by argument(help = "PCFG wird in den Dateien GRAMMAR.rules, GRAMMAR.lexicon und GRAMMAR.words gespeichert").optional()

    private val readNotEmptyLnOrNull = { val line = readlnOrNull(); if (line.isNullOrEmpty()) null else line }
    private val rulesChannel = Channel<ArrayList<Rule>>(capacity = Channel.UNLIMITED)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceString() = produce<String>(context = Dispatchers.IO, capacity = 10) {
        var line = readNotEmptyLnOrNull()
        while (line != null && isActive) {
            send(line)
            line = readNotEmptyLnOrNull()
        }
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<String>) = launch(context = Dispatchers.Default) {
        val expressionEvaluator = ExpressionEvaluator()
        for (expression in channel) {
            rulesChannel.send(expressionEvaluator.parseToEnd(expression).parseToRules())
        }
    }

    @OptIn(FlowPreview::class)
    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = produceString()

                launch {
                    try {
                        coroutineScope {
                            repeat(8) {
                                launchProcessor(producer)
                            }
                        }
                        rulesChannel.close()
                    } catch (e: ParseException) {
                        println("Ungültige Eingabe! Bitte geben Sie Bäume im Penn Treebank Format ein!")
                        exitProcess(5)
                    }
                }
                val rules = rulesChannel.receiveAsFlow().flatMapConcat { it.asFlow() }
                    .toList()
                if (grammar == null) {
                    echo(Grammar.createFromRules(rules).toString())
                } else {
                    writeToFiles(Grammar.createFromRules(rules), grammar.toString())
                }
            }
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

class Parse : CliktCommand() {
    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)

    val paradigma by option("-p", "--paradigma").choice("cyk", "deductive").default("deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")

    val unking by option("-u", "--unking")
    val smoothing by option("-s", "--smoothing")
    val thresholdBeam by option("-t", "--threshold-beam")
    val rankBeam by option("-r", "--rank-beam")
    val kbest by option("-k", "--kbest")
    val astar by option("-a", "--astar")

    private val readNotEmptyLnOrNull = { val line = readlnOrNull(); if (line.isNullOrEmpty()) null else line }

    override fun run() {
        try {

            runBlocking(Dispatchers.Default) {

                if (!(unking.isNullOrEmpty() || smoothing.isNullOrEmpty() || thresholdBeam.isNullOrEmpty() || rankBeam.isNullOrEmpty() || kbest.isNullOrEmpty() || astar.isNullOrEmpty() || paradigma == "cyk")) {
                    throw ProgramResult(22)
                }

                val getRulesFromFile = async {
                    val rulesBr = rules.bufferedReader(); generateSequence { rulesBr.readLine() }.map {
                    RulesExpressionEvaluator().parseToEnd(
                        it
                    )
                }
                }

                val getLexiconsFromFile = async {
                    val lexiconBr = lexicon.bufferedReader(); generateSequence { lexiconBr.readLine() }.map {
                    LexiconExpressionEvaluator().parseToEnd(
                        it
                    )
                }
                }

                val grammar = Grammar.create(
                    initialNonterminal,
                    (getLexiconsFromFile.await() + getRulesFromFile.await()).toMap()
                )

                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()

                val rules = generateSequence(readNotEmptyLnOrNull).map {
                    async {
                        DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical).weightedDeductiveParsing(
                            it.split(" ")
                        )
                    }
                }

                rules.forEach {
                    val result = it.await()
                    if (result.second != null) {
                        echo(result.second?.t5?.getTree()) //TODO
                        //println(result.second!!.t4)
                    } else {
                        echo("(NOPARSE " + result.first.joinToString(" ") + ")")
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

class Binarise : CliktCommand() {
    val horizontal by option("-h", "--horizontal").int().default(999)
    val vertical by option("-v", "--vertical").int().default(1)

    override fun run() {
        throw ProgramResult(22)
    }
}

class Debinarise : CliktCommand() {
    override fun run() {
        throw ProgramResult(22)
    }
}

class Unk : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        throw ProgramResult(22)
    }
}

class Smooth : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        throw ProgramResult(22)
    }
}

class Outside : CliktCommand() {
    val initial by option("-i", "--initial-nonterminal").default("ROOT")

    override fun run() {
        throw ProgramResult(22)
    }
}

