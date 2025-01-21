package com.example.emorecord.utils

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings

enum class MoodType {
    SAD,
    HAPPY
}

object NfcUtils {
    fun isNfcEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    fun showNfcSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        context.startActivity(intent)
    }
}