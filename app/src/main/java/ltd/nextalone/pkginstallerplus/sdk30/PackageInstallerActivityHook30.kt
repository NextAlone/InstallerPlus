package ltd.nextalone.pkginstallerplus.sdk30

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.getObjectField
import ltd.nextalone.pkginstallerplus.*
import ltd.nextalone.pkginstallerplus.HookEntry.injectModuleResources

object PackageInstallerActivityHook30 {

    @SuppressLint("PrivateApi")
    fun initOnce(cl: ClassLoader) {
        if (cl.hasClass("com.android.packageinstaller.PackageInstallerActivity")) {
            cl.loadClass("com.android.packageinstaller.PackageInstallerActivity")
                .getDeclaredMethod("startInstallConfirm").hook(object : XC_MethodHook() {
                    @RequiresApi(Build.VERSION_CODES.P)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val ctx: Activity = param.thisObject as Activity
                        injectModuleResources(ctx.resources)
                        val installId = ctx.resources.getIdentifier("install_confirm_question", "id", "com.android.packageinstaller")
                        val install: View? = ctx.findViewById(installId)
                        if (install != null) {
                            Thread {
                                Thread.sleep(100)
                                ctx.runOnUiThread {
                                    replaceSpacerWithInfoView(install, ctx)
                                }
                            }.start()
                        } else {
                            Log.e(TAG, "confirm view not found")
                        }
                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun replaceSpacerWithInfoView(install: View, activity: Activity) {
        val context = install.context
        val textView = TextView(context)
        textView.setTextIsSelectable(true)
        textView.typeface = Typeface.MONOSPACE
        val layout = LinearLayout(context)
        val newPkgInfo: PackageInfo = getObjectField(activity, "mPkgInfo") as PackageInfo
        val pkgName = newPkgInfo.packageName
        val oldPkgInfo = try {
            activity.packageManager.getPackageInfo(pkgName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        if (oldPkgInfo == null) {
            val oldVersionStr =
                (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.longVersionCode + ")"
            sb.append("${context.resources.getString(R.string.package_name)}:\n")
                .append(" +$pkgName", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append("${context.resources.getString(R.string.version)}:\n")
                .append(" +$oldVersionStr", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            layout.setPadding(0, install.height, 0, 0)
        } else {
            val updateId = activity.resources.getIdentifier("install_confirm_question_update", "id", "com.android.packageinstaller")
            val update: View? = activity.findViewById(updateId)
            val oldVersionStr = """${oldPkgInfo.versionName ?: "N/A"}(${oldPkgInfo.longVersionCode})"""
            val newVersionStr = """${newPkgInfo.versionName ?: "N/A"}(${newPkgInfo.longVersionCode})"""
            sb.append("${context.resources.getString(R.string.package_name)}:\n")
                .append("  $pkgName\n")
                .append("${context.resources.getString(R.string.version)}:\n")
                .append(" -$oldVersionStr", ForegroundColorSpan(ThemeUtil.colorRed), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(" +$newVersionStr", ForegroundColorSpan(ThemeUtil.colorGreen), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (update != null) {
                layout.setPadding(0, update.height, 0, 0)
            } else {
                layout.setPadding(0, context.dip2px(45f), 0, 0)
            }
        }
        textView.text = sb
        layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        (install.parent as ViewGroup).addView(layout)
    }
}
