import com.github.h0tk3y.betterParse.grammar.parseToEnd
import okio.*
import okio.Path.Companion.toPath

val expressionEvaluator = ExpressionEvaluator()
val trainingPath = "src/commonMain/resources/training.mrg".toPath()

fun parseExpressionToPcfg(expression: String): String {
    val expr = "(S (NP John)(VP (V hit) (NP (DET the) (N (ball (NP (DET the) (N ground)))))))"
    val trees = expressionEvaluator.parseToEnd(expression)
    return createGrammar(trees.parseToRules()).toString()
}

fun parseExpressionToPcfg(expression: String, grammarFileName: String) {
    val trees = expressionEvaluator.parseToEnd(expression)
    val grammar = createGrammar(trees.parseToRules()).toString()

    writeToFiles(training(), grammarFileName)
}

fun writeToFiles(grammar: PcfgGrammar, grammarFileName: String){

    write("$grammarFileName.rules".toPath(),grammar.getRules().reduce { acc, s -> "$acc\n$s" } )
    write("$grammarFileName.lexicon".toPath(), grammar.getLexicon().reduce { acc, s -> "$acc\n$s" } )
    write("$grammarFileName.words".toPath(), grammar.getTerminals().reduce { acc, s -> "$acc\n$s" } )
}


fun createGrammar(rules : ArrayList<Rule>): PcfgGrammar {

    val absoluteRules = rules.groupingBy { it }.eachCount()
    val lhsCount = rules.groupingBy { it.lhs }.eachCount()
    val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrElse(rule.lhs) { 0 }) )}

    return PcfgGrammar(pRules =  pcfgRules)
}

fun createGrammarWithTraining(rules: ArrayList<Rule>): PcfgGrammar {

    val trainingGrammar = training()

    val absoluteRules = rules.groupingBy { it }.eachCount()
    val lhsCount = rules.groupingBy { it.lhs }.eachCount()
    val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrElse(rule.lhs) { 0 }) )}

    return PcfgGrammar(pRules =  pcfgRules)
}


fun training(): PcfgGrammar {
    val trainingGrammar = createGrammar(readTrainingFile(trainingPath))
    return trainingGrammar
}

fun readTrainingFile(path: Path): ArrayList<Rule> {
    val rulesArray = ArrayList<Rule>()
    fileSystem.source(path).use { fileSource ->
        fileSource.buffer().use { bufferedFileSource ->
            while (true) {
                val line = bufferedFileSource.readUtf8Line() ?: break
                rulesArray.addAll(expressionEvaluator.parseToEnd(line).parseToRules())
            }
        }
    }
    return rulesArray
}
@Throws(IOException::class)
fun write(path: Path, text: String) {
    fileSystem.write(path) {
            writeUtf8(text)
    }
}
