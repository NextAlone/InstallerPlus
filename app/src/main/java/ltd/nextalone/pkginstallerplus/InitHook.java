package ltd.nextalone.pkginstallerplus;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ltd.nextalone.pkginstallerplus.sdk25.PackageInstallerActivityHook;

public class InitHook implements IXposedHookLoadPackage {
    private static final String TAG = "NextAlone";

    private static boolean sInitialized = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: " + lpparam.packageName);
        if ("com.google.android.packageinstaller".equals(lpparam.packageName)
                || "com.android.packageinstaller".equals(lpparam.packageName)) {
            if (!sInitialized) {
                sInitialized = true;
                initializeHookInternal(lpparam);
            }
        }
    }

    private static void initializeHookInternal(XC_LoadPackage.LoadPackageParam lpparam) {
        Log.d(TAG, "Hooked");
        PackageInstallerActivityHook.INSTANCE.initOnce(lpparam.classLoader);
    }
}
