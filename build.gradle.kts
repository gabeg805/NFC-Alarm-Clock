// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
	id("com.android.application") version "8.10.1" apply false
	id("org.jetbrains.kotlin.android") version "2.2.10" apply false
	id("com.google.dagger.hilt.android") version "2.57.1" apply false
	// KSP version works by: <Kotlin version>-<KSP version>
	// This combination of versions should be present in the KSP github, or else do not
	// use it.
	// This is also why the Kotlin part should always match the Kotlin version above
	id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
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
