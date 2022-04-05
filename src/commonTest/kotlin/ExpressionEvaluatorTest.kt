import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldHaveSameHashCodeAs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ExpressionEvaluatorTest {


    val expressionEvaluator = ExpressionEvaluator()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnATree() = runTest {
        val expression = "(ROOT (SBARQ (WH-NP (W6P Who)) (SQ (VBZ 's) (VP (VBG :) (NP (DT the) (NN ;)))) (. ?)))"

        val tree18 = Tree("?" , arrayListOf())
        val tree17 = Tree(";" , arrayListOf())
        val tree16 = Tree("the" , arrayListOf())
        val tree15 = Tree(":" , arrayListOf())
        val tree14 = Tree("'s" , arrayListOf())
        val tree13 = Tree("Who" , arrayListOf())
        val tree12 = Tree("." , arrayListOf(tree18))
        val tree11 = Tree("NN" , arrayListOf(tree17))
        val tree10 = Tree("DT" , arrayListOf(tree16))
        val tree9 = Tree("NP", arrayListOf(tree10, tree11))
        val tree8 = Tree("VBG", arrayListOf(tree15))
        val tree7 = Tree("VP", arrayListOf(tree8, tree9))
        val tree6 = Tree("VBZ", arrayListOf(tree14))
        val tree5 = Tree("SQ", arrayListOf(tree6, tree7))
        val tree4 = Tree("W6P", arrayListOf(tree13))
        val tree3 = Tree("WH-NP", arrayListOf(tree4))
        val tree2 = Tree("SBARQ", arrayListOf(tree3, tree5, tree12))
        val tree1 = Tree("ROOT", arrayListOf(tree2))

        expressionEvaluator.parseToEnd(expression).printExpressionTree() shouldBeEqualComparingTo  tree1.printExpressionTree()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnALexicalRule() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )
        val child2 = Tree(atom = "NP-SB", arrayListOf())
        val child3 = Tree(atom = "OP", arrayListOf())
        val child4 = Tree(atom = "VP", arrayListOf(child3))
        val tree = Tree(atom = "NP",  arrayListOf(child1))
        tree.parseToRule() shouldBe Rule(true, lhs = "NP", rhs = "ART-NK")
    }



}