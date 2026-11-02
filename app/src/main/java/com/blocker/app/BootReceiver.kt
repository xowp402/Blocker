package com.blocker.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // سرویس دسترس‌پذیری را خود اندروید بعد از ریستارت روشن می‌کند.
        // زمان قفل هم به‌صورت تاریخ پایان ذخیره شده و از همان‌جا ادامه می‌یابد.
        Store.now(context)
    }
}
