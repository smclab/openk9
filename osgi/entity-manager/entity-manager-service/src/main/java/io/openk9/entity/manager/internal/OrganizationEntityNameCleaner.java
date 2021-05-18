package io.openk9.entity.manager.internal;

import io.openk9.common.api.constant.Strings;
import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import java.util.Map;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class OrganizationEntityNameCleaner extends DefaultEntityNameCleaner {

	@interface Config {
		String[] stopWords() default {"spa", "s.p.a.", "srl", "s.r.l.", "s.r.l", "s.p.a"};
	}

	@Activate
	void activate(Config config) {
		_stopWords = config.stopWords();
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_stopWords = null;
	}

	@Override
	public String getEntityType() {
		return "organization";
	}

	@Override
	public Map<String, Object> cleanEntityName(long tenantId, String entityName) {
		return super.cleanEntityName(tenantId, entityName);
	}

	@Override
	public String cleanEntityName(String entityName) {

		for (String stopWord : _stopWords) {
			entityName = entityName.replaceAll(stopWord, Strings.BLANK);
		}

		return super.cleanEntityName(entityName);
	}

	@Override
	protected Map<String, Object> createQueryBuilder(String entityName) {
		return Map.of(
			Constants.ENTITY_NAME_FIELD, entityName,
			Constants.ENTITY_TYPE_FIELD, getEntityType());
	}

	private String[] _stopWords;

}
