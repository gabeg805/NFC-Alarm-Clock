# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ====================================================================
# FOSS Specific Configurations (Disable Obfuscation)
# ====================================================================

# Tells R8/ProGuard not to rename packages, classes, methods, or fields.
# This keeps stack traces perfectly readable without needing a mapping file.
-dontobfuscate
-dontoptimize

# Preserve line numbers and source file names for cleaner crash logs
-keepattributes EnclosingMethod,SourceFile,LineNumberTable

# ====================================================================
# Common FOSS Stack & Code Quality Keeps
# ====================================================================

# If your app prints logs or needs verbose stack trace printing
-verbose

# Prevent optimization loops on common data structures (Enums)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Maintain attributes needed for reflection or JSON parsing if used
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

## Enforce a deterministic build output across different machines
#-repackageclasses ''
#-allowaccessmodification
