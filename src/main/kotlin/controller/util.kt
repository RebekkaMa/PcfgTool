package controller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import model.Rule
import readNotEmptyLnOrNull
import java.io.File
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.produceStringsFromInput() = produce(context = Dispatchers.IO, capacity = 10) {
    generateSequence(readNotEmptyLnOrNull).forEachIndexed { i, sentence ->
        send(Pair(i + 1, sentence))
    }
}

fun CoroutineScope.printTreesInOrder(outputChannel: ReceiveChannel<Pair<Int, String>>) = launch {
    val queue = PriorityQueue(10, compareBy<Pair<Int, String>> { it.first })
    var i = 1
    val start = System.currentTimeMillis()
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
    //println(System.currentTimeMillis() - start)
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
