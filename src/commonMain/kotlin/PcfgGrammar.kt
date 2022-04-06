class PcfgGrammar(val initial: String = "ROOT", val pRules: Map<Rule, Float>) {

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
        return "Initial: " + initial + "\n" + "Rules: \n" + pRules.map { (rule, p) -> "$rule    $p \n" }.toString()
    }


    private fun getWholeNumberOrNull(float: Float): Int? {
        val intValue = float.toInt()
        if (float.rem(intValue) == 0.0f) return intValue
        return null
    }
}
