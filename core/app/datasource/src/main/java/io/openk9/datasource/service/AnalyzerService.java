package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.AnalyzerMapper;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.Analyzer_;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.AnalyzerDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

;

@ApplicationScoped
public class AnalyzerService extends BaseK9EntityService<Analyzer, AnalyzerDTO> {
	AnalyzerService(AnalyzerMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<Analyzer> getEntityClass() {return Analyzer.class;} ;


	@Override
	public String[] getSearchFields() {
		return new String[] {Analyzer_.NAME, Analyzer_.DESCRIPTION, Analyzer_.TYPE};
	}

	public Uni<Connection<TokenFilter>> getTokenFilters(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Analyzer_.TOKEN_FILTERS, TokenFilter.class,
			_tokenFilterService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Connection<CharFilter>> getCharFilters(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Analyzer_.CHAR_FILTERS, CharFilter.class,
			_charFilterService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Tokenizer> getTokenizer(Analyzer analyzer) {
		return sessionFactory.withTransaction(
			s -> s.fetch(analyzer.getTokenizer()));
	}

	public Uni<Tokenizer> getTokenizer(long analyzerId) {
		return findById(analyzerId).flatMap(this::getTokenizer);
	}

	public Uni<Tuple2<Analyzer, TokenFilter>> addTokenFilterToAnalyzer(
		long id, long tokenFilterId) {

		return sessionFactory.withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _tokenFilterService.findById(tokenFilterId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilter ->
					s.fetch(analyzer.getTokenFilters())
						.onItem()
						.ifNotNull()
						.transformToUni(tokenFilters -> {

							if (tokenFilters.add(tokenFilter)) {

								analyzer.setTokenFilters(tokenFilters);

								return persist(analyzer)
									.map(newSC -> Tuple2.of(newSC, tokenFilter));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Analyzer, TokenFilter>> removeTokenFilterToAnalyzer(
		long id, long tokenFilterId) {
		return sessionFactory.withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getTokenFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilters -> {

					if (analyzer.removeTokenFilter(tokenFilters, tokenFilterId)) {

						return persist(analyzer)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Analyzer> removeTokenFilterListFromAnalyzer(
		long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getTokenFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(tokenFilters -> {

					if(!tokenFilters.isEmpty()){
						tokenFilters.clear();
						return persist(analyzer);
					};

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Analyzer> removeCharFilterListFromAnalyzer(
		long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getCharFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(charFilters -> {

					if(!charFilters.isEmpty()){
						charFilters.clear();
						return persist(analyzer);
					};

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Analyzer, CharFilter>> addCharFilterToAnalyzer(
		long id, long charFilterId) {

		return sessionFactory.withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _charFilterService.findById(charFilterId)
				.onItem()
				.ifNotNull()
				.transformToUni(charFilter ->
					s.fetch(analyzer.getCharFilters())
						.onItem()
						.ifNotNull()
						.transformToUni(charFilters -> {

							if (charFilters.add(charFilter)) {

								analyzer.setCharFilters(charFilters);

								return persist(analyzer)
									.map(newSC -> Tuple2.of(newSC, charFilter));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Analyzer, CharFilter>> removeCharFilterFromAnalyzer(
		long id, long charFilterId) {
		return sessionFactory.withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> s.fetch(analyzer.getCharFilters())
				.onItem()
				.ifNotNull()
				.transformToUni(charFilters -> {

					if (analyzer.removeCharFilter(charFilters, charFilterId)) {

						return persist(analyzer)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Analyzer, Tokenizer>> bindTokenizer(long analyzerId, long tokenizerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> _tokenizerService.findById(tokenizerId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenizer -> {
					analyzer.setTokenizer(tokenizer);
					return persist(analyzer).map(t -> Tuple2.of(t, tokenizer));
				})));
	}

	public Uni<Tuple2<Analyzer, Tokenizer>> unbindTokenizer(long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(analyzerId)
			.onItem()
			.ifNotNull()
			.transformToUni(analyzer -> {
				analyzer.setTokenizer(null);
				return persist(analyzer).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Void> load(Analyzer analyzer) {
		return sessionFactory.withTransaction(s -> {

			List<Uni<?>> unis = new ArrayList<>();

			unis.add(s.fetch(analyzer.getTokenizer()));
			unis.add(s.fetch(analyzer.getCharFilters()));
			unis.add(s.fetch(analyzer.getTokenFilters()));

			return Uni.combine().all().unis(unis).collectFailures().discardItems();

		});
	}


	@Inject
	TokenFilterService _tokenFilterService;
	@Inject
	TokenizerService _tokenizerService;
	@Inject
	CharFilterService _charFilterService;


}
