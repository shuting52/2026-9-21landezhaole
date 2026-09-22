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
                // 防调试检测：被调试器附加（MT 管理器等工具常伴调试）时阻止运行
                if (android.os.Debug.isDebuggerConnected()) {
                    killWithWarning(context, "检测到调试环境，已阻止运行。")
                    return@thread
                }
                // 防模拟器/修改工具环境特征检测
                if (detectMtLikeTools()) {
                    killWithWarning(context, "检测到第三方修改工具环境，已阻止运行。")
                    return@thread
                }
                val repo = RemoteConfigRepository(context)
                val data = repo.fetchAdminDataBlocking()
                val sec = data?.settings?.security ?: return@thread
                if (!sec.enabled) return@thread
                val expected = sec.expectedSha.lowercase()
                if (expected.isBlank()) return@thread
                val actual = currentSigningSha(context)
                if (actual == null || !expected.contains(actual)) {
                    killWithWarning(context, "⚠️ 检测到软件被二次修改或签名异常，为保障安全已阻止运行。请卸载后从官方渠道重新安装。")
                }
            } catch (e: Exception) {
                // 校验失败不阻断正常运行
            }
        }
    }

    /** 检测常见修改/管理工具特征（MT 管理器等会在 /proc 或文件系统留下痕迹） */
    private fun detectMtLikeTools(): Boolean {
        val suspiciousPaths = listOf(
            "/data/local/tmp/mt",
            "/data/local/tmp/mtmanager",
            "/sdcard/MT2",
            "/sdcard/mt",
            "/data/data/bin.mt.plus",
            "/data/data/bin.mt.plus/databases"
        )
        for (p in suspiciousPaths) {
            try {
                val f = java.io.File(p)
                if (f.exists() || f.canRead()) {
                    // 需同时存在 MT 特征目录才算命中，避免误伤
                    if (f.exists() && f.list() != null) return true
                }
            } catch (e: Exception) {
            }
        }
        return false
    }

    private fun killWithWarning(context: Context, msg: String) {
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
        Thread.sleep(2200)
        android.os.Process.killProcess(android.os.Process.myPid())
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
