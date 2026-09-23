package com.example.clonecenter.ui

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonecenter.database.CloneEntity
import com.example.clonecenter.model.InstalledApp
import com.example.clonecenter.profile.ProfileManager
import com.example.clonecenter.repository.CloneRepository
import com.example.clonecenter.scanner.AppScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloneCenterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scanner =
        AppScanner(application)

    private val profileManager =
        ProfileManager(application)

    private val repository =
        CloneRepository(application)

    private val _apps =
        MutableStateFlow<List<InstalledApp>>(emptyList())

    val apps: StateFlow<List<InstalledApp>> =
        _apps

    private val _isScanning =
        MutableStateFlow(false)

    val isScanning: StateFlow<Boolean> =
        _isScanning

    private val _message =
        MutableStateFlow<String?>(null)

    /** 一次性提示消息（UI 消费后清空） */
    val message: StateFlow<String?> =
        _message

    init {
        refreshApps()
    }

    /** 观察已创建的分身记录 */
    fun observeClones(): Flow<List<CloneEntity>> =
        repository.observeClones()

    fun refreshApps() {
        viewModelScope.launch {
            _isScanning.value = true
            val result = withContext(Dispatchers.IO) {
                scanner.scan()
            }
            _apps.value = result
            _isScanning.value = false
        }
    }

    fun supportsProfile(): Boolean {
        return profileManager.supportsManagedProfile()
    }

    fun isProfileOwner(): Boolean {
        return profileManager.isProfileOwner()
    }

    /** 发起 Work Profile 创建（系统引导） */
    fun createProfile(activity: Activity): Boolean {
        return profileManager.startProvisioning(activity)
    }

    /** 为指定应用创建分身（落库 + 尝试安装进分身空间） */
    fun cloneApp(app: InstalledApp) {
        if (!isProfileOwner()) {
            _message.value = "请先点击「创建工作分身空间」，完成系统引导后再创建应用分身"
            return
        }
        viewModelScope.launch {
            val id = repository.createRecord(app)
            val installed = withContext(Dispatchers.IO) {
                profileManager.installExistingPackage(app.packageName)
            }
            repository.updateState(
                id,
                if (installed) "INSTALLED" else "CREATED",
                null
            )
            _message.value = if (installed) {
                "分身创建成功：${app.label}（请在分身空间查看）"
            } else {
                "已记录分身：${app.label}，请在系统设置中确认分身空间状态"
            }
        }
    }

    /** 消费一次性提示消息 */
    fun consumeMessage() {
        _message.value = null
    }
}
