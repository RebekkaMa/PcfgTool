import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
}