import com.github.h0tk3y.betterParse.utils.Tuple5

class DeductiveParser(val grammar: Grammar) {

    val grammarRhsNonterminal =
        grammar.pRules.asSequence().sortedByDescending { it.value }
            .fold(mutableMapOf<List<String>, MutableList<Pair<Rule, Double>>>()) { acc, entry ->
                if (!entry.key.lexical) {
                    acc.compute(entry.key.rhs) { _, v ->
                        if (v == null) {
                            mutableListOf(entry.toPair())
                        } else {
                            v.add(entry.toPair())
                            v
                        }
                    }
                }
                acc
            }

    val queue = mutableListOf<Tuple5<Int, String, Int, Double, Bactrace?>>()
    val itemsLeft = mutableMapOf<Pair<Int, String>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    val itemsRight = mutableMapOf<Pair<String, Int>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()

    data class Bactrace(val bin: Pair<Rule, Double>, val chain: Pair<Bactrace?, Bactrace?>?)

    fun weightedDeductiveParsing(sentence: List<String>): Tuple5<Int, String, Int, Double, Bactrace?>? {
        fillQueueElementsFromLexicalRules(sentence)
        while (queue.isNotEmpty()) {
            val (i, nt, j, wt, bt) = zeile5()

            val presentC = itemsLeft[Pair(i, nt)]?.find { it.t3 == j }
            val isCnull = presentC?.t4?.equals(0.0) ?: true

            if (isCnull) {
                zeile8(i, nt, j, wt, bt, presentC != null)

                val a = zeile9(i, nt, j, wt, bt, sentence)
                if (a != null) return a
                val b = zeile10(i, nt, j, wt, bt, sentence)
                if (b != null) return b
                val c = zeile11(i, nt, j, wt, bt, sentence)
                if (c != null) return c
            }
        }
        return null
    }

    fun fillQueueElementsFromLexicalRules(
        sentence: List<String>
    ) {
        grammar.pRules.forEach {
            if (it.key.lexical) {
                sentence
                    .forEachIndexed { index, word ->
                        if (word == it.key.rhs.first()) {
                            queue.add(Tuple5(index, it.key.lhs, index + 1, it.value, Bactrace(it.toPair(), null)))
                        }
                    }
            }
        }
    }

    fun zeile5(): Tuple5<Int, String, Int, Double, Bactrace?> {
        val tupel = queue.maxByOrNull { it.t4 } ?: throw Exception("Nooooo")
        queue.remove(tupel)
        return tupel
    }

    fun zeile8(i: Int, nt: String, j: Int, wt: Double, bt: Bactrace?, presentC: Boolean) {
        itemsLeft.compute(Pair(i, nt)) { _, v ->
            if (!presentC) {
                if (v == null) {
                    mutableListOf(Tuple5(i, nt, j, wt, bt))
                } else {
                    v.add(Tuple5(i, nt, j, wt, bt))
                    v
                }
            } else {
                v?.remove(Tuple5(i, nt, j, 0.0, bt)) ?: throw Exception("Nooooo2")
                v.add(Tuple5(i, nt, j, wt, bt))
                v
            }
        }
        itemsRight.compute(Pair(nt, j)) { _, v ->
            if (!presentC) {
                if (v == null) {
                    mutableListOf(Tuple5(i, nt, j, wt, bt))
                } else {
                    v.add(Tuple5(i, nt, j, wt, bt))
                    v
                }
            } else {
                v?.remove(Tuple5(i, nt, j, 0.0, bt)) ?: throw Exception("Nooooo2")
                v.add(Tuple5(i, nt, j, wt, bt))
                v
            }
        }
    }

    fun zeile9(
        i: Int,
        nt: String,
        j: Int,
        wt: Double,
        bt: Bactrace?,
        sentence: List<String>
    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        itemsLeft.filterKeys { it.first == j }.map { entry ->
            entry.value.forEach { tuple ->
                grammarRhsNonterminal[listOf(nt, tuple.t2)]?.forEach {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(
                            i, it.first.lhs, tuple.t3, it.second * wt * tuple.t4, Bactrace(
                                it,
                                Pair(bt, tuple.t5)
                            )
                        )
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
        bt: Bactrace?,
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        itemsRight.filterKeys { it.second == i }.map { entry ->
            entry.value.forEach { tuple ->
                grammarRhsNonterminal[listOf(tuple.t2, nt)]?.forEach {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(tuple.t1, it.first.lhs, j, it.second * tuple.t4 * wt, Bactrace(it, Pair(tuple.t5, bt)))
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.add(newQueueElement)
                }
            }
        }
        return null
    }

    fun zeile11(
        i: Int,
        nt: String,
        j: Int,
        wt: Double,
        bt: Bactrace?,
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        grammarRhsNonterminal[listOf(nt)]?.forEach {
            val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                Tuple5(i, it.first.lhs, j, it.second * wt, Bactrace(it, Pair(bt, null)))
            if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                return newQueueElement
            }
            queue.add(newQueueElement)
        }
        return null
    }
}