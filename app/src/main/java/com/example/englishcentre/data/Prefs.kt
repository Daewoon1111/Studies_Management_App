package com.example.englishcentre.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** App settings saved on the device; reading them in Compose recomposes on change. */
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var lang by pref("lang")   // 0 = Tiếng Việt, 1 = English, 2 = 日本語
    var theme by pref("theme") // 0 = system, 1 = light, 2 = dark
    var user by pref("user")   // logged-in student id, 0 = logged out

    private fun pref(key: String) = object : ReadWriteProperty<Any?, Long> {
        var v by mutableLongStateOf(sp.getLong(key, 0))
        override fun getValue(thisRef: Any?, property: KProperty<*>) = v
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            v = value
            sp.edit { putLong(key, value) }
        }
    }
}
