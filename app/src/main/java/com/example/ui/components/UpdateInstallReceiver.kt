package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 接收 PackageInstaller 安装会话的提交结果（v1.7.5 参照 AppUpdater）。
 * 通过 [results] 流订阅安装成败，供更新弹窗在安装完成后刷新状态。
 */
class UpdateInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
        Results.emit(
            success = status == PackageInstaller.STATUS_SUCCESS,
            message = message
        )
    }

    object Results {
        private val _flow = MutableSharedFlow<Pair<Boolean, String>>(extraBufferCapacity = 1)
        val flow: SharedFlow<Pair<Boolean, String>> = _flow.asSharedFlow()

        fun emit(success: Boolean, message: String) {
            _flow.tryEmit(success to message)
        }
    }
}
