package com.blocker.app

import android.os.Bundle

class WarningActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.page(this, getString(R.string.warning_title))

        val custom = Store.warningText(this)
        val msg = if (custom.isNotEmpty()) custom else getString(R.string.warning_default_text)

        val card = Ui.card(this)
        card.addView(Ui.text(this, msg, 20f))
        root.addView(card)

        root.addView(Ui.button(this, getString(R.string.warning_ok)) {
            finishAndRemoveTask()
        })
    }
}
