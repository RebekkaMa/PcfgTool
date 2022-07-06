package expressionParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.utils.Tuple3
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BinarisedNodeParserTest {
    @Test
    fun testAll(){
        val testString = "FRAG|<NP-TMP,.>^<ROOT>"
        BinarisedNodeParser().parseToEnd(testString) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf("ROOT"))

        val testString2 = "FRAG|<NP-TMP,.>^<ROOT,DA,DO>"
        BinarisedNodeParser().parseToEnd(testString2) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf("ROOT", "DA", "DO"))

        val testString3 = "FRAG^<ROOT>"
        BinarisedNodeParser().parseToEnd(testString3) shouldBe Tuple3("FRAG", listOf(), listOf("ROOT"))

        val testString4 = "FRAG|<NP-TMP,.>"
        BinarisedNodeParser().parseToEnd(testString4) shouldBe Tuple3("FRAG", listOf("NP-TMP","."), listOf())

        val testString5 = "FRAG"
        BinarisedNodeParser().parseToEnd(testString5) shouldBe Tuple3("FRAG", listOf(), listOf())
    }
}