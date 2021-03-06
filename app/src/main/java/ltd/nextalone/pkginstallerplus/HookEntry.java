package ltd.nextalone.pkginstallerplus;

import static ltd.nextalone.pkginstallerplus.utils.LogUtilsKt.*;
import static ltd.nextalone.pkginstallerplus.utils.ReflectUtilsKt.iGetObjectOrNull;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.lang.reflect.Method;
import ltd.nextalone.pkginstallerplus.sdk25.PackageInstallerActivityHook;
import ltd.nextalone.pkginstallerplus.sdk30.PackageInstallerActivityHook30;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "NextAlone";
    public static ClassLoader myClassLoader;
    public static ClassLoader lpClassLoader;
    private static boolean sInitialized = false;
    private static String sModulePath = null;
    private static long sResInjectBeginTime = 0;
    private static long sResInjectEndTime = 0;

    private static void initializeHookInternal(LoadPackageParam lpparam) {
        logDebug("Hooked");
        try {
            lpClassLoader = lpparam.classLoader;
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                PackageInstallerActivityHook30.INSTANCE.initOnce();
            } else {
                throw new UnsupportedClassVersionError();
            }
        } catch (Exception e) {
            try {
                PackageInstallerActivityHook.INSTANCE.initOnce();
            } catch (Exception e1) {
                e.addSuppressed(e1);
                logThrowable("initializeHookInternal: ", e);
            }
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
            if (myClassLoader == null) {
                myClassLoader = HookEntry.class.getClassLoader();
            }
            if (sModulePath == null) {
                if (sResInjectBeginTime == 0) {
                    sResInjectBeginTime = System.currentTimeMillis();
                }
                String modulePath = null;
                BaseDexClassLoader pcl = (BaseDexClassLoader) myClassLoader;
                Object pathList = iGetObjectOrNull(pcl, "pathList");
                Object[] dexElements = (Object[]) iGetObjectOrNull(pathList, "dexElements");
                for (Object element : dexElements) {
                    File file = (File) iGetObjectOrNull(element, "path");
                    if (file == null || file.isDirectory()) {
                        file = (File) iGetObjectOrNull(element, "zip");
                    }
                    if (file == null || file.isDirectory()) {
                        file = (File) iGetObjectOrNull(element, "file");
                    }
                    if (file != null && !file.isDirectory()) {
                        String path = file.getPath();
                        if (modulePath == null || !modulePath.contains("ltd.nextalone.pkginstallerplus")) {
                            modulePath = path;
                        }
                    }
                }
                if (modulePath == null) {
                    throw new RuntimeException("get module path failed, loader=" + myClassLoader);
                }
                sModulePath = modulePath;
            }
            AssetManager assets = res.getAssets();
            @SuppressLint("DiscouragedPrivateApi")
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            int cookie = (int) addAssetPath.invoke(assets, sModulePath);
            try {
                logDetail("injectModuleResources", res.getString(R.string.res_inject_success));
                if (sResInjectEndTime == 0) {
                    sResInjectEndTime = System.currentTimeMillis();
                }
            } catch (Resources.NotFoundException e) {
                logError("Fatal: injectModuleResources: test injection failure!");
                logError("injectModuleResources: cookie=" + cookie + ", path=" + sModulePath + ", loader=" + myClassLoader);
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
                    logError(String.valueOf(e2));
                }
                logError("sModulePath: exists = " + exist + ", isDirectory = " + isDir + ", canRead = " + read + ", fileLength = " + length);
            }
        } catch (Exception e) {
            logError(String.valueOf(e));
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        logDetail("handleLoadPackage", lpparam.packageName);
        if ("com.google.android.packageinstaller".equals(lpparam.packageName)
            || "com.android.packageinstaller".equals(lpparam.packageName)) {
            if (!sInitialized) {
                sInitialized = true;
                initializeHookInternal(lpparam);
            }
        }
    }
}
