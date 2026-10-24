# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ML Kit creates its component registrars (e.g. BarcodeRegistrar) by reflection through
# their no-arg constructors. The firebase-components version ML Kit pulls in (16.1.0) only
# ships "-keep class * implements ComponentRegistrar", which in R8 full mode keeps the class
# but not its constructor, so BarcodeScanning.getClient() crashed with a NullPointerException.
# This is the rule newer firebase-components versions ship.
-keep class * implements com.google.firebase.components.ComponentRegistrar { void <init>(); }
