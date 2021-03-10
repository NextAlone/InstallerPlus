package ltd.nextalone.pkginstallerplus

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
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

fun findField(clazz: Class<*>?, type: Class<*>?, name: String?): Field? {
    if (clazz != null && name?.length!! > 0) {
        var clz: Class<*> = clazz
        do {
            for (field in clz.declaredFields) {
                if ((type == null || field.type == type) && (field.name == name)
                ) {
                    field.isAccessible = true
                    return field
                }
            }
        } while (clz.superclass.also { clz = it } != null)
    }
    return null
}

fun iget_object_or_null(obj: Any, name: String?): Any? {
    return iget_object_or_null<Any>(obj, name, null)
}

fun <T> iget_object_or_null(obj: Any, name: String?, type: Class<T>?): T? {
    val clazz: Class<*> = obj.javaClass
    try {
        val f: Field = findField(clazz, type, name) as Field
        f.isAccessible = true
        return f[obj] as T
    } catch (e: Exception) {
    }
    return null
}
