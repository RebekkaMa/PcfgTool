package Util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


fun <T, A : MutableCollection<T>> Iterable<T>.getWindow(buffer : A, startIndex : Int = 0, limit: Int = -1, transform: ((T) -> A)? = null): A {
    var count = 0
    for (element in this) {
        if (startIndex + 1 > ++count) continue
        if (limit < 0 || count <= limit + startIndex) {
            buffer.add(element)
        } else break
    }
    return buffer
}


fun <T> Iterable<T>.joinToStringWithStartAndEnd(separator: CharSequence = ", ", prefix: String = "", postfix: String = "", startIndex : Int = 0, limit: Int = -1, transform: ((T) -> String)? = null): String {
    val buffer = StringBuffer()
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (startIndex + 1 > ++count) continue
        if (limit < 0 || count <= limit + startIndex) {
            if (count > startIndex + 1) buffer.append(separator)
            buffer.append(transform?.invoke(element) ?: element.toString())
        } else break
    }
    buffer.append(postfix)
    return buffer.toString()
}

fun Double.format(fracDigits: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale.UK)
    val df = nf as DecimalFormat
    df.maximumFractionDigits = fracDigits
    return df.format(this)
}



//fun <T> Iterable<T>.joinToString(separator: CharSequence = ", ", prefix: String = "", postfix: String = "", startIndex : Int = 0, limit: Int = -1, transform: ((T) -> String)? = null): String {
//    val buffer = StringBuffer()
//    buffer.append(prefix)
//    var count = 0
//    for (element in this) {
//        if (startIndex + 1 > ++count) continue
//        if (limit < 0 || count <= limit) {
//            if (count > startIndex + 1) buffer.append(separator)
//            buffer.append(transform?.invoke(element) ?: element.toString())
//        } else break
//    }
//    buffer.append(postfix)
//    return buffer.toString()
//}

//fun Iterable<String>.joinToString(
//    buffer: StringBuilder = StringBuilder(),
//    separator: String = ", ",
//    prefix: String = "",
//    postfix: String = "",
//    startpoint: Int = 0,
//    limit: Int = -1
//): String {
//    buffer.append(prefix)
//    var count = 0
//    for (element in this) {
//        if (startpoint + 1 > ++count) continue
//        if (limit < 0 || count <= limit) {
//            if (count > startpoint + 1) buffer.append(separator)
//            buffer.append(element)
//        } else break
//    }
//    buffer.append(postfix)
//    return buffer.toString()
//}
