package io.openk9.bundle.installer.via.config;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

@Component(
	immediate = true,
	configurationPolicy = ConfigurationPolicy.REQUIRE,
	service = BundleInstallerConfiguration.class
)
@Designate(ocd = BundleInstallerConfigurationOCD.class, factory = true)
public class BundleInstallerConfigurationImpl
	implements BundleInstallerConfiguration {

	@Activate
	void activate(BundleInstallerConfigurationOCD config) {
		bundleSymbolicName = config.bundleSymbolicName();
		bundleBase64 = config.bundleBase64();
	}

	@Override
	public String bundleSymbolicName() {
		return bundleSymbolicName;
	}

	@Override
	public String bundleBase64() {
		return bundleBase64;
	}

	private String bundleSymbolicName;
	private String bundleBase64;

}
