package io.openk9.auth.tenant;

public interface TenantResolver {
	long getTenantId();

	String getTenantName();

	void setTenant(String name);

}
