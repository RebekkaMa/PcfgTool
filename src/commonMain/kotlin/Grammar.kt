class Grammar(rules: ArrayList<Rule>) {
    val initial = "ROOT"
    val pRules : Map<Rule,Float>

    init {
        val absoluteRules = rules.groupingBy { it }.eachCount()
        val lhsCount = rules.groupingBy { it.lhs }.eachCount()
        val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrElse(rule.lhs) { 1 }) )}
        pRules = pcfgRules
    }

    fun getTerminals(): List<String> {
        return pRules.keys.filter { it.lexical }.map { it.rhs }
    }

    fun getLexicon(): List<String> {
        return pRules.filterKeys { it.lexical }.map { (rule, p) -> rule.lhs + " " + rule.rhs + " " + (getWholeNumberOrNull(p)?: p ) }
    }

    fun getRules(): List<String> {
        return pRules.filterKeys { !it.lexical }.map { (rule, p) -> rule.lhs + " -> " + rule.rhs + " " + (getWholeNumberOrNull(p)?: p )}
    }

    override fun toString(): String {
        return "Initial: " + initial + "\n" + "Rules: \n" + pRules.map { (rule, p) -> "$rule    $p" }.joinToString("\n")
    }

    private fun getWholeNumberOrNull(float: Float): Int? {
        val intValue = float.toInt()
        if (float.rem(intValue) == 0.0f) return intValue
        return null
    }
}
