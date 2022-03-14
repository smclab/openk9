package io.openk9.resources;

import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Startup
public class PrintLogo {

	@PostConstruct
	void onStart() {
		try {
			Class.forName("io.openk9.resources.Release");
		}
		catch (Exception e) {}
	}

}
