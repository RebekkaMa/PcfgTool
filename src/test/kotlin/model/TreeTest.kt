package model
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
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
        tree.transformToRule() shouldBe Rule(false, lhs = "NP", rhs = listOf("ART-NK", "NP-SB", "VP"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnALexicalRule() = runTest {
        val child1 = Tree(atom = "ART-NK", arrayListOf() )
        val tree = Tree(atom = "NP",  arrayListOf(child1))
        tree.transformToRule() shouldBe Rule(true, lhs = "NP", rhs = listOf("ART-NK"))
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun shouldReturnAnException() = runTest {
//        val child1 = Tree(atom = "ART-NK", arrayListOf() )
//
//        shouldThrowAny {
//            child1.parseToRule()
//        }
//
//    }

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


        tree.transformToRule() shouldBe Rule(false, lhs = "NP", rhs = listOf("ART-NK", "NP-SB", "VP"))
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

        //vertical=2--horizontal=999

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnABinarisedTree_simpleTree() = runTest {
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

        //vertical=1--horizontal=999

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldReturnABinarisedTreeVertical1() = runTest {
        val child11 = Tree(atom = ".", arrayListOf())
        val child10 = Tree(atom = ".", arrayListOf(child11))
        val child9 = Tree(atom = "year", arrayListOf())
        val child8 = Tree(atom = "NN", arrayListOf(child9))
        val child7 =Tree(atom = "this", arrayListOf())
        val child6 = Tree(atom = "DT", arrayListOf(child7))
        val child5 = Tree(atom = "NP-TMP", arrayListOf(child6, child8))
        val child4 = Tree(atom = "FRAG|<NP-TMP,.>", arrayListOf(child5, child10))
        val child3 = Tree(atom = "Not", arrayListOf())
        val child2 = Tree(atom = "RB", arrayListOf(child3))
        val child1 = Tree(atom = "FRAG", arrayListOf(child2, child4) )
        val tree = Tree(atom = "ROOT",  arrayListOf(child1))

        val child4_right = Tree("NP-TMP", arrayListOf(child6, child8))
        val child1_right =  Tree(atom = "FRAG", arrayListOf(child2, child4_right, child10) )
        val tree_right = Tree(atom = "ROOT",  arrayListOf(child1_right))

        tree_right.binarise(1,999).toString() shouldBe tree.toString()
    }

    @Test
    fun shouldReturnADebinarisedTree_complexTree(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7_b = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10_b = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14_b = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17_b = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21_b = Tree("NP", mutableListOf(treeNNS21))

        val tree_5_7_b = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7_b))
        val tree_12_14_b = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14_b))
        val tree_20_21_b = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21_b))

        val tree_11_14_b = Tree("VP", mutableListOf(treeVBD11, tree_12_14_b))
        val tree_19_21_b = Tree("VP|<RB,PP-PRD>", mutableListOf(treeRB19, tree_20_21_b))

        val tree_9_14_b = Tree("S", mutableListOf(tree_9_10_b, tree_11_14_b))
        val tree_18_21_b = Tree("VP", mutableListOf(treeVBZ18, tree_19_21_b))

        val tree_8_14_b = Tree("SBAR", mutableListOf(treeIN8, tree_9_14_b))
        val tree_17_21_b = Tree("S", mutableListOf(tree_17_17_b, tree_18_21_b))

        val tree_5_14_b = Tree("VP|<PP-TMP,SBAR>", mutableListOf(tree_5_7_b, tree_8_14_b))
        val tree_17_22_b = Tree("S|<S,.>", mutableListOf(tree_17_21_b, treePoint22))

        val tree_3_14_b = Tree("VP|<NP,PP-TMP,SBAR>", mutableListOf(tree_3_4_b, tree_5_14_b))
        val tree_16_22_b = Tree("S|<CC,S,.>", mutableListOf(treeCC16, tree_17_22_b))

        val tree_2_14_b = Tree("VP", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,,CC,S,.>", mutableListOf(treeComma15, tree_16_22_b ))

        val tree_1_14_b = Tree("S", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root_b.debinarise().toString() shouldBeEqualComparingTo "(ROOT (S (S (NP-SBJ (PRP It)) (VP (VBZ gets) (NP (JJR more) (NN mail)) (PP-TMP (IN in) (NP (DT a) (NN month))) (SBAR (IN than) (S (NP-SBJ (NNP McCall) (POS 's)) (VP (VBD got) (PP-TMP (IN in) (NP (DT a) (NN year)))))))) (, ,) (CC and) (S (NP-SBJ (PRP it)) (VP (VBZ 's) (RB not) (PP-PRD (IN from) (NP (NNS mothers))))) (. .)))"
    }

    @Test
    fun shouldReturnABinarisedTree_complexTree(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7_b = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10_b = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14_b = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17_b = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21_b = Tree("NP", mutableListOf(treeNNS21))

        val tree_5_7_b = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7_b))
        val tree_12_14_b = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14_b))
        val tree_20_21_b = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21_b))

        val tree_11_14_b = Tree("VP", mutableListOf(treeVBD11, tree_12_14_b))
        val tree_19_21_b = Tree("VP|<RB,PP-PRD>", mutableListOf(treeRB19, tree_20_21_b))

        val tree_9_14_b = Tree("S", mutableListOf(tree_9_10_b, tree_11_14_b))
        val tree_18_21_b = Tree("VP", mutableListOf(treeVBZ18, tree_19_21_b))

        val tree_8_14_b = Tree("SBAR", mutableListOf(treeIN8, tree_9_14_b))
        val tree_17_21_b = Tree("S", mutableListOf(tree_17_17_b, tree_18_21_b))

        val tree_5_14_b = Tree("VP|<PP-TMP,SBAR>", mutableListOf(tree_5_7_b, tree_8_14_b))
        val tree_17_22_b = Tree("S|<S,.>", mutableListOf(tree_17_21_b, treePoint22))

        val tree_3_14_b = Tree("VP|<NP,PP-TMP,SBAR>", mutableListOf(tree_3_4_b, tree_5_14_b))
        val tree_16_22_b = Tree("S|<CC,S,.>", mutableListOf(treeCC16, tree_17_22_b))

        val tree_2_14_b = Tree("VP", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,,CC,S,.>", mutableListOf(treeComma15, tree_16_22_b ))

        val tree_1_14_b = Tree("S", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root.binarise(1, 999).toString() shouldBeEqualComparingTo root_b.toString()
    }

    @Test
    fun shouldReturnABinarisedTree_complexTree_vertical2(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ^<S>", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP^<VP>", mutableListOf(treeJJR, treeNN4))
        val tree_6_7_b = Tree("NP^<PP-TMP>", mutableListOf(treeDT6, treeNN7))
        val tree_9_10_b = Tree("NP-SBJ^<S>", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14_b = Tree("NP^<PP-TMP>", mutableListOf(treeDT13, treeNN14))
        val tree_17_17_b = Tree("NP-SBJ^<S>", mutableListOf(treePRP17))
        val tree_21_21_b = Tree("NP^<PP-PRD>", mutableListOf(treeNNS21))

        val tree_5_7_b = Tree("PP-TMP^<VP>", mutableListOf(treeIN5, tree_6_7_b))
        val tree_12_14_b = Tree("PP-TMP^<VP>", mutableListOf(treeIN12, tree_13_14_b))
        val tree_20_21_b = Tree("PP-PRD^<VP>", mutableListOf(treeIN20, tree_21_21_b))

        val tree_11_14_b = Tree("VP^<S>", mutableListOf(treeVBD11, tree_12_14_b))
        val tree_19_21_b = Tree("VP|<RB,PP-PRD>^<S>", mutableListOf(treeRB19, tree_20_21_b))

        val tree_9_14_b = Tree("S^<SBAR>", mutableListOf(tree_9_10_b, tree_11_14_b))
        val tree_18_21_b = Tree("VP^<S>", mutableListOf(treeVBZ18, tree_19_21_b))

        val tree_8_14_b = Tree("SBAR^<VP>", mutableListOf(treeIN8, tree_9_14_b))
        val tree_17_21_b = Tree("S^<S>", mutableListOf(tree_17_17_b, tree_18_21_b))

        val tree_5_14_b = Tree("VP|<PP-TMP,SBAR>^<S>", mutableListOf(tree_5_7_b, tree_8_14_b))
        val tree_17_22_b = Tree("S|<S,.>^<ROOT>", mutableListOf(tree_17_21_b, treePoint22))

        val tree_3_14_b = Tree("VP|<NP,PP-TMP,SBAR>^<S>", mutableListOf(tree_3_4_b, tree_5_14_b))
        val tree_16_22_b = Tree("S|<CC,S,.>^<ROOT>", mutableListOf(treeCC16, tree_17_22_b))

        val tree_2_14_b = Tree("VP^<S>", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,,CC,S,.>^<ROOT>", mutableListOf(treeComma15, tree_16_22_b ))

        val tree_1_14_b = Tree("S^<S>", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S^<ROOT>", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root.binarise(2, 999).toString() shouldBeEqualComparingTo root_b.toString()
    }

    @Test
    fun shouldReturnA_BinarisedTree_and_DebinarisedTree_complexTree_vertical3(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ^<S,S>", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP^<VP,S>", mutableListOf(treeJJR, treeNN4))
        val tree_6_7_b = Tree("NP^<PP-TMP,VP>", mutableListOf(treeDT6, treeNN7))
        val tree_9_10_b = Tree("NP-SBJ^<S,SBAR>", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14_b = Tree("NP^<PP-TMP,VP>", mutableListOf(treeDT13, treeNN14))
        val tree_17_17_b = Tree("NP-SBJ^<S,S>", mutableListOf(treePRP17))
        val tree_21_21_b = Tree("NP^<PP-PRD,VP>", mutableListOf(treeNNS21))

        val tree_5_7_b = Tree("PP-TMP^<VP,S>", mutableListOf(treeIN5, tree_6_7_b))
        val tree_12_14_b = Tree("PP-TMP^<VP,S>", mutableListOf(treeIN12, tree_13_14_b))
        val tree_20_21_b = Tree("PP-PRD^<VP,S>", mutableListOf(treeIN20, tree_21_21_b))

        val tree_11_14_b = Tree("VP^<S,SBAR>", mutableListOf(treeVBD11, tree_12_14_b))
        val tree_19_21_b = Tree("VP|<RB,PP-PRD>^<S,S>", mutableListOf(treeRB19, tree_20_21_b))

        val tree_9_14_b = Tree("S^<SBAR,VP>", mutableListOf(tree_9_10_b, tree_11_14_b))
        val tree_18_21_b = Tree("VP^<S,S>", mutableListOf(treeVBZ18, tree_19_21_b))

        val tree_8_14_b = Tree("SBAR^<VP,S>", mutableListOf(treeIN8, tree_9_14_b))
        val tree_17_21_b = Tree("S^<S,ROOT>", mutableListOf(tree_17_17_b, tree_18_21_b))

        val tree_5_14_b = Tree("VP|<PP-TMP,SBAR>^<S,S>", mutableListOf(tree_5_7_b, tree_8_14_b))
        val tree_17_22_b = Tree("S|<S,.>^<ROOT>", mutableListOf(tree_17_21_b, treePoint22))

        val tree_3_14_b = Tree("VP|<NP,PP-TMP,SBAR>^<S,S>", mutableListOf(tree_3_4_b, tree_5_14_b))
        val tree_16_22_b = Tree("S|<CC,S,.>^<ROOT>", mutableListOf(treeCC16, tree_17_22_b))

        val tree_2_14_b = Tree("VP^<S,S>", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,,CC,S,.>^<ROOT>", mutableListOf(treeComma15, tree_16_22_b ))

        val tree_1_14_b = Tree("S^<S,ROOT>", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S^<ROOT>", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root.binarise(3, 999).toString() shouldBeEqualComparingTo root_b.toString()

        root_b.debinarise().toString() shouldBeEqualComparingTo root.toString()
    }

    @Test
    fun shouldReturnA_BinarisedTree_and_DebinarisedTree_complexTree_vertical3_horizontal2(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ^<S,S>", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP^<VP,S>", mutableListOf(treeJJR, treeNN4))
        val tree_6_7_b = Tree("NP^<PP-TMP,VP>", mutableListOf(treeDT6, treeNN7))

        val tree_5_7_b = Tree("PP-TMP^<VP,S>", mutableListOf(treeIN5, tree_6_7_b))

        val tree_3_14_b = Tree("VP|<NP,PP-TMP>^<S,S>", mutableListOf(tree_3_4_b, tree_5_7_b))

        val tree_2_14_b = Tree("VP^<S,S>", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,,CC>^<ROOT>", mutableListOf(treeComma15, treeCC16 ))

        val tree_1_14_b = Tree("S^<S,ROOT>", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S^<ROOT>", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root.binarise(3, 2).toString() shouldBeEqualComparingTo root_b.toString()

    }

    @Test
    fun shouldReturnA_BinarisedTree_and_DebinarisedTree_complexTree_vertical3_horizizontal1(){
        val treePRP = createPreTerminalTree("PRP", "It")
        val treeVBZ = createPreTerminalTree("VBZ", "gets")
        val treeJJR = createPreTerminalTree("JJR", "more")
        val treeNN4 = createPreTerminalTree("NN", "mail")
        val treeIN5 = createPreTerminalTree("IN", "in")
        val treeDT6 = createPreTerminalTree("DT", "a")
        val treeNN7 = createPreTerminalTree("NN", "month")
        val treeIN8 = createPreTerminalTree("IN", "than")
        val treeNNP9 = createPreTerminalTree("NNP", "McCall")
        val treePOS10 = createPreTerminalTree("POS", "'s")
        val treeVBD11 = createPreTerminalTree("VBD", "got")
        val treeIN12 = createPreTerminalTree("IN", "in")
        val treeDT13 = createPreTerminalTree("DT", "a")
        val treeNN14 = createPreTerminalTree("NN", "year")
        val treeComma15 = createPreTerminalTree(",", ",")
        val treeCC16 = createPreTerminalTree("CC", "and")
        val treePRP17 = createPreTerminalTree("PRP", "it")
        val treeVBZ18 = createPreTerminalTree("VBZ", "'s")
        val treeRB19 = createPreTerminalTree("RB", "not")
        val treeIN20 = createPreTerminalTree("IN", "from")
        val treeNNS21 = createPreTerminalTree("NNS", "mothers")
        val treePoint22 = createPreTerminalTree(".", ".")

        val tree_1_1_b = Tree("NP-SBJ^<S,S>", mutableListOf(treePRP))
        val tree_3_4_b = Tree("NP^<VP,S>", mutableListOf(treeJJR, treeNN4))

        val tree_3_14_b = Tree("VP|<NP>^<S,S>", mutableListOf(tree_3_4_b))

        val tree_2_14_b = Tree("VP^<S,S>", mutableListOf(treeVBZ, tree_3_14_b))
        val tree_15_22_b = Tree("S|<,>^<ROOT>", mutableListOf(treeComma15))

        val tree_1_14_b = Tree("S^<S,ROOT>", mutableListOf( tree_1_1_b, tree_2_14_b))

        val tree_1_22_b = Tree("S^<ROOT>", mutableListOf(tree_1_14_b, tree_15_22_b))

        val root_b = Tree("ROOT", mutableListOf(tree_1_22_b))

        //-------

        val tree_1_1 = Tree("NP-SBJ", mutableListOf(treePRP))
        val tree_3_4 = Tree("NP", mutableListOf(treeJJR, treeNN4))
        val tree_6_7 = Tree("NP", mutableListOf(treeDT6, treeNN7))
        val tree_9_10 = Tree("NP-SBJ", mutableListOf(treeNNP9, treePOS10))
        val tree_13_14 = Tree("NP", mutableListOf(treeDT13, treeNN14))
        val tree_17_17 = Tree("NP-SBJ", mutableListOf(treePRP17))
        val tree_21_21 = Tree("NP", mutableListOf(treeNNS21))


        val tree_5_7 = Tree("PP-TMP", mutableListOf(treeIN5, tree_6_7))
        val tree_12_14 = Tree("PP-TMP", mutableListOf(treeIN12, tree_13_14))
        val tree_20_21 = Tree("PP-PRD", mutableListOf(treeIN20, tree_21_21))

        val tree_11_14 = Tree("VP", mutableListOf(treeVBD11, tree_12_14))
        val tree_18_21 = Tree("VP", mutableListOf(treeVBZ18, treeRB19, tree_20_21))

        val tree_9_14 = Tree("S", mutableListOf(tree_9_10, tree_11_14))
        val tree_17_21= Tree("S", mutableListOf(tree_17_17, tree_18_21))

        val tree_8_14 = Tree("SBAR", mutableListOf(treeIN8, tree_9_14))
        val tree_2_14 = Tree("VP", mutableListOf(treeVBZ, tree_3_4, tree_5_7, tree_8_14))
        val tree_1_14 = Tree("S", mutableListOf(tree_1_1, tree_2_14))

        val tree_1_22 = Tree("S", mutableListOf(tree_1_14, treeComma15, treeCC16, tree_17_21, treePoint22))

        val root = Tree("ROOT", mutableListOf(tree_1_22))

        root.binarise(3, 1).toString() shouldBeEqualComparingTo root_b.toString()

    }

    fun createPreTerminalTree(atom : String, child : String): Tree {
        return Tree(atom, mutableListOf(Tree(child, mutableListOf())))
    }

}