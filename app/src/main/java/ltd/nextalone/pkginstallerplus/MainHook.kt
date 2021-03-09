package ltd.nextalone.pkginstallerplus

import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook  {
    private val name = "carlos"

    fun printlnName() {
        Log.d(TAG, "printlnName: $name")
    }

    private fun hookPackageInstaller(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.d(TAG, "hookPackageInstaller: ")
        val hookClass =
            lpparam.classLoader.loadClass("ltd.nextalone.pkginstallerplus.MainHook") ?: return
        // 通过XposedHelpers调用静态方法printlnHelloWorld
        XposedHelpers.callStaticMethod(hookClass, "printlnHelloWorld")
        val demoClass = hookClass.newInstance()
        val field = hookClass.getDeclaredField("name")
        field.isAccessible = true
        field.set(demoClass, "xbd")
        XposedHelpers.callMethod(hookClass.newInstance(), "printlnName")
    }

    companion object {
        const val TAG: String = "NextAlone"
    }
}
