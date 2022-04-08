import com.github.h0tk3y.betterParse.grammar.parseToEnd
import okio.*
import okio.Path.Companion.toPath

val expressionEvaluator = ExpressionEvaluator()
val trainingPath = "src/commonMain/resources/training.mrg".toPath()

fun writeToFiles(grammar: Grammar, grammarFileName: String){
    write("$grammarFileName.rules".toPath(),grammar.getRules().joinToString("\n") )
    write("$grammarFileName.lexicon".toPath(), grammar.getLexicon().joinToString("\n") )
    write("$grammarFileName.words".toPath(), grammar.getTerminals().joinToString("\n") )
}


fun getGrammarFromTrainingFile(): Grammar {
    val rulesArray = ArrayList<Rule>()
    fileSystem.source(trainingPath).use { fileSource ->
        fileSource.buffer().use { bufferedFileSource ->
            while (true) {
                val line = bufferedFileSource.readUtf8Line() ?: break
                rulesArray.addAll(expressionEvaluator.parseToEnd(line).parseToRules())
            }
        }
    }
    return Grammar(rulesArray)
}

@Throws(IOException::class)
fun write(path: Path, text: String) {
    fileSystem.write(path) {
            writeUtf8(text)
    }
}
