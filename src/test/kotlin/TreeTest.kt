
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import model.Rule
import model.Tree
import kotlin.test.Test

class TreeTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnANonLexicalRule() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )
        val child2 = Tree(atom = "NP-SB", arrayListOf())
        val child3 = Tree(atom = "OP", arrayListOf())
        val child4 = Tree(atom = "VP", arrayListOf(child3))
        val tree = Tree(atom = "NP",  arrayListOf(child1, child2, child4))
        tree.parseToRule() shouldBe Rule(false, lhs = "NP", rhs = listOf("ART-NK", "NP-SB", "VP"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnALexicalRule() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )
        val tree = Tree(atom = "NP",  arrayListOf(child1))
        tree.parseToRule() shouldBe Rule(true, lhs = "NP", rhs = listOf("ART-NK"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnAnException() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )

        shouldThrowAny {
            child1.parseToRule()
        }

    }

    //--------------------parseToRules---------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnACompleteRuleArray() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )
        val child2 = Tree(atom = "NP-SB", arrayListOf())
        val child3 = Tree(atom = "OP", arrayListOf())
        val child4 = Tree(atom = "VP", arrayListOf(child3))
        val tree = Tree(atom = "NP",  arrayListOf(child1, child2, child4))


        val treeRule = Rule(false, lhs = "NP", rhs= listOf("ART-NK", "NP-SB", "VP"))
        val child4Rule = Rule(true, lhs = "VP", rhs= listOf("OP"))
        val child5Rule = Rule(false, lhs = "IN", rhs= listOf("ID", "FD"))


        tree.parseToRule() shouldBe Rule(false, lhs = "NP", rhs = listOf("ART-NK", "NP-SB", "VP"))
    }


    //------------------debinarise-------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnADebinarisedTree() = runTest {


        val child11 = Tree(atom = ".", arrayListOf())
        val child10 = Tree(atom = ".", arrayListOf(child11))
        val child9 = Tree(atom = "year", arrayListOf())
        val child8 = Tree(atom = "NN", arrayListOf(child9))
        val child7 =Tree(atom = "this", arrayListOf())
        val child6 = Tree(atom = "DT", arrayListOf(child7))
        val child5 = Tree(atom = "NP-TMP^<FRAG,ROOT>", arrayListOf(child6, child8))
        val child4 = Tree(atom = "FRAG|<NP-TMP,.>^<ROOT>", arrayListOf(child5, child10))
        val child3 = Tree(atom = "Not", arrayListOf())
        val child2 = Tree(atom = "RB", arrayListOf(child3))
        val child1 = Tree(atom = "FRAG^<ROOT>", arrayListOf(child2, child4) )
        val tree = Tree(atom = "ROOT",  arrayListOf(child1))

        val child4_right = Tree("NP-TMP", arrayListOf(child6, child8))
        val child1_right =  Tree(atom = "FRAG", arrayListOf(child2, child4_right, child10) )
        val tree_right = Tree(atom = "ROOT",  arrayListOf(child1_right))

        tree.debinarise().toString() shouldBe tree_right.toString()
    }

    //---------------------binarise--------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnABinarisedTree() = runTest {


        val child11 = Tree(atom = ".", arrayListOf())
        val child10 = Tree(atom = ".", arrayListOf(child11))
        val child9 = Tree(atom = "year", arrayListOf())
        val child8 = Tree(atom = "NN", arrayListOf(child9))
        val child7 =Tree(atom = "this", arrayListOf())
        val child6 = Tree(atom = "DT", arrayListOf(child7))
        val child5 = Tree(atom = "NP-TMP^<FRAG,ROOT>", arrayListOf(child6, child8))
        val child4 = Tree(atom = "FRAG|<NP-TMP,.>^<ROOT>", arrayListOf(child5, child10))
        val child3 = Tree(atom = "Not", arrayListOf())
        val child2 = Tree(atom = "RB", arrayListOf(child3))
        val child1 = Tree(atom = "FRAG^<ROOT>", arrayListOf(child2, child4) )
        val tree = Tree(atom = "ROOT",  arrayListOf(child1))

        val child4_right = Tree("NP-TMP", arrayListOf(child6, child8))
        val child1_right =  Tree(atom = "FRAG", arrayListOf(child2, child4_right, child10) )
        val tree_right = Tree(atom = "ROOT",  arrayListOf(child1_right))

        tree_right.binarise(3,999).toString() shouldBe tree.toString()
    }

}