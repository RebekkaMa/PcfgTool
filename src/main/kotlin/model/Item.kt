package model

class Item(val i: Int, val nt: Int, val j: Int, var wt: Double, val comparisonValue: Double, var bt: List<Item>?) :
    Comparable<Item> {

    fun getBacktraceAsString(sentence: List<String>, lexicon: Map<Int, String>): String {
        var i = -1
        fun getBacktraceAsString(item: Item = this): String {
            val nonTerminalAsString = lexicon[item.nt]
                ?: throw Exception("Item: getBacktraceAsString -> Nonterminal ist nicht im Lexicon enthalten")
            return when {
                item.bt == null -> {
                    i++
                    val terminal = sentence.elementAtOrNull(i)
                        ?: throw Exception("Item: getBacktraceAsString -> Anzahl der Sätze ist kleiner als die Anzahl der Blätter des Parsingbaumes")
                    "($nonTerminalAsString $terminal)"
                }
                item.bt!!.size == 1 -> "($nonTerminalAsString " + getBacktraceAsString(item.bt!![0]) + ")"
                else -> "($nonTerminalAsString " + getBacktraceAsString(item.bt!![0]) + " " + getBacktraceAsString(item.bt!![1]) + ")"
            }
        }
        return getBacktraceAsString()
    }

    override fun compareTo(other: Item): Int {
        return when {
            this.comparisonValue < other.comparisonValue -> 1
            this.comparisonValue > other.comparisonValue -> -1
            else -> 0
        }
    }

}