package model

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.com.google.common.collect.MinMaxPriorityQueue
import org.junit.jupiter.api.Test

class DeductiveParserTest {

    @Test
    fun insertItem_thresHoldBeam() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.4, null)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.insertItemToQueue(item3, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item1, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item2, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item5, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item4, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item6, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item7, parser.thresholdBeam, parser.rankBeam)

        val resQueue = MinMaxPriorityQueue.create(listOf(item3, item2,  item4, item6, item5))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun insertItem_ThresHoldBeam_zero() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.0, rankBeam = 6)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.2, listOf())

        parser.insertItemToQueue(item3, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item1, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item2, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item5, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item4, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item6, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item7, parser.thresholdBeam, parser.rankBeam)

        val resQueue = MinMaxPriorityQueue.create(listOf(item2, item3, item4, item5, item6, item1))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun insertItem_thresHoldBeam_one() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 1.0, rankBeam = 6)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.2, listOf())

        parser.insertItemToQueue(item3, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item1, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item2, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item5, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item4, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item6, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item7, parser.thresholdBeam, parser.rankBeam)

        val resQueue = MinMaxPriorityQueue.create(listOf( item3, item4))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun insertItem_thresHoldBeam_and_rankBeam() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.4, rankBeam = 3)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.insertItemToQueue(item3, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item1, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item2, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item5, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item4, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item6, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item7, parser.thresholdBeam, parser.rankBeam)

        val resQueue = MinMaxPriorityQueue.create(listOf(item3, item4, item6))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun insertItemToQueue_rankBeam() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), rankBeam = 3, thresholdBeam = null)


        val item1 = Item(1, 2, 3, 0.3, 0.3, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())

        parser.insertItemToQueue(item3, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item1, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item2, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item5, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item4, parser.thresholdBeam, parser.rankBeam)
        parser.insertItemToQueue(item6, parser.thresholdBeam, parser.rankBeam)

        val resQueue = MinMaxPriorityQueue.create(listOf(item4, item6, item3))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun prune_thresHoldBeam() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.4, rankBeam = 6)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.queue.addAll(listOf(item1, item2, item3, item4, item5, item6, item7))
        parser.prune(thresholdBeam = 0.4, rankBeam = 6)

        val resQueue = MinMaxPriorityQueue.create(listOf(item3, item2, item4, item6))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun prune_thresHoldBeam_Zero() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.0, rankBeam = 6)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.queue.addAll(listOf(item1, item2, item3, item4, item5, item6, item7))
        parser.prune(thresholdBeam = 0.0, rankBeam = 6)

        val resQueue = MinMaxPriorityQueue.create(listOf(  item2, item3, item4, item5, item6, item7))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun prune_thresHoldBeam_One() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 1.0, rankBeam = 6)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.queue.addAll(listOf(item1, item2, item3, item4, item5, item6, item7))
        parser.prune(thresholdBeam = 1.0, rankBeam = 6)

        parser.queue.size shouldBe 0 //TODO

    }


    @Test
    fun prune_rankBeam() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.4, rankBeam = 2)

        val item1 = Item(1, 2, 3, 0.3, 0.24, listOf())
        val item2 = Item(2, 3, 4, 0.5, 0.5, listOf())
        val item3 = Item(4, 5, 6, 0.6, 0.6, listOf())
        val item4 = Item(3, 2, 4, 1.0, 1.0, listOf())
        val item5 = Item(2, 3, 4, 0.4, 0.4, listOf())
        val item6 = Item(2, 3, 4, 0.8, 0.8, listOf())
        val item7 = Item(2, 3, 4, 0.24, 0.24, listOf())

        parser.queue.addAll(listOf(item1, item2, item3, item4, item5, item6, item7))
        parser.prune(thresholdBeam = 0.4, rankBeam = 2)

        val resQueue = MinMaxPriorityQueue.create(listOf(item4, item6))
        parser.queue.size shouldBe resQueue.size
        while (parser.queue.isNotEmpty()) {
            parser.queue.pollFirst() shouldBe resQueue.pollFirst()
        }
    }

    @Test
    fun prune_emptyQueue() {
        val parser = DeductiveParser(1, mapOf(), mapOf(), mapOf(), mapOf(), mapOf(), thresholdBeam = 0.4, rankBeam = 2)
        parser.prune(thresholdBeam = 0.4, rankBeam = 2)
        parser.queue.size shouldBe 0

    }

    //--------------------------------------------------

   @Test
   fun parse_withA() {
       val pRules = buildMap<Rule, Double> {
           this[Rule(true, "NN", listOf("Fruit"))] = 1.0
           this[Rule(true, "NNS", listOf("flies"))] = 1/3.toDouble()
           this[Rule(true, "NNS", listOf("bananas"))] = 2/3.toDouble()
           this[Rule(true, "VBP", listOf("like"))] = 1.0
           this[Rule(true, "VBZ", listOf("flies"))] = 1.0
           this[Rule(true, "IN", listOf("like"))] = 1.0
           this[Rule(true, "S", listOf("bananas"))] = 0.6

           this[Rule(false, "S", listOf("NP", "VP"))] = 1.0
           this[Rule(false, "NP", listOf("NN", "NNS"))] = 0.25
           this[Rule(false, "NP", listOf("NNS"))] = 0.5
           this[Rule(false, "NP", listOf("NN"))] = 0.25
           this[Rule(false, "VP", listOf("VBP", "NP"))] = 0.5
           this[Rule(false, "VP", listOf("VBZ", "PP"))] = 0.5
           this[Rule(false, "PP", listOf("IN", "NP"))] = 1.0

       }
       val grammar = Grammar("S", pRules)
       val (accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal, lexiconByInt, lexiconByString, numberNonTerminals) = grammar.getGrammarDataStructuresForParsing()
       val outsideScores = buildMap {
           this[lexiconByString["NN"]!!] = 1.0
           this[lexiconByString["NNS"]!!] = 1.0
           this[lexiconByString["VBP"]!!] = 1.0
           this[lexiconByString["VBZ"]!!] = 0.013 //Schranke -> 0.013888888888888
           this[lexiconByString["IN"]!!] = 1.0
           this[lexiconByString["S"]!!] = 1.0
           this[lexiconByString["NP"]!!] = 1.0
           this[lexiconByString["VP"]!!] = 1.0
           this[lexiconByString["PP"]!!] = 1.0
       }
       val parser1 = DeductiveParser(lexiconByString["S"]!!, accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal,null,null, null)
       val parser2 = DeductiveParser(lexiconByString["S"]!!, accessRulesBySecondNtOnRhs, accessRulesByFirstNtOnRhs, accessChainRulesByNtRhs, accessRulesByTerminal,outsideScores,null, null)

       val sentenceAsInt = intArrayOf(lexiconByString["Fruit"]!!, lexiconByString["flies"]!!,lexiconByString["like"]!!,lexiconByString["bananas"]!!,)

       parser1.weightedDeductiveParsing(sentenceAsInt).second?.getBacktraceAsString(listOf("Fruit","flies", "like", "bananas"),lexiconByInt).toString() shouldBe  "(S (NP (NN Fruit)) (VP (VBZ flies) (PP (IN like) (NP (NNS bananas)))))"

       parser2.weightedDeductiveParsing(sentenceAsInt).second?.getBacktraceAsString(listOf("Fruit","flies", "like", "bananas"),lexiconByInt).toString() shouldBe "(S (NP (NN Fruit) (NNS flies)) (VP (VBP like) (NP (NNS bananas))))"
   }

}