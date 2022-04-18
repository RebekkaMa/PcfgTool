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
        return pRules.filterKeys { it.lexical }.map { (rule, p) -> rule.lhs + " " + rule.rhs.joinToString(" ") + " " + String.format("%.15f", p)}
    }

    fun getRules(): List<String> {
        return pRules.filterKeys { !it.lexical }.map { (rule, p) -> rule.lhs + " -> " + rule.rhs.joinToString(" ") + " " + String.format("%.15f", p )}
    }

    override fun toString(): String {
        return pRules.map { (rule, p) -> rule.toString() + " " + String.format("%.15f", p ) }.joinToString("\n")
    }
}