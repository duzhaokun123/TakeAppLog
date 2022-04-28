package io.github.duzhaokun123.takeapplog;

import android.app.Application;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    static String TAG = "TakeAppLog";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        var packageName = lpparam.packageName;
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                new Thread(() -> {
                    var application = (Application) param.thisObject;
                    var file = application.getExternalCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".log";
                    XposedBridge.log(TAG + ": " + packageName + " log to " + file);

                    try (var logcat = new CloseableProcess(Runtime.getRuntime().exec("logcat"));
                         var inputStream = logcat.process().getInputStream();
                         var outputStream = new java.io.FileOutputStream(file);
                         var inputStreamReader = new java.io.InputStreamReader(inputStream);
                         var outputStreamWriter = new java.io.OutputStreamWriter(outputStream);
                         var bufferedReader = new java.io.BufferedReader(inputStreamReader)
                    ) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            outputStreamWriter.write(line);
                            outputStreamWriter.write("\n");
                            outputStreamWriter.flush();
                        }
                    } catch (Exception e) {
                        XposedBridge.log(TAG + ": " + packageName + " logcat error " + e.getMessage());
                        XposedBridge.log(e);
                    }
                }).start();
            }
        });
    }
}
