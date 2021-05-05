package io.openk9.datasource.event.configuration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
	name = "datasource event configuration"
)
public @interface DatasourceEventConfiguration {

	@AttributeDefinition(
		name = "exchange",
		type = AttributeType.STRING,
		required = false
	)
	String exchange() default "openk9.datasource.direct";

	@AttributeDefinition(
		name = "routingKeyTemplate",
		type = AttributeType.STRING,
		required = false
	)
	String routingKeyTemplate() default "datasource.{eventType}.{entityName}";

}
