package com.blocker.app

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.page(this, getString(R.string.settings_title))
        val locked = Store.isLocked(this)

        // متن هشدار
        root.addView(Ui.text(this, getString(R.string.warning_text_label), 16f, true))
        val warn = Ui.input(this, 3)
        warn.hint = getString(R.string.warning_default_text)
        warn.setText(Store.warningText(this))
        if (locked) warn.isEnabled = false
        root.addView(warn)
        if (locked) {
            root.addView(
                Ui.text(this, getString(R.string.lock_blocked), 14f, color = Ui.col(this, R.color.danger))
            )
        }

        // زبان
        root.addView(Ui.text(this, getString(R.string.language_label), 16f, true))
        val langGroup = RadioGroup(this)
        val rbFa = Ui.radio(this, getString(R.string.lang_fa))
        val rbEn = Ui.radio(this, getString(R.string.lang_en))
        langGroup.addView(rbFa)
        langGroup.addView(rbEn)
        langGroup.layoutParams = Ui.lp(this, 4)
        if (Store.lang(this) == "en") rbEn.isChecked = true else rbFa.isChecked = true
        root.addView(langGroup)

        // تم
        root.addView(Ui.text(this, getString(R.string.theme_label), 16f, true))
        val themeGroup = RadioGroup(this)
        val rbSys = Ui.radio(this, getString(R.string.theme_system))
        val rbLight = Ui.radio(this, getString(R.string.theme_light))
        val rbDark = Ui.radio(this, getString(R.string.theme_dark))
        themeGroup.addView(rbSys)
        themeGroup.addView(rbLight)
        themeGroup.addView(rbDark)
        themeGroup.layoutParams = Ui.lp(this, 4)
        when (Store.theme(this)) {
            1 -> rbLight.isChecked = true
            2 -> rbDark.isChecked = true
            else -> rbSys.isChecked = true
        }
        root.addView(themeGroup)

        root.addView(Ui.button(this, getString(R.string.save)) {
            if (!locked) Store.setWarningText(this, warn.text.toString())
            Store.setLang(this, if (rbEn.isChecked) "en" else "fa")
            val theme = if (rbLight.isChecked) 1 else if (rbDark.isChecked) 2 else 0
            Store.setTheme(this, theme)
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            AppCompatDelegate.setDefaultNightMode(Ui.nightMode(theme))
            recreate()
        })
    }
}
