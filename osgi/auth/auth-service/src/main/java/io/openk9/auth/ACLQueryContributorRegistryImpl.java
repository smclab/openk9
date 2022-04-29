package io.openk9.auth;

import io.openk9.auth.api.ACLQueryContributor;
import io.openk9.auth.api.ACLQueryContributorRegistry;
import io.openk9.auth.api.UserInfo;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
