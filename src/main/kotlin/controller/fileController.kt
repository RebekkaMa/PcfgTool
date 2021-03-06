package controller
import model.Grammar
import java.io.File

fun writeGrammarToFiles(grammar: Grammar, grammarFileName: String){
    File("$grammarFileName.rules").writeText(grammar.getRulesAsStrings().joinToString("\n"))
    File("$grammarFileName.lexicon").writeText(grammar.getLexiconAsStrings().joinToString("\n"))
    File("$grammarFileName.words").writeText(grammar.getTerminalsAsStrings().joinToString("\n"))
}
fun writeOutsideScoreToFiles(outsideWeights: Map<String, Double>, fileName: String){
    File("$fileName.outside").writeText(outsideWeights.map { it.key + " " + it.value}.joinToString("\n") )
}

val readNotEmptyLnOrNull = {
    val line = readlnOrNull()
    if (line.isNullOrEmpty()) null else line
}