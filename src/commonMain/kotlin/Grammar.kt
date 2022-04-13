import kotlin.math.absoluteValue
import kotlin.math.pow
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
        return pRules.keys.filter { it.lexical }.map { it.rhs }.distinct()
    }

    fun getLexicon(): List<String> {
        return pRules.filterKeys { it.lexical }.map { (rule, p) -> rule.lhs + " " + rule.rhs + " " + (p.getRoundetNumber(22)) }
    }

    fun getRules(): List<String> {
        return pRules.filterKeys { !it.lexical }.map { (rule, p) -> rule.lhs + " -> " + rule.rhs + " " + (p.getRoundetNumber(22) )}
    }

    override fun toString(): String {
        return "Initial: " + initial + "\n" + "Rules: \n" + pRules.map { (rule, p) -> "$rule    $p" }.joinToString("\n")
    }

//    private fun getWholeNumberOrNull(double: Double): Int? {
//        val intValue = double.toInt()
//        if (double.rem(intValue) == 0.0) return intValue
//        return null
//    }
}

fun Double.getRoundetNumber(backNumber: Int): String {
    val beforeDecimalPointRange = this.toInt()
    var afterDecimalPointDouble = this - beforeDecimalPointRange
    var afterDecimalPointRange = ""
    for (i in 1 until backNumber){
        afterDecimalPointDouble = afterDecimalPointDouble.times(10)
        afterDecimalPointRange += afterDecimalPointDouble.toInt().toString()
        afterDecimalPointDouble -= afterDecimalPointDouble.toInt()
    }
    afterDecimalPointRange += afterDecimalPointDouble.times(10).roundToInt().toString()

    return "$beforeDecimalPointRange.$afterDecimalPointRange".replace("\\.?(0+)$".toRegex(), "")
}