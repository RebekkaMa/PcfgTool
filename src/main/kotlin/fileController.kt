import java.io.File

fun writeToFiles(grammar: Grammar, grammarFileName: String){
    File("$grammarFileName.rules").writeText(grammar.getRules().joinToString("\n"))
    File("$grammarFileName.lexicon").writeText(grammar.getLexicon().joinToString("\n"))
    File("$grammarFileName.words").writeText(grammar.getTerminals().joinToString("\n"))
}

fun readFileLineByLine(fileName: String){
    File(fileName).bufferedReader().readLine()
}
