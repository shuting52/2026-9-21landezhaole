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
 *
 * v1.8.7 修复「更新弹窗卡在安装中」：系统返回 STATUS_PENDING_USER_ACTION（安装确认页）时，
 * 直接拉起系统安装确认（即「直接呈现新版本安装」），完成后通过 results 驱动弹窗完成动画。
 */
class UpdateInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
        // 系统需要用户确认安装（直接呈现新版本安装，不是「允许未知应用」设置页）
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
            if (confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(confirm)
                } catch (e: Exception) {
                    Results.emit(success = false, message = "无法打开系统安装确认，请重新点击更新重试")
                }
            } else {
                Results.emit(success = false, message = "安装确认页不可用，请重新点击更新重试")
            }
            return
        }
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
