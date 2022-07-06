package evaluators

import model.Tree
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TreeParserTest {


    val treeParser = TreeParser()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAComplexTree() = runTest {
        val expression = "(ROOT (SBARQ (WH-NP (W6P Who)) (SQ (VBZ 's) (VP (VBG :) (NP (DT the) (NN ;)))) (. ?)))"

        val tree18 = Tree("?", arrayListOf())
        val tree17 = Tree(";", arrayListOf())
        val tree16 = Tree("the", arrayListOf())
        val tree15 = Tree(":", arrayListOf())
        val tree14 = Tree("'s", arrayListOf())
        val tree13 = Tree("Who", arrayListOf())
        val tree12 = Tree(".", arrayListOf(tree18))
        val tree11 = Tree("NN", arrayListOf(tree17))
        val tree10 = Tree("DT", arrayListOf(tree16))
        val tree9 = Tree("NP", arrayListOf(tree10, tree11))
        val tree8 = Tree("VBG", arrayListOf(tree15))
        val tree7 = Tree("VP", arrayListOf(tree8, tree9))
        val tree6 = Tree("VBZ", arrayListOf(tree14))
        val tree5 = Tree("SQ", arrayListOf(tree6, tree7))
        val tree4 = Tree("W6P", arrayListOf(tree13))
        val tree3 = Tree("WH-NP", arrayListOf(tree4))
        val tree2 = Tree("SBARQ", arrayListOf(tree3, tree5, tree12))
        val tree1 = Tree("ROOT", arrayListOf(tree2))

        treeParser.parseToEnd(expression)
            .toString() shouldBeEqualComparingTo tree1.toString()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnASimpleTree() = runTest {
        val expression = "(ROOT SBARQ)"

        val tree2 = Tree("SBARQ", arrayListOf())
        val tree1 = Tree("ROOT", arrayListOf(tree2))


        treeParser.parseToEnd(expression)
            .toString() shouldBeEqualComparingTo tree1.toString()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAnExeptionDueMissingAtom() = runTest {
        val expression = "(ROOT ( (WH-NP (W6P Who)) (SQ (VBZ 's) (VP (VBG :) (NP (DT the) (NN ;)))) (. ?)))"

        shouldThrowAny {
            treeParser.parseToEnd(expression)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAnExeptionDueNoRoot() = runTest {

        val expression3 = "((ROOT Who) (. ?))"

        shouldThrowAny {
            treeParser.parseToEnd(expression3)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAnExeptionDueMissingChild() = runTest {

        val expression2 = "(ROOT (SBQR (WH-NP (W6P )) (SQ (VBZ 's) (VP (VBG :) (NP (DT the) (NN ;)))) (. ?)))"

        shouldThrowAny {
            treeParser.parseToEnd(expression2)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAnExeptionDueFalseExpression() = runTest {

        val expression2 = "(ROOT (SBQR (WH-NP (W6P sdf ss)) (SQ (VBZ 's) (VP (VBG :) (NP (DT the) (NN ;)))) (. ?)))"

        shouldThrowAny {
            treeParser.parseToEnd(expression2)
        }
    }
}