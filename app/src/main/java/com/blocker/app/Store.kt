package com.blocker.app

import android.content.Context
import android.content.SharedPreferences

object Store {

    private fun p(c: Context): SharedPreferences =
        (c.applicationContext ?: c).getSharedPreferences("blocker2", Context.MODE_PRIVATE)

    // ساعت امن: اگر تاریخ گوشی عقب برده شود، قفل باز نمی‌شود
    fun now(c: Context): Long {
        val n = System.currentTimeMillis()
        val last = p(c).getLong("last_seen", 0L)
        if (n > last) {
            if (n - last > 60000L) p(c).edit().putLong("last_seen", n).apply()
            return n
        }
        return last
    }

    // قفل
    fun lockEnd(c: Context): Long = p(c).getLong("lock_end", 0L)
    fun isLocked(c: Context): Boolean = now(c) < lockEnd(c)

    fun daysLeft(c: Context): Int {
        val left = lockEnd(c) - now(c)
        return if (left <= 0L) 0 else ((left + 86399999L) / 86400000L).toInt()
    }

    fun startLock(c: Context, days: Int) {
        if (isLocked(c)) return
        p(c).edit().putLong("lock_end", now(c) + days.toLong() * 86400000L).commit()
    }

    // کلمه‌ها
    fun words(c: Context): List<String> =
        (p(c).getString("words", "") ?: "").split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    fun saveWords(c: Context, list: List<String>) {
        val clean = list.map { it.trim() }.filter { it.isNotEmpty() }
        val result = if (isLocked(c)) (words(c) + clean).distinct() else clean.distinct()
        p(c).edit().putString("words", result.joinToString("\n")).apply()
    }

    // برنامه‌ها: 0 = همه، 1 = فقط انتخاب‌شده‌ها
    fun mode(c: Context): Int = p(c).getInt("mode", 0)

    fun setMode(c: Context, m: Int) {
        if (isLocked(c) && m == 1 && mode(c) == 0) return
        p(c).edit().putInt("mode", m).apply()
    }

    fun selectedApps(c: Context): Set<String> =
        HashSet<String>(p(c).getStringSet("apps", emptySet<String>()) ?: emptySet<String>())

    fun saveApps(c: Context, s: Set<String>) {
        val result: Set<String> = if (isLocked(c)) selectedApps(c) + s else s
        p(c).edit().putStringSet("apps", HashSet<String>(result)).apply()
    }

    // تنظیمات ظاهری و متن هشدار
    fun warningText(c: Context): String = p(c).getString("warning", "") ?: ""
    fun setWarningText(c: Context, s: String) { p(c).edit().putString("warning", s.trim()).apply() }

    fun lang(c: Context): String = p(c).getString("lang", "fa") ?: "fa"
    fun setLang(c: Context, l: String) { p(c).edit().putString("lang", l).apply() }

    // 0 = سیستم، 1 = روشن، 2 = تاریک
    fun theme(c: Context): Int = p(c).getInt("theme", 0)
    fun setTheme(c: Context, t: Int) { p(c).edit().putInt("theme", t).apply() }

    // آمار
    class Event(val time: Long, val pkg: String, val term: String)

    fun total(c: Context): Int = p(c).getInt("total", 0)

    fun addEvent(c: Context, pkg: String, term: String) {
        val clean = term.replace('\t', ' ').replace('\n', ' ')
        val old = p(c).getString("events", "") ?: ""
        val lines = if (old.isEmpty()) ArrayList<String>() else ArrayList<String>(old.split("\n"))
        lines.add("${System.currentTimeMillis()}\t$pkg\t$clean")
        while (lines.size > 3000) lines.removeAt(0)
        p(c).edit()
            .putString("events", lines.joinToString("\n"))
            .putInt("total", total(c) + 1)
            .apply()
    }

    fun events(c: Context): List<Event> {
        val old = p(c).getString("events", "") ?: ""
        if (old.isEmpty()) return emptyList()
        val out = ArrayList<Event>()
        for (line in old.split("\n")) {
            val parts = line.split("\t")
            if (parts.size >= 3) {
                val t = parts[0].toLongOrNull() ?: continue
                out.add(Event(t, parts[1], parts[2]))
            }
        }
        return out
    }
}
