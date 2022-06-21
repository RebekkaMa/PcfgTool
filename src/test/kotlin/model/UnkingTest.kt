package model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class UnkingTest : ShouldSpec({

    //replaceTokensByInt
    context("unking = false") {
        context("smoothing = false") {
            context("lexicon contains all tokens") {
                should("return an Intarray with the corresponding values") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("telescope", 30)
                        this.put("blue", 44)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")

                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = false
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, 30)

                }
            }
            context("lexicon does not contain all tokens") {
                should("return an Intarray with the corresponding values or -1") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("telescope", 30)
                        this.put("blue", 44)
                        this.put("UNK", 55)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")
                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = false
                    ) shouldBe intArrayOf(3, 4, -1, 7, 8, -1, 30)
                }
            }
        }
        context("smoothing = true") {
            context("lexicon contains all tokens") {
                should("return an Intarray with the corresponding values") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("UNK-Le", 30)
                        this.put("blue", 44)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")

                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = true
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, 30)
                }
            }
            context("lexicon does not contain all tokens") {
                should("return an Intarray with the corresponding values or -1") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("blue", 44)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")
                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = true
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, -1)
                }
            }
        }
    }
    context("unking = true") {
        context("smoothing = false") {
            context("lexicon contains all tokens") {
                should("return an Intarray with the corresponding values") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("telescope", 30)
                        this.put("blue", 44)
                        this.put("UNK", 55)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")

                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = true,
                        smoothing = false
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, 30)

                }
            }
            context("lexicon does not contain all tokens") {
                should("return an Intarray with the corresponding values or -1") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("telescope", 30)
                        this.put("blue", 44)
                        this.put("UNK", 55)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")
                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = true,
                        smoothing = false
                    ) shouldBe intArrayOf(3, 4, 55, 7, 8, 55, 30)
                }
            }
        }
        context("smoothing = true") {
            context("lexicon contains all signature tokens") {
                should("return an Intarray with the corresponding values") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("UNK-Le", 30)
                        this.put("blue", 44)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")

                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = true
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, 30)
                }
            }
            context("lexicon does not contain all signature tokens") {
                should("return an Intarray with the corresponding values or -1") {
                    val lexiconByString = buildMap {
                        this.put("He", 3)
                        this.put("saw", 4)
                        this.put("the", 6)
                        this.put("red", 33)
                        this.put("man", 7)
                        this.put("with", 8)
                        this.put("blue", 44)
                    }
                    val tokensAsString = listOf("He", "saw", "the", "man", "with", "the", "telescope")
                    replaceTokensByInts(
                        lexiconByString,
                        tokensAsString,
                        unking = false,
                        smoothing = true
                    ) shouldBe intArrayOf(3, 4, 6, 7, 8, 6, -1)
                }
            }
        }
    }
    //getTerminalCountFromCorpus
    should("return Terminal count") {
        val tree1 = Tree("S", mutableListOf(Tree("B", mutableListOf()), Tree("B", mutableListOf())))
        val tree2 = Tree("O", mutableListOf(Tree("C", mutableListOf()), Tree("B", mutableListOf())))
        getTerminalCountFromCorpus(listOf(tree1, tree2)) shouldBe mutableMapOf("B" to 3, "C" to 1)
    }
    //replaceRareWordsInTree
    context("smooth = false") {
        context("threshold = 2") {
            should {
                val wordcount = mutableMapOf("A" to 1, "B" to 2, "C" to 3, "D" to 4, "E" to 5, "F" to 6, "G" to 2)
                val tree1 = Tree(
                    "S",
                    mutableListOf(
                        Tree("A", mutableListOf()),
                        Tree("LB", mutableListOf(Tree("B", mutableListOf()))),
                        Tree("C", mutableListOf())
                    )
                )
                val tree2 = Tree(
                    "D",
                    mutableListOf(
                        Tree("E", mutableListOf()),
                        Tree("RB", mutableListOf(Tree("F", mutableListOf()))),
                        Tree("G", mutableListOf())
                    )
                )

                replaceRareWordsInTree(false, wordcount, threshold = 2, tree1)
                replaceRareWordsInTree(false, wordcount, threshold = 2, tree2)

                tree1.toString() shouldBe Tree(
                    "S",
                    mutableListOf(
                        Tree("UNK", mutableListOf()),
                        Tree("LB", mutableListOf(Tree("UNK", mutableListOf()))),
                        Tree("C", mutableListOf())
                    )
                ).toString()
                tree2.toString() shouldBe Tree(
                    "D",
                    mutableListOf(
                        Tree("E", mutableListOf()),
                        Tree("RB", mutableListOf(Tree("F", mutableListOf()))),
                        Tree("UNK", mutableListOf())
                    )
                ).toString()
            }
        }
    }
    context("smooth = true") {
        context("threshold = 2") {
            should {
                val wordcount =
                    mutableMapOf("Aber" to 1, "Baber" to 2, "C" to 3, "D" to 4, "E" to 5, "faultier" to 6, "G" to 2)
                val tree1 = Tree(
                    "S",
                    mutableListOf(
                        Tree("Aber", mutableListOf()),
                        Tree("LB", mutableListOf(Tree("Baber", mutableListOf()))),
                        Tree("C", mutableListOf())
                    )
                )
                val tree2 = Tree(
                    "D",
                    mutableListOf(
                        Tree("E", mutableListOf()),
                        Tree("RB", mutableListOf(Tree("faultier", mutableListOf()))),
                        Tree("G", mutableListOf())
                    )
                )

                replaceRareWordsInTree(true, wordcount, threshold = 2, tree1)
                replaceRareWordsInTree(true, wordcount, threshold = 2, tree2)

                tree1.toString() shouldBe Tree(
                    "S",
                    mutableListOf(
                        Tree("UNK-SCr", mutableListOf()),
                        Tree("LB", mutableListOf(Tree("UNK-Cr", mutableListOf()))),
                        Tree("C", mutableListOf())
                    )
                ).toString()
                tree2.toString() shouldBe Tree(
                    "D",
                    mutableListOf(
                        Tree("E", mutableListOf()),
                        Tree("RB", mutableListOf(Tree("faultier", mutableListOf()))),
                        Tree("UNK-AC", mutableListOf())
                    )
                ).toString()
            }
        }
    }
})