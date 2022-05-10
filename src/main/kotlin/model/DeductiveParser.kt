package model

import com.github.h0tk3y.betterParse.utils.Tuple5
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import kotlin.time.measureTime

class DeductiveParser(

    val initial: String,
    val grammarRhs: Map<String, MutableList<Pair<Rule, Double>>>,
    val grammarLhs: Map<String, MutableList<Pair<Rule, Double>>>,
    val grammarChain: Map<String, MutableList<Pair<Rule, Double>>>,
    val grammarLexical: Map<String, MutableList<Pair<Rule, Double>>>
) {

    val queue = PriorityBlockingQueue(100, compareBy<Tuple5<Int, String, Int, Double, Bactrace?>> { it.t4 }.reversed())
    val itemsLeft = ConcurrentHashMap<Pair<Int, String>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    val itemsRight = ConcurrentHashMap<Pair<String, Int>, MutableList<Tuple5<Int, String, Int, Double, Bactrace?>>>()
    lateinit var selectedItem: Tuple5<Int, String, Int, Double, Bactrace?>

    fun weightedDeductiveParsing(sentence: List<String>): Pair<List<String>, Tuple5<Int, String, Int, Double, Bactrace?>?> {

        fillQueueElementsFromLexicalRules(sentence)
        while (queue.isNotEmpty()) {
            findMaxInQueueSaveAsSelectedItem()
            if (addSelectedItemPropertyToSavedItems()) continue

            val a = findRuleAddItemToQueueRhs(sentence)
            if (a != null) {
                clearAll()
                return sentence to a
            }
            val b = findRuleAddItemToQueueLhs(sentence)
            if (b != null) {
                clearAll()
                return sentence to b
            }
            val c = findRuleAddItemToQueueChain(sentence)
            if (c != null) {
                clearAll()
                return sentence to c
            }
        }
        clearAll()
        return sentence to null
    }

//    suspend fun weightedDeductiveParsing(sentence: List<String>): Pair<List<String>, Tuple5<Int, String, Int, Double, model.Bactrace?>?> =
//        coroutineScope {
//                fillQueueElementsFromLexicalRules(sentence)
//                while (queue.isNotEmpty()) {
//                    findMaxInQueueSaveAsSelectedItem()
//                    if (addSelectedItemPropertyToSavedItems()) continue
//
//                    val b = async { findRuleAddItemToQueueLhs(sentence) }
//
//                    val c = async { findRuleAddItemToQueueChain(sentence) }
//
//                    val a = async { findRuleAddItemToQueueRhs(sentence) }
//
//
//                    val results = awaitAll(a, b, c)
//
//
//
//                    results.forEach {
//                        if (it != null) {
//                            clearAll()
//                            return@coroutineScope sentence to it
//                        }
//                    }
//
//                }
//                clearAll()
//
//            return@coroutineScope sentence to null
//        }

    fun fillQueueElementsFromLexicalRules(
        sentence: List<String>
    ) {
        sentence.forEachIndexed { index, word ->
            grammarLexical[word]?.forEach {
                queue.add(Tuple5(index, it.first.lhs, index + 1, it.second, Bactrace(it, null)))
            }
        }
    }

    // Zeile 5
    fun findMaxInQueueSaveAsSelectedItem() {
        selectedItem = queue.poll()
    }

    // Zeile 8
    fun addSelectedItemPropertyToSavedItems(): Boolean {
        var notNullPropertyEntryLhs = false
        var notNullPropertyEntryRhs = false
        itemsLeft.compute(Pair(selectedItem.t1, selectedItem.t2)) { _, v ->
            if (v == null) {
                mutableListOf(selectedItem)
            } else {
                val presentItem = v.find { it.t3 == selectedItem.t3 }
                when {
                    presentItem == null -> {
                        v.add(selectedItem)
                    }
                    presentItem.t4 < selectedItem.t4 -> {
                        v.remove(presentItem)
                        v.add(selectedItem)
                    }
                    else -> {
                        notNullPropertyEntryLhs = true
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
                    presentItem.t4 < selectedItem.t4 -> {
                        v.remove(presentItem)
                        v.add(selectedItem)
                    }
                    else -> {
                        notNullPropertyEntryRhs = true
                    }
                }
                v
            }

        }
        if (notNullPropertyEntryLhs != notNullPropertyEntryRhs) throw Exception("Internal Error: itemsRight and itemsLeft not equal")
        return notNullPropertyEntryRhs
    }


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
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == initial && newQueueElement.t3 == sentence.size) {
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
                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == initial && newQueueElement.t3 == sentence.size) {
                        return newQueueElement
                    }
                    queue.add(newQueueElement)
                }
            }

        }
        return null
    }

//    //Zeile 9
//    suspend fun findRuleAddItemToQueueRhs(
//        sentence: List<String>
//    ): Tuple5<Int, String, Int, Double, Bactrace?>? = coroutineScope {
//        val (i, nt, j, wt, bt) = selectedItem
//        grammarLhs[nt]?.forEach { rulePair ->
//            itemsLeft[Pair(j, rulePair.first.rhs.component2())]?.forEach {
//                if (rulePair.first.rhs.component2() == it.t2 && j < it.t3) {
//                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
//                        Tuple5(
//                            i, rulePair.first.lhs, it.t3, rulePair.second * wt * it.t4, Bactrace(
//                                rulePair,
//                                Pair(bt, it.t5)
//                            )
//                        )
//                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == initial && newQueueElement.t3 == sentence.size) {
//                        return@coroutineScope newQueueElement
//                    }
//                    queue.put(newQueueElement)
//                }
//            }
//        }
//        return@coroutineScope null
//    }
//
//    //Zeile 10
//    suspend fun findRuleAddItemToQueueLhs(
//        sentence: List<String>
//
//    ): Tuple5<Int, String, Int, Double, Bactrace?>? = coroutineScope {
//        val (i, nt, j, wt, bt) = selectedItem
//        grammarRhs[nt]?.forEach { rulePair ->
//            itemsRight[Pair(rulePair.first.rhs.first(), i)]?.forEach { tuple ->
//                if (rulePair.first.rhs.first() == tuple.t2) {
//                    val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
//                        Tuple5(
//                            tuple.t1,
//                            rulePair.first.lhs,
//                            j,
//                            rulePair.second * tuple.t4 * wt,
//                            Bactrace(rulePair, Pair(tuple.t5, bt))
//                        )
//                    if (newQueueElement.t1 == 0 && newQueueElement.t2 == initial && newQueueElement.t3 == sentence.size) {
//                        return@coroutineScope newQueueElement
//                    }
//                    withContext(Dispatchers.IO) {
//                        queue.put(newQueueElement)
//                    }
//                }
//            }
//        }
//        return@coroutineScope null
//    }

    //Zeile 11
    fun findRuleAddItemToQueueChain(
        sentence: List<String>

    ): Tuple5<Int, String, Int, Double, Bactrace?>? {
        val (i, nt, j, wt, bt) = selectedItem
        grammarChain[nt]?.forEach {
            val newQueueElement: Tuple5<Int, String, Int, Double, Bactrace?> =
                Tuple5(i, it.first.lhs, j, it.second * wt, Bactrace(it, Pair(bt, null)))
            if (newQueueElement.t1 == 0 && newQueueElement.t2 == initial && newQueueElement.t3 == sentence.size) {
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