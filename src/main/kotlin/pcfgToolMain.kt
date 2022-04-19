import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
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

    fun CoroutineScope.launchProcessor(channel: ReceiveChannel<String>) = launch(context = Dispatchers.Default) {
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
                    echo(Grammar.fromRules(rules).toString())
                } else {
                    writeToFiles(Grammar.fromRules(rules), grammar.toString())
                }
            }
        } catch (e: Exception) {
            println("Ein Fehler ist aufgetreten!")
            println(e.message)
            println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

class Parse : CliktCommand() {
    val rules by argument()
    val lexicon by argument()

    val paradigma by option("-p", "--paradigma").choice("cyk", "deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")
    val unking by option("-u", "--unking")
    val smoothing by option("-s", "--smoothing")
    val thresholdBeam by option("-t", "--threshold-beam")
    val rankBeam by option("-r", "--rank-beam")
    val kbest by option("-k", "--kbest")
    val astar by option("-a", "--astar")

    override fun run() {
        throw ProgramResult(22)
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
