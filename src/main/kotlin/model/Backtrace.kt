package model

data class Backtrace(val bin: Pair<Rule, Double>, val chain: Pair<Backtrace?, Backtrace?>?) {

    fun getParseTreeAsString(): String {
        fun getPartOfTree(bt : Backtrace?): String {
            if (bt == null) return ""
            if (bt.chain == null) {
                return "(" + bt.bin.first.lhs + " " + bt.bin.first.rhs.first() + ")"
            }
            if (bt.chain.second == null) {
                return "(" + bt.bin.first.lhs + " " + getPartOfTree(bt.chain.first) + ")"
            }
            return "(" + bt.bin.first.lhs + " " + getPartOfTree(bt.chain.first) + " " + getPartOfTree(bt.chain.second) + ")"
        }
        return getPartOfTree(this)
    }
}
