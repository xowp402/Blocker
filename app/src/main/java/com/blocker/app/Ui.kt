package com.blocker.app

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

object Ui {

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun col(c: Context, id: Int): Int = ContextCompat.getColor(c, id)

    fun nightMode(t: Int): Int {
        return when (t) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun lp(c: Context, top: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        p.topMargin = dp(c, top)
        return p
    }

    fun page(a: Activity, title: String): LinearLayout {
        val root = LinearLayout(a)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(a, 20), dp(a, 24), dp(a, 20), dp(a, 28))
        val sv = ScrollView(a)
        sv.setBackgroundColor(col(a, R.color.bg))
        sv.isFillViewport = true
        sv.addView(root)
        a.setContentView(sv)
        root.addView(text(a, title, 28f, true))
        return root
    }

    fun text(
        c: Context,
        s: String,
        size: Float = 16f,
        bold: Boolean = false,
        secondary: Boolean = false,
        color: Int? = null
    ): TextView {
        val t = TextView(c)
        t.text = s
        t.textSize = size
        t.setTextColor(color ?: col(c, if (secondary) R.color.text_secondary else R.color.text))
        if (bold) t.setTypeface(t.typeface, Typeface.BOLD)
        t.layoutParams = lp(c, 8)
        return t
    }

    fun card(c: Context): LinearLayout {
        val l = LinearLayout(c)
        l.orientation = LinearLayout.VERTICAL
        l.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14))
        val bg = GradientDrawable()
        bg.setColor(col(c, R.color.surface))
        bg.cornerRadius = dp(c, 16).toFloat()
        l.background = bg
        l.layoutParams = lp(c, 12)
        return l
    }

    fun button(c: Context, label: String, filled: Boolean = true, onClick: () -> Unit): MaterialButton {
        val b = MaterialButton(c)
        b.text = label
        b.cornerRadius = dp(c, 12)
        if (filled) {
            b.backgroundTintList = ColorStateList.valueOf(col(c, R.color.primary))
            b.setTextColor(col(c, R.color.on_primary))
        } else {
            b.backgroundTintList = ColorStateList.valueOf(col(c, R.color.surface))
            b.setTextColor(col(c, R.color.text))
        }
        b.layoutParams = lp(c, 10)
        b.setOnClickListener { onClick() }
        return b
    }

    fun input(c: Context, lines: Int): EditText {
        val e = EditText(c)
        e.minLines = lines
        e.gravity = Gravity.TOP or Gravity.START
        e.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        styleInput(c, e)
        return e
    }

    fun numberInput(c: Context): EditText {
        val e = EditText(c)
        e.inputType = InputType.TYPE_CLASS_NUMBER
        styleInput(c, e)
        return e
    }

    private fun styleInput(c: Context, e: EditText) {
        e.setTextColor(col(c, R.color.text))
        e.setHintTextColor(col(c, R.color.text_secondary))
        e.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12))
        val bg = GradientDrawable()
        bg.setColor(col(c, R.color.surface))
        bg.cornerRadius = dp(c, 12).toFloat()
        e.background = bg
        e.layoutParams = lp(c, 10)
    }

    fun radio(c: Context, label: String): RadioButton {
        val r = RadioButton(c)
        r.id = View.generateViewId()
        r.text = label
        r.textSize = 16f
        r.setTextColor(col(c, R.color.text))
        return r
    }

    fun check(c: Context, label: String): CheckBox {
        val k = CheckBox(c)
        k.text = label
        k.textSize = 16f
        k.setTextColor(col(c, R.color.text))
        return k
    }
}
