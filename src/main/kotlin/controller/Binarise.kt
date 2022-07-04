package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import evaluators.ExpressionEvaluatorParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import readNotEmptyLnOrNull

class Binarise : CliktCommand() {
    override val commandHelp = "Liest eine Sequenz Konstituentenbäume von der Standardeingabe " +
            "und gibt die entsprechenden binarisierten Konstituentenbäume auf der Standardausgabe aus."

    val horizontal by option("-h", "--horizontal", help = "Horizontale Markovisierung mit H.").int().default(999)
        .check("value must be greater than null") { it > 0 }
    val vertical by option("-v", "--vertical", help = "Vertikale Markovisierung mit V.").int().default(1).check("value must be greater than null") { it > 0 }
    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.produceBinarisedTreesAsStrings(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluatorParser = ExpressionEvaluatorParser()
            for ((lineNumber, unbinarisedTreeAsString) in channel) {
                outputChannel.send(
                    lineNumber to expressionEvaluatorParser.parseToEnd(unbinarisedTreeAsString)
                        .binarise(vertical, horizontal)
                        .toString()
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceStringsFromInput() = produce(context = Dispatchers.IO, capacity = 10) {
        generateSequence(readNotEmptyLnOrNull).forEachIndexed { i, sentence ->
            send(Pair(i + 1, sentence))
        }
    }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = produceStringsFromInput()

                launch {
                    repeat(numberOfParallelParsers) {
                        produceBinarisedTreesAsStrings(
                            producer
                        )
                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }

                printTreesInOrder(outputChannel)
            }
        } catch (e: ParseException) {
            System.err.println("Ungültige Eingabe! Bitte geben Sie Bäume im Penn Treebank Format ein!")
            throw ProgramResult(5)
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}