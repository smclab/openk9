package io.openk9.bundle.installer.via.config;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface BundleInstallerConfigurationOCD {
	String bundleSymbolicName() default "";
	String bundleBase64() default "";
}
