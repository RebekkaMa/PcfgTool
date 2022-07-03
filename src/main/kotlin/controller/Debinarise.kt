package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import evaluators.ExpressionEvaluatorParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Debinarise : CliktCommand() {
    override val commandHelp = "Liest eine Sequenz (binarisierter) Konstituentenbäume von der " +
            "Standardeingabe und gibt die ursprünglichen (nicht " +
            "binarisierten) Konstituentenbäume auf der Standardausgabe aus."

    private val numberOfParallelParsers by option("-p", "--number-parallel-parsers").int().default(6)
        .validate { it > 0 }

    private val outputChannel = Channel<Pair<Int, String>>(Channel.UNLIMITED)

    private fun CoroutineScope.produceDebinarisedTreesAsStrings(
        channel: ReceiveChannel<Pair<Int, String>>
    ) =
        launch {
            val expressionEvaluatorParser = ExpressionEvaluatorParser()
            for ((lineNumber, binarisedTreeAsString) in channel) {
                outputChannel.send(lineNumber to expressionEvaluatorParser.parseToEnd(binarisedTreeAsString).debinarise().toString())
            }
        }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = produceStringsFromInput()

                launch {
                    repeat(numberOfParallelParsers) {
                        produceDebinarisedTreesAsStrings(
                            producer
                        )
                    }
                }.invokeOnCompletion {
                    outputChannel.close()
                }

                printTreesInOrder(outputChannel)
            }
        } catch (e: ParseException) {
            System.err.println("Ungültiger Baumkorpus! Bitte geben Sie den Baumkorpus im Penn Treebank Format ein!")  //TODO
            throw ProgramResult(5)
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}
