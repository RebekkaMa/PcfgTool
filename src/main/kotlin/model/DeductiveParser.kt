package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import org.jetbrains.kotlin.com.google.common.collect.MinMaxPriorityQueue

class DeductiveParser(
    private val initial: Int,
    private val accessRulesBySecondNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByFirstNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessChainRulesByNtRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByTerminal: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val outsideScores: Map<Int, Double>?,
    private val thresholdBeam: Double?,
    private val rankBeam: Int?,
    initialArraySize: Int = 100_000,
) {
    private var queue =
        MinMaxPriorityQueue.orderedBy(compareBy<Item> { it.wt }).expectedSize(rankBeam ?: 100).create<Item>()
    private val accessFoundItemsFromLeft =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)
    private val accessFoundItemsFromRight =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)

    fun weightedDeductiveParsing(sentence: IntArray, kbest: Int): Pair<IntArray, List<Item?>> {
        val resultItems = mutableListOf<Item?>()
        fillQueueWithItemsFromLexicalRules(sentence)
        while (queue.isNotEmpty()) {
            val selectedItem: Item = queue.pollLast()
            selectedItem.wt =
                if (outsideScores.isNullOrEmpty()) selectedItem.wt else selectedItem.wt / (outsideScores[selectedItem.nt]
                    ?: 1.0)
            if (selectedItem.i == 0 && selectedItem.nt == initial && selectedItem.j == sentence.size) {
                resultItems.add(selectedItem)
                if (resultItems.size == kbest){
                    return sentence to resultItems
                }
            }
            if (addSelectedItemProbabilityToSavedItems(selectedItem)) continue
            findRulesAddItemsToQueueSecondNtOnRhs(selectedItem)
            findRulesAddItemsToQueueFirstNtOnRhs(selectedItem)
            findRulesAddItemsToQueueChain(selectedItem)
            //prune(thresholdBeam = thresholdBeam, rankBeam = rankBeam)
        }
        while (resultItems.size < kbest){
            resultItems.add(null)
        }
        return sentence to resultItems
    }

    fun fillQueueWithItemsFromLexicalRules(sentence: IntArray) {
        sentence.forEachIndexed { index, word ->
            accessRulesByTerminal[word]?.forEach { (lhs, rhs, ruleProbability) ->
                queue.offer(Item(index, lhs, index + 1, ruleProbability * (outsideScores?.get(lhs) ?: 1.0), null))
            }
        }
    }

    //Zeile 8
    fun addSelectedItemProbabilityToSavedItems(selectedItem: Item): Boolean {
        var notNullProbabilityEntryLhs = false
        var notNullProbabilityEntryRhs = false

        accessFoundItemsFromLeft.compute(Pair(selectedItem.i, selectedItem.nt)) outerCompute@{ _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.j to selectedItem)
            } else {
                v.compute(selectedItem.j) { _, presentItem ->
                    when {
                        (presentItem?.wt ?: 0.0) >= selectedItem.wt -> {
                            notNullProbabilityEntryLhs = true
                            return@compute presentItem
                        }
                        else -> return@compute selectedItem
                    }
                }
                v
            }
        }
        if (notNullProbabilityEntryLhs) return true
        accessFoundItemsFromRight.compute(Pair(selectedItem.nt, selectedItem.j)) outerCompute@{ _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.i to selectedItem)
            } else {
                v.compute(selectedItem.i) { _, presentItem ->
                    when {
                        (presentItem?.wt ?: 0.0) >= selectedItem.wt -> {
                            notNullProbabilityEntryRhs = true
                            return@compute presentItem
                        }
                        else -> return@compute selectedItem
                    }
                }
                v
            }
        }
        if (notNullProbabilityEntryLhs != notNullProbabilityEntryRhs) throw Exception("Internal Error: itemsRight and itemsLeft not equal")
        return notNullProbabilityEntryRhs
    }

    //Zeile 9
    fun findRulesAddItemsToQueueSecondNtOnRhs(selectedItem: Item) {
        accessRulesByFirstNtOnRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromLeft[Pair(
                selectedItem.j,
                rhs.component2()
            )]?.forEach { (_, combinationItem) -> // --> j == i2
                if (rhs.component2() == combinationItem.nt && selectedItem.j < combinationItem.j && combinationItem.wt > 0.0) {
                    insertItemToQueue(
                        Item(
                            selectedItem.i,
                            lhs,
                            combinationItem.j,
                            ruleProbability * selectedItem.wt * combinationItem.wt * (outsideScores?.get(lhs) ?: 1.0),
                            listOf(selectedItem, combinationItem)
                        ), thresholdBeam, rankBeam
                    )
                }
            }
        }
    }

    //Zeile 10
    fun findRulesAddItemsToQueueFirstNtOnRhs(selectedItem: Item) {
        accessRulesBySecondNtOnRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromRight[Pair(
                rhs.component1(),
                selectedItem.i
            )]?.forEach { (_, combinationItem) ->    // --> j0 = i
                if (rhs.component1() == combinationItem.nt && combinationItem.i < selectedItem.i && combinationItem.wt > 0.0) {
                    val newProbability = ruleProbability * combinationItem.wt * selectedItem.wt * (outsideScores?.get(
                        lhs
                    ) ?: 1.0)
                    insertItemToQueue(
                        Item(
                            combinationItem.i,
                            lhs,
                            selectedItem.j,
                            newProbability,
                            listOf(combinationItem, selectedItem)
                        ), thresholdBeam, rankBeam
                    )
                }
            }
        }
    }

    //Zeile 11
    fun findRulesAddItemsToQueueChain(selectedItem: Item) {
        accessChainRulesByNtRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            insertItemToQueue(
                Item(
                    selectedItem.i,
                    lhs,
                    selectedItem.j,
                    ruleProbability * selectedItem.wt * (outsideScores?.get(lhs) ?: 1.0),
                    listOf(selectedItem)
                ), thresholdBeam, rankBeam
            )
        }
    }

    fun prune(thresholdBeam: Double?, rankBeam: Int?) {
        if (thresholdBeam != null && !queue.isEmpty()) {
            val m = queue.peekLast()!!.wt
            while (queue.peekFirst().wt > thresholdBeam * m){
                queue.pollFirst()
            }
        }
        if (rankBeam != null && !queue.isEmpty()) {
            while (queue.size > rankBeam){
                queue.pollFirst()
            }
        }
    }

    fun insertItemToQueue(item: Item, thresholdBeam: Double?, rankBeam: Int?) {

        fun thresholdBeam() {
            if (item.wt > ((queue.peekLast()?.wt ?: 0.0) * thresholdBeam!!)) {
                queue.offer(
                    item
                )
            }
        }

        fun rankBeam() {
            if (queue.size < rankBeam!!) {
                queue.offer(
                    item
                )
            } else
                if ((queue.peekFirst()?.wt ?: 0.0) < item.wt) {
                    queue.pollFirst()
                    queue.offer(
                        item
                    )
                }
        }

        when {
            thresholdBeam != null && rankBeam != null -> {
                if (item.wt > ((queue.peekLast()?.wt ?: 0.0) * thresholdBeam)) {
                    rankBeam()
                }
            }
            thresholdBeam != null -> thresholdBeam()
            rankBeam != null -> rankBeam()
            else -> {
                queue.offer(
                    item
                )
            }
        }
    }
}