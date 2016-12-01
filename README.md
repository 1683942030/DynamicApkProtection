# DynamicApkProtection

This sample is to demonstrate how Dynamic APK Protection mechanism works. 

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
Corresponding messages will be displayed on a screen.
