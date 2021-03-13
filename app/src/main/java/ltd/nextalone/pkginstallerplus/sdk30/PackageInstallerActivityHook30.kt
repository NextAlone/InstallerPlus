package ltd.nextalone.pkginstallerplus.sdk30

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import de.robv.android.xposed.XposedHelpers.getObjectField
import ltd.nextalone.pkginstallerplus.HookEntry.injectModuleResources
import ltd.nextalone.pkginstallerplus.R
import ltd.nextalone.pkginstallerplus.TAG
import ltd.nextalone.pkginstallerplus.dip2px
import ltd.nextalone.pkginstallerplus.utils.*


object PackageInstallerActivityHook30 {

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("PrivateApi")
    fun initOnce(cl: ClassLoader) {
        "com.android.packageinstaller.PackageInstallerActivity".clazz?.method("startInstallConfirm")?.hookAfter {
            val ctx: Activity = it.thisObject as Activity
            injectModuleResources(ctx.resources)
            Thread {
                Thread.sleep(100)
                ctx.runOnUiThread {
                    addInstallDetails(ctx)
                }
            }.start()
        }


        "com.android.packageinstaller.UninstallerActivity".clazz?.method("showConfirmationDialog")?.hookBefore {
            val ctx: Activity = it.thisObject as Activity
            injectModuleResources(ctx.resources)
            "com.android.packageinstaller.handheld.UninstallAlertDialogFragment".clazz?.method("onCreateDialog", Bundle::class.java)?.hookAfter { it2 ->
                val dialog = it2.result as AlertDialog
                Thread {
                    Thread.sleep(100)
                    ctx.runOnUiThread {
                        addUninstallDetails(ctx, dialog)
                    }
                }.start()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun addInstallDetails(activity: Activity) {
        val textView = TextView(activity)
        textView.setTextIsSelectable(true)
        textView.typeface = Typeface.MONOSPACE
        val layout = LinearLayout(activity)
        val newPkgInfo: PackageInfo = getObjectField(activity, "mPkgInfo") as PackageInfo
        val pkgName = newPkgInfo.packageName
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(pkgName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        if (oldPkgInfo == null) {
            val installId = activity.resources.getIdentifier("install_confirm_question", "id", "com.android.packageinstaller")
            val install: View? = activity.findViewById(installId)
            val oldVersionStr = (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.longVersionCode + ")"
            sb.append("${activity.resources.getString(R.string.package_name)}:\n")
                .append(" +$pkgName", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append("${activity.resources.getString(R.string.version)}:\n")
                .append(" +$oldVersionStr", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (install != null) {
                layout.setPadding(0, install.height, 0, 0)
                textView.text = sb
                layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                (install.parent as ViewGroup).addView(layout)
            }
        } else {
            val updateId = activity.resources.getIdentifier("install_confirm_question_update", "id", "com.android.packageinstaller")
            val update: View? = activity.findViewById(updateId)
            val oldVersionStr = """${oldPkgInfo.versionName ?: "N/A"}(${oldPkgInfo.longVersionCode})"""
            val newVersionStr = """${newPkgInfo.versionName ?: "N/A"}(${newPkgInfo.longVersionCode})"""
            sb.append("${activity.resources.getString(R.string.package_name)}:\n")
                .append("  $pkgName\n")
                .append("${activity.resources.getString(R.string.version)}:\n")
                .append(" -$oldVersionStr", ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(" +$newVersionStr", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (update != null) {
                layout.setPadding(0, update.height, 0, 0)
                layout.setPadding(0, activity.dip2px(45f), 0, 0)
                textView.text = sb
                layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                (update.parent as ViewGroup).addView(layout)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun addUninstallDetails(activity: Activity, dialog: AlertDialog) {
        Log.i(TAG, "addUninstallDetails: ${dialog.get("mAlertController")?.get("message")}")
        val textView = TextView(activity)
        textView.setTextIsSelectable(true)
        textView.typeface = Typeface.MONOSPACE
        val layout = LinearLayout(activity)
        val packageName = getObjectField(activity, "mPackageName") as String
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        if (oldPkgInfo != null) {
            val oldVersionStr = (oldPkgInfo.versionName ?: "N/A") + "(" + oldPkgInfo.longVersionCode + ")"
            sb.append("${activity.resources.getString(R.string.package_name)}:\n")
                .append(" -$packageName", ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append("${activity.resources.getString(R.string.version)}:\n")
                .append(" -$oldVersionStr", ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            layout.setPadding(0, activity.dip2px(21f), 0, 0)
            textView.text = sb
            layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            dialog.setMessage("要卸载此应用吗?\n$sb")
        }
    }
}

