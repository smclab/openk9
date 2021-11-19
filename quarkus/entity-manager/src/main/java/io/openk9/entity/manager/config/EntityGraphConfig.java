package io.openk9.entity.manager.config;

import io.quarkus.arc.Unremovable;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Getter
@Setter
@ApplicationScoped
@Unremovable
public class EntityGraphConfig {

	@Inject
	@ConfigProperty(
		name = "openk9.entity.score-threshold",
		defaultValue = "0.8"
	)
	float scoreThreshold;

	@Inject
	@ConfigProperty(
		name = "openk9.entity.unique-entities",
		defaultValue = "date,organization,loc,email,person,document"
	)
	String[] uniqueEntities;

	@Inject
	@ConfigProperty(
		name = "openk9.entity.min-hops",
		defaultValue = "1"
	)
	int minHops;
	@Inject
	@ConfigProperty(
		name = "openk9.entity.max-hops",
		defaultValue = "2"
	)
	int maxHops;

}
