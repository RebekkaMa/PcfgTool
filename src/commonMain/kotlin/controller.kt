import com.github.h0tk3y.betterParse.grammar.parseToEnd
import okio.*
import okio.Path.Companion.toPath

val expressionEvaluator = ExpressionEvaluator()
val trainingPath = "src/commonMain/resources/training.mrg".toPath()

fun writeToFiles(grammar: PcfgGrammar, grammarFileName: String){
    write("$grammarFileName.rules".toPath(),grammar.getRules().joinToString("\n") )
    write("$grammarFileName.lexicon".toPath(), grammar.getLexicon().joinToString("\n") )
    write("$grammarFileName.words".toPath(), grammar.getTerminals().joinToString("\n") )
}


fun createGrammar(rules : ArrayList<Rule>): PcfgGrammar {
    val absoluteRules = rules.groupingBy { it }.eachCount()
    val lhsCount = rules.groupingBy { it.lhs }.eachCount()
    val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrElse(rule.lhs) { 0 }) )}

    return PcfgGrammar(pRules =  pcfgRules)
}

fun createGrammarWithTraining(rules: ArrayList<Rule>): PcfgGrammar {

    val trainingGrammar = getGrammarFromTrainingFile()

    val absoluteRules = rules.groupingBy { it }.eachCount()
    val lhsCount = rules.groupingBy { it.lhs }.eachCount()
    val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrElse(rule.lhs) { 0 }) )}

    return PcfgGrammar(pRules =  pcfgRules)
}


fun getGrammarFromTrainingFile(): PcfgGrammar {
    val rulesArray = ArrayList<Rule>()
    fileSystem.source(trainingPath).use { fileSource ->
        fileSource.buffer().use { bufferedFileSource ->
            while (true) {
                val line = bufferedFileSource.readUtf8Line() ?: break
                rulesArray.addAll(expressionEvaluator.parseToEnd(line).parseToRules())
            }
        }
    }
    return createGrammar(rulesArray)
}

@Throws(IOException::class)
fun write(path: Path, text: String) {
    fileSystem.write(path) {
            writeUtf8(text)
    }
}
