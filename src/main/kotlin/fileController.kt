import com.github.h0tk3y.betterParse.grammar.parseToEnd
import okio.*
import okio.Path.Companion.toPath

val expressionEvaluator = ExpressionEvaluator()

fun writeToFiles(grammar: Grammar, grammarFileName: String){
    write("$grammarFileName.rules".toPath(),grammar.getRules().joinToString("\n") )
    write("$grammarFileName.lexicon".toPath(), grammar.getLexicon().joinToString("\n") )
    write("$grammarFileName.words".toPath(), grammar.getTerminals().joinToString("\n") )
}

@Throws(IOException::class)
fun write(path: Path, text: String) {
    FileSystem.SYSTEM.write(path) {
            writeUtf8(text)
    }
}
