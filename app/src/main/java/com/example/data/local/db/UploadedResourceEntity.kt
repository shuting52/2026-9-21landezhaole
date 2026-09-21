package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaded_resources")
data class UploadedResourceEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "software" or "skill"
    val title: String,
    val desc: String,
    val url: String,
    val author: String,
    val badge: String = "用户上传",
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
