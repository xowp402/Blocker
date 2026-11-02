package com.blocker.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

class BlockerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var scheduled = false
    private var lastAction = 0L
    private var rulesKey = "\u0000"
    private var rules: List<Matcher.Rule> = emptyList()
    private var ignored: Set<String> = emptySet()

    private val guard: List<String> = listOf(
        "blocker", "accessibility", "دسترس‌پذیری",
        "developer options", "گزینه‌های برنامه‌نویسان", "گزینه‌های توسعه‌دهنده",
        "wireless debugging", "اشکال‌زدایی بی‌سیم", "usb debugging", "اشکال‌زدایی usb"
    ).map { Matcher.norm(it) }

    private val check = Runnable {
        scheduled = false
        runCheck()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        refreshIgnored()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || scheduled) return
        val p = event.packageName?.toString()
        if (p != null && isIgnored(p)) return
        scheduled = true
        handler.postDelayed(check, 300)
    }

    private fun refreshIgnored() {
        val set = HashSet<String>()
        set.add(packageName)
        set.add("com.android.systemui")
        set.add("android")
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            for (m in imm.enabledInputMethodList) set.add(m.packageName)
        } catch (e: Exception) {
        }
        ignored = set
    }

    private fun isIgnored(pkg: String): Boolean =
        ignored.contains(pkg) || pkg.contains("launcher") || pkg == "com.miui.home"

    private fun isSettingsLike(pkg: String): Boolean =
        pkg.contains("settings") || pkg.contains("packageinstaller") ||
            pkg.contains("permissioncontroller") || pkg.contains("securitycenter") ||
            pkg.contains("appmanager")

    private fun runCheck() {
        val root = rootInActiveWindow ?: return
        val pkg = root.packageName?.toString() ?: return
        if (isIgnored(pkg)) return

        val nowMs = System.currentTimeMillis()
        if (nowMs - lastAction < 1200) return

        val locked = Store.isLocked(this)
        val settingsLike = isSettingsLike(pkg)
        val mode = Store.mode(this)
        val watched = mode == 0 || Store.selectedApps(this).contains(pkg)

        if (!watched && !(locked && settingsLike)) return

        val sb = StringBuilder()
        collect(root, sb, IntArray(1), 0)
        val screen = Matcher.norm(sb.toString())
        if (screen.isEmpty()) return

        // محافظت در دوره‌ی قفل
        if (locked && settingsLike && guard.any { screen.contains(it) }) {
            trigger(null, null)
            return
        }

        if (!watched) return

        val list = Store.words(this)
        val key = list.joinToString("\n")
        if (key != rulesKey) {
            rules = Matcher.compile(list)
            rulesKey = key
        }

        val hit = Matcher.find(rules, screen)
        if (hit != null) trigger(pkg, hit)
    }

    private fun trigger(pkg: String?, term: String?) {
        lastAction = System.currentTimeMillis()
        if (pkg != null && term != null) Store.addEvent(this, pkg, term)
        performGlobalAction(GLOBAL_ACTION_HOME)

        val ctx = Lang.wrap(this)
        val custom = Store.warningText(this)
        val msg = if (custom.isNotEmpty()) custom else ctx.getString(R.string.warning_default_text)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

        handler.postDelayed({
            try {
                val i = Intent(this, WarningActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } catch (e: Exception) {
            }
        }, 350)
    }

    private fun collect(node: AccessibilityNodeInfo?, sb: StringBuilder, count: IntArray, depth: Int) {
        if (node == null || depth > 45 || count[0] > 4000 || sb.length > 80000) return
        if (!node.isVisibleToUser) return
        count[0]++
        if (!node.isPassword) {
            node.text?.let { sb.append(it).append('\n') }
            node.contentDescription?.let { sb.append(it).append('\n') }
        }
        for (i in 0 until node.childCount) {
            collect(node.getChild(i), sb, count, depth + 1)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        handler.removeCallbacksAndMessages(null)
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {}
}
