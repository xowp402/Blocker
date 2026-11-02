package com.blocker.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Toast

class AppsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.page(this, getString(R.string.apps_title))
        val locked = Store.isLocked(this)
        val mode = Store.mode(this)

        val group = RadioGroup(this)
        val rbAll = Ui.radio(this, getString(R.string.apps_mode_all))
        val rbSel = Ui.radio(this, getString(R.string.apps_mode_selected))
        group.addView(rbAll)
        group.addView(rbSel)
        group.layoutParams = Ui.lp(this, 12)
        if (mode == 1) rbSel.isChecked = true else rbAll.isChecked = true
        if (locked && mode == 0) rbSel.isEnabled = false
        root.addView(group)

        root.addView(Ui.text(this, getString(R.string.apps_hint), 14f, secondary = true))

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .map { Pair(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.first != packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val selected = Store.selectedApps(this)
        val boxes = ArrayList<CheckBox>()
        val card = Ui.card(this)

        for (app in apps) {
            val box = Ui.check(this, app.second)
            box.tag = app.first
            val isSel = selected.contains(app.first)
            box.isChecked = isSel
            if (locked && isSel) box.isEnabled = false
            boxes.add(box)
            card.addView(box)
        }

        root.addView(card)

        root.addView(Ui.button(this, getString(R.string.save)) {
            val picked = HashSet<String>()
            for (b in boxes) if (b.isChecked) picked.add(b.tag as String)
            Store.setMode(this, if (rbSel.isChecked) 1 else 0)
            Store.saveApps(this, picked)
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            recreate()
        })
    }
}
