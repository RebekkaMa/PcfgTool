import Util.format
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.utils.Tuple3
import evaluators.ExpressionEvaluator
import evaluators.LexiconExpressionEvaluator
import evaluators.OutsideScoreEvaluator
import evaluators.RulesExpressionEvaluator
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import model.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    PcfgTool().subcommands(
        Induce(), Parse(), Binarise(), Debinarise(), Unk(), Smooth(),
        Outside()
    ).main(args)
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

    private val rulesChannel = Channel<ArrayList<Rule>>(capacity = Channel.UNLIMITED)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceString() = produce<String>(context = Dispatchers.IO, capacity = 5) {
        var line = readNotEmptyLnOrNull()
        while (line != null && isActive) {
            send(line)
            line = readNotEmptyLnOrNull()
        }
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<String>) =
        launch(context = Dispatchers.Default) {
            val expressionEvaluator = ExpressionEvaluator()
            for (expression in channel) {
                rulesChannel.send(expressionEvaluator.parseToEnd(expression).transformToRules())
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
                        System.err.println("Ungültige Eingabe! Bitte geben Sie Bäume im Penn Treebank Format ein!")
                        exitProcess(5)
                    }
                }
                val rules = rulesChannel.receiveAsFlow().flatMapConcat { it.asFlow() }
                    .toList()

                grammar?.let { writeGrammarToFiles(Grammar.createFromRules(rules = rules), grammar.toString()) }
                    ?: println(Grammar.createFromRules(rules = rules).toString())
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

    init {
        eagerOption("-k", "--kbest") {
            exitProcess(22)
        }
    }

    val paradigma by option("-p", "--paradigma").choice("cyk", "deductive").default("deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")
    val numberOfParallelParsers by option("-c", "--number-parallel-parsers").int().default(2)
        .check("value must be greater than null") { it > 0 }
    val unking by option("-u", "--unking").flag(default = false)
    val smoothing by option("-s", "--smoothing").flag(default = false)
    val astar by option("-a", "--astar").file(mustExist = true)

    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)

    private val thresholdBeam by option("-t", "--threshold-beam").double().check { it in 0.0..1.0 }
    private val rankBeam by option("-r", "--rank-beam").int().check { it in 1..Int.MAX_VALUE }

    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.launchProcessor(
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
                    lexiconByString[initial] ?: 0,
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


    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceString() = produce(context = Dispatchers.IO, capacity = 10) {
        generateSequence(readNotEmptyLnOrNull).forEachIndexed { i, sentence ->
            send(Pair(i + 1, sentence))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {

                if (paradigma == "cyk") {
                    exitProcess(22)
                }

                val getRulesFromRulesFile = async { getRulesFromFile(rules, RulesExpressionEvaluator()) }
                val getRulesFromLexiconFile = async { getRulesFromFile(lexicon, LexiconExpressionEvaluator()) }

                val grammar = Grammar.create(
                    initialNonterminal,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )

                val (accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal, lexiconByInt, lexiconByString, numberNonTerminals) = grammar.getGrammarDataStructuresForParsing()

                val outsideScoreEvaluator = OutsideScoreEvaluator()
                var outsideScores: MutableMap<Int, Double>? = null

                try {
                    astar?.apply {
                        outsideScores = mutableMapOf()
                        this.forEachLine {
                            val (nonTerminal, score) = outsideScoreEvaluator.parseToEnd(it)
                            val nonTerminalAsInt =
                                lexiconByString[nonTerminal]
                                    ?: throw Exception("Missing Nonterminal in Outside Score File")
                            outsideScores!![nonTerminalAsInt] = score
                        }
                    }
                } catch (e: ParseException) {
                    System.err.println("Ungültige Eingabe! Bitte geben Sie Outside Scores im richtigen Format ein!")
                    exitProcess(5)
                }

                val producer = produceString()

//                startProcessor(numberOfParallelParsers, outputChannel) {
//                    launchProcessor(
//                        producer,
//                        grammar.initial,
//                        accessRulesBySecondNtOnRhs,
//                        accessRulesByFirstNtOnRhs,
//                        accessChainRulesByNtRhs,
//                        accessRulesByTerminal,
//                        lexiconByInt,
//                        lexiconByString,
//                        numberNonTerminals,
//                        outsideScores
//                    )
//                }


                launch {
                    repeat(numberOfParallelParsers) {
                        launchProcessor(
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
        } catch (e: java.lang.Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            throw ProgramResult(1)
        }
    }
}

class Binarise : CliktCommand() {
    val horizontal by option("-h", "--horizontal").int().default(999)
        .check("value must be greater than null") { it > 0 }
    val vertical by option("-v", "--vertical").int().default(1).check("value must be greater than null") { it > 0 }
    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }


    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for ((sentenceNumber, sentence) in channel) {
                outputChannel.send(
                    sentenceNumber to expressionEvaluator.parseToEnd(sentence).binarise(vertical, horizontal).toString()
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceString() = produce(context = Dispatchers.IO, capacity = 10) {
        generateSequence(readNotEmptyLnOrNull).forEachIndexed { i, sentence ->
            send(Pair(i + 1, sentence))
        }
    }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = produceString()

                launch {
                    repeat(numberOfParallelParsers) {
                        launchProcessor(
                            producer
                        )
                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }

                printTreesInOrder(outputChannel)
            }
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

class Debinarise : CliktCommand() {

    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for ((sentenceNumber, sentence) in channel) {
                outputChannel.send(sentenceNumber to expressionEvaluator.parseToEnd(sentence).debinarise().toString())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.readLinesFromInput() = produce(context = Dispatchers.IO, capacity = 10) {
        generateSequence(readNotEmptyLnOrNull).forEachIndexed { i, sentence ->
            send(Pair(i + 1, sentence))
        }
    }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = readLinesFromInput()

                launch {
                    repeat(numberOfParallelParsers) {
                        launchProcessor(
                            producer
                        )
                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }

                printTreesInOrder(outputChannel)
            }
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

class Unk : CliktCommand() {
    val threshold by option("-t", "--threshold").int().default(2).validate { it > 0 }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val expressionEvaluator = ExpressionEvaluator()
                val trees = generateSequence(readNotEmptyLnOrNull).map { sentence ->
                    expressionEvaluator.parseToEnd(sentence)
                }.toList()
                val wordcount = getTerminalCountFromCorpus(trees)
                trees.onEach {
                    replaceRareWordsInTree(smooth = false, wordcount, threshold, it)
                    println(it)
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

class Smooth : CliktCommand() {
    val threshold by option("-t", "--threshold").int().default(3)

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val expressionEvaluator = ExpressionEvaluator()
                val trees = generateSequence(readNotEmptyLnOrNull).map { sentence ->
                    expressionEvaluator.parseToEnd(sentence)
                }.toList()
                val wordcount = getTerminalCountFromCorpus(trees)

                val smoothedTree = trees.map {
                    async { //TODO
                        replaceRareWordsInTree(smooth = true, wordcount, threshold, it)
                    }
                }

                smoothedTree.forEach {
                    println(it.await())
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

class Outside : CliktCommand() {
    val initial by option("-i", "--initial-nonterminal").default("ROOT")
    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)
    private val outputFileName by argument(name = "grammar").optional()


    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {

                val getRulesFromRulesFile = async { getRulesFromFile(rules, RulesExpressionEvaluator()) }
                val getRulesFromLexiconFile = async { getRulesFromFile(lexicon, LexiconExpressionEvaluator()) }

                val grammar = Grammar.create(
                    initial,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )
                val outSideWeights = grammar.getViterbiOutsideScores()
                if (outputFileName.isNullOrEmpty()) {
                    outSideWeights.forEach {
                        println(it.key + " " + it.value.format(15))
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

fun CoroutineScope.printTreesInOrder(outputChannel: ReceiveChannel<Pair<Int, String>>) = launch {
    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
    var i = 1
    for ((lineNumber, treeAsString) in outputChannel) {
        if (lineNumber == i) {
            println(treeAsString)
            i++
            while ((queue.peek()?.first ?: 0) == i) {
                println(queue.poll().second)
                i++
            }
        } else {
            queue.add(lineNumber to treeAsString)
        }
    }
}

fun getRulesFromFile(
    file: File,
    evaluator: com.github.h0tk3y.betterParse.grammar.Grammar<Pair<Rule, Double>>
): Sequence<Pair<Rule, Double>> {
    val rulesBr = file.bufferedReader()
    return generateSequence { rulesBr.readLine() }.map {
        evaluator.parseToEnd(
            it
        )
    }
}


fun CoroutineScope.startProcessor(
    numberOfParallelParsers: Int,
    outputChannel: Channel<Pair<Int, String>>,
    processor: () -> Job
) = launch {
    repeat(numberOfParallelParsers) {
        processor()
    }
}.invokeOnCompletion { outputChannel.close() }
