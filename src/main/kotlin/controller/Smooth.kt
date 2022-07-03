package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import evaluators.ExpressionEvaluatorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import model.getTerminalCountFromCorpus
import model.replaceRareWordsInTree
import readNotEmptyLnOrNull

class Smooth : CliktCommand() {
    override val commandHelp = "Liest eine Sequenz Konstituentenbäume von der Standardeingabe " +
            "und gibt die durch Smoothing erhaltenen Bäume auf der " +
            "Standardausgabe aus."

    val threshold by option("-t", "--threshold", help = "Schwellwert der absoluten Häufigkeit für das Unking.").int().default(3)

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val expressionEvaluatorParser = ExpressionEvaluatorParser()
                val trees = generateSequence(readNotEmptyLnOrNull).map { sentence ->
                    expressionEvaluatorParser.parseToEnd(sentence)
                }.toList()
                val wordCount = getTerminalCountFromCorpus(trees)

                val smoothedTree = trees.map {
                    async { //TODO
                        replaceRareWordsInTree(smooth = true, wordCount, threshold, it)
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