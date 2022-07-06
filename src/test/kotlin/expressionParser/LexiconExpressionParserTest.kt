package expressionParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import model.Rule
import org.junit.jupiter.api.Test

internal class LexiconExpressionParserTest {

    val lexiconExpressionParser = LexiconExpressionParser()

    @Test
    fun should() {
        val rule1 = "VP eats 0.25"
        val rule2 = "NP she 0.6666666666666666666666666"
        val rule3 = "V eats 1"
        val rule4 = "P with 0"
        val rule5 = "N fish 0,333333333"
        val rule6 = "N 0.67654 0,333333333"

        lexiconExpressionParser.parseToEnd(rule1) shouldBe Pair(Rule(true, "VP", listOf("eats")), 0.25)
        lexiconExpressionParser.parseToEnd(rule2) shouldBe Pair(
            Rule(true, "NP", listOf("she")),
            0.6666666666666666
        )
        lexiconExpressionParser.parseToEnd(rule3) shouldBe Pair(Rule(true, "V", listOf("eats")), 1.0)
        lexiconExpressionParser.parseToEnd(rule4) shouldBe Pair(Rule(true, "P", listOf("with")), 0.0)
        lexiconExpressionParser.parseToEnd(rule5) shouldBe Pair(Rule(true, "N", listOf("fish")), 0.333333333)
        lexiconExpressionParser.parseToEnd(rule6) shouldBe Pair(Rule(true, "N", listOf("0.67654")), 0.333333333)

    }

    @Test
    fun shouldReturnException_noNumber() {
        val rule2 = "NP she 0;6666666666666666666666666"
        shouldThrowAny {
            lexiconExpressionParser.parseToEnd(rule2)
        }
    }

    @Test
    fun shouldReturnException_stringAsNumber() {
        val rule4 = "P with ere"

        shouldThrowAny {
            lexiconExpressionParser.parseToEnd(rule4)
        }
    }

    @Test
    fun shouldReturnNull_pointNumber() {

        val rule5 = "N fish  0."

            lexiconExpressionParser.parseToEnd(rule5) shouldBe Pair(Rule(true, "N", listOf("fish")), 0.0)

    }
}