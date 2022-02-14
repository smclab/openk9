package io.openk9.search.query.internal.parser;

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = QueryParser.class
)
@Designate(ocd = EntityQueryParser.Config.class)
public class EntityQueryParser implements QueryParser {

	@ObjectClassDefinition
	@interface Config {
		float boost() default 50.0f;
		boolean manageEntityName() default true;
	}

	@Activate
	@Modified
	void activate(EntityQueryParser.Config config) {
		_boost = config.boost();
		_manageEntityName = config.manageEntityName();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		List<SearchToken> searchTokens = context
			.getTokenTypeGroup()
			.getOrDefault(TYPE, List.of());

		if (searchTokens.isEmpty()) {
			return QueryParser.NOTHING_CONSUMER;
		}

		Mono<Consumer<BoolQueryBuilder>> entityQueryConsumerMono =
			_createEntityQueryConsumer(context, searchTokens);

		if (_manageEntityName && _textQueryParser != null) {
			List<SearchToken> collect =
				searchTokens
					.stream()
					.filter(searchToken ->
						searchToken.getEntityName() != null &&
						!searchToken.getEntityName().isBlank())
					.map(SearchToken::getEntityName)
					.distinct()
					.map(SearchToken::ofText)
					.collect(Collectors.toList());

			if (!collect.isEmpty()) {

				return entityQueryConsumerMono
					.then(
					_textQueryParser.apply(
						Context.of(
							context.getTenant(),
							context.getDatasourceList(),
							context.getPluginDriverDocumentTypeList(),
							Map.of("TEXT", collect),
							context.getHttpRequest(),
							context.getQueryCondition()
						)
					)
				);

			}

		}

		return entityQueryConsumerMono;
	}

	private Mono<Consumer<BoolQueryBuilder>> _createEntityQueryConsumer(
		Context context, List<SearchToken> searchTokens) {
		return Mono.fromSupplier(() -> bool -> {

			Map<String, List<SearchToken>> searchTokenGroupingByType =
				searchTokens
					.stream()
					.collect(Collectors.groupingBy(
							searchToken ->
								searchToken.getEntityType() == null
									? ""
									: searchToken.getEntityType()
						)
					);

			BoolQueryBuilder outerBoolQueryBuilder =
				QueryBuilders.boolQuery();

			for (Map.Entry<String, List<SearchToken>> groupSearchTokens : searchTokenGroupingByType.entrySet()) {

				String type = groupSearchTokens.getKey();

				if (!type.isBlank()) {
					outerBoolQueryBuilder
						.must(QueryBuilders.matchQuery(
							ENTITIES_ENTITY_TYPE, type));
				}

				List<SearchToken> value = groupSearchTokens.getValue();

				String[] ids =
					value
						.stream()
						.map(SearchToken::getValues)
						.flatMap(Arrays::stream)
						.distinct()
						.toArray(String[]::new);

				if (ids.length != 0) {
					outerBoolQueryBuilder.must(
						_multiMatchValues(ENTITIES_ID, ids));
				}

				String[] keywordKeys =
					value
						.stream()
						.map(SearchToken::getKeywordKey)
						.filter(Objects::nonNull)
						.toArray(String[]::new);

				if (keywordKeys.length != 0) {
					outerBoolQueryBuilder.must(
						_multiMatchValues(ENTITIES_CONTEXT, keywordKeys));
				}

			}

			outerBoolQueryBuilder.boost(_boost);

			context.getQueryCondition().accept(bool, outerBoolQueryBuilder);

		});
	}

	private QueryBuilder _multiMatchValues(String field, String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.matchQuery(field, id));
		}

		return boolQuery;

	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(component.name=io.openk9.search.query.internal.parser.TextQueryParser)"
	)
	private QueryParser _textQueryParser;

	private float _boost;
	private boolean _manageEntityName;

	public static final String TYPE = "ENTITY";
	public static final String ENTITIES_ID = "entities.id";
	public static final String ENTITIES_ENTITY_TYPE = "entities.entityType";
	public static final String ENTITIES_CONTEXT = "entities.context";

}
