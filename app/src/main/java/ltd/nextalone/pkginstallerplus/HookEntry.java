package ltd.nextalone.pkginstallerplus;

import android.os.Build.VERSION;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ltd.nextalone.pkginstallerplus.sdk25.PackageInstallerActivityHook;
import ltd.nextalone.pkginstallerplus.sdk30.PackageInstallerActivityHook30;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "NextAlone";

    private static boolean sInitialized = false;

    private static void initializeHookInternal(XC_LoadPackage.LoadPackageParam lpparam) {
        Log.d(TAG, "Hooked");
        switch (VERSION.SDK_INT) {
            case 25:
                PackageInstallerActivityHook.INSTANCE.initOnce(lpparam.classLoader);
                break;
            case 30:
                PackageInstallerActivityHook30.INSTANCE.initOnce(lpparam.classLoader);
                break;
        }
    }

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
}
