package ltd.nextalone.pkginstallerplus;

import static ltd.nextalone.pkginstallerplus.UtilsKt.iget_object_or_null;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;
import java.lang.reflect.Method;
import ltd.nextalone.pkginstallerplus.sdk25.PackageInstallerActivityHook;
import ltd.nextalone.pkginstallerplus.sdk30.PackageInstallerActivityHook30;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "NextAlone";

    private static boolean sInitialized = false;
    private static String sModulePath = null;
    private static long sResInjectBeginTime = 0;
    private static long sResInjectEndTime = 0;
    private static ClassLoader classLoader;

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

    public static void injectModuleResources(Resources res) {
        if (res == null) {
            return;
        }
        try {
            res.getString(R.string.res_inject_success);
            return;
        } catch (Resources.NotFoundException ignored) {
        }
        try {
            if (classLoader == null) {
                classLoader = HookEntry.class.getClassLoader();
            }
            if (sModulePath == null) {
                if (sResInjectBeginTime == 0) {
                    sResInjectBeginTime = System.currentTimeMillis();
                }
                String modulePath = null;
                BaseDexClassLoader pcl = (BaseDexClassLoader) classLoader;
                Object pathList = iget_object_or_null(pcl, "pathList");
                Object[] dexElements = (Object[]) iget_object_or_null(pathList, "dexElements");
                for (Object element : dexElements) {
                    File file = (File) iget_object_or_null(element, "path");
                    if (file == null || file.isDirectory()) {
                        file = (File) iget_object_or_null(element, "zip");
                    }
                    if (file == null || file.isDirectory()) {
                        file = (File) iget_object_or_null(element, "file");
                    }
                    if (file != null && !file.isDirectory()) {
                        String path = file.getPath();
                        if (modulePath == null || !modulePath.contains("ltd.nextalone.pkginstallerplus")) {
                            modulePath = path;
                        }
                    }
                }
                if (modulePath == null) {
                    throw new RuntimeException("get module path failed, loader=" + classLoader);
                }
                sModulePath = modulePath;
            }
            AssetManager assets = res.getAssets();
            @SuppressLint("DiscouragedPrivateApi")
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            int cookie = (int) addAssetPath.invoke(assets, sModulePath);
            try {
                Log.d(TAG, "injectModuleResources: " + res.getString(R.string.res_inject_success));
                if (sResInjectEndTime == 0) {
                    sResInjectEndTime = System.currentTimeMillis();
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Fatal: injectModuleResources: test injection failure!");
                Log.e(TAG, "injectModuleResources: cookie=" + cookie + ", path=" + sModulePath + ", loader=" + classLoader);
                long length = -1;
                boolean read = false;
                boolean exist = false;
                boolean isDir = false;
                try {
                    File f = new File(sModulePath);
                    exist = f.exists();
                    isDir = f.isDirectory();
                    length = f.length();
                    read = f.canRead();
                } catch (Throwable e2) {
                    Log.e(TAG, String.valueOf(e2));
                }
                Log.e(TAG, "sModulePath: exists = " + exist + ", isDirectory = " + isDir + ", canRead = " + read + ", fileLength = " + length);
            }
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
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
