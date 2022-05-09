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

package io.openk9.auth;

import io.openk9.auth.api.ACLQueryContributor;
import io.openk9.auth.api.ACLQueryContributorRegistry;
import io.openk9.auth.api.UserInfo;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Collection;
import java.util.List;

@Component(
	immediate = true,
	service = ACLQueryContributorRegistry.class
)
public class ACLQueryContributorRegistryImpl
	implements ACLQueryContributorRegistry {

	@Override
	public boolean contribute(
		String driverServiceName, UserInfo userInfo,
		BoolQueryBuilder booleanQuery) {

		return contribute(List.of(driverServiceName), userInfo, booleanQuery);

	}

	@Override
	public boolean contribute(
		Collection<String> driverServiceNames, UserInfo userInfo,
		BoolQueryBuilder booleanQuery) {

		if (userInfo == null) {
			return false;
		}

		ACLQueryContributor aclQueryContributor =
			_findACLQueryContributors(driverServiceNames);

		if (aclQueryContributor == ACLQueryContributor.NOTHING) {
			return false;
		}

		aclQueryContributor.accept(userInfo, booleanQuery);

		return true;

	}

	private ACLQueryContributor _findACLQueryContributor(
		String driverServiceName) {

		return _findACLQueryContributors(List.of(driverServiceName));

	}

	private ACLQueryContributor _findACLQueryContributors(
		Collection<String> driverServiceNames) {

		return _aclQueryContributorList
			.stream()
			.filter(aclQueryContributor -> driverServiceNames.contains(aclQueryContributor.driverServiceName()))
			.reduce(ACLQueryContributor.NOTHING, ACLQueryContributor::andThen);

	}

	@Reference(
		policyOption = ReferencePolicyOption.GREEDY,
		cardinality = ReferenceCardinality.MULTIPLE
	)
	private List<ACLQueryContributor> _aclQueryContributorList;

}
