// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
	id("com.android.application") version "8.7.2" apply false
	id("org.jetbrains.kotlin.android") version "1.9.20" apply false
	id("com.google.dagger.hilt.android") version "2.51.1" apply false
	// KSP version works by: <Kotlin version>-<KSP version>
	// This is why the Kotlin part should always match the Kotlin version above
	id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

// Add this snippet to main project build file like this.
allprojects {
	gradle.projectsEvaluated {
		tasks.withType<JavaCompile> {
			options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-Xdiags:verbose"))
		}
	}
}

configurations {
	all {
		exclude(group = "com.google.firebase")
	}
}
