package evaluators

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.utils.Tuple3
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MarkovizationNodeEvaluatorTest {
    @Test
    fun testAll(){
        val testString = "FRAG|<NP-TMP,.>^<ROOT>"
        MarkovizationNodeEvaluator().parseToEnd(testString) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf("ROOT"))

        val testString2 = "FRAG|<NP-TMP,.>^<ROOT,DA,DO>"
        MarkovizationNodeEvaluator().parseToEnd(testString2) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf("ROOT", "DA", "DO"))

        val testString3 = "FRAG^<ROOT>"
        MarkovizationNodeEvaluator().parseToEnd(testString3) shouldBe Tuple3("FRAG", listOf(), listOf("ROOT"))

        val testString4 = "FRAG|<NP-TMP,.>"
        MarkovizationNodeEvaluator().parseToEnd(testString4) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf())

        val testString5 = "FRAG"
        MarkovizationNodeEvaluator().parseToEnd(testString5) shouldBe Tuple3("FRAG", listOf(), listOf())
    }
}