package ltd.nextalone.pkginstallerplus.sdk25

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.getObjectField
import ltd.nextalone.pkginstallerplus.*

object PackageInstallerActivityHook {

    @SuppressLint("PrivateApi")
    fun initOnce(cl: ClassLoader) {
        if (cl.hasClass("com.android.packageinstaller.PackageInstallerActivity")) {
            cl.loadClass("com.android.packageinstaller.PackageInstallerActivity")
                .getDeclaredMethod("startInstallConfirm").hook(object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val ctx: Activity = param.thisObject as Activity
                        val spacerId = ctx.getId("spacer")
                        val spacer: View? = ctx.findViewById(spacerId)
                        if (spacer != null) {
                            replaceSpacerWithInfoView(spacer, ctx)
                        } else {
                            Log.e(TAG, "spacer view not found")
                        }
                    }
                })
        }
    }

    fun replaceSpacerWithInfoView(spacer: View, activity: Activity) {
        val lp: ViewGroup.LayoutParams = spacer.layoutParams
        val parent: ViewGroup = spacer.parent as ViewGroup
        val idx = parent.indexOfChild(spacer)
        lp.height = WRAP_CONTENT
        val tv = TextView(spacer.context)
        tv.typeface = Typeface.MONOSPACE
        tv.textSize = 14f
        tv.setTextIsSelectable(true)
        val a: Int = spacer.context.dip2px(16f)
        tv.setPadding(a, 0, a, 0)
        parent.removeViewAt(idx)
        parent.addView(tv, idx, lp)
        val newPkgInfo: PackageInfo = getObjectField(activity, "mPkgInfo") as PackageInfo
        val pkgName = newPkgInfo.packageName
        val oldPkgInfo = try {
            activity.packageManager
                .getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val sb = SpannableStringBuilder()
        val greenSpan = ForegroundColorSpan(ThemeUtil.colorGreen)
        val redSpan = ForegroundColorSpan(ThemeUtil.colorRed)
        if (oldPkgInfo == null) {
            val oldVersionStr =
                (newPkgInfo.versionName ?: "N/A") + "(" + newPkgInfo.versionCode + ")"
            sb.append("PackageName:\n")
                .append(" +" + pkgName, greenSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(" +" + oldVersionStr, greenSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val oldVersionStr = (oldPkgInfo.versionName ?: "N/A") +
                    "(" + oldPkgInfo.versionCode + ")"
            val newVersionStr = (newPkgInfo.versionName ?: "N/A") +
                    "(" + newPkgInfo.versionCode + ")"
            sb.append("PackageName:\n")
                .append("  " + pkgName)
                .append('\n')
                .append("Version:\n")
                .append(" +" + newVersionStr, greenSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append('\n')
                .append(" -" + oldVersionStr, redSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tv.text = sb
    }
}