package io.openk9.ingestion.identifier.generator.internal;

import io.openk9.ingestion.identifier.generator.api.IdentifierGenerator;
import org.osgi.service.component.annotations.Component;

import java.util.UUID;

@Component(
	immediate = true,
	service = IdentifierGenerator.class
)
public class IdentifierGeneratorImpl implements IdentifierGenerator {
	@Override
	public String create() {
		return UUID.randomUUID().toString();
	}
}
