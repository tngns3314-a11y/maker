package kr.co.coinnumber

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Entry(val id: Long, val multiple: Boolean, val input: Input)
data class Food(val id: String, val name: String, val unit: String, val priceWon: Long)
// 2025년 기준: 앱 소유자가 지정한 고정 비교 단가.
val foods = listOf(
    Food("chicken", "치킨", "마리", 25000),
    Food("jokbal", "족발", "접시", 40000),
    Food("naengmyeon", "냉면", "그릇", 17000),
    Food("sundaeguk", "순대국밥", "그릇", 11000),
    Food("burger", "햄버거", "개", 8000)
)
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("calculator", Context.MODE_PRIVATE)
    fun history(): List<Entry> = runCatching {
        val array = JSONArray(prefs.getString("history", "[]"))
        (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            Entry(o.getLong("id"), o.getBoolean("multiple"), Input(o.getDouble("percent"), o.getLong("price"), o.getLong("bundle"), if(o.isNull("ceiling")) null else o.getLong("ceiling")))
        }
    }.getOrDefault(emptyList())
    fun save(entries: List<Entry>) {
        val array = JSONArray()
        entries.take(50).forEach { e -> array.put(JSONObject().put("id", e.id).put("multiple", e.multiple).put("percent", e.input.percent).put("price", e.input.price).put("bundle", e.input.bundle).put("ceiling", e.input.ceiling ?: JSONObject.NULL)) }
        prefs.edit().putString("history", array.toString()).apply()
    }
}
