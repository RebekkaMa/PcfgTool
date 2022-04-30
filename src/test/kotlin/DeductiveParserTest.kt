import com.github.h0tk3y.betterParse.utils.Tuple4
import com.github.h0tk3y.betterParse.utils.Tuple5
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeductiveParserTest {

    val rule1 = Rule(false, "S", listOf("NP", "VP"))
    val rule2 = Rule(false, "NP", listOf("NN", "NNS"))
    val rule3 = Rule(false, "NP", listOf("NNS"))
    val rule4 = Rule(false, "NP", listOf("NN"))
    val rule5 = Rule(false, "VP", listOf("VBP", "NP"))
    val rule6 = Rule(false, "VP", listOf("VBZ", "PP"))
    val rule7 = Rule(false, "PP", listOf("IN", "NP"))

    val rule8 = Rule(true, "NN", listOf("Fruit"))
    val rule9 = Rule(true, "NNS", listOf("flies"))
    val rule10 = Rule(true, "NNS", listOf("bananas"))
    val rule11 = Rule(true, "VBP", listOf("like"))
    val rule12 = Rule(true, "VBZ", listOf("flies"))
    val rule13 = Rule(true, "IN", listOf("like"))


    @Test
    fun shouldReturnQueueElements_getQueueElementsFromLexicalRules() = runTest {

        val grammar = Grammar.create(
            initial = "ROOT",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val tuple1 = Tuple5(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null))
        val tuple2 = Tuple5(1, "NNS", 2, 1 / 3.toDouble(), DeductiveParser.Bactrace(rule9 to 1 / 3.toDouble(), null))
        val tuple3 = Tuple5(1, "VBZ", 2, 1.0, DeductiveParser.Bactrace(rule12 to 1.0, null))
        val tuple4 = Tuple5(2, "VBP", 3, 1.0, DeductiveParser.Bactrace(rule11 to 1.0, null))
        val tuple5 = Tuple5(2, "IN", 3, 1.0, DeductiveParser.Bactrace(rule13 to 1.0, null))
        val tuple6 = Tuple5(3, "NNS", 4, 2 / 3.toDouble(), DeductiveParser.Bactrace(rule10 to 2 / 3.toDouble(), null))


        val parser = DeductiveParser(grammar)
        parser.fillQueueElementsFromLexicalRules(listOf("Fruit", "flies", "like", "bananas"))
        parser.queue shouldContain tuple1
        parser.queue shouldContain tuple2
        parser.queue shouldContain tuple3
        parser.queue shouldContain tuple4
        parser.queue shouldContain tuple5
        parser.queue shouldContain tuple6
        parser.queue.size shouldBe 6
    }


    @Test
    fun should_zeile8_empty() = runTest {

        val grammar = Grammar.create(
            initial = "ROOT",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)
        parser.zeile8(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null), false)
        parser.itemsLeft shouldBe mutableMapOf(
            Pair(
                Pair(0, "NN"),
                mutableListOf(Tuple5(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))
            )
        )
        parser.itemsRight shouldBe mutableMapOf(
            Pair(
                Pair("NN", 1),
                mutableListOf(Tuple5(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))
            )
        )
    }

    @Test
    fun should_zeile8_2() = runTest {

        val grammar = Grammar.create(
            initial = "ROOT",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)
        parser.itemsLeft[Pair(0, "NN")] =
            mutableListOf(Tuple5(0, "NN", 1, 0.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))
        parser.itemsRight[Pair("NN", 1)] =
            mutableListOf(Tuple5(0, "NN", 1, 0.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))

        parser.zeile8(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null), true)
        parser.itemsLeft shouldBe mutableMapOf(
            Pair(
                Pair(0, "NN"),
                mutableListOf(Tuple5(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))
            )
        )
        parser.itemsRight shouldBe mutableMapOf(
            Pair(
                Pair("NN", 1),
                mutableListOf(Tuple5(0, "NN", 1, 1.0, DeductiveParser.Bactrace(rule8 to 1.0, null)))
            )
        )
    }

    @Test
    fun should_zeile8() = runTest {

        val grammar = Grammar.create(
            initial = "ROOT",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)
        parser.itemsLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.5, null))
        parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, null))
        parser.itemsRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.5, null))
        parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, null))


        parser.zeile8(0, "NN", 1, 1.0, null, false)
        parser.itemsLeft shouldBe mutableMapOf(
            Pair(
                Pair(0, "NN"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 2, 0.5, null), Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 1, 1.0, null))
            ), Pair(
                Pair(2, "NNS"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.5, null))
            )
        )
        parser.itemsRight shouldBe mutableMapOf(
            Pair(
                Pair("NN", 2),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 2, 0.5, null))
            ), Pair(
                Pair("NNS", 3),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.5, null))
            ),
            Pair(
                Pair("NN", 1),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 1, 1.0, null))
            )
        )
    }

    @Test
    fun should_zeile9_empty() = runTest {

        val grammar = Grammar.create(
            initial = "ROOT",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)

        parser.itemsLeft[Pair(3, "NN")] = mutableListOf(Tuple5(3, "NN", 4, 0.5, null))
        parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, null))
        parser.itemsLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.4, null))


        parser.itemsRight[Pair("NN", 4)] = mutableListOf(Tuple5(3, "NN", 4, 0.5, null))
        parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, null))
        parser.itemsRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.4, null))


        parser.zeile9(0, "NN", 2, 0.4, null, listOf("Fruit", "flies", "like", "bananas"))

        parser.itemsLeft shouldBe mutableMapOf(
            Pair(
                Pair(3, "NN"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(3, "NN", 4, 0.5, null))
            ), Pair(
                Pair(2, "NNS"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.6, null))
            ), Pair(
                Pair(0, "NN"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 2, 0.4, null))
            )
        )
        parser.itemsRight shouldBe mutableMapOf(
            Pair(
                Pair("NN", 4),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(3, "NN", 4, 0.5, null))
            ), Pair(
                Pair("NNS", 3),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.6, null))
            ),
            Pair(
                Pair("NN", 2),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NN", 2, 0.4, null))
            )
        )

        parser.queue shouldBe mutableListOf(Tuple5(0, "NP", 3, (1 / 4.toDouble() * 0.6 * 0.4), DeductiveParser.Bactrace(rule2 to 1 / 4.toDouble(), Pair(null, null) )))
    }

    @Test
    fun should_zeile9_empy() = runTest {

        val grammar = Grammar.create(
            initial = "S",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)

        parser.itemsLeft[Pair(3, "VP")] = mutableListOf(Tuple5(3, "VP", 4, 0.5, null))
        parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, null))
        parser.itemsLeft[Pair(0, "NP")] = mutableListOf(Tuple5(0, "NP", 3, 0.4, null))


        parser.itemsRight[Pair("VP", 4)] = mutableListOf(Tuple5(3, "VP", 4, 0.5, null))
        parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, null))
        parser.itemsRight[Pair("NP", 3)] = mutableListOf(Tuple5(0, "NP", 3, 0.4, null))


        parser.zeile10(3, "VP", 4, 0.5, null, listOf("Fruit", "flies", "like", "bananas")) shouldBe Tuple5(
            0,
            "S",
            4,
            0.5 * 0.4,
            DeductiveParser.Bactrace(rule1 to 1.0, Pair(null, null) )
        )

        parser.itemsLeft shouldBe mutableMapOf(
            Pair(
                Pair(3, "VP"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(3, "VP", 4, 0.5, null))
            ), Pair(
                Pair(2, "NNS"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.6, null))
            ), Pair(
                Pair(0, "NP"),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NP", 3, 0.4,null))
            )
        )
        parser.itemsRight shouldBe mutableMapOf(
            Pair(
                Pair("VP", 4),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(3, "VP", 4, 0.5, null))
            ), Pair(
                Pair("NNS", 3),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(2, "NNS", 3, 0.6, null))
            ),
            Pair(
                Pair("NP", 3),
                mutableListOf(Tuple5<Int, String, Int, Double, DeductiveParser.Bactrace?>(0, "NP", 3, 0.4, null))
            )
        )

        parser.queue shouldBe mutableListOf()
    }

    @Test
    fun should_testAll() = runTest {

        val grammar = Grammar.create(
            initial = "S",
            mapOf(
                rule1 to 1.0,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)
        val tupel = parser.weightedDeductiveParsing(listOf("Fruit", "flies", "like", "bananas"))
        tupel?.t1 shouldBe 0
        tupel?.t2 shouldBe "S"
        tupel?.t3 shouldBe 4
        tupel?.t4 shouldBe 1 / 24.toDouble()
    }

    @Test
    fun should_returnNull() = runTest {

        val grammar = Grammar.create(
            initial = "S",
            mapOf(
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)

        parser.weightedDeductiveParsing(listOf("Fruit", "flies", "like", "bananas")) shouldBe null
    }

    @Test
    fun should_returnNull_() = runTest {

        val grammar = Grammar.create(
            initial = "O",
            mapOf(
                rule1 to 1.0,
                Rule(false, "O", listOf("S")) to 0.5,
                rule2 to 1 / 4.toDouble(),
                rule3 to 1 / 2.toDouble(),
                rule4 to 1 / 4.toDouble(),
                rule5 to 1 / 2.toDouble(),
                rule6 to 1 / 2.toDouble(),
                rule7 to 1.0,
                rule8 to 1.0,
                rule9 to 1 / 3.toDouble(),
                rule10 to 2 / 3.toDouble(),
                rule11 to 1.0,
                rule12 to 1.0,
                rule13 to 1.0
            )
        )
        val parser = DeductiveParser(grammar)

        val tupel = parser.weightedDeductiveParsing(listOf("Fruit", "flies", "like", "bananas"))
        tupel?.t1 shouldBe 0
        tupel?.t2 shouldBe "O"
        tupel?.t3 shouldBe 4
        tupel?.t4 shouldBe 1 / 24.toDouble() * 0.5
    }

}