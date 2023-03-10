package top.mrxiaom.syncme

import org.bukkit.util.NumberConversions

enum class Config(
    val def: Any,
    val parent: String = ""
) {
    mysql__table_prefix(""),
    server("main"),
    always_show_help_message(true);
    val key = name.lowercase()
        .replace("__", ".")
        .replace("_", "-")
    inline fun <reified T : Any> get(noinline default: () -> T = {def as T}): T {
        val obj = SyncMe.config.get(key) ?: return default()
        return when (T::class) {
            String::class -> obj.toString()
            Int::class -> toInt(obj, default)
            Float::class -> toFloat(obj, default)
            Double::class -> toDouble(obj, default)
            Long::class -> toLong(obj, default)
            Byte::class -> toByte(obj, default)
            else -> obj
        } as T
    }

}
fun toInt(any: Any, default: () -> Any): Int =
    if (any is Number) NumberConversions.toInt(any)
    else default() as Int
fun toFloat(any: Any, default: () -> Any): Float =
    if (any is Number) NumberConversions.toFloat(any)
    else default() as Float
fun toDouble(any: Any, default: () -> Any): Double =
    if (any is Number) NumberConversions.toDouble(any)
    else default() as Double
fun toLong(any: Any, default: () -> Any): Long =
    if (any is Number) NumberConversions.toLong(any)
    else default() as Long
fun toByte(any: Any, default: () -> Any): Byte =
    if (any is Number) NumberConversions.toByte(any)
    else default() as Byte
