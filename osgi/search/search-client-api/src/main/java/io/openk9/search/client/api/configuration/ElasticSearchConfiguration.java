package io.openk9.search.client.api.configuration;

public interface ElasticSearchConfiguration {

	String getDataIndex();

	String getEntityIndex();

	String[] hosts();

	int bufferMaxSize();

	long bufferMaxTime();

}
