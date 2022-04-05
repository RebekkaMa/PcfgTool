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
        tree.parseToRule() shouldBe Rule(false, lhs = "NP", rhs = "ART-NK NP-SB VP")

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