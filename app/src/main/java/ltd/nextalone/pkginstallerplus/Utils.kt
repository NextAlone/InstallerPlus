package ltd.nextalone.pkginstallerplus

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Member

internal const val TAG = "NextAlone"

const val MATCH_PARENT = -1
const val WRAP_CONTENT = -2

fun ClassLoader.hasClass(name: String): Boolean = try {
    loadClass(name)
    true
} catch (e: ClassNotFoundException) {
    false
}

fun ClassLoader.loadOrNull(name: String): Class<*>? = try {
    loadClass(name)
} catch (e: ClassNotFoundException) {
    null
}

internal fun Member.hook(callback: XC_MethodHook) = try {
    XposedBridge.hookMethod(this, callback)
} catch (e: Throwable) {
    Log.e(TAG, e.message, e)
    null
}

fun Context.getId(name: String): Int = resources.getIdentifier(name, "id", packageName)

fun Context.dip2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.dip2sp(dpValue: Float): Int {
    val scale = resources.displayMetrics.density / resources.displayMetrics.scaledDensity
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.px2sp(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}
