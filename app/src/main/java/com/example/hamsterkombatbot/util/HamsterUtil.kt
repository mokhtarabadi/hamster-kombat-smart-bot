package com.example.hamsterkombatbot.util

import android.util.Base64

object HamsterUtil {
    fun cipherDecode(e: String): String {
        val t = e.substring(0, 3) + e.substring(4)
        return String(Base64.decode(t, Base64.DEFAULT))
    }
}
