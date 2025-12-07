package ltd.nextalone.pkginstallerplus;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import ltd.nextalone.pkginstallerplus.hook.InstallerHookBaklava;
import ltd.nextalone.pkginstallerplus.hook.InstallerHookN;
import ltd.nextalone.pkginstallerplus.hook.InstallerHookQ;

import static ltd.nextalone.pkginstallerplus.utils.HookUtilsKt.isV2InstallerAvailable;
import static ltd.nextalone.pkginstallerplus.utils.LogUtilsKt.logDebug;
import static ltd.nextalone.pkginstallerplus.utils.LogUtilsKt.logDetail;
import static ltd.nextalone.pkginstallerplus.utils.LogUtilsKt.logError;
import static ltd.nextalone.pkginstallerplus.utils.LogUtilsKt.logThrowable;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static ClassLoader myClassLoader;
    public static ClassLoader lpClassLoader;
    private static boolean sInitialized = false;
    private static String sModulePath = null;
    private static long sResInjectBeginTime = 0;
    private static long sResInjectEndTime = 0;

    private static void initializeHookInternal(LoadPackageParam lpparam) {
        logDebug("initializeHookInternal start");
        lpClassLoader = lpparam.classLoader;
        if (isV2InstallerAvailable()) {
            // Android 16 QPR2
            try {
                logDebug("initializeHook: Baklava QPR2");
                InstallerHookBaklava.INSTANCE.initOnce();
            } catch (Exception e) {
                logThrowable("initializeHook(Baklava QPR2): ", e);
            }
        }
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                //Android Q -- Android T
                logDebug("initializeHook: Q");
                InstallerHookQ.INSTANCE.initOnce();
            } else {
                throw new Exception("UnsupportApiVersionError");
            }
        } catch (Exception e) {
            try {
                //Android Nougat
                logDebug("initializeHook: N");
                InstallerHookN.INSTANCE.initOnce();
            } catch (Exception e1) {
                e.addSuppressed(e1);
                logThrowable("initializeHookInternal: ", e);
            }
        }
    }

    public static void injectModuleResources(Resources res) {
        logDebug("injectModuleResources start");
        if (res == null) {
            return;
        }
        try {
            res.getString(R.string.IPP_res_inject_success);
            return;
        } catch (Resources.NotFoundException ignored) {
        }
        try {
            if (myClassLoader == null) {
                myClassLoader = HookEntry.class.getClassLoader();
            }
            if (sModulePath == null) {
                // should not happen
                throw new IllegalStateException("sModulePath is null");
            }
            if (sResInjectBeginTime == 0) {
                sResInjectBeginTime = System.currentTimeMillis();
            }
            AssetManager assets = res.getAssets();
            @SuppressLint("DiscouragedPrivateApi")
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            int cookie = (int) addAssetPath.invoke(assets, sModulePath);
            try {
                logDetail("injectModuleResources", res.getString(R.string.IPP_res_inject_success));
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

    @Override
    public void initZygote(StartupParam startupParam) {
        String modulePath = startupParam.modulePath;
        assert modulePath != null;
        sModulePath = modulePath;
    }
}
