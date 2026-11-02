package com.blocker.app

import android.os.Bundle
import android.widget.Toast

class WordsActivity : BaseActivity() {

    private fun lines(s: String): List<String> =
        s.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.page(this, getString(R.string.words_title))

        root.addView(Ui.text(this, getString(R.string.words_hint), 14f, secondary = true))
        if (Store.isLocked(this)) {
            root.addView(
                Ui.text(this, getString(R.string.words_locked_note), 14f, true,
                    color = Ui.col(this, R.color.danger))
            )
        }

        val input = Ui.input(this, 12)
        input.setText(Store.words(this).joinToString("\n"))
        root.addView(input)

        root.addView(Ui.button(this, getString(R.string.words_load_defaults), false) {
            val merged = (lines(input.text.toString()) + DefaultWords.list).distinct()
            input.setText(merged.joinToString("\n"))
        })

        root.addView(Ui.button(this, getString(R.string.save)) {
            Store.saveWords(this, lines(input.text.toString()))
            input.setText(Store.words(this).joinToString("\n"))
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        })
    }
}
