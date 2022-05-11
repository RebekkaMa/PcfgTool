import com.github.h0tk3y.betterParse.utils.Tuple5
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import model.Backtrace
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

    context("fillQueueWithItemsFromLexicalRules") {
            should("add Items to Queue") {
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
                    parser.fillQueueWithItemsFromLexicalRules(listOf("Fruit", "flies", "like", "bananas"))
                    parser.queue shouldContain Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null))
                    parser.queue shouldContain Tuple5(
                        1,
                        "NNS",
                        2,
                        1 / 3.toDouble(),
                        Backtrace(rule9 to 1 / 3.toDouble(), null)
                    )
                    parser.queue shouldContain Tuple5(1, "VBZ", 2, 1.0, Backtrace(rule12 to 1.0, null))
                    parser.queue shouldContain Tuple5(2, "VBP", 3, 1.0, Backtrace(rule11 to 1.0, null))
                    parser.queue shouldContain Tuple5(2, "IN", 3, 1.0, Backtrace(rule13 to 1.0, null))
                    parser.queue shouldContain Tuple5(
                        3,
                        "NNS",
                        4,
                        2 / 3.toDouble(),
                        Backtrace(rule10 to 2 / 3.toDouble(), null)
                    )
                    parser.queue.size shouldBe 6
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
                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null))
                parser.addSelectedItemProbabilityToSavedItems()
                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null)))
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null)))
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
                parser.accessFoundItemsFromLeft[Pair(0, "NN")] =
                    mutableListOf(Tuple5(0, "NN", 1, 0.0, Backtrace(rule8 to 1.0, null)))
                parser.accessFoundItemsFromRight[Pair("NN", 1)] =
                    mutableListOf(Tuple5(0, "NN", 1, 0.0, Backtrace(rule8 to 1.0, null)))

                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null))
                parser.addSelectedItemProbabilityToSavedItems()
                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null)))
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(Tuple5(0, "NN", 1, 1.0, Backtrace(rule8 to 1.0, null)))
                    )
                )


            }


        }

        context("When itemsLeft and itemsRight have present key but not item") {
            should("should add selected Item to itemsLeft and itemsRight") {
                val backtrace = Backtrace(Pair(rule1, 1.0), null)
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
                parser.accessFoundItemsFromLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.5, backtrace))
                parser.accessFoundItemsFromLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, backtrace))


                parser.selectedItem = Tuple5(0, "NN", 1, 1.0, backtrace)
                parser.addSelectedItemProbabilityToSavedItems()
                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(0, "NN", 2, 0.5, backtrace),
                            Tuple5<Int, String, Int, Double, Backtrace?>(0, "NN", 1, 1.0, backtrace)
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                backtrace
                            )
                        )
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 2),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                0,
                                "NN",
                                2,
                                0.5,
                                backtrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                backtrace
                            )
                        )
                    ),
                    Pair(
                        Pair("NN", 1),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                0,
                                "NN",
                                1,
                                1.0,
                                backtrace
                            )
                        )
                    )
                )
            }
        }

        context("When itemsLeft and itemsRight have selected Item already with probability greater than zero") {
            should("should make nothing") {
                val backtrace = Backtrace(Pair(rule1, 1.0), null)
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
                parser.accessFoundItemsFromLeft[Pair(0, "NN")] = mutableListOf(Tuple5(0, "NN", 2, 0.5, backtrace))
                parser.accessFoundItemsFromLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NN", 2)] = mutableListOf(Tuple5(0, "NN", 2, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.5, backtrace))


                parser.selectedItem = Tuple5(0, "NN", 2, 0.3, backtrace)
                parser.addSelectedItemProbabilityToSavedItems()
                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(0, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(0, "NN", 2, 0.5, backtrace)
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                backtrace
                            )
                        )
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 2),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                0,
                                "NN",
                                2,
                                0.5,
                                backtrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.5,
                                backtrace
                            )
                        )
                    )
                )
            }
        }
    }

    context("findRulesAddItemsToQueueSecondNtOnRhs") {
        context("When there is a matching rule and saved item") {
            should("should add the new Item to the queue") {
                val backtrace = Backtrace(Pair(rule1, 1.0), null)
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

                parser.accessFoundItemsFromLeft[Pair(3, "NN")] = mutableListOf(Tuple5(3, "NN", 4, 0.5, backtrace))
                parser.accessFoundItemsFromLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))

                parser.accessFoundItemsFromRight[Pair("NN", 4)] = mutableListOf(Tuple5(3, "NN", 4, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))

                parser.selectedItem = Tuple5(0, "NN", 2, 0.4, backtrace)
                parser.findRulesAddItemsToQueueSecondNtOnRhs()

                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(3, "NN"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                3,
                                "NN",
                                4,
                                0.5,
                                backtrace
                            )
                        )
                    ), Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                backtrace
                            )
                        )
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                    Pair(
                        Pair("NN", 4),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                3,
                                "NN",
                                4,
                                0.5,
                                backtrace
                            )
                        )
                    ), Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                backtrace
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
                        Backtrace(rule2 to 1 / 4.toDouble(), Pair(backtrace, backtrace))
                    )
                )
            }
        }
    }
    context("findRulesAddItemsToQueueFirstNtOnRhs") {
        context("When there is a matching rule and saved item") {
            should("should add the new Item to the queue") {
                val backtrace = Backtrace(Pair(rule1, 1.0), null)
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

                parser.accessFoundItemsFromLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))
                parser.accessFoundItemsFromLeft[Pair(0, "NP")] = mutableListOf(Tuple5(0, "NP", 3, 0.4, backtrace))

                parser.accessFoundItemsFromRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))
                parser.accessFoundItemsFromRight[Pair("NP", 3)] = mutableListOf(Tuple5(0, "NP", 3, 0.4, backtrace))


                parser.selectedItem = Tuple5(3, "VP", 4, 0.5, backtrace)

                parser.accessFoundItemsFromLeft shouldBe mutableMapOf(
                    Pair(
                        Pair(2, "NNS"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                backtrace
                            )
                        )
                    ), Pair(
                        Pair(0, "NP"),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                0,
                                "NP",
                                3,
                                0.4,
                                backtrace
                            )
                        )
                    )
                )
                parser.accessFoundItemsFromRight shouldBe mutableMapOf(
                     Pair(
                        Pair("NNS", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                2,
                                "NNS",
                                3,
                                0.6,
                                backtrace
                            )
                        )
                    ),
                    Pair(
                        Pair("NP", 3),
                        mutableListOf(
                            Tuple5<Int, String, Int, Double, Backtrace?>(
                                0,
                                "NP",
                                3,
                                0.4,
                                backtrace
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
                val backtrace = Backtrace(Pair(rule1, 1.0), null)
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

                parser.accessFoundItemsFromLeft[Pair(3, "NN")] = mutableListOf(Tuple5(3, "NN", 4, 0.5, backtrace))
                parser.accessFoundItemsFromLeft[Pair(2, "NNS")] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))

                parser.accessFoundItemsFromRight[Pair("NN", 4)] = mutableListOf(Tuple5(3, "NN", 4, 0.5, backtrace))
                parser.accessFoundItemsFromRight[Pair("NNS", 3)] = mutableListOf(Tuple5(2, "NNS", 3, 0.6, backtrace))

                parser.selectedItem = Tuple5(0, "S", 2, 0.4, backtrace)
                parser.findRulesAddItemsToQueueChain()


                parser.queue shouldBe mutableListOf(
                    Tuple5(
                        0,
                        "First",
                        2,
                        (0.4 * 0.9),
                        Backtrace(Rule(false, "First", listOf("S") ) to 0.9, Pair(backtrace, null))
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