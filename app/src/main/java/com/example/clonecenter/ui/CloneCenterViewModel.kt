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

    /** 工作分身空间是否已启用（桌面存在「工作」标签） */
    fun isProfileActive(): Boolean {
        return profileManager.isManagedProfileActive()
    }

    /** 目标应用是否已存在于分身空间 */
    fun isAppInProfile(packageName: String): Boolean {
        return profileManager.isPackageInManagedProfile(packageName)
    }

    /** 发起 Work Profile 创建（系统引导） */
    fun createProfile(activity: Activity): Boolean {
        return profileManager.startProvisioning(activity)
    }

    /** 移除工作分身空间（删除桌面「工作」标签与全部分身应用） */
    fun removeProfile(): Boolean {
        val ok = profileManager.removeManagedProfile()
        _message.value = if (ok) {
            "分身空间已移除，桌面「工作」标签与分身应用已清理"
        } else {
            "无法直接移除分身空间（需分身空间管理员权限），已引导打开系统设置，请手动移除"
        }
        refreshApps()
        return ok
    }

    /** 打开系统工作资料设置页 */
    fun openProfileSettings(): Boolean {
        val ok = profileManager.openWorkProfileSettings()
        if (!ok) {
            _message.value = "无法打开工作资料设置，请前往系统设置-用户与账号 中管理"
        }
        return ok
    }

    /** 为指定应用创建分身（落库 + 尝试安装进分身空间） */
    fun cloneApp(app: InstalledApp) {
        if (!isProfileOwner()) {
            _message.value = "请先点击「创建工作分身空间」，完成系统引导后再创建应用分身"
            return
        }
        // 已在分身空间：直接提示，不重复安装
        if (profileManager.isPackageInManagedProfile(app.packageName)) {
            _message.value = "${app.label} 已在分身空间中，可直接切换工作空间使用"
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
                "分身创建成功：${app.label}（已添加到分身空间，切换工作空间即可使用）"
            } else {
                "已记录分身：${app.label}\n系统未允许自动添加，请打开「分身空间设置」→ 应用商店中手动安装"
            }
        }
    }

    /** 消费一次性提示消息 */
    fun consumeMessage() {
        _message.value = null
    }
}
