package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.example.data.remote.RemoteConfigRepository
import java.security.MessageDigest
import kotlin.concurrent.thread

/**
 * 安全加固：签名自校验（温和提醒模式，v1.6.7 起不再强制退出）
 *
 * 【1.6.7 修复】加密/加固后的 APK 闪退问题：
 * - 旧逻辑：签名不匹配 / 检测到调试器 / 检测到修改工具 → 直接 killProcess 阻止运行。
 *   但部分加密壳、加固方案、以及 debug 签名包都会触发误判，导致「加密后闪退」。
 * - 新逻辑：所有校验仅作安全提醒（Toast 提示一次），绝不终止进程，
 *   保障正常用户与合法加固包都能稳定运行，同时保留对二次打包的警示作用。
 *
 * 校验项（仅提醒）：
 * 1. 运行时签名 SHA-256 与云端 settings.security.expectedSha 比对
 * 2. 调试器附加检测（防逆向调试）
 * 3. MT 管理器等修改工具环境特征检测
 */
object SecurityGuard {

    /** 主入口：异步执行温和版安全校验 */
    fun verifyInBackground(context: Context) {
        thread {
            try {
                // 防调试检测：命中仅提醒（加固壳/逆向调试场景不再闪退）
                if (android.os.Debug.isDebuggerConnected()) {
                    showWarning(context, "当前运行环境检测到调试器附加（加固/调试场景属正常现象）。")
                }
                // 修改工具环境特征检测：命中仅提醒
                if (detectMtLikeTools()) {
                    showWarning(context, "检测到第三方修改工具环境特征，请注意软件来源安全。")
                }
                // 签名自校验：不一致仅提醒，不再阻止运行
                val repo = RemoteConfigRepository(context)
                val data = repo.fetchAdminDataBlocking()
                val sec = data?.settings?.security ?: return@thread
                if (!sec.enabled) return@thread
                val expected = sec.expectedSha.lowercase()
                if (expected.isBlank()) return@thread
                val actual = currentSigningSha(context)
                if (actual == null || !expected.contains(actual)) {
                    showWarning(
                        context,
                        "当前安装包签名与官方签名不一致（可能经过二次打包或加密加固）。" +
                                "为确保体验与安全，请优先从官方渠道获取最新版本。"
                    )
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

    /** 温和提醒：仅 Toast 提示，不杀进程、不阻断运行 */
    private fun showWarning(context: Context, msg: String) {
        try {
            android.os.Handler(context.mainLooper).post {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // 提醒失败也不影响运行
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
