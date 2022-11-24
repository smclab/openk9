package io.openk9.auth.tenant;

public interface TenantResolver {

	String getTenantName();

	void setTenant(String name);

}
