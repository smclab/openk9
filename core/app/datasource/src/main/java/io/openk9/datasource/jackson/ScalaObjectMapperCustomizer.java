package io.openk9.datasource.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;

@Singleton
public class ScalaObjectMapperCustomizer implements ObjectMapperCustomizer {
	@Override
	public void customize(ObjectMapper objectMapper) {
		objectMapper.registerModule(new DefaultScalaModule());
	}
}
