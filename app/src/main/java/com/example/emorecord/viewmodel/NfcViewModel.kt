package com.example.emorecord.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 更新NfcViewModel
class NfcViewModel : ViewModel() {
    private val _nfcStatus = MutableLiveData<NfcStatus>()
    val nfcStatus: LiveData<NfcStatus> = _nfcStatus

    var isWritingHappy = false
        private set

    fun writeHappyMood() {
        isWritingHappy = true
        _nfcStatus.value = NfcStatus.Writing
    }

    fun writeSadMood() {
        isWritingHappy = false
        _nfcStatus.value = NfcStatus.Writing
    }

    fun onNfcOperationComplete() {
        _nfcStatus.value = NfcStatus.Success
    }

    fun onNfcError(message: String) {
        _nfcStatus.value = NfcStatus.Error(message)
    }
}

sealed class NfcStatus {
    object Writing : NfcStatus()
    object Success : NfcStatus()
    data class Error(val message: String) : NfcStatus()
}
