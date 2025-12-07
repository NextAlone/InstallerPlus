package ltd.nextalone.pkginstallerplus.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import ltd.nextalone.pkginstallerplus.HookEntry
import java.lang.reflect.Member
import java.lang.reflect.Method

const val INSTALLER_V2_PKG = "com.android.packageinstaller.v2.ui"

internal val String.clazz: Class<*>?
    get() = try {
        HookEntry.lpClassLoader.loadClass(this)
    } catch (e: ClassNotFoundException) {
        null
    }

internal fun Member.hook(callback: XC_MethodHook) = try {
    XposedBridge.hookMethod(this, callback)
} catch (e: Throwable) {
    Log.e(TAG, e.message, e)
    null
}

internal inline fun Member.hookBefore(crossinline hooker: (XC_MethodHook.MethodHookParam) -> Unit) = hook(object : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam?) = try {
        hooker(param!!)
    } catch (e: Throwable) {
        Log.e(TAG, e.message, e)
        Unit
    }
})

internal inline fun Member.hookAfter(crossinline hooker: (XC_MethodHook.MethodHookParam) -> Unit) = hook(object : XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) = try {
        hooker(param!!)
    } catch (e: Throwable) {
        Log.e(TAG, e.message, e)
        Unit
    }
})

internal fun Member.replace(result: Any?) = this.replace { result }

internal inline fun <T : Any> Member.replace(crossinline hooker: (XC_MethodHook.MethodHookParam) -> T?) = hook(object : XC_MethodReplacement() {
    override fun replaceHookedMethod(param: MethodHookParam?): Any = {
        hooker(param!!)
    }
})

internal fun Class<*>.method(name: String): Method? = this.declaredMethods.run {
    this.forEach {
        if (it.name == name) {
            return it
        }
    }
    return null
}

@Throws(IllegalArgumentException::class)
internal fun Class<*>.method(name: String, vararg argsTypesAndReturnType: Any): Method? {
    var clazz = this
    val argc = argsTypesAndReturnType.size / 2
    val argt: Array<Class<*>?> = arrayOfNulls(argc)
    val argv = arrayOfNulls<Any>(argc)
    var returnType: Class<*>? = null
    if (argc * 2 + 1 == argsTypesAndReturnType.size) returnType = argsTypesAndReturnType[argsTypesAndReturnType.size - 1] as Class<*>
    var i: Int
    var ii: Int
    var m: Array<Method>
    var method: Method? = null
    var _argt: Array<Class<*>>
    i = 0
    while (i < argc) {
        argt[i] = argsTypesAndReturnType[argc + i] as Class<*>
        argv[i] = argsTypesAndReturnType[i]
        i++
    }
    loop_main@ do {
        m = clazz.declaredMethods
        i = 0
        loop@ while (i < m.size) {
            if (m[i].name == name) {
                _argt = m[i].parameterTypes
                if (_argt.size == argt.size) {
                    ii = 0
                    while (ii < argt.size) {
                        if (argt[ii] != _argt[ii]) {
                            i++
                            continue@loop
                        }
                        ii++
                    }
                    if (returnType != null && returnType != m[i].returnType) {
                        i++
                        continue
                    }
                    method = m[i]
                    break@loop_main
                }
            }
            i++
        }
    } while (Any::class.java != clazz.superclass.also { clazz = it })
    if (method != null) method.isAccessible = true
    return method
}

internal fun Class<*>.method(size: Int, returnType: Class<*>, condition: (method: Member) -> Boolean = { true }): Method? = this.declaredMethods.run {
    this.forEach {
        if (it.returnType == returnType && it.parameterTypes.size == size && condition(it)) {
            return it
        }
    }
    return null
}

internal fun Class<*>.method(name: String, size: Int, returnType: Class<*>, condition: (method: Member) -> Boolean = { true }): Method? = this.declaredMethods.run {
    this.forEach {
        if (it.name == name && it.returnType == returnType && it.parameterTypes.size == size && condition(it)) {
            return it
        }
    }
    return null
}

internal val isV2InstallerAvailable: Boolean
    get() = "$INSTALLER_V2_PKG.InstallLaunch".clazz != null

internal fun PackageManager.getPackageInfoOrNull(pkgName: String): PackageInfo? =
    try {
        @Suppress("DEPRECATION", "InlinedApi")
        getPackageInfo(pkgName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
