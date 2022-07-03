package model
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import utils.format
import kotlin.test.Test

class GrammarTest {

    val rule1 = Rule(false, "S", listOf("NP", "VP"))
    val rule2 = Rule(true, "NP", listOf("John"))
    val rule3 = Rule(false, "VP", listOf("V", "NP"))
    val rule4 = Rule(true, "V", listOf("hit"))
    val rule5 = Rule(false, "NP", listOf("DET", "N"))
    val rule6 = Rule(true, "DET", listOf("the"))
    val rule7 = Rule(true, "N", listOf("ball"))
    val rule8 = Rule(false, "NP", listOf("DET", "N"))
    val rule9 = Rule(true, "DET", listOf("the"))
    val rule10 = Rule(true, "N", listOf("ground"))
    val rule11 = Rule(true, "LD", listOf("hit"))

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun init_shouldReturnASimpleGrammar() = runTest {
        val grammar = Grammar.createFromRules(
            rules = arrayListOf(
                rule1,
                rule2,
                rule3,
                rule4,
                rule5,
                rule6,
                rule7,
                rule8,
                rule9,
                rule10,
                rule11
            )
        )
        grammar.pRules shouldBe mapOf(
            (Rule(false, "S", listOf("NP", "VP")) to 1.0),
            (Rule(true, "NP", listOf("John")) to 1 / 3.toDouble()),
            (Rule(false, "VP", listOf("V", "NP")) to 1.0),
            (Rule(true, "V", listOf("hit")) to 1.0),
            (Rule(false, "NP", listOf("DET", "N")) to 2 / 3.toDouble()),
            (Rule(true, "DET", listOf("the")) to 1.0),
            (Rule(true, "N", listOf("ball")) to 1 / 2.toDouble()),
            (Rule(true, "N", listOf("ground")) to 1 / 2.toDouble()),
            (Rule(true, "LD", listOf("hit")) to 1.0)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun format_shouldReturnTheSameNumberString() = runTest {
        val double = 0.04
        double.format(10) shouldBe "0.04"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun format_shouldReturnTheRoundedNumberString() = runTest {
        0.00123456789191.format(10) shouldBe "0.0012345679"
        0.00123456784191.format(10) shouldBe "0.0012345678"

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun format_shouldReturnTheRoundedNumberEndlessNumberString() = runTest {
        val double = 1.toDouble() / 3.toDouble()
        double.format(15) shouldBe "0.333333333333333"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun format_shouldReturnTheNumberWithoutPointString() = runTest {
        0.0.format(10) shouldBe "0"
        1.0.format(10) shouldBe "1"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getLexicon_shouldReturnLexicon() = runTest {
        val grammar = Grammar.createFromRules(
            rules = arrayListOf(
                rule1,
                rule2,
                rule3,
                rule4,
                rule5,
                rule6,
                rule7,
                rule8,
                rule9,
                rule10,
                rule11
            )
        )
        grammar.getLexiconAsStrings() shouldBe listOf(
            "NP John 0.333333333333333",
            "V hit 1",
            "DET the 1",
            "N ball 0.5",
            "N ground 0.5",
            "LD hit 1"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getTerminals_shouldReturnTerminals() = runTest {
        val grammar = Grammar.createFromRules(
           rules =  arrayListOf(
                rule1,
                rule2,
                rule3,
                rule4,
                rule5,
                rule6,
                rule7,
                rule8,
                rule9,
                rule10,
                rule11
            )
        )
        grammar.getTerminalsAsStrings() shouldBe listOf("John", "hit", "the", "ball", "ground")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getRules_shouldReturnRules() = runTest {
        val grammar = Grammar.createFromRules(
           rules = arrayListOf(
                rule1,
                rule2,
                rule3,
                rule4,
                rule5,
                rule6,
                rule7,
                rule8,
                rule9,
                rule10,
                rule11
            )
        )

        grammar.getRulesAsStrings() shouldBe listOf("S -> NP VP 1", "VP -> V NP 1", "NP -> DET N 0.666666666666667")
    }

    @Test
    fun viterbiOutsideScoreTest() = runTest{
        val pRules = buildMap<Rule, Double> {
            this[Rule(true, "NN", listOf("Fruit"))] = 1.0
            this[Rule(true, "NNS", listOf("flies"))] = 1/3.toDouble()
            this[Rule(true, "NNS", listOf("bananas"))] = 2/3.toDouble()
            this[Rule(true, "VBP", listOf("like"))] = 1.0
            this[Rule(true, "VBZ", listOf("flies"))] = 1.0
            this[Rule(true, "IN", listOf("like"))] = 1.0

            this[Rule(false, "S", listOf("NP", "VP"))] = 1.0
            this[Rule(false, "NP", listOf("NN", "NNS"))] = 0.25
            this[Rule(false, "NP", listOf("NNS"))] = 0.5
            this[Rule(false, "NP", listOf("NN"))] = 0.25
            this[Rule(false, "VP", listOf("VBP", "NP"))] = 0.5
            this[Rule(false, "VP", listOf("VBZ", "PP"))] = 0.5
            this[Rule(false, "PP", listOf("IN", "NP"))] = 1.0

        }

        val results = buildMap {
            this["S"] = 1.0
            this["NP"] = 1/6.toDouble()
            this["VP"] = 1/3.toDouble()
            this["PP"] = 1/6.toDouble()
            this["NN"] = 1/24.toDouble()
            this["NNS"] = 1/12.toDouble()
            this["VBP"] = 1/18.toDouble()
            this["VBZ"] = 1/18.toDouble()
            this["IN"] = 1/18.toDouble()

        }

        val grammar = Grammar.create("S", pRules)
        grammar.getViterbiOutsideScores() shouldBe results
    }


    @Test
    fun getInsideWeightsTest() = runTest{
        val pRules = buildMap<Rule, Double> {
            this[Rule(true, "NN", listOf("Fruit"))] = 1.0
            this[Rule(true, "NNS", listOf("flies"))] = 1/3.toDouble()
            this[Rule(true, "NNS", listOf("bananas"))] = 2/3.toDouble()
            this[Rule(true, "VBP", listOf("like"))] = 1.0
            this[Rule(true, "VBZ", listOf("flies"))] = 1.0
            this[Rule(true, "IN", listOf("like"))] = 1.0

            this[Rule(false, "S", listOf("NP", "VP"))] = 1.0
            this[Rule(false, "NP", listOf("NN", "NNS"))] = 0.25
            this[Rule(false, "NP", listOf("NNS"))] = 0.5
            this[Rule(false, "NP", listOf("NN"))] = 0.25
            this[Rule(false, "VP", listOf("VBP", "NP"))] = 0.5
            this[Rule(false, "VP", listOf("VBZ", "PP"))] = 0.5
            this[Rule(false, "PP", listOf("IN", "NP"))] = 1.0

        }

        val results = buildMap {
            this["S"] = 1/18.toDouble()
            this["NP"] = 1/3.toDouble()
            this["VP"] = 1/6.toDouble()
            this["PP"] = 1/3.toDouble()
            this["NN"] = 1.toDouble()
            this["NNS"] = 2/3.toDouble()
            this["VBP"] = 1.toDouble()
            this["VBZ"] = 1.toDouble()
            this["IN"] = 1.toDouble()

        }

        val grammar = Grammar.create("S", pRules)

        val accessRulesFromLhsNonLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesFromLhsLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()



        pRules.forEach { (rule, ruleProbability) ->
            if (rule.lexical) {
                accessRulesFromLhsLexical.addTuple(rule.lhs, rule to ruleProbability)
            } else {
                accessRulesFromLhsNonLexical.addTuple(rule.lhs, rule to ruleProbability)
            }
        }


        grammar.getInsideWeights(accessRulesFromLhsNonLexical, accessRulesFromLhsLexical) shouldBe results


    }

    fun <K, V> MutableMap<K, MutableList<V>>.addTuple(key: K, item: V) {
        this.compute(key) { _, v ->
            if (v != null) {
                v.add(
                    item
                )
                v
            } else {
                mutableListOf(item)
            }
        }
    }

}