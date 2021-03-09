package ltd.nextalone.pkginstallerplus;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class InitHook implements IXposedHookLoadPackage {
    private static final String TAG = "NextAlone";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "handleLoadPackage:" + lpparam.packageName);
        if (!lpparam.packageName.equals("com.google.android.packageinstaller")&&!lpparam.packageName.equals("com.android.packageinstaller")) return;
        Log.d(TAG, "Hooked");
    }
}
