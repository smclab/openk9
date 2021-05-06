package io.openk9.plugin.driver.manager.service;

import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeProvider;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

		if (documentTypeList.isEmpty()) {
			documentTypeList = List.of(defaultDocumentType);
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
			pluginDriver.getName(),
			pluginDriver.schedulerEnabled(),
			documentTypeDTOS,
			DocumentTypeDTO.of(
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
					searchKeyword.isText(),
					FieldBoostDTO.of(
						searchKeyword.getFieldBoost().getKey(),
						searchKeyword.getFieldBoost().getValue()
					)
				)
			)
			.collect(Collectors.toList());
	}

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

}
