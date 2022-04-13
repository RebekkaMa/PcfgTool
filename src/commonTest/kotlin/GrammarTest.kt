import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GrammarTest {

    @Test
    fun init_shouldReturnASimpleGrammar() = runTest {

        val rule1 = Rule(false, "S", "NP VP")
        val rule2 = Rule(true, "NP", "John")
        val rule3 = Rule(false, "VP", "V NP")
        val rule4 = Rule(true, "V", "hit")
        val rule5 = Rule(false, "NP", "DET N")
        val rule6 = Rule(true, "DET", "the")
        val rule7 = Rule(true, "N", "ball")
        val rule8 = Rule(false, "NP", "DET N")
        val rule9 = Rule(true, "DET", "the")
        val rule10 = Rule(true, "N", "ground")

        val grammar = Grammar(arrayListOf(rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8, rule9, rule10))
        grammar.pRules shouldBe mapOf(
            (Rule(false, "S", "NP VP") to 1.0),
            (Rule(true, "NP", "John") to 1/3.toDouble()),
            (Rule(false, "VP", "V NP") to 1.0),
            (Rule(true, "V", "hit") to 1.0),
            (Rule(false, "NP", "DET N") to 2/3.toDouble()),
            (Rule(true, "DET", "the") to 1.0),
            (Rule(true, "N", "ball") to 1/2.toDouble()),
            (Rule(true, "N", "ground") to 1/2.toDouble())
        )
    }


    @Test
    fun getRoundedNumber_shouldReturnTheSameNumberString() = runTest {

        val double = 0.04
        double.getRoundetNumber(10) shouldBe "0.04"

    }

    @Test
    fun getRoundedNumber_shouldReturnTheRoundedNumberString() = runTest {

        val double = 0.00123456789191
        double.getRoundetNumber(10) shouldBe "0.0012345679"

    }

    @Test
    fun getRoundedNumber_shouldReturnTheNumberWithoutPointString() = runTest {

        0.0.getRoundetNumber(10) shouldBe "0"
        1.0.getRoundetNumber(10) shouldBe "1"

    }

}