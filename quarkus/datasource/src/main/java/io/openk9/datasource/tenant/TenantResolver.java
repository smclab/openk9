package io.openk9.datasource.tenant;

public interface TenantResolver {
	long getTenantId();

	String getTenantName();

	void setTenant(String name);

}
