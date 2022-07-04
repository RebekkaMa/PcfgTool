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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import model.getTerminalCountFromCorpus
import model.replaceRareWordsInTree
import readNotEmptyLnOrNull

class Unk : CliktCommand() {
    override val commandHelp = "Liest eine Sequenz Konstituentenbäume von der Standardeingabe " +
            "und gibt die durch triviales Unking erhaltenen Bäume auf der " +
            "Standardausgabe aus."

    val threshold by option("-t", "--threshold", help = "Schwellwert der absoluten Häufigkeit für das Unking.").int().default(2).validate { it > 0 }

    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val start = System.currentTimeMillis()

                val expressionEvaluatorParser = ExpressionEvaluatorParser()
                val trees = generateSequence(readNotEmptyLnOrNull).map { notUnkedTreeAsString ->
                    expressionEvaluatorParser.parseToEnd(notUnkedTreeAsString)
                }.toList()

                val wordcount = getTerminalCountFromCorpus(trees)

                trees.onEach {tree ->
                    replaceRareWordsInTree(smooth = false, wordcount, threshold, tree)
                    println(tree)
                }

                println(System.currentTimeMillis() - start)

            }
        } catch (e: ParseException) {
            System.err.println("Ungültige Eingabe! Bitte geben Sie Bäume im Penn Treebank Format ein!")
            throw ProgramResult(5)
        }catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}

//class Unk : CliktCommand() {
//    override val commandHelp = "Liest eine Sequenz Konstituentenbäume von der Standardeingabe " +
//            "und gibt die durch triviales Unking erhaltenen Bäume auf der " +
//            "Standardausgabe aus."
//
//    val threshold by option("-t", "--threshold", help = "Schwellwert der absoluten Häufigkeit für das Unking.").int().default(2).validate { it > 0 }
//
//
//
//    override fun run() {
//        try {
//            runBlocking(Dispatchers.Default) {
//                val start = System.currentTimeMillis()
//
//                val expressionEvaluatorParser = ExpressionEvaluatorParser()
//
//                val trees = generateSequence(readNotEmptyLnOrNull).map { notUnkedTreeAsString ->
//
//                    expressionEvaluatorParser.parseToEnd(notUnkedTreeAsString)
//                }.toList()
//
//                val wordcount = getTerminalCountFromCorpus(trees)
//                val unkedTrees = mutableListOf<Deferred<Tree>>()
//
//                trees.forEach {tree ->
//                    unkedTrees.add(
//                        async {
//                            replaceRareWordsInTree(smooth = false, wordcount, threshold, tree)
//                        }
//                    )
//
//                }
//                unkedTrees.forEach { println(it.await()) }
//                println(System.currentTimeMillis() - start)
//
//            }
//        } catch (e: ParseException) {
//            System.err.println("Ungültiger Baumkorpus! Bitte geben Sie den Baumkorpus im Penn Treebank Format ein!")
//            throw ProgramResult(5)
//        }catch (e: Exception) {
//            System.err.println("Ein Fehler ist aufgetreten!")
//            System.err.println(e.message)
//            System.err.println(e.stackTrace)
//            throw ProgramResult(1)
//        }
//    }
//}