import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class Grammar(rules: List<Rule>) {
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
        return pRules.filterKeys { it.lexical }.map { (rule, p) -> rule.lhs + " " + rule.rhs.joinToString(" ") + " " +  p.format(15)}
    }
    //String.format("%.15f", p)

    fun getRules(): List<String> {
        return pRules.filterKeys { !it.lexical }.map { (rule, p) -> rule.lhs + " -> " + rule.rhs.joinToString(" ") + " " + p.format(15)}
    }

    override fun toString(): String {
        return pRules.map { (rule, p) -> rule.toString() + " " + p.format(15) }.joinToString("\n")
    }
}

fun Double.format(fracDigits: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale.UK)
    val df = nf as DecimalFormat
    df.maximumFractionDigits = fracDigits
    return df.format(this)
}