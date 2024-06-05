
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToDec
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToHex
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.binToString
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.convertBinToBit
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.decToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.fill
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.hexToBin
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.isNumeric
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.splitEqually
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.strZero
import com.apes.capuchin.rfidcorelib.epctagcoder.util.Converter.stringToBin
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConverterTest {

    @Test
    fun testConvertBinToBit() {
        assertThat("1".convertBinToBit(1, 4)).isEqualTo("0001")
        assertThat("10".convertBinToBit(2, 4)).isEqualTo("0010")
    }

    @Test
    fun testHexToBin() {
        assertThat("0".hexToBin()).isEqualTo("0000")
        assertThat("F".hexToBin()).isEqualTo("1111")
    }

    @Test
    fun testBinToHex() {
        assertThat("0000".binToHex()).isEqualTo("0")
        assertThat("1111".binToHex()).isEqualTo("F")
    }

    @Test
    fun testStringToBin() {
        assertThat("15".stringToBin(4)).isEqualTo("1111")
        assertThat("2".stringToBin(4)).isEqualTo("0010")
    }

    @Test
    fun testBinToString() {
        assertThat("00000001".binToString()).isEqualTo("1")
        assertThat("00000010".binToString()).isEqualTo("2")
    }

    @Test
    fun testDecToBin() {
        assertThat(1.decToBin()).isEqualTo("1")
        assertThat(2.decToBin()).isEqualTo("10")
    }

    @Test
    fun testDecToBinWithBits() {
        assertThat(1.decToBin(4)).isEqualTo("0001")
        assertThat(2.decToBin(4)).isEqualTo("0010")
    }

    @Test
    fun testStringDecToBin() {
        assertThat("1".decToBin(4)).isEqualTo("0001")
        assertThat("2".decToBin(4)).isEqualTo("0010")
    }

    @Test
    fun testBinToDec() {
        assertThat("1".binToDec()).isEqualTo("1")
        assertThat("10".binToDec()).isEqualTo("2")
    }

    @Test
    fun testSplitEqually() {
        assertThat("1234".splitEqually(2)).containsExactly("12", "34")
        assertThat("12345".splitEqually(2)).containsExactly("12", "34", "5")
    }

    @Test
    fun testIsNumeric() {
        assertThat("1234".isNumeric()).isTrue()
        assertThat("123a".isNumeric()).isFalse()
    }

    @Test
    fun testFill() {
        assertThat("1".fill(4)).isEqualTo("1000")
        assertThat("12".fill(4)).isEqualTo("1200")
    }

    @Test
    fun testStrZero() {
        assertThat("1".strZero(4)).isEqualTo("0001")
        assertThat("12".strZero(4)).isEqualTo("0012")
    }
}