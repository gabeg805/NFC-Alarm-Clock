// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
	id("com.android.application") version "8.1.1" apply false
	id("org.jetbrains.kotlin.android") version "1.8.0" apply false
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
		//exclude(group = "com.google.firebase", module = "firebase-core")
		//exclude(group = "com.google.firebase", module = "firebase-iid")
	}
}

//task clean(type: Delete) {
//	delete rootProject.buildDir
//}
