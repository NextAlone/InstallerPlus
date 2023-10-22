package ltd.nextalone.pkginstallerplus.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.UserManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.robv.android.xposed.XposedHelpers.getObjectField
import ltd.nextalone.pkginstallerplus.HookEntry
import ltd.nextalone.pkginstallerplus.R
import ltd.nextalone.pkginstallerplus.utils.*

@SuppressLint("PrivateApi")
object InstallerHookN {
    fun initOnce() {
        "com.android.packageinstaller.PackageInstallerActivity".clazz?.method("startInstallConfirm")?.hookAfter {
            val ctx: Activity = it.thisObject as Activity
            HookEntry.injectModuleResources(ctx.resources)
            val spacerId = ctx.getId("spacer")
            val spacer: View? = ctx.findViewById(spacerId)
            if (spacer != null) {
                replaceSpacerWithInfoView(spacer, ctx)
            } else {
                Log.e(TAG, "spacer view not found")
            }
        }
    }

    private fun replaceSpacerWithInfoView(spacer: View, activity: Activity) {
        val layout: ViewGroup.LayoutParams = spacer.layoutParams
        val parent: ViewGroup = spacer.parent as ViewGroup
        val idx = parent.indexOfChild(spacer)
        layout.height = WRAP_CONTENT
        val textView = TextView(spacer.context)
        textView.typeface = Typeface.MONOSPACE
        textView.textSize = 14f
        textView.setTextIsSelectable(true)
        val padding: Int = spacer.context.dip2px(16f)
        textView.setPadding(padding, 0, padding, 0)
        parent.removeViewAt(idx)
        parent.addView(textView, idx, layout)

        val newPkgInfo: PackageInfo = getObjectField(activity, "mPkgInfo") as PackageInfo
        val pkgName = newPkgInfo.packageName
        val usrManager: UserManager = activity.get("mUserManager") as UserManager
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        val sb = SpannableStringBuilder()
        if (oldPkgInfo == null) {
            val newVersionStr = (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.versionCode + ")"
            val newSdkStr = newPkgInfo.applicationInfo.targetSdkVersion.toString()

            sb.append(activity.getString(R.string.IPP_info_user) + ": ")
                .append(usrManager.userName)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_package) + ": ")
                .append(pkgName, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_version) + ": ")
                .append(newVersionStr, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_sdk) + ": ")
                .append(newSdkStr, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        } else {
            val oldVersionStr = (oldPkgInfo.versionName ?: "N/A") + "(" + oldPkgInfo.versionCode + ")"
            val newVersionStr = (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.versionCode + ")"
            val oldSdkStr = oldPkgInfo.applicationInfo.targetSdkVersion.toString()
            val newSdkStr = newPkgInfo.applicationInfo.targetSdkVersion.toString()

            sb.append(activity.getString(R.string.IPP_info_user) + ": ")
                .append(usrManager.userName)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_package) + ": ")
                .append(pkgName, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_version) + ": ")
                .append(oldVersionStr, ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(" ➞ ")
                .append(newVersionStr, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(activity.getString(R.string.IPP_info_sdk) + ": ")
                .append(oldSdkStr, ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(" ➞ ")
                .append(newSdkStr, ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = sb
    }
}
