package evaluators

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import model.Rule
import org.junit.jupiter.api.Test

class RulesExpressionParserTest {

    val rulesExpressionParser = RulesExpressionParser()

    @Test
    fun should() {
        val rule1 = "S -> NP VP 1"
        val rule2 = "VP -> VP PP 0.25"
        val rule3 = "VP -> V NP 0.5"
        val rule4 = "PP -> P NP 1"
        val rule5 = "NP -> Det N 0.33333333333333333"
        val rule6 = "N -> 0.67654 . 0,333333333"
        val rule7 = "-> -> -> jk->jk 0,333333333"

        rulesExpressionParser.parseToEnd(rule1) shouldBe Pair(Rule(false, "S", listOf("NP", "VP")), 1.0)
        rulesExpressionParser.parseToEnd(rule2) shouldBe Pair(Rule(false, "VP", listOf("VP", "PP")), 0.25)
        rulesExpressionParser.parseToEnd(rule3) shouldBe Pair(Rule(false, "VP", listOf("V", "NP")), 0.5)
        rulesExpressionParser.parseToEnd(rule4) shouldBe Pair(Rule(false, "PP", listOf("P", "NP")), 1.0)
        rulesExpressionParser.parseToEnd(rule5) shouldBe Pair(Rule(false, "NP", listOf("Det", "N")), 0.33333333333333333)
        rulesExpressionParser.parseToEnd(rule6) shouldBe Pair(Rule(false, "N", listOf("0.67654", ".")), 0.333333333)
        rulesExpressionParser.parseToEnd(rule7) shouldBe Pair(Rule(false, "->", listOf("->", "jk->jk")), 0.333333333)
    }

    @Test
    fun shouldReturnException_falseNumber() {
        val rule1 = "S -> NP VP 5.25"

        shouldThrowAny {
            rulesExpressionParser.parseToEnd(rule1)
        }
    }

    @Test
    fun shouldReturnException_noNumber() {
        val rule2 = "S -> NP VP 0;6666666666666666666666666"
        shouldThrowAny {
            rulesExpressionParser.parseToEnd(rule2)
        }
    }

    @Test
    fun shouldReturnException_stringAsNumber() {
        val rule4 = "S -> NP VP ere"

        shouldThrowAny {
            rulesExpressionParser.parseToEnd(rule4)
        }
    }

    @Test
    fun shouldReturnException_pointNumber() {

        val rule5 = "S -> NP VP  6."

        shouldThrowAny {
            rulesExpressionParser.parseToEnd(rule5)
        }

    }

    @Test
    fun shouldReturnException_StringAsNumber() {

        val rule5 = "S -> 6."


        shouldThrowAny {
            rulesExpressionParser.parseToEnd(rule5)
        }

    }


}