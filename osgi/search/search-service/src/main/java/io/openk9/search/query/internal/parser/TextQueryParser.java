package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = QueryParser.class
)
@Designate(ocd = TextQueryParser.Config.class)
public class TextQueryParser implements QueryParser {

	@ObjectClassDefinition
	@interface Config {
		float boost() default 1.0f;
	}

	@Activate
	@Modified
	void activate(TextQueryParser.Config config) {
		_boost = config.boost();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _textEntityQuery(
				searchTokens, bool,
				context.getPluginDriverDocumentTypeList());

		});
	}

	private void _textEntityQuery(
		List<SearchToken> tokenTextList, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList) {

		if (!tokenTextList.isEmpty()) {

			for (SearchToken searchToken : tokenTextList) {
				_termQueryPrefixValues(
					searchToken, query, entityMapperList);
			}

		}

	}

	private void _termQueryPrefixValues(
		SearchToken tokenText, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList) {

		String[] values = tokenText.getValues();

		if (values.length == 0) {
			return;
		}

		String keywordKey = tokenText.getKeywordKey();

		Predicate<SearchKeywordDTO> keywordKeyPredicate =
			searchKeyword -> keywordKey == null || keywordKey.isEmpty() ||
							 searchKeyword.getKeyword().equals(keywordKey);

		Map<String, Float> keywordBoostMap =
			entityMapperList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isText)
				.filter(keywordKeyPredicate)
				.map(SearchKeywordDTO::getFieldBoost)
				.collect(
					Collectors.toMap(
						FieldBoostDTO::getKeyword,
						FieldBoostDTO::getBoost,
						Math::max,
						HashMap::new
					)
				);

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.boost(_boost);

		for (String value : values) {

			MultiMatchQueryBuilder multiMatchQueryBuilder =
				new MultiMatchQueryBuilder(value);

			multiMatchQueryBuilder.fields(keywordBoostMap);

			boolQueryBuilder.should(multiMatchQueryBuilder);

			if (value.split("\\s+").length > 1) {

				multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				multiMatchQueryBuilder.type(
					MultiMatchQueryBuilder.Type.PHRASE);

				multiMatchQueryBuilder.slop(2);

				multiMatchQueryBuilder.boost(2.0f);

				boolQueryBuilder.should(multiMatchQueryBuilder);

			}

		}

		query.should(boolQueryBuilder);

	}

	private float _boost;

	private static final String TYPE = "TEXT";

}
