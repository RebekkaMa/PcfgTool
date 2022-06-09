
import model.Grammar
import java.io.File

fun writeGrammarToFiles(grammar: Grammar, grammarFileName: String){
    File("$grammarFileName.rules").writeText(grammar.getRules().joinToString("\n"))
    File("$grammarFileName.lexicon").writeText(grammar.getLexicon().joinToString("\n"))
    File("$grammarFileName.words").writeText(grammar.getTerminals().joinToString("\n"))
}
fun writeOutsideScoreToFiles(outsideWeights: Map<String, Double>, fileName: String){
    File("$fileName.outside").writeText(outsideWeights.map { it.key + " " + it.value}.joinToString { "\n" } )
}
