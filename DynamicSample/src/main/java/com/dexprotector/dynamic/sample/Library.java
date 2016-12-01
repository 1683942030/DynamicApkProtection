package com.dexprotector.dynamic.sample;

/**
 * Sample of a library with ordinary and native methods
 */
public class Library {

    public String sayHello() {
        return "Hello from com.dexprotector.dynamic.sample.sayHello()";
    }

    /**
     * Wrapper for the internal native method
     */
    public String sayHelloJNIProxy() {
        return sayHelloJNI();

    }

    private native String sayHelloJNI();

}
