package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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

			BoolQueryBuilder innerBoolQueryBuilder =
				QueryBuilders.boolQuery();

			List<PluginDriverDTO> pluginDriverDocumentTypeList =
				context.getPluginDriverDocumentTypeList();

			List<FunctionScoreQueryBuilder.FilterFunctionBuilder> list =
				new ArrayList<>(pluginDriverDocumentTypeList.size());

			pluginDriverDocumentTypeList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getName)
				.distinct()
				.forEach(name -> {

					String documentFieldName = name + "." + _fieldName;

					ExistsQueryBuilder existsQueryBuilder =
						QueryBuilders.existsQuery(documentFieldName);

					innerBoolQueryBuilder.should(existsQueryBuilder);

					list.add(
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(
							existsQueryBuilder,
							ScoreFunctionBuilders.linearDecayFunction(
								documentFieldName, null, _scale)));

				});


			if (!list.isEmpty()) {

				bool.should(
					QueryBuilders
						.functionScoreQuery(
							QueryBuilders.boolQuery().must(innerBoolQueryBuilder),
							list.toArray(
								FunctionScoreQueryBuilder.FilterFunctionBuilder[]::new)
						)
				);

			}

		});
	}

	private String _fieldName;

	private String _scale;

}
