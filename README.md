# DynamicApkProtection

This sample is to demonstrate how Dynamic APK Protection mechanism works. 

## Description
https://dexprotector.com/docs#dynamic-apk-protection

## Requirements
- Android Studio or Gradle
- NDK
- DexProtector Enterpise with a valid license

## Configuring 
- Set path to Android SDK in local.properties
- Set path to DexProtector Enterprise in build.gradle (project’s root) instead of `/Users/developer/DexProtector`

## Building

### Building 2nd apk
```
cd DynamicApkProtection/DynamicSample
<ndk-dir>/ndk-build
../gradlew clean assembleRelease
cp build/outputs/apk/DynamicSample-release.apk ../Main/src/main/assets/DynamicSample.apk
```
### Building Main apk
```
cd DynamicApkProtection/Main
<ndk-dir>/ndk-build
../gradlew clean assembleRelease
```

## Evaluating
```
adb install -r build/outputs/apk/Main-release.apk
adb shell am start -n com.dexprotector.dynamic.main/.MainActivity
```

When the Main application is started the following protection features are demonstrated:

- Loading of a native library from the Main application which is protected with Native Library Encryption (NLE) + JNI Obfuscation (JNI)
- Loading of a file from the assets of the Main application which is protected with Resource Encryption
- Loading of a protected apk from the Main application’s assets
- Loading of a native library (NLE+JNI) from the second apk
- Call of an ordinary and a JNI method (Hide Access + JNI) from the 2nd apk
- Call of a JNI method from the Main app
- Loading of a file from the assets folder of the 2nd apk protected with Resource Encryption

Corresponding messages will be displayed on a screen.
