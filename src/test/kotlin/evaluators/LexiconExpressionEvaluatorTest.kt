package evaluators

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import model.Rule
import org.junit.jupiter.api.Test

internal class LexiconExpressionEvaluatorTest {

    val lexiconExpressionEvaluator = LexiconExpressionEvaluator()

    @Test
    fun should() {
        val rule1 = "VP eats 0.25"
        val rule2 = "NP she 0.6666666666666666666666666"
        val rule3 = "V eats 1"
        val rule4 = "P with 0"
        val rule5 = "N fish 0,333333333"
        val rule6 = "N 0.67654 0,333333333"

        lexiconExpressionEvaluator.parseToEnd(rule1) shouldBe Pair(Rule(true, "VP", listOf("eats")), 0.25)
        lexiconExpressionEvaluator.parseToEnd(rule2) shouldBe Pair(
            Rule(true, "NP", listOf("she")),
            0.6666666666666666666666666
        )
        lexiconExpressionEvaluator.parseToEnd(rule3) shouldBe Pair(Rule(true, "V", listOf("eats")), 1.0)
        lexiconExpressionEvaluator.parseToEnd(rule4) shouldBe Pair(Rule(true, "P", listOf("with")), 0.0)
        lexiconExpressionEvaluator.parseToEnd(rule5) shouldBe Pair(Rule(true, "N", listOf("fish")), 0.333333333)
        lexiconExpressionEvaluator.parseToEnd(rule6) shouldBe Pair(Rule(true, "N", listOf("0.67654")), 0.333333333)

    }

    @Test
    fun shouldReturnException_falseNumber() {
        val rule1 = "VP eats 5.25"

        shouldThrowAny {
            lexiconExpressionEvaluator.parseToEnd(rule1)
        }
    }

    @Test
    fun shouldReturnException_noNumber() {
        val rule2 = "NP she 0;6666666666666666666666666"
        shouldThrowAny {
            lexiconExpressionEvaluator.parseToEnd(rule2)
        }
    }

    @Test
    fun shouldReturnException_stringAsNumber() {
        val rule4 = "P with ere"

        shouldThrowAny {
            lexiconExpressionEvaluator.parseToEnd(rule4)
        }
    }

    @Test
    fun shouldReturnException_pointNumber() {

        val rule5 = "N fish  6."

        shouldThrowAny {
            lexiconExpressionEvaluator.parseToEnd(rule5)
        }

    }

    @Test
    fun shouldReturnException_StringAsNumber() {

        val rule5 = "N fish  6."


        shouldThrowAny {
            lexiconExpressionEvaluator.parseToEnd(rule5)
        }

    }
}