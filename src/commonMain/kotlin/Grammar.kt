import kotlin.math.roundToInt

class Grammar(rules: ArrayList<Rule>) {
    val initial = "ROOT"
    val pRules : Map<Rule,Double>

    init {
        val absoluteRules = rules.groupingBy { it }.eachCount()
        val lhsCount = rules.groupingBy { it.lhs }.eachCount()
        val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toDouble()/(lhsCount.getOrElse(rule.lhs) { 1 }) )}
        pRules = pcfgRules
    }

    fun getTerminals(): List<String> {
        return pRules.keys.filter { it.lexical }.map { it.rhs.first() }.distinct()
    }

    fun getLexicon(): List<String> {
        return pRules.filterKeys { it.lexical }.map { (rule, p) -> rule.lhs + " " + rule.rhs.joinToString(" ") + " " + (p.getRoundetNumber()) }
    }

    fun getRules(): List<String> {
        return pRules.filterKeys { !it.lexical }.map { (rule, p) -> rule.lhs + " -> " + rule.rhs.joinToString(" ") + " " + (p.getRoundetNumber() )}
    }

    override fun toString(): String {
        return pRules.map { (rule, p) -> rule.toString() + " " + p.getRoundetNumber() }.joinToString("\n")
    }
}

fun Double.getRoundetNumber(backNumber: Int = 15): String {
    val beforeDecimalPointNumbers = this.toInt()
    var afterDecimalPointNumbers = this - beforeDecimalPointNumbers
    var afterDecimalPointNumbersString = ""
    for (i in 1 until backNumber){

        afterDecimalPointNumbers = afterDecimalPointNumbers.times(10)
        afterDecimalPointNumbersString += afterDecimalPointNumbers.toInt().toString()
        afterDecimalPointNumbers -= afterDecimalPointNumbers.toInt()
    }
    afterDecimalPointNumbersString += afterDecimalPointNumbers.times(10).roundToInt().toString()

    return "$beforeDecimalPointNumbers.$afterDecimalPointNumbersString".replace("\\.?(0+)$".toRegex(), "")
}