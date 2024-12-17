# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

#-keep class com.nfcalarmclock.** { *; }
-verbose
-keepattributes EnclosingMethod,LineNumberTable,SourceFile

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-dontshrink
#-dontoptimize

#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
