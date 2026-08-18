# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-verbose
-keep class com.nfcalarmclock.** { *; }
-keepattributes EnclosingMethod,LineNumberTable,SourceFile
-dontobfuscate

# Enforce a deterministic build output across different machines
-repackageclasses ''
-allowaccessmodification



# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-dontshrink
#-dontoptimize

#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
