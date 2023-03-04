package top.mrxiaom.syncme

import org.bukkit.ChatColor
import java.util.*

fun parseGradient(s: String, extraFormat: String, startHex: String, endHex: String): String {
    val color1 = hex(startHex)
    val color2 = hex(endHex)
    val colors = createGradient(color1, color2, s.length)
    val result = StringBuilder()
    for (i in colors.indices) {
        result.append(hexToMc(colors[i])).append(extraFormat).append(s[i])
    }
    return result.toString()
}

fun createGradient(startHex: Int, endHex: Int, step: Int): IntArray {
    if (step == 1) return intArrayOf(startHex)
    val colors = IntArray(step)
    val start = hexToRGB(startHex)
    val end = hexToRGB(endHex)
    val stepR = (end[0] - start[0]) / (step - 1)
    val stepG = (end[1] - start[1]) / (step - 1)
    val stepB = (end[2] - start[2]) / (step - 1)
    for (i in 0 until step) {
        colors[i] = rgbToHex(
            start[0] + stepR * i,
            start[1] + stepG * i,
            start[2] + stepB * i
        )
    }
    return colors
}

fun hexToMc(hex: Int): String {
    val result = StringBuilder("§x")
    for (c in hex(hex).substring(1).lowercase(Locale.getDefault()).toCharArray()) {
        result.append('§').append(c)
    }
    return result.toString()
}

fun hex(hex: String): Int {
    return hex.substring(1).toInt(16)
}

fun hex(hex: Int): String {
    return "#" + String.format("%06x", hex)
}

fun hexToRGB(hex: Int): IntArray {
    return intArrayOf(
        hex shr 16 and 0xff,
        hex shr 8 and 0xff,
        hex and 0xff
    )
}

fun rgbToHex(r: Int, g: Int, b: Int): Int {
    return (r shl 16) + (g shl 8) + b
}

val regexColor = Regex("&#([0123456789ABCDEFabcdef]{6})")
val regexColorGradient = Regex("&\\{#([0123456789ABCDEFabcdef]{6}):#([0123456789ABCDEFabcdef]{6}):(.*)}")
val regexStartWithFormat = Regex("^(\u00A7[LMNKOlmnko])+")
fun String.color(): String {
    var str = ChatColor.translateAlternateColorCodes('&', this)
    // hex color
    regexColor.split(str) { s, isMatched ->
        if (!isMatched) s
        else "\u00A7x" + s.substring(2).map { "\u00A7" + it.lowercase() }.joinToString("")
    }?.joinToString("")?.also { str = it }
    // gradient color
    regexColorGradient.split(str) { s, isMatched ->
        if (!isMatched) s
        else s.removeSurrounding("&{", "}").split(Regex(":"), 3).run {
            val extra = regexStartWithFormat.matchEntire(this[2])?.groupValues?.get(1) ?: ""
            parseGradient(this[2].removePrefix(extra), extra, this[0], this[1])
        }
    }?.joinToString("")?.also { str = it }
    return str
}