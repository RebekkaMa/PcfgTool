import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.utils.Tuple3
import evaluators.ExpressionEvaluator
import evaluators.LexiconExpressionEvaluator
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

    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }
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
                        System.err.println("Ungültige Eingabe! Bitte geben Sie Bäume im Penn Treebank Format ein!")
                        exitProcess(5)
                    }
                }
                val rules = rulesChannel.receiveAsFlow().flatMapConcat { it.asFlow() }
                    .toList()
                if (grammar == null) {
                    echo(Grammar.createFromRules(rules = rules).toString())
                } else {
                    writeGrammarToFiles(Grammar.createFromRules(rules = rules), grammar.toString())
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
    init {

        eagerOption("-t", "--threshold-beam") {
            throw ProgramResult(22)
        }
        eagerOption("-r", "--rank-beam") {
            throw ProgramResult(22)
        }
        eagerOption("-k", "--kbest") {
            throw ProgramResult(22)
        }
        eagerOption("-a", "--astar") {
            throw ProgramResult(22)
        }
    }

    val paradigma by option("-p", "--paradigma").choice("cyk", "deductive").default("deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")
    val numberOfParallelParsers by option("-c", "--number-parallel-parsers").int().default(2).check("value must be greater than null") { it > 0 }
    val unking by option("-u", "--unking").flag(default = false)
    val smoothing by option("-s", "--smoothing").flag(default = false)

    val rules by argument().file(mustExist = true)
    val lexicon by argument().file(mustExist = true)

    private val readNotEmptyLnOrNull = { val line = readlnOrNull(); if (line.isNullOrEmpty()) null else line }
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
        numberNonTerminals: Int
    ) =
        launch {
            for (line in channel) {
                val tokensAsString = line.second.split(" ")
                val tokensAsInt = replaceTokensByInts(lexiconByString, tokensAsString,unking,smoothing)

                if (-1 in tokensAsInt) {
                    outputChannel.send(line.first to "(NOPARSE ${line.second})")
                    continue
                }

                val result = DeductiveParser(
                    lexiconByString[initial] ?: 0,
                    accessRulesBySecondNtOnRhs,
                    accessRulesByFirstNtOnRhs,
                    accessChainRulesByNtRhs,
                    accessRulesByTerminal,
                    (numberNonTerminals * tokensAsInt.size * 0.21).toInt(),
                ).weightedDeductiveParsing(tokensAsInt)

                if (result.second != null) {
                    outputChannel.send(
                        line.first to result.second!!.getParseTreeAsString(
                            tokensAsString,
                            lexiconByInt
                        )
                    )
                } else {
                    outputChannel.send(line.first to "(NOPARSE ${line.second})")
                }
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

                val getRulesFromRulesFile = async {
                    val rulesBr = rules.bufferedReader()
                    generateSequence { rulesBr.readLine() }.map {
                        RulesExpressionEvaluator().parseToEnd(
                            it
                        )
                    }
                }

                val getRulesFromLexiconFile = async {
                    val lexiconBr = lexicon.bufferedReader()
                    generateSequence { lexiconBr.readLine() }.map {
                        LexiconExpressionEvaluator().parseToEnd(
                            it
                        )
                    }
                }

                val grammar = Grammar.create(
                    initialNonterminal,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )

                val (accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal, lexiconByInt, lexiconByString, numberNonTerminals) = grammar.getGrammarDataStructuresForParsing()

                val producer = produceString()

                val parser = launch {
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
                            numberNonTerminals
                        )

                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }

                launch {
                    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
                    var i = 1
                    for (parseResult in outputChannel) {
                        if (parseResult.first == i) {
                            echo(parseResult.second)
                            i++
                            while ((queue.peek()?.first ?: 0) == i) {
                                echo(queue.poll().second)
                                i++
                            }
                        } else {
                            queue.add(parseResult)
                        }
                    }
                }
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
    val horizontal by option("-h", "--horizontal").int().default(999).check("value must be greater than null") { it > 0 }
    val vertical by option("-v", "--vertical").int().default(1).check("value must be greater than null") { it > 0 }
    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }


    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }
    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for (line in channel) {
                outputChannel.send(line.first to expressionEvaluator.parseToEnd(line.second).binarise(vertical, horizontal).toString())
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
                launch {
                    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
                    var i = 1
                    for (parseResult in outputChannel) {
                        if (parseResult.first == i) {
                            echo(parseResult.second)
                            i++
                            while ((queue.peek()?.first ?: 0) == i) {
                                echo(queue.poll().second)
                                i++
                            }
                        } else {
                            queue.add(parseResult)
                        }
                    }
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

class Debinarise : CliktCommand() {

    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }
    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.launchProcessor(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for (line in channel) {
                outputChannel.send(line.first to expressionEvaluator.parseToEnd(line.second).debinarise().toString())
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
                launch {
                    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
                    var i = 1
                    for (parseResult in outputChannel) {
                        if (parseResult.first == i) {
                            echo(parseResult.second)
                            i++
                            while ((queue.peek()?.first ?: 0) == i) {
                                echo(queue.poll().second)
                                i++
                            }
                        } else {
                            queue.add(parseResult)
                        }
                    }
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

class Unk : CliktCommand() {
    val threshold by option("-t", "--threshold").int().default(2).validate { it > 0 }
    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }
    private val treeChannel = Channel<Pair<Int, Tree>>(Channel.UNLIMITED)
    private val treeWithUnkChannel = Channel<Pair<Int, Tree>>(Channel.UNLIMITED)
    private val treeAsStringChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.transformExpressionToTreeJob(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for (line in channel) {
                treeChannel.send(line.first to expressionEvaluator.parseToEnd(line.second))
            }
        }

    private fun CoroutineScope.transformTreeToTreeWithUnkJob(
        wordcount: MutableMap<String, Int>,
        threshold: Int,
    ) =
        launch {
            val expressionEvaluator = ExpressionEvaluator()
            for (tree in treeWithUnkChannel) {
                replaceRareWordsInTree(smooth = false, wordcount, threshold, tree.second)
                treeAsStringChannel.send(tree.first to tree.second.toString())
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
                val expressionEvaluator = ExpressionEvaluator()
                val trees = generateSequence(readNotEmptyLnOrNull).map{
                    sentence -> expressionEvaluator.parseToEnd(sentence)
                }.toList()
                val wordcount = getTerminalCountFromCorpus(trees)
                trees.onEach {
                    replaceRareWordsInTree(smooth = false, wordcount, threshold, it)
                    echo(it)
                }



//                val producer = produceString()
//
//                launch {
//                    repeat(numberOfParallelParsers) {
//                        transformExpressionToTreeJob(
//                            producer
//                        )
//                    }
//                }.invokeOnCompletion {
//                    treeChannel.close()
//                }
//                val job = launch(start = CoroutineStart.LAZY) {
//                    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
//                    var i = 1
//                    for (parseResult in treeAsStringChannel) {
//                        if (parseResult.first == i) {
//                            echo(parseResult.second)
//                            i++
//                            while ((queue.peek()?.first ?: 0) == i) {
//                                echo(queue.poll().second)
//                                i++
//                            }
//                        } else {
//                            queue.add(parseResult)
//                        }
//                    }
//                }
//
//                launch {
//                    val trees = mutableMapOf<Int,Tree>()
//                    val treeList = mutableListOf<Tree>()
//                    for (tree in treeChannel) {
//                        trees[tree.first] = tree.second
//                        treeList.add(tree.second)
//                    }
//                    val wordcount = Unking().getTerminalCountFromCorpus(treeList)
//                    trees.forEach { (i, tree) ->
//                        treeWithUnkChannel.send(i to tree)
//                    }
//                    launch {
//                        repeat(numberOfParallelParsers){
//                            transformTreeToTreeWithUnkJob(wordcount, threshold)
//                        }
//                    }.invokeOnCompletion {
//                        treeAsStringChannel.close()
//                    }
//                    job.start()
//                }



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
    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val expressionEvaluator = ExpressionEvaluator()
                val trees = generateSequence(readNotEmptyLnOrNull).map{
                        sentence -> expressionEvaluator.parseToEnd(sentence)
                }.toList()
                val wordcount = getTerminalCountFromCorpus(trees)
                trees.onEach {
                    replaceRareWordsInTree(smooth = true, wordcount, threshold, it)
                    echo(it)
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


    private val readNotEmptyLnOrNull = {
        val line = readlnOrNull()
        if (line.isNullOrEmpty()) null else line
    }


    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {

                val getRulesFromRulesFile = async {
                    val rulesBr = rules.bufferedReader()
                    generateSequence { rulesBr.readLine() }.map {
                        RulesExpressionEvaluator().parseToEnd(
                            it
                        )
                    }
                }

                val getRulesFromLexiconFile = async {
                    val lexiconBr = lexicon.bufferedReader()
                    generateSequence { lexiconBr.readLine() }.map {
                        LexiconExpressionEvaluator().parseToEnd(
                            it
                        )
                    }
                }
                val grammar = Grammar.create(
                    initial,
                    (getRulesFromLexiconFile.await() + getRulesFromRulesFile.await()).toMap()
                )
                val outSideWeights = grammar.viterbiOutsideScore()
                if (outputFileName.isNullOrEmpty()) {
                    outSideWeights.forEach {
                        echo(it.key + " " + it.value)
                    }
                } else {
                    writeOutsideScoreToFiles(outSideWeights, outputFileName!!)
                }
            }
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
        throw ProgramResult(22)
    }
}


