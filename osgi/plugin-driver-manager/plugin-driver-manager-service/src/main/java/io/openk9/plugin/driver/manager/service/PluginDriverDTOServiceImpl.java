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

package io.openk9.plugin.driver.manager.service;

import io.openk9.auth.api.ACLQueryContributorRegistry;
import io.openk9.auth.api.UserInfo;
import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeProvider;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import io.openk9.plugin.driver.manager.api.SearchKeyword;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverContextDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = PluginDriverDTOService.class
)
public class PluginDriverDTOServiceImpl implements PluginDriverDTOService {

	@Override
	public Optional<PluginDriverDTO> findPluginDriverDTOByName(
		String name) {

		return _pluginDriverRegistry
			.getPluginDriver(name)
			.map(this::_findDocumentType);
	}

	@Override
	public PluginDriverDTOList findPluginDriverDTOByNames(
		Collection<String> names) {
		return _pluginDriverRegistry
			.getPluginDriverList(names)
			.stream()
			.map(this::_findDocumentType)
			.collect(
				Collectors.collectingAndThen(
					Collectors.toList(),
					PluginDriverDTOList::of
				)
			);
	}

	@Override
	public PluginDriverContextDTO findPluginDriverContextDTO(
		Collection<String> names, UserInfo userInfo) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolean contribute = _aclQueryContributorRegistry.contribute(
			names, userInfo, boolQueryBuilder);

		return PluginDriverContextDTO.of(
			findPluginDriverDTOByNames(names).getPluginDriverDTOList(),
			contribute ? Strings.toString(boolQueryBuilder) : null
		);
	}

	@Override
	public PluginDriverDTOList findPluginDriverDTOList() {
		return _pluginDriverRegistry
			.getPluginDriverList()
			.stream()
			.map(this::_findDocumentType)
			.collect(
				Collectors.collectingAndThen(
					Collectors.toList(),
					PluginDriverDTOList::of
				)
			);
	}

	private PluginDriverDTO _findDocumentType(PluginDriver pluginDriver) {

		String name = pluginDriver.getName();

		List<DocumentType> documentTypeList =
			_documentTypeProvider.getDocumentTypeList(name);

		DocumentType defaultDocumentType =
			_documentTypeProvider.getDefaultDocumentType(name);

		if (documentTypeList.isEmpty() && defaultDocumentType != null) {
			documentTypeList = List.of(defaultDocumentType);
		}
		else if(!documentTypeList.isEmpty() && defaultDocumentType == null) {
			defaultDocumentType = documentTypeList.get(0);
		}

		List<DocumentTypeDTO> documentTypeDTOS =
			documentTypeList
				.stream()
				.map(documentType ->
					DocumentTypeDTO.of(
						documentType.getName(),
						documentType.getIcon(),
						_wrapSearchKeywords(documentType)
					)
				)
				.collect(Collectors.toList());


			return PluginDriverDTO.of(
				pluginDriver.getDriverServiceName(),
				pluginDriver.getName(),
				pluginDriver.schedulerEnabled(),
				documentTypeDTOS,
				defaultDocumentType == null
					? null
					: DocumentTypeDTO.of(
						defaultDocumentType.getName(),
						defaultDocumentType.getIcon(),
						_wrapSearchKeywords(defaultDocumentType)
					)
			);

	}

	private List<SearchKeywordDTO> _wrapSearchKeywords(
		DocumentType defaultDocumentType) {
		return defaultDocumentType
			.getSearchKeywords()
			.stream()
			.map(searchKeyword ->
				SearchKeywordDTO.of(
					searchKeyword.getKeyword(),
					searchKeyword.getReferenceKeyword(),
					_toSearchKeywordDTOType(searchKeyword.getType()),
					FieldBoostDTO.of(
						searchKeyword.getFieldBoost().getKey(),
						searchKeyword.getFieldBoost().getValue()
					)
				)
			)
			.collect(Collectors.toList());
	}

	private SearchKeywordDTO.Type _toSearchKeywordDTOType(
		SearchKeyword.Type type) {

		switch (type) {
			case DATE: return SearchKeywordDTO.Type.DATE;
			case TEXT: return SearchKeywordDTO.Type.TEXT;
			case NUMBER: return SearchKeywordDTO.Type.NUMBER;
			case AUTOCOMPLETE: return SearchKeywordDTO.Type.AUTOCOMPLETE;
		}

		return SearchKeywordDTO.Type.TEXT;
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DocumentTypeProvider _documentTypeProvider;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ACLQueryContributorRegistry _aclQueryContributorRegistry;

}
