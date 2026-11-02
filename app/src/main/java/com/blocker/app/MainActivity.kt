package com.blocker.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : BaseActivity() {

    private var createdLang = ""
    private var daysInput: EditText? = null
    private var daysText = "30"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createdLang = Store.lang(this)
    }

    override fun onResume() {
        super.onResume()
        if (Store.lang(this) != createdLang) {
            recreate()
            return
        }
        build()
    }

    private fun statCard(value: String, label: String): LinearLayout {
        val c = Ui.card(this)
        val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        p.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0)
        c.layoutParams = p
        c.addView(Ui.text(this, value, 26f, true, color = Ui.col(this, R.color.primary)))
        c.addView(Ui.text(this, label, 13f, secondary = true))
        return c
    }

    private fun isAccessibilityOn(): Boolean {
        val s = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return s != null && s.contains(packageName)
    }

    private fun isAdminOn(): Boolean {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(ComponentName(this, AdminReceiver::class.java))
    }

    private fun statusLine(label: String, on: Boolean): android.widget.TextView {
        val state = getString(if (on) R.string.on else R.string.off)
        val color = Ui.col(this, if (on) R.color.accent else R.color.danger)
        return Ui.text(this, "$label: $state", 16f, true, color = color)
    }

    private fun build() {
        daysInput?.let { daysText = it.text.toString() }

        val root = Ui.page(this, getString(R.string.app_name))
        val locked = Store.isLocked(this)

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = Ui.lp(this, 8)
        val daysValue = if (locked) Store.daysLeft(this).toString() else getString(R.string.home_no_lock)
        row.addView(statCard(daysValue, getString(R.string.home_days_left)))
        row.addView(statCard(Store.total(this).toString(), getString(R.string.home_detections)))
        root.addView(row)

        val status = Ui.card(this)
        status.addView(statusLine(getString(R.string.status_accessibility), isAccessibilityOn()))
        status.addView(statusLine(getString(R.string.status_admin), isAdminOn()))
        root.addView(status)

        root.addView(Ui.button(this, getString(R.string.btn_enable_accessibility)) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        root.addView(Ui.button(this, getString(R.string.btn_enable_admin)) {
            val i = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            i.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(this, AdminReceiver::class.java)
            )
            startActivity(i)
        })

        root.addView(Ui.button(this, getString(R.string.btn_apps), false) {
            startActivity(Intent(this, AppsActivity::class.java))
        })
        root.addView(Ui.button(this, getString(R.string.btn_words), false) {
            startActivity(Intent(this, WordsActivity::class.java))
        })
        root.addView(Ui.button(this, getString(R.string.btn_stats), false) {
            startActivity(Intent(this, StatsActivity::class.java))
        })
        root.addView(Ui.button(this, getString(R.string.btn_settings), false) {
            startActivity(Intent(this, SettingsActivity::class.java))
        })

        val lockCard = Ui.card(this)
        lockCard.addView(Ui.text(this, getString(R.string.lock_days_hint), 14f, secondary = true))
        val input = Ui.numberInput(this)
        input.setText(daysText)
        daysInput = input
        lockCard.addView(input)
        lockCard.addView(Ui.button(this, getString(R.string.btn_start_lock)) { confirmLock() })
        root.addView(lockCard)
    }

    private fun confirmLock() {
        if (Store.isLocked(this)) {
            Toast.makeText(this, R.string.lock_already, Toast.LENGTH_SHORT).show()
            return
        }
        val days = (daysInput?.text.toString().toIntOrNull() ?: 30).coerceIn(1, 3650)
        AlertDialog.Builder(this)
            .setTitle(R.string.lock_confirm_title)
            .setMessage(getString(R.string.lock_confirm_msg, days))
            .setPositiveButton(R.string.lock_yes) { _, _ ->
                Store.startLock(this, days)
                build()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
