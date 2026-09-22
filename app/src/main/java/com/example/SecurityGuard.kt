package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.example.data.remote.RemoteConfigRepository
import java.security.MessageDigest
import kotlin.concurrent.thread

/**
 * 安全加固：签名自校验（防止二次打包篡改）
 * - 从云端读取加固配置（settings.security）：enabled + expectedSha
 * - 开启后运行时读取 APK 签名 SHA-256 指纹，与云端内置指纹比对
 * - 不一致 → 提示风险并退出（防破解二改）
 */
object SecurityGuard {

    /** 当前正式签名指纹（由构建时签名证书决定；此处从 APK 运行时读取自身签名） */
    fun verifyInBackground(context: Context) {
        thread {
            try {
                val repo = RemoteConfigRepository(context)
                val data = repo.fetchAdminDataBlocking()
                val sec = data?.settings?.security ?: return@thread
                if (!sec.enabled) return@thread
                val expected = sec.expectedSha.lowercase()
                if (expected.isBlank()) return@thread
                val actual = currentSigningSha(context)
                if (actual == null || !expected.contains(actual)) {
                    // 签名不匹配：提示风险并退出
                    android.os.Handler(context.mainLooper).post {
                        Toast.makeText(
                            context,
                            "⚠️ 检测到软件被二次修改或签名异常，为保障安全已阻止运行。请卸载后从官方渠道重新安装。",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Thread.sleep(2500)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            } catch (e: Exception) {
                // 校验失败不阻断正常运行
            }
        }
    }

    /** 读取当前 APK 签名证书 SHA-256（十六进制小写） */
    private fun currentSigningSha(context: Context): String? {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val certs = info.signingInfo?.apkContentsSigners ?: return null
            certs.firstOrNull()?.toByteArray()?.let { bytes ->
                val md = MessageDigest.getInstance("SHA-256")
                md.digest(bytes).joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
}
