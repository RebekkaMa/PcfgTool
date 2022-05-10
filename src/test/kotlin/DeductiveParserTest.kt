import com.github.h0tk3y.betterParse.utils.Tuple5
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import model.Bactrace
import model.DeductiveParser
import model.Grammar
import model.Rule

class DeductiveParserTest : ShouldSpec({

    val rule1 = Rule(false, "S", listOf("NP", "VP"))
    val rule2 = Rule(false, "NP", listOf("NN", "NNS"))
    val rule3 = Rule(false, "NP", listOf("NNS"))
    val rule4 = Rule(false, "NP", listOf("NN"))
    val rule5 = Rule(false, "VP", listOf("VBP", "NP"))
    val rule6 = Rule(false, "VP", listOf("VBZ", "PP"))
    val rule7 = Rule(false, "PP", listOf("IN", "NP"))

    val rule8 = Rule(true, "NN", listOf("Fruit"))
    val rule9 = Rule(true, "NNS", listOf("flies"))
    val rule10 = Rule(true, "NNS", listOf("bananas"))
    val rule11 = Rule(true, "VBP", listOf("like"))
    val rule12 = Rule(true, "VBZ", listOf("flies"))
    val rule13 = Rule(true, "IN", listOf("like"))

    context("getQueueElementsFromLexicalRules") {
        should("return Queue Elements from lexical Rules") {
            runTest {
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )

                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                parser.fillQueueElementsFromLexicalRules(listOf("Fruit", "flies", "like", "bananas")) shouldBe null
                parser.queue shouldContain Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null))
                parser.queue shouldContain Tuple5(
                    1,
                    "NNS",
                    2,
                    1 / 3.toDouble(),
                    Bactrace(rule9 to 1 / 3.toDouble(), null)
                )
                parser.queue shouldContain Tuple5(1, "VBZ", 2, 1.0, Bactrace(rule12 to 1.0, null))
                parser.queue shouldContain Tuple5(2, "VBP", 3, 1.0, Bactrace(rule11 to 1.0, null))
                parser.queue shouldContain Tuple5(2, "IN", 3, 1.0, Bactrace(rule13 to 1.0, null))
                parser.queue shouldContain Tuple5(
                    3,
                    "NNS",
                    4,
                    2 / 3.toDouble(),
                    Bactrace(rule10 to 2 / 3.toDouble(), null)
                )
                parser.queue.size shouldBe 6
            }
        }
        context("sentence contains of one word"){
            context("Tree exist"){
                should("return Result Element with Bactrace") {
                    runTest {
                        val grammar = Grammar.create(
                            initial = "NNS",
                            mapOf(
                                rule1 to 1.0,
                                rule2 to 1 / 4.toDouble(),
                                rule3 to 1 / 2.toDouble(),
                                rule4 to 1 / 4.toDouble(),
                                rule5 to 1 / 2.toDouble(),
                                rule6 to 1 / 2.toDouble(),
                                rule7 to 1.0,
                                rule8 to 1.0,
                                Rule(true, "NNS", listOf("flies")) to 1 / 3.toDouble(),
                                rule10 to 2 / 3.toDouble(),
                                rule11 to 1.0,
                                rule12 to 1.0,
                                rule13 to 1.0
                            )
                        )

                        val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                        val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                        parser.fillQueueElementsFromLexicalRules(listOf("flies")) shouldBe Tuple5(0, "NNS", 1, 1 / 3.toDouble(), Bactrace(Rule(true, "NNS", listOf("flies")) to 1 / 3.toDouble(), null))
                    }
                }
            }
            context("Tree not exist"){
                should("return null") {
                    runTest {
                        val grammar = Grammar.create(
                            initial = "VBZ",
                            mapOf(
                                rule1 to 1.0,
                                rule2 to 1 / 4.toDouble(),
                                rule3 to 1 / 2.toDouble(),
                                rule4 to 1 / 4.toDouble(),
                                rule5 to 1 / 2.toDouble(),
                                rule6 to 1 / 2.toDouble(),
                                rule7 to 1.0,
                                rule8 to 1.0,
                                Rule(true, "VBZ", listOf("flies")) to 1 / 3.toDouble(),
                                rule10 to 2 / 3.toDouble(),
                                rule11 to 1.0,
                                rule12 to 1.0,
                                rule13 to 1.0
                            )
                        )

                        val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                        val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                        parser.fillQueueElementsFromLexicalRules(listOf("Ananas")) shouldBe null
                    }
                }
            }
        }
    }

    context("addSelectedItemPropertyToSavedItems") {
        context("When itemsLeft and itemsRight are empty") {
            should("should add selected Item to itemsLeft and itemsRight") {
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null))
                parser.addSelectedItemPropertyToSavedItems()
                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null)))
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null)))
                    )
                )
            }
        }
        context("When itemsLeft and itemsRight have selected Item already but with probability zero") {
            should("should change probability to new higher value") {
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                parser.itemsLeft[Pair(0, "NN")] =
                    mutableListOf(Tuple5(0, "NN", 1, 0.0, Bactrace(rule8 to 1.0, null)))
                parser.itemsRight[Pair("NN", 1)] =
                    mutableListOf(Tuple5(0, "NN", 1, 0.0, Bactrace(rule8 to 1.0, null)))

                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null))
                parser.addSelectedItemPropertyToSavedItems()
                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null)))
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Bactrace(rule8 to 1.0, null)))
                    )
                )


            }


        }

        context("When itemsLeft and itemsRight have present key but not item") {
            should("should add selected Item to itemsLeft and itemsRight") {
                val bactrace = Bactrace(Pair(rule1, 1.0), null)
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                parser.itemsLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.5, bactrace))
                parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, bactrace))
                parser.itemsRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.5, bactrace))
                parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, bactrace))


                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, bactrace)
                parser.addSelectedItemPropertyToSavedItems()
                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(0, "NN", 2, 0.5, bactrace),
                            Tuple5<Int, String, Int, Double, Bactrace?>(0, "NN", 1, 1.0, bactrace)
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                bactrace
                            )
                        )
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 2),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                0,
                                "NN",
                                2,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                bactrace
                            )
                        )
                    ),
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                0,
                                "NN",
                                1,
                                1.0,
                                bactrace
                            )
                        )
                    )
                )
            }
        }

        context("When itemsLeft and itemsRight have selected Item already with probability greater than zero") {
            should("should make nothing") {
                val bactrace = Bactrace(Pair(rule1, 1.0), null)
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                parser.itemsLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.5, bactrace))
                parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, bactrace))
                parser.itemsRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.5, bactrace))
                parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, bactrace))


                parser.selectedItem = Tuple5(0, "NN", 2, 0.3, bactrace)
                parser.addSelectedItemPropertyToSavedItems()
                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(0, "NN", 2, 0.5, bactrace)
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                bactrace
                            )
                        )
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 2),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                0,
                                "NN",
                                2,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                bactrace
                            )
                        )
                    )
                )
            }
        }
    }

    context("findRuleAddItemToQueueRHS") {
        context("When there is a matching rule and saved item") {
            should("should add the new Item to the queue") {
                val bactrace = Bactrace(Pair(rule1, 1.0), null)
                val grammar = Grammar.create(
                    initial = "ROOT",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)

                parser.itemsLeft[Pair(3, "NN")] = mutableListOf(Tuple5(3, "NN", 4, 0.5, bactrace))
                parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))

                parser.itemsRight[Pair("NN", 4)] = mutableListOf(Tuple5(3, "NN", 4, 0.5, bactrace))
                parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))

                parser.selectedItem = Tuple5(0, "NN", 2, 0.4, bactrace)
                parser.findRuleAddItemToQueueRhs(listOf("Fruit", "flies", "like", "bananas"))

                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(3, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                3,
                                "NN",
                                4,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                bactrace
                            )
                        )
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 4),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                3,
                                "NN",
                                4,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                bactrace
                            )
                        )
                    )
                )

                parser.queue shouldBe mutableListOf(
                    Tuple5(
                        0,
                        "NP",
                        3,
                        (1 / 4.toDouble() * 0.6 * 0.4),
                        Bactrace(rule2 to 1 / 4.toDouble(), Pair(bactrace, bactrace))
                    )
                )
            }
        }
    }
    context("findRuleAddItemtoQueueLhs") {
        context("When there is a matching rule and saved item") {
            should("should add the new Item to the queue") {
                val bactrace = Bactrace(Pair(rule1, 1.0), null)
                val grammar = Grammar.create(
                    initial = "S",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)

                parser.itemsLeft[Pair(3, "VP")] = mutableListOf(Tuple5(3, "VP", 4, 0.5, bactrace))
                parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))
                parser.itemsLeft[Pair(0, "NP")] = mutableListOf(Tuple5(0, "NP", 3, 0.4, bactrace))


                parser.itemsRight[Pair("VP", 4)] = mutableListOf(Tuple5(3, "VP", 4, 0.5, bactrace))
                parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))
                parser.itemsRight[Pair("NP", 3)] = mutableListOf(Tuple5(0, "NP", 3, 0.4, bactrace))


                parser.selectedItem = Tuple5(3, "VP", 4, 0.5, bactrace)
                parser.findRuleAddItemToQueueLhs(listOf("Fruit", "flies", "like", "bananas")) shouldBe Tuple5(
                    0,
                    "S",
                    4,
                    0.5 * 0.4,
                    Bactrace(rule1 to 1.0, Pair(bactrace, bactrace))
                )

                parser.itemsLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(3, "VP"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                3,
                                "VP",
                                4,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair(0, "NP"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                0,
                                "NP",
                                3,
                                0.4,
                                bactrace
                            )
                        )
                    )
                )
                parser.itemsRight shouldBe mutableMapOf(
                    Pair(
                        Pair("VP", 4),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                3,
                                "VP",
                                4,
                                0.5,
                                bactrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                bactrace
                            )
                        )
                    ),
                    Pair(
                        Pair("NP", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Bactrace?>(
                                0,
                                "NP",
                                3,
                                0.4,
                                bactrace
                            )
                        )
                    )
                )

                parser.queue shouldBe mutableListOf()
            }
        }
    }

    context("findRuleAddItemToQueueChain"){
        context("When there is a matiching rule and saved item"){
            should("should add the new Item to the queue"){
                val bactrace = Bactrace(Pair(rule1, 1.0), null)
                val grammar = Grammar.create(
                    initial = "First",
                    mapOf(
                        Rule(false, "First", listOf("S") ) to 0.9,
                        rule1 to 0.5,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)

                parser.itemsLeft[Pair(3, "NN")] = mutableListOf(Tuple5(3, "NN", 4, 0.5, bactrace))
                parser.itemsLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))

                parser.itemsRight[Pair("NN", 4)] = mutableListOf(Tuple5(3, "NN", 4, 0.5, bactrace))
                parser.itemsRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, bactrace))

                parser.selectedItem = Tuple5(0, "S", 2, 0.4, bactrace)
                parser.findRuleAddItemToQueueChain(listOf("Fruit", "flies", "like", "bananas"))


                parser.queue shouldBe mutableListOf(
                    Tuple5(
                        0,
                        "First",
                        2,
                        (0.4 * 0.9),
                        Bactrace(Rule(false, "First", listOf("S") ) to 0.9, Pair(bactrace, null))
                    )
                )
            }
        }
    }

    context("weightedDeductiveParsing") {
        context("When there is a parsing tree for the sentence") {
            should("return the corresponding item with the parsing tree") {
                val grammar = Grammar.create(
                    initial = "S",
                    mapOf(
                        rule1 to 1.0,
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)
                val tupel = parser.weightedDeductiveParsing(listOf("Fruit", "flies", "like", "bananas"))
                tupel.second?.t1 shouldBe 0
                tupel.second?.t2 shouldBe "S"
                tupel.second?.t3 shouldBe 4
                tupel.second?.t4 shouldBe 1 / 24.toDouble()
            }
        }
        context("When there is not a parsing tree for the sentence") {
            should("return null") {
                val grammar = Grammar.create(
                    initial = "S",
                    mapOf(
                        rule2 to 1 / 4.toDouble(),
                        rule3 to 1 / 2.toDouble(),
                        rule4 to 1 / 4.toDouble(),
                        rule5 to 1 / 2.toDouble(),
                        rule6 to 1 / 2.toDouble(),
                        rule7 to 1.0,
                        rule8 to 1.0,
                        rule9 to 1 / 3.toDouble(),
                        rule10 to 2 / 3.toDouble(),
                        rule11 to 1.0,
                        rule12 to 1.0,
                        rule13 to 1.0
                    )
                )
                val (grammarRhs, grammarLhs, grammarChain, grammarLexical) = grammar.getGrammarDataStructuresForParsing()
                val parser = DeductiveParser(grammar.initial, grammarRhs, grammarLhs, grammarChain, grammarLexical)

                parser.weightedDeductiveParsing(listOf("Fruit", "flies", "like", "bananas")) shouldBe Pair(listOf("Fruit", "flies", "like", "bananas"), null)
            }
        }

    }
})