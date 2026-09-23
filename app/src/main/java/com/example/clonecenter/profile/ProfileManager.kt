package com.example.clonecenter.profile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.UserManager
import com.example.clonecenter.admin.CloneAdminReceiver

/**
 * Work Profile（工作分身空间）管理器
 * 通过系统 DevicePolicyManager 创建隔离的托管资料，实现真正意义的应用分身
 */
class ProfileManager(
    private val context: Context
) {
    private val dpm =
        context.getSystemService(
            Context.DEVICE_POLICY_SERVICE
        ) as DevicePolicyManager

    private val admin = ComponentName(
        context,
        CloneAdminReceiver::class.java
    )

    /** 设备是否支持托管用户（Work Profile 前提条件） */
    fun supportsManagedProfile(): Boolean {
        return context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_MANAGED_USERS
        )
    }

    /** 发起 Work Profile 创建流程（系统引导界面） */
    fun startProvisioning(activity: Activity): Boolean {
        if (!supportsManagedProfile()) {
            return false
        }

        val intent = Intent(
            DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE
        ).apply {
            putExtra(
                DevicePolicyManager
                    .EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                admin
            )

            putExtra(
                DevicePolicyManager
                    .EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                false
            )

            putExtra(
                DevicePolicyManager
                    .EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED,
                true
            )
        }

        if (intent.resolveActivity(
                context.packageManager
            ) == null
        ) {
            return false
        }

        activity.startActivityForResult(
            intent,
            REQUEST_PROVISION_PROFILE
        )

        return true
    }

    /** 当前应用是否为 Profile Owner */
    fun isProfileOwner(): Boolean {
        return dpm.isProfileOwnerApp(
            context.packageName
        )
    }

    /** 启用 Profile（Profile Owner 回调中调用） */
    fun enableProfile() {
        if (!isProfileOwner()) {
            return
        }

        dpm.setProfileName(
            admin,
            "分身空间"
        )

        dpm.setProfileEnabled(admin)
    }

    /** 将已安装应用安装进分身空间（需 Profile Owner） */
    fun installExistingPackage(
        packageName: String
    ): Boolean {
        if (!isProfileOwner()) {
            return false
        }

        return try {
            dpm.installExistingPackage(
                admin,
                packageName
            )
        } catch (_: SecurityException) {
            false
        }
    }

    /** 配置分身空间限制（禁止从主空间分享内容进分身空间） */
    fun setProfileRestrictions() {
        if (!isProfileOwner()) {
            return
        }

        dpm.addUserRestriction(
            admin,
            UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE
        )
    }

    companion object {
        const val REQUEST_PROVISION_PROFILE = 2001
    }
}
