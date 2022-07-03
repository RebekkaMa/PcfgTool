package controller

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import evaluators.ExpressionEvaluatorParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import model.Grammar
import model.Rule
import readNotEmptyLnOrNull
import writeGrammarToFiles
import kotlin.system.exitProcess

class Induce : CliktCommand() {
    override val commandHelp = """
        Liest eine Sequenz Konstituentenbäume von der Standardeingabe und gibt eine aus diesen Bäumen induzierte PCFG auf der Standardausgabe aus.
    """

    private val grammarFileName by argument(name = "GRAMMAR", help = "PCFG wird in den Dateien GRAMMAR.rules, GRAMMAR.lexicon und GRAMMAR.words gespeichert").optional()

    private val rulesChannel = Channel<ArrayList<Rule>>(capacity = Channel.UNLIMITED)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceStringsFromInput() = produce<String>(context = Dispatchers.IO, capacity = 5) {
        var line = readNotEmptyLnOrNull()
        while (line != null && isActive) {
            send(line)
            line = readNotEmptyLnOrNull()
        }
    }

    private fun CoroutineScope.produceRulesFromStrings(channel: ReceiveChannel<String>) =
        launch(context = Dispatchers.Default) {
            val expressionEvaluatorParser = ExpressionEvaluatorParser()
            for (sentence in channel) {
                rulesChannel.send(expressionEvaluatorParser.parseToEnd(sentence).transformToRules())
            }
        }

    @OptIn(FlowPreview::class)
    override fun run() {
        try {
            runBlocking(Dispatchers.Default) {
                val producer = produceStringsFromInput()

                launch {
                    try {
                        coroutineScope {
                            repeat(8) {
                                produceRulesFromStrings(producer)
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

                val grammar = Grammar.createFromRules(rules = rules)
                grammarFileName?.let { writeGrammarToFiles(grammar, grammarFileName.toString()) }
                    ?: println(grammar.toString())
            }
        } catch (e: Exception) {
            System.err.println("Ein Fehler ist aufgetreten!")
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            throw ProgramResult(1)
        }
    }
}