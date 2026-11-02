package com.blocker.app

object Matcher {

    // kind: 0 = کلمه (با مرز)، 1 = زیررشته (با ~ اول خط)، 2 = دامنه
    class Rule(val label: String, val text: String, val kind: Int)

    private val spaces = Regex("\\s+")

    private fun isIgnorable(ch: Char): Boolean =
        ch == '\u0640' || ch == '\u200B' || ch == '\u200D' || ch == '\u200E' ||
        ch == '\u200F' || ch == '\uFEFF' || ch == '\u00AD' ||
        (ch in '\u202A'..'\u202E') || (ch in '\u064B'..'\u065F') || ch == '\u0670'

    private fun mapChar(ch: Char): Char = when (ch) {
        '\u064A', '\u0649' -> '\u06CC'
        '\u0643' -> '\u06A9'
        '\u0629', '\u06C0' -> '\u0647'
        '\u0623', '\u0625', '\u0671' -> '\u0627'
        in '\u06F0'..'\u06F9' -> '0' + (ch - '\u06F0')
        in '\u0660'..'\u0669' -> '0' + (ch - '\u0660')
        else -> ch
    }

    fun norm(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch == '\u200C') {
                sb.append(' ')
            } else if (!isIgnorable(ch)) {
                sb.append(mapChar(ch))
            }
        }
        return spaces.replace(sb.toString().lowercase(), " ").trim()
    }

    fun compile(lines: List<String>): List<Rule> {
        val out = ArrayList<Rule>()
        for (raw in lines) {
            var t = raw.trim()
            if (t.isEmpty() || t.startsWith("#")) continue
            var substring = false
            if (t.startsWith("~")) {
                substring = true
                t = t.substring(1)
            }
            var n = norm(t).trim('.')
            if (n.isEmpty()) continue
            if (n.contains('.') && !n.contains(' ')) {
                n = n.removePrefix("http://").removePrefix("https://").removePrefix("www.").trimEnd('/')
                if (n.isNotEmpty()) out.add(Rule(raw.trim(), n, 2))
            } else {
                out.add(Rule(raw.trim(), n, if (substring) 1 else 0))
            }
        }
        return out
    }

    fun find(rules: List<Rule>, text: String): String? {
        for (r in rules) {
            val hit = when (r.kind) {
                1 -> text.contains(r.text)
                2 -> bounded(text, r.text, true)
                else -> bounded(text, r.text, false)
            }
            if (hit) return r.label
        }
        return null
    }

    private fun wordChar(c: Char, domain: Boolean): Boolean =
        c.isLetterOrDigit() || (domain && c == '-')

    private fun bounded(h: String, n: String, domain: Boolean): Boolean {
        var i = h.indexOf(n)
        while (i >= 0) {
            val before = if (i == 0) ' ' else h[i - 1]
            val e = i + n.length
            val after = if (e >= h.length) ' ' else h[e]
            if (!wordChar(before, domain) && !wordChar(after, domain)) return true
            i = h.indexOf(n, i + 1)
        }
        return false
    }
}
