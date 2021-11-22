package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
@Designate(ocd = DateOrderQueryParser.Config.class)
public class DateOrderQueryParser implements QueryParser {

	@ObjectClassDefinition
	@interface Config {
		String fieldName() default "pubDate.sortable";
		String scale() default "3650d";
	}

	@Activate
	@Modified
	void activate(Config config) {
		_fieldName = config.fieldName();
		_scale = config.scale();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		return Mono.fromSupplier(() -> (bool) -> {

			List<PluginDriverDTO> pluginDriverDocumentTypeList =
				context.getPluginDriverDocumentTypeList();

			FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders =
				pluginDriverDocumentTypeList
					.stream()
					.map(PluginDriverDTO::getDocumentTypes)
					.flatMap(Collection::stream)
					.map(DocumentTypeDTO::getSearchKeywords)
					.flatMap(Collection::stream)
					.filter(searchKeywordDTO -> searchKeywordDTO.getType() == SearchKeywordDTO.Type.DATE)
					.distinct()
					.map(searchKeywordDTO ->
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(
							ScoreFunctionBuilders.linearDecayFunction(
								searchKeywordDTO.getKeyword(), null, _scale))
					)
					.toArray(FunctionScoreQueryBuilder.FilterFunctionBuilder[]::new);


			if (filterFunctionBuilders.length != 0) {
				bool.should(
					QueryBuilders.functionScoreQuery(filterFunctionBuilders));
			}

		});
	}

	private String _fieldName;

	private String _scale;

}
