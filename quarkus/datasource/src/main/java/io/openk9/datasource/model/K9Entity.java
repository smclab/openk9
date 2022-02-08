package io.openk9.datasource.model;

public interface K9Entity {

	default Class<? extends K9Entity> getType() {
		return this.getClass();
	}

}
