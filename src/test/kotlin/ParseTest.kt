import controller.Parse
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test

class ParseTest {

    @Test
    fun parseGoldShort() {
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))

        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    //    @Test
//    fun parseGoldShortWithAFormat(){
//        val sentencesFile = File("src/main/resources/small/sentences")
//        val standardOut = System.out
//        val outputStreamCaptor = ByteArrayOutputStream()
//        System.setOut(PrintStream(outputStreamCaptor))
//        val sentences = sentencesFile.inputStream()
//        System.setIn(sentences)
//
//        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
//        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
//
//
//        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
//    }
//
//    @Test
//    fun parseGoldShortWithANotFormat(){
//        val sentencesFile = File("src/main/resources/small/sentences")
//        val standardOut = System.out
//        val outputStreamCaptor = ByteArrayOutputStream()
//        System.setOut(PrintStream(outputStreamCaptor))
//        val sentences = sentencesFile.inputStream()
//        System.setIn(sentences)
//
//        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
//        Parse().parse(listOf("-a", "src/test/resources/outsideShortNotFormat.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
//
//
//        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
//    }
//
////
    @Test
    fun parseGoldShortWithRank6() {
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
    fun parseGoldShortWithRank5() {
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
    fun parseGoldShortWithRank4() {
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = "Fruit flies like bananas".byteInputStream()
        System.setIn(sentences)

        Parse().parse(
            listOf(
                "-i",
                "S",
                "-r",
                "1",
                "src/test/resources/grammarFruitFlies.rules",
                "src/test/resources/grammarFruitFlies.lexicon"
            )
        )
        outputStreamCaptor.toString() shouldBe "(NOPARSE Fruit flies like bananas)\n"
    }

    @Test
    fun parseGoldShortWithThreshold0() {
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
    fun parseGoldShortWithThreshold() {
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
    fun parseGoldShortWithThreshold25() {
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
}