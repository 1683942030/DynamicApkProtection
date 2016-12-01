package com.dexprotector.dynamic.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            log("Loading of the native library from the Main application protected with NLE + JNI");
            System.loadLibrary("main-jni");

            log("Loading a file from Main's assets protected with RE");
            final AssetManager assetManager = getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                log(line);
            }
            reader.close();

            log("Loading the protected 2nd apk from Main's assets");
            InputStream in = assetManager.open("DynamicSample.apk");
            File sampleAPK = new File(getApplicationContext().getFilesDir(), System.currentTimeMillis() + ".apk");
            sampleAPK.deleteOnExit();
            FileOutputStream out = new FileOutputStream(sampleAPK);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();

            log("Initializing 2apk");
            File dexOut = new File(getFilesDir(), "dexout");
            if(!dexOut.exists()) {
                dexOut.mkdir();
            }
            log("Updating paths to 2nd apks' assets in Main");
            AssetManager.class.getMethod("addAssetPath", String.class)
                    .invoke(getAssets(), sampleAPK.getAbsolutePath());
            log("Creating DexClassLoader and initializing 2nd apk");
            final DexClassLoader dexClassLoader = new DexClassLoader(sampleAPK.getAbsolutePath(), dexOut.getAbsolutePath(), null, MainActivity.class.getClassLoader());
            // IMPORTANT! DexProtector specific. In order to initialize the protection mechnisms it is needed to call the static method
            // <apk package>.Application.initApk(Context, <absolute path to 2apk>)
            Class applicationClass = dexClassLoader.loadClass("com.dexprotector.dynamic.sample.Application");
            applicationClass.getDeclaredMethod("initAPK", Context.class, String.class)
                    .invoke(applicationClass.newInstance(), getApplicationContext(), sampleAPK.getAbsolutePath());

            log("Loading of a native library (protected NLE+JNI) from the 2nd apk");
            // As it is not installed the native libraries are to be extracted manually
            ZipFile zf = new ZipFile(new File(sampleAPK.getAbsolutePath()));
            ZipEntry ze = null;
            String[] abis;
            if (Build.VERSION.SDK_INT < 21) {
                abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            } else {
                abis = Build.SUPPORTED_ABIS;
            }
            for (String abi : abis) {
                String libName = "lib/" + abi + "/libsample-jni.so";
                ze = zf.getEntry(libName);
                log("Try load: " + libName + " " + (ze != null));
                if (ze != null) {
                    break;
                }
            }
            if (ze == null) {
                throw new Exception("no native lib found");
            }
            in = zf.getInputStream(ze);
            File nativeLib = new File(getApplicationContext().getFilesDir(), "libsample-jni.so");
            nativeLib.deleteOnExit();
            out = new FileOutputStream(nativeLib);
            buffer = new byte[1024];
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            // add native library (if you want use it in dexclassloader)
            Method m = Runtime.class.getDeclaredMethod("load", String.class, ClassLoader.class);
            m.setAccessible(true);
            m.invoke(Runtime.getRuntime(), nativeLib.getAbsolutePath(), dexClassLoader);

            log("Calling of an ordinary method and a jni method (HA+JNI) from the protected 2nd apk");
            Class libraryClass = dexClassLoader.loadClass("com.dexprotector.dynamic.sample.Library");
            Object library = libraryClass.getConstructor().newInstance();
            log("Calling com.dexprotector.dynamic.sample.Library.sayHello()->" + libraryClass.getDeclaredMethod("sayHello").invoke(library));
            log("Calling com.dexprotector.dynamic.sample.Library.sayHelloJNIProxy()->" + libraryClass.getDeclaredMethod("sayHelloJNIProxy").invoke(library));

            log("Calling a jni method from Main");
            log("Calling a native method sayMainHello()->" + sayMainHelloJNI());

            log("Loading an encrypted file from assets from the protected 2nd apk");
            reader = new BufferedReader(new InputStreamReader(assetManager.open("names.txt")));
            while ((line = reader.readLine()) != null) {
                log(line);
            }
            reader.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    native String sayMainHelloJNI();

    void log(String msg) {
        TextView tv = (TextView) findViewById(R.id.text);
        tv.append(msg + "\n");
        System.out.println(msg);
    }

}
