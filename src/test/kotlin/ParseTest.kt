import controller.Parse
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test

class ParseTest {

    @Test
    fun parseGoldShort() {
        val sentencesFile = File("src/test/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("src/test/resources/small/grammar.rules", "src/test/resources/small/grammar.lexicon"))

        File("src/test/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    @Test
    fun parseGoldShortWithAFormat(){
        val sentencesFile = File("src/test/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/test/resources/small/grammar.rules", "src/test/resources/small/grammar.lexicon"))


        File("src/test/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    @Test
    fun parseWithRank6() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-r",
                "6",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(S (NP (NN Fruit)) (VP (VBZ flies) (PP (IN like) (NP (NNS bananas)))))\n"
    }
    @Test
    fun parseWithRank5() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-r",
                "5",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )

        outputStreamCaptor.toString() shouldBe "(S (NP (NN Fruit) (NNS flies)) (VP (VBP like) (NP (NNS bananas))))\n"

    }

    @Test
    fun parseWithRank4() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-r",
                "4",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(NOPARSE Fruit flies like bananas)\n"
    }

    @Test
    fun parseWithThreshold0() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-t",
                "0.0",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(S (NP (NN Fruit)) (VP (VBZ flies) (PP (IN like) (NP (NNS bananas)))))\n"
    }
    @Test
    fun parseWithThreshold() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-t",
                "0.25",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(NOPARSE Fruit flies like bananas)\n"
    }
    @Test
    fun parseWithThreshold25() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-t",
                "0.25",
                "src/test/resources/grammarFruitFliesv2.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(S (NP (NN Fruit)) (VP (VBZ flies) (PP (IN like) (NP (NNS bananas)))))\n"
    }

    @Test
    fun parseWithA() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-a",
                "src/test/resources/grammarFruitFlies.outside",
                "-i",
                "S",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(S (NP (NN Fruit) (NNS flies)) (VP (VBP like) (NP (NNS bananas))))\n"
    }
}