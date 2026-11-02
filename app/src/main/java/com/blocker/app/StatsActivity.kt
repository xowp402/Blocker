package com.blocker.app

import android.os.Bundle
import android.widget.LinearLayout

class StatsActivity : BaseActivity() {

    private fun appLabel(pkg: String): String {
        return try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            pkg
        }
    }

    private fun section(root: LinearLayout, title: String, list: List<Store.Event>) {
        val card = Ui.card(this)
        card.addView(Ui.text(this, title, 14f, secondary = true))
        card.addView(
            Ui.text(this, list.size.toString(), 30f, true, color = Ui.col(this, R.color.primary))
        )
        if (list.isEmpty()) {
            card.addView(Ui.text(this, getString(R.string.stats_empty), 14f, secondary = true))
        } else {
            card.addView(Ui.text(this, getString(R.string.stats_top), 15f, true))
            val terms = list.groupingBy { it.term }.eachCount().entries
                .sortedByDescending { it.value }.take(5)
            for (t in terms) card.addView(Ui.text(this, "${t.key}  ×${t.value}", 15f))

            card.addView(Ui.text(this, getString(R.string.stats_apps), 15f, true))
            val apps = list.groupingBy { it.pkg }.eachCount().entries
                .sortedByDescending { it.value }.take(5)
            for (a in apps) card.addView(Ui.text(this, "${appLabel(a.key)}  ×${a.value}", 15f))
        }
        root.addView(card)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.page(this, getString(R.string.stats_title))

        val events = Store.events(this)
        val now = System.currentTimeMillis()
        val day = 86400000L

        val totalCard = Ui.card(this)
        totalCard.addView(Ui.text(this, getString(R.string.stats_total), 14f, secondary = true))
        totalCard.addView(
            Ui.text(this, Store.total(this).toString(), 30f, true, color = Ui.col(this, R.color.primary))
        )
        root.addView(totalCard)

        section(root, getString(R.string.stats_week), events.filter { it.time >= now - 7 * day })
        section(root, getString(R.string.stats_month), events.filter { it.time >= now - 30 * day })
    }
}
