import com.github.h0tk3y.betterParse.utils.Tuple5
import kotlinx.coroutines.internal.ThreadSafeHeap

class DeductiveParser(val grammar: Grammar) {

    val grammarRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
    val grammarLhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
    val grammarChain = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()

    init {
        grammar.pRules.forEach {
            when (it.key.rhs.size) {
                2 -> {
                    grammarLhs.compute(it.key.rhs.first()) { _, v ->
                        if (v != null) {
                            v.add(it.toPair())
                            v
                        } else {
                            mutableListOf(it.toPair())
                        }
                    }
                    grammarRhs.compute(it.key.rhs.component2()) { _, v ->
                        if (v != null) {
                            v.add(it.toPair())
                            v
                        } else {
                            mutableListOf(it.toPair())
                        }
                    }
                }
                1 -> {
                    grammarChain.compute(it.key.rhs.first()) { _, v ->
                        if (v != null) {
                            v.add(it.toPair())
                            v
                        } else {
                            mutableListOf(it.toPair())
                        }
                    }
                }
                else -> {
                    //TODO lexical in Queue
                }
            }
        }
    }


//TODO BinaryHeap
    val queue = mutableListOf<Tuple5<Int, String, Int, Double, Bactrace?>>()
    val itemsLeft = mutableMapOf<Pair<Int, String>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    val itemsRight = mutableMapOf<Pair<String, Int>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    lateinit var selectedItem: Tuple5<Int, String, Int, Double, Bactrace?>

    data class Bactrace(val bin: Pair<Rule, Double>, val chain: Pair<Bactrace?, Bactrace?>?)

    fun weightedDeductiveParsing(sentence: List<String>): Tuple5<Int, String, Int, Double, Bactrace?>? {
        fillQueueElementsFromLexicalRules(sentence)
        while (queue.isNotEmpty()) {
            findMaxInQueueSaveAsSelectedItem()
            val (i, nt, j, _, _) = selectedItem
            val presentC = itemsLeft[Pair(i, nt)]?.find { it.t3 == j }
            val isCnull = presentC?.t4?.equals(0.0) ?: true

            if (isCnull) {
                addSelectedItemPropertyToSavedItems(presentC != null)

                val a = findRuleAddItemToQueueRhs(sentence)
                if (a != null) {
                    clearAll(); return a
                }
                val b = findRuleAddItemToQueueLhs(sentence)
                if (b != null) {
                    clearAll(); return b
                }
                val c = findRuleAddItemToQueueChain(sentence)
                if (c != null) {
                    clearAll(); return c
                }
            }
        }
        clearAll()
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

    // Zeile 5
    fun findMaxInQueueSaveAsSelectedItem() {
        selectedItem = queue.maxByOrNull { it.t4 } ?: throw Exception("Queue is Empty")
        queue.remove(selectedItem)
    }

    // Zeile 8
    fun addSelectedItemPropertyToSavedItems(presentC: Boolean) {
        val (i, nt, j, wt, bt) = selectedItem
        itemsLeft.compute(Pair(i, nt)) { _, v ->
            if (!presentC) {
                if (v == null) {
                    mutableListOf(Tuple5(i, nt, j, wt, bt))
                } else {
                    v.add(Tuple5(i, nt, j, wt, bt))
                    v
                }
            } else {
                v?.remove(Tuple5(i, nt, j, 0.0, bt)) ?: throw Exception("v cannot be null")
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
                v?.remove(Tuple5(i, nt, j, 0.0, bt)) ?: throw Exception("v cannot be null")
                v.add(Tuple5(i, nt, j, wt, bt))
                v
            }
        }
    }

    //Zeile 9
    fun findRuleAddItemToQueueRhs(
        sentence: List<String>
    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        val (i, nt, j, wt, bt) = selectedItem
        itemsLeft.filterKeys { it.first == j }.map { entry ->
            entry.value.forEach { tuple ->
                grammarLhs[nt]?.filter { it.first.rhs.component2() == tuple.t2 }?.
                forEach {
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


    //Zeile 10
    fun findRuleAddItemToQueueLhs(
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        val (i, nt, j, wt, bt) = selectedItem
        itemsRight.filterKeys { it.second == i }.map { entry ->
            entry.value.forEach { tuple ->
                grammarRhs[nt]?.filter { it.first.rhs.first() == tuple.t2 }
                    ?.forEach {
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

    //Zeile 11
    fun findRuleAddItemToQueueChain(
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        val (i, nt, j, wt, bt) = selectedItem
        grammarChain[nt]?.forEach {
            val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                Tuple5(i, it.first.lhs, j, it.second * wt, Bactrace(it, Pair(bt, null)))
            if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                return newQueueElement
            }
            queue.add(newQueueElement)
        }
        return null
    }

    fun clearAll() {
        queue.clear()
        itemsLeft.clear()
        itemsRight.clear()
    }
}