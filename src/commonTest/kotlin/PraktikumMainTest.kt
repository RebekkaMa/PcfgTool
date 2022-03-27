import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PraktikumMainTest {

    @Test
    fun shouldIrgendetwas() = runTest {
        testDummy(true) shouldBe false
    }

}