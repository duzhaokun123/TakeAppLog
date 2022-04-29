package io.github.duzhaokun123.takeapplog;

import android.app.Application;
import android.app.Instrumentation;

import java.io.IOException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    static String TAG = "TakeAppLog";
    XC_MethodHook.Unhook unhook;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        unhook = XposedHelpers.findAndHookMethod(Instrumentation.class, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws IOException {
                if (lpparam.packageName.contains(":")) return;
                var packageName = lpparam.packageName;
                var application = (Application) param.args[0];
                var file = application.getExternalCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".log";
                XposedBridge.log(TAG + ": " + packageName + " log to " + file);
                Runtime.getRuntime().exec("logcat -f " + file);
                unhook.unhook();
            }
        });
    }
}
