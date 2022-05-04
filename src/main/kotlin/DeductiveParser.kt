import com.github.h0tk3y.betterParse.utils.Tuple5
import java.util.PriorityQueue

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


    val queue =
        PriorityQueue(compareBy<Tuple5<Int, String, Int, Double, Bactrace?>> { it.t4 }.reversed()) //TODO BinaryHeap
    val itemsLeft = mutableMapOf<Pair<Int, String>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    val itemsRight = mutableMapOf<Pair<String, Int>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    lateinit var selectedItem: Tuple5<Int, String, Int, Double, Bactrace?>

    data class Bactrace(val bin: Pair<Rule, Double>, val chain: Pair<Bactrace?, Bactrace?>?)

    fun weightedDeductiveParsing(sentence: List<String>): Tuple5<Int, String, Int, Double, Bactrace?>? {
        fillQueueElementsFromLexicalRules(sentence)
        while (queue.isNotEmpty()) {
            findMaxInQueueSaveAsSelectedItem()
            if (addSelectedItemPropertyToSavedItems()) continue

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
                            queue.offer(Tuple5(index, it.key.lhs, index + 1, it.value, Bactrace(it.toPair(), null)))
                        }
                    }
            }
        }
    }

    // Zeile 5
    fun findMaxInQueueSaveAsSelectedItem() {
        selectedItem = queue.poll() ?: throw Exception("Queue is Empty")
    }

    // Zeile 8
    fun addSelectedItemPropertyToSavedItems(): Boolean {
        var notNullPropertyEntry = false
        itemsLeft.compute(Pair(selectedItem.t1, selectedItem.t2)) { _, v ->
            if (v == null) {
                mutableListOf(selectedItem)
            } else {
                val presentItem = v.find { it.t3 == selectedItem.t3 }
                when {
                    presentItem == null -> {
                        v.add(selectedItem)
                    }
                    presentItem.t4.equals(0.0) -> {
                        v.remove(presentItem)
                        v.add(selectedItem)
                    }
                    else -> {
                        notNullPropertyEntry = true
                    }
                }
                v
            }
        }
        itemsRight.compute(Pair(selectedItem.t2, selectedItem.t3)) { _, v ->
            if (v == null) {
                mutableListOf(selectedItem)
            } else {
                val presentItem = v.find { it.t1 == selectedItem.t1 }
                when {
                    presentItem == null -> {
                        v.add(selectedItem)
                    }
                    presentItem.t4.equals(0.0) -> {
                        v.remove(presentItem)
                        v.add(selectedItem)
                    }
                    else -> {
                        notNullPropertyEntry = true
                    }
                }
                v
            }
        }
        return notNullPropertyEntry
    }

    //Zeile 9
    fun findRuleAddItemToQueueRhs(
        sentence: List<String>
    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        val (i, nt, j, wt, bt) = selectedItem
        grammarLhs[nt]?.forEach { rulePair ->
            itemsLeft[Pair(j, rulePair.first.rhs.component2())]?.forEach {
                if (rulePair.first.rhs.component2() == it.t2 && j < it.t3) {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(
                            i, rulePair.first.lhs, it.t3, rulePair.second * wt * it.t4, Bactrace(
                                rulePair,
                                Pair(bt, it.t5)
                            )
                        )
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.offer(newQueueElement)
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
        grammarRhs[nt]?.forEach { rulePair ->
            itemsRight[Pair(rulePair.first.rhs.first(), i)]?.forEach { tuple ->
                if (rulePair.first.rhs.first() == tuple.t2) {
                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                        Tuple5(
                            tuple.t1,
                            rulePair.first.lhs,
                            j,
                            rulePair.second * tuple.t4 * wt,
                            Bactrace(rulePair, Pair(tuple.t5, bt))
                        )
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == grammar.initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.offer(newQueueElement)
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
            queue.offer(newQueueElement)
        }
        return null
    }

    fun clearAll() {
        queue.clear()
        itemsLeft.clear()
        itemsRight.clear()
    }
}