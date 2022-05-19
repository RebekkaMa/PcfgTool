package model

data class Backtrace(val lhs : Int, val rhs: List<Int>,val probability: Double, val chain: Pair<Backtrace, Backtrace?>?) {

    fun getParseTreeAsString(lexicon: Map<Int, String>): String {

        fun getPartOfTree(bt : Backtrace): String {
            return when {
                bt.chain == null -> "(" + lexicon[bt.lhs] + " " + lexicon[bt.rhs.first()] + ")"
                bt.chain.second == null -> "(" + lexicon[bt.lhs] + " " + getPartOfTree(bt.chain.first) + ")"
                else -> "(" + lexicon[bt.lhs]  + " " + getPartOfTree(bt.chain.first) + " " + getPartOfTree(bt.chain.second!!) + ")"
            }
        }
        return getPartOfTree(this)
    }
}