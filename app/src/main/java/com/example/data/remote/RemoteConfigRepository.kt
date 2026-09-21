package com.example.data.remote

import android.content.Context
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 云端配置仓库：拉取「云台-懒得找了」发布的 admin-data.json。
 * 优先通过 GitHub API 读取（绕过 raw CDN 缓存，实现控制台「应用」后本体零延迟感知），
 * 失败时回退 raw 直链读取；全部失败返回 null，由调用方回退本地默认数据。
 */
class RemoteConfigRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AdminData::class.java)

    private val owner = "shuting52"
    private val repo = "2026-9-21landezhaole"
    private val branch = "main"
    private val filePath = "admin-data.json"

    // GitHub API 直读（无 CDN 缓存，最快）
    private val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$filePath?ref=$branch"
    // raw 直链兜底（CDN 有缓存但更稳）
    private val rawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$filePath"

    suspend fun fetchAdminData(): AdminData? = withContext(Dispatchers.IO) {
        fetchFromApi() ?: fetchFromRaw()
    }

    /** 通过 GitHub API 读取（contents API 返回 base64 内容，需解码） */
    private fun fetchFromApi(): AdminData? {
        return try {
            val request = Request.Builder().url(apiUrl).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val json = resp.body?.string() ?: return null
                // contents API 响应含 "content" 字段（base64）
                val contentField = org.json.JSONObject(json).optString("content", "")
                if (contentField.isBlank()) return null
                val decoded = String(
                    Base64.decode(contentField.replace("\n", ""), Base64.DEFAULT),
                    Charsets.UTF_8
                )
                adapter.fromJson(decoded)
            }
        } catch (e: Exception) {
            null
        }
    }

    /** 通过 raw 直链读取（CDN 有缓存延迟，作兜底） */
    private fun fetchFromRaw(): AdminData? {
        return try {
            val request = Request.Builder().url(rawUrl).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                adapter.fromJson(body)
            }
        } catch (e: Exception) {
            null
        }
    }
}
