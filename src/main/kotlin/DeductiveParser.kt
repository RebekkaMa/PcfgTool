import com.github.h0tk3y.betterParse.utils.Tuple4
import com.github.h0tk3y.betterParse.utils.Tuple5

class DeductiveParser(val grammar: Grammar) {

    val grammarRhsNonterminal =
        grammar.pRules.asSequence()
            .fold(mutableMapOf<List<String>, MutableList<Pair<Rule, Double>>>()) { acc, entry ->
                if (!entry.key.lexical) {
                    acc.compute(entry.key.rhs) { _, v ->
                        if (v == null) {
                            mutableListOf(entry.toPair())
                        } else {
                            v.add(entry.toPair());
                            v
                        }
                    }
                }
                acc
            }

    var queue = mutableListOf<Tuple5<Int, String, Int, Double, Bactrace?>>()
    val itemsLeft = mutableMapOf<Pair<Int, String>, MutableList<Tuple4<Int, String, Int, Double>>>()
    val itemsRight = mutableMapOf<Pair<String, Int>, MutableList<Tuple4<Int, String, Int, Double>>>()

    data class Bactrace(val bin: Pair<Rule, Int>, val chain: Pair<Rule, Int>, val term: Rule)

    fun weightedDeductiveParsing(grammar: Grammar, sentence: List<String>): Tuple5<Int, String, Int, Double, Bactrace?>? {
            getQueueElementsFromLexicalRules(sentence)
            while (queue.isNotEmpty()) {
                val (i, nt, j, wt, bt) = zeile5()
                val presentC = itemsLeft[Pair(i, nt)]?.find { it.t3 == j }
                val isCnull = presentC?.t4?.equals(0.0) ?: true // if key ist nicht

                if (isCnull) {
                    zeile8(i, nt, j, wt, presentC != null)

                    val b = zeile9(i, nt, j, wt, sentence)
                    if (b!=null) return b
                    val c = zeile10(i, nt, j, wt, sentence)
                    if (c!=null) return c
                    //------------------------------------------------------------------------- Zeile 11
                    grammarRhsNonterminal[listOf(nt)]?.forEach {
                        val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                            Tuple5(i, it.first.lhs, j, it.second * wt, null)
                        if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                            return newQueueElement
                        }
                        queue.add(newQueueElement)
                    }
                }
            }
            return null
    }

    fun getQueueElementsFromLexicalRules(
        sentence: List<String>
    ) {
        grammar.pRules.forEach {
            if (it.key.lexical) {
                val index = sentence.indexOf(it.key.rhs.first())
                if (index > -1) {
                    queue.add(Tuple5(index, it.key.lhs, index + 1, it.value, null))
                }
            }
        }
    }

    fun zeile5(): Tuple5<Int, String, Int, Double, Bactrace?> {
        val tupel = queue.maxByOrNull { it.t4 } ?: throw Exception("Nooooo")
        queue.remove(tupel)
        return tupel
    }

    fun zeile8(i: Int, nt: String, j: Int, wt: Double, presentC: Boolean) {
        itemsLeft.compute(Pair(i, nt)) { _, v ->
            if (!presentC) {
                if (v == null) {
                    mutableListOf(Tuple4(i, nt, j, wt))
                } else {
                    v.add(Tuple4(i, nt, j, wt))
                    v
                }
            } else {
                v?.remove(Tuple4(i, nt, j, 0.0)) ?: throw Exception("Nooooo2")
                v.add(Tuple4(i, nt, j, wt))
                v
            }
        }
        itemsRight.compute(Pair(nt, j)) { _, v ->
            if (!presentC) {
                if (v == null) {
                    mutableListOf(Tuple4(i, nt, j, wt))
                } else {
                    v.add(Tuple4(i, nt, j, wt))
                    v
                }
            } else {
                v?.remove(Tuple4(i, nt, j, 0.0)) ?: throw Exception("Nooooo2")
                v.add(Tuple4(i, nt, j, wt))
                v
            }
        }
    }

    fun zeile9(
        i: Int,
        nt: String,
        j: Int,
        wt: Double,
        sentence: List<String>
    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        itemsLeft.filterKeys { it.first == j }.map { entry ->
            println(entry.key.second)
            entry.value.forEach { tuple ->
                grammarRhsNonterminal[listOf(nt, tuple.t2)]?.forEach {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(i, it.first.lhs, tuple.t3, it.second * wt * tuple.t4, null)
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.add(newQueueElement)
                }
            }
        }
        return null
    }


    fun zeile10(
        i: Int,
        nt: String,
        j: Int,
        wt: Double,
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        itemsRight.filterKeys { it.second == i }.map { entry ->
            entry.value.forEach { tuple ->
                grammarRhsNonterminal[listOf(tuple.t2, nt)]?.forEach {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(tuple.t1, it.first.lhs, j, it.second * tuple.t4 * wt, null)
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.add(newQueueElement)
                }
            }
        }
        return null
    }

}