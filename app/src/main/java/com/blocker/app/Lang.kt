package com.blocker.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object Lang {
    fun wrap(base: Context): Context {
        val loc = Locale(Store.lang(base))
        Locale.setDefault(loc)
        val cfg = Configuration(base.resources.configuration)
        cfg.setLocale(loc)
        cfg.setLayoutDirection(loc)
        return base.createConfigurationContext(cfg)
    }
}
