---
id: how-to-create-a-plugin
title: How to create a plugin
---

In OpenK9 you can create custom plugin.

## Prerequisites

Prerequisites are described in [`OSGi project requirements`](/docs/osgi-requirements).

### Create Plugin

A complete example is present at the link https://github.com/smclab/openk9-example-java-datasource 

To create a new plugin you need to have a gradle project with this structure:

```
example-plugin/
├── bnd.bnd
├── build.gradle
└── src
    └── main
        └── java
            └── com
                └── openk9
                    └── plugins
                        └── example
                            └── driver
                                ├── DocumentTypeDefinition.java
                                └── ExamplePluginDriver.java
```

- `bnd.bnd` identifies plugin as bundle
```aidl
Bundle-Name: [OpenK9 - Plugin] Example
Bundle-SymbolicName: com.openk9.plugins.example

Bundle-Version: 0.0.1

-noimportjava: true
```
- `build.gradle` defines java dependencies and adds bndtools plugin 
```aidl
buildscript {
	repositories {
		maven {
			url "https://plugins.gradle.org/m2/"
		}
	}
	dependencies {
		classpath "biz.aQute.bnd:biz.aQute.bnd.gradle:5.2.0"
	}
}

apply plugin: 'java'
apply plugin: 'biz.aQute.bnd.builder'

sourceCompatibility = 11
targetCompatibility = 11

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	compile 'com.openk9:com.openk9.release.api:OPENK9_VERSION'
}
```
- `ExamplePluginDriver.java` contains java code to enable plugin inside OpenK9.
- `DocumentTypeDefinition.java` contains java code to define indexed document types, fields and search keywords