import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
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

    @Test
    fun init_shouldReturnASimpleGrammar() = runTest {
        val grammar = Grammar.fromRules(
            arrayListOf(
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

    @Test
    fun getRoundedNumber_shouldReturnTheSameNumberString() = runTest {
        val double = 0.04
        double.format(10) shouldBe "0.04"
    }

    @Test
    fun getRoundedNumber_shouldReturnTheRoundedNumberString() = runTest {
        0.00123456789191.format(10) shouldBe "0.0012345679"
        0.00123456784191.format(10) shouldBe "0.0012345678"

    }

    @Test
    fun getRoundedNumber_shouldReturnTheRoundedNumberEndlessNumberString() = runTest {
        val double = 1.toDouble() / 3.toDouble()
        double.format(15) shouldBe "0.333333333333333"
    }

    @Test
    fun getRoundedNumber_shouldReturnTheNumberWithoutPointString() = runTest {
        0.0.format(10) shouldBe "0"
        1.0.format(10) shouldBe "1"
    }

    @Test
    fun getLexicon_shouldReturnLexicon() = runTest {
        val grammar = Grammar.fromRules(
            arrayListOf(
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
        grammar.getLexicon() shouldBe listOf(
            "NP John 0.333333333333333",
            "V hit 1",
            "DET the 1",
            "N ball 0.5",
            "N ground 0.5",
            "LD hit 1"
        )
    }

    @Test
    fun getTerminals_shouldReturnTerminals() = runTest {
        val grammar = Grammar.fromRules(
            arrayListOf(
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
        grammar.getTerminals() shouldBe listOf("John", "hit", "the", "ball", "ground")
    }

    @Test
    fun getRules_shouldReturnRules() = runTest {
        val grammar = Grammar.fromRules(
            arrayListOf(
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

        grammar.getRules() shouldBe listOf("S -> NP VP 1", "VP -> V NP 1", "NP -> DET N 0.666666666666667")
    }

}