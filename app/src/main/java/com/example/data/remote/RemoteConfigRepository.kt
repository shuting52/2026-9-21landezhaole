package com.example.data.remote

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 云端配置仓库：拉取「懒得找了·云端控制台」发布的 admin-data.json。
 * 拉取失败时返回 null，由调用方回退本地默认数据，保证离线可用。
 */
class RemoteConfigRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AdminData::class.java)

    // 云端配置中枢地址（由控制台程序 publish 发布）
    private val configUrl = "https://raw.githubusercontent.com/shuting52/16-47-2026-09-20/main/admin-data.json"

    suspend fun fetchAdminData(): AdminData? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(configUrl).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                adapter.fromJson(body)
            }
        } catch (e: Exception) {
            null
        }
    }
}
