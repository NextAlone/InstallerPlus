package ltd.nextalone.pkginstallerplus.sdk31

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import ltd.nextalone.pkginstallerplus.HookEntry.injectModuleResources
import ltd.nextalone.pkginstallerplus.dip2px
import ltd.nextalone.pkginstallerplus.utils.*

@RequiresApi(31)
object PackageInstallerActivityHook31 {
    fun initOnce() {
        "com.android.packageinstaller.PackageInstallerActivity".clazz?.method("startInstallConfirm")
            ?.hookAfter {
                val ctx: Activity = it.thisObject as Activity
                Thread {
                    Thread.sleep(100)
                    ctx.runOnUiThread {
                        addInstallDetails(ctx)
                    }
                }.start()
            }

        "com.android.packageinstaller.UninstallerActivity".clazz?.method("showConfirmationDialog")
            ?.hookBefore {
                val ctx: Activity = it.thisObject as Activity
                injectModuleResources(ctx.resources)
                "com.android.packageinstaller.handheld.UninstallAlertDialogFragment".clazz?.method("onCreateDialog")
                    ?.hookAfter { it2 ->
                        val dialog = it2.result as AlertDialog
                        addUninstallDetails(ctx, dialog)
                    }
            }
    }

    private fun addInstallDetails(activity: Activity) {
        val textView = TextView(activity)
        textView.setTextIsSelectable(true)
        textView.typeface = Typeface.MONOSPACE
        val layout = LinearLayout(activity)
        val newPkgInfo: PackageInfo = activity.get("mPkgInfo") as PackageInfo
        val pkgName = newPkgInfo.packageName
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(
                pkgName,
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        if (oldPkgInfo == null) {
            val install: View? = activity.findHostView("install_confirm_question")
            val oldVersionStr = (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.longVersionCode + ")"
            sb.append("包名: $pkgName\n版本: $oldVersionStr")
            if (install != null) {
                layout.setPadding(0, install.height, 0, 0)
                textView.text = sb
                layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                (install.parent as ViewGroup).addView(layout)
            }
        } else {
            val update: View? = activity.findHostView("install_confirm_question_update")
            val oldVersionStr = """${oldPkgInfo.versionName ?: "N/A"}(${oldPkgInfo.longVersionCode})"""
            val newVersionStr = """${newPkgInfo.versionName ?: "N/A"}(${newPkgInfo.longVersionCode})"""
            sb.append("包名: $pkgName\n版本: $oldVersionStr ➞ $newVersionStr")
            if (update != null) {
                layout.setPadding(0, update.height, 0, 0)
                textView.text = sb
                layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                (update.parent as ViewGroup).addView(layout)
            }
        }
    }

    private fun addUninstallDetails(activity: Activity, dialog: AlertDialog) {
        val textView = TextView(activity)
        textView.setTextIsSelectable(true)
        textView.typeface = Typeface.MONOSPACE
        val layout = LinearLayout(activity)
        if (activity.taskId == -1) return
        val packageName = activity.get("mDialogInfo")?.get("appInfo")?.get("packageName") as String
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(
                packageName,
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        if (oldPkgInfo != null) {
            val oldVersionStr = (oldPkgInfo.versionName ?: "N/A") + "(" + oldPkgInfo.longVersionCode + ")"
            sb.append("包名: ")
                .append(
                    packageName,
                    ForegroundColorSpan(ThemeUtil.colorRed),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                .append('\n')
                .append("版本: ")
                .append(
                    oldVersionStr,
                    ForegroundColorSpan(ThemeUtil.colorRed),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            layout.setPadding(activity.dip2px(24f), 0, activity.dip2px(24f), 0)
            textView.text = sb
            layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            dialog.setView(layout)
        }
    }
}

