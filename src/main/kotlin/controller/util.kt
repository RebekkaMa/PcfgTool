package controller

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import model.Rule
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