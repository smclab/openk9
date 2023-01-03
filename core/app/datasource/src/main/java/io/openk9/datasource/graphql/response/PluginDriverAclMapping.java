package io.openk9.datasource.graphql.response;

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;

public record PluginDriverAclMapping(
	DocTypeField docTypeField,
	UserField userField) {
}
