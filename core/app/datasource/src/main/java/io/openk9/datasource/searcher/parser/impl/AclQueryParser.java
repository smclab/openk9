/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.searcher.parser.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

@ApplicationScoped
public class AclQueryParser implements QueryParser {

	@ConfigProperty(
		name = "openk9.datasource.acl.query.extra.params.key", defaultValue = "OPENK9_ACL"
	)
	String extraParamsKey;
	@ConfigProperty(
		name = "openk9.datasource.acl.query.extra.params.enabled", defaultValue = "false"
	)
	boolean extraParamsEnabled;

	public static void apply(
		DocTypeField docTypeField,
		List<String> terms,
		BoolQueryBuilder boolQueryBuilder) {

		if (terms != null && !terms.isEmpty()) {
			boolQueryBuilder.should(
				QueryBuilders.termsQuery(docTypeField.getPath(), terms));
		}

	}

	@Override
	public String getType() {
		return "ACL";
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		var innerQuery = getAclFilterQuery(
			parserContext, this.extraParamsKey, this.extraParamsEnabled);

		parserContext.getMutableQuery().filter(innerQuery);

		return Uni.createFrom().voidItem();
	}

	protected static BoolQueryBuilder getAclFilterQuery(
		ParserContext parserContext, String extraParamsKey, boolean extraParamsEnabled) {

		BoolQueryBuilder innerQuery =
			QueryBuilders
				.boolQuery()
				.minimumShouldMatch(1)
				.should(QueryBuilders.matchQuery("acl.public", true));

		JWT jwt = parserContext.getJwt();

		if (!jwt.isEmpty()) {

			Iterator<AclMapping> iterator =
				parserContext
					.getBucket()
					.getDatasources()
					.stream()
					.flatMap(d -> d.getPluginDriver().getAclMappings().stream())
					.distinct()
					.iterator();

			while (iterator.hasNext()) {
				AclMapping aclMapping = iterator.next();

				DocTypeField docTypeField = aclMapping.getDocTypeField();

				UserField userField = aclMapping.getUserField();

				apply(docTypeField, userField.getTerms(jwt), innerQuery);

			}

		}
		else {

			if (extraParamsEnabled) {

				Map<String, List<String>> extraParams = parserContext.getExtraParams();

				if (extraParams != null && !extraParams.isEmpty()) {
					List<String> roles = extraParams.get(extraParamsKey);
					if (roles != null && !roles.isEmpty()) {
						parserContext
							.getBucket()
							.getDatasources()
							.stream()
							.flatMap(d -> d.getPluginDriver().getAclMappings().stream())
							.filter(aclMapping -> aclMapping.getUserField() == UserField.ROLES)
							.findFirst()
							.ifPresent((aclMapping) -> apply(
								aclMapping.getDocTypeField(),
								roles,
								innerQuery
							));
					}
				}
			}
		}
		return innerQuery;
	}

	@Override
	public boolean isQueryParserGroup() {
		return false;
	}

}