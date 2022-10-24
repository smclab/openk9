package io.openk9.datasource.searcher;

import com.google.protobuf.ByteString;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.Tenant_;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.dto.ParserSearchToken;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.mapper.SearcherMapper;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
public class SearcherService implements Searcher {
    @Override
    @ActivateRequestContext
    public Uni<QueryParserResponse> queryParser(QueryParserRequest request) {

        return Uni.createFrom().deferred(() -> {

            Map<String, List<ParserSearchToken>> tokenGroup =
                request
                    .getSearchTokenList()
                    .stream()
                    .map(searcherMapper::toParserSearchToken)
                    .collect(
                        Collectors.groupingBy(ParserSearchToken::getTokenType));

            if (tokenGroup.isEmpty()) {
                return Uni
                    .createFrom()
                    .item(
                        QueryParserResponse
                            .newBuilder()
                            .setQuery(ByteString.EMPTY)
                            .build()
                    );
            }

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            return sf
                .withTransaction(s -> _getTenantAndFetchRelations(s, request.getTenantId()))
                .map(tenant -> {

                    for (Map.Entry<String, List<ParserSearchToken>> entry : tokenGroup.entrySet()) {
                        String tokenType = entry.getKey();
                        List<ParserSearchToken> parserSearchTokens = entry.getValue();
                        for (QueryParser queryParser : queryParserInstance) {
                            if (queryParser.isQueryParserGroup() && queryParser.getType().equals(tokenType)) {
                                queryParser.accept(
                                    ParserContext
                                        .builder()
                                        .tokenTypeGroup(parserSearchTokens)
                                        .mutableQuery(boolQueryBuilder)
                                        .currentTenant(tenant)
                                        .build()
                                );
                            }
                        }
                    }

                    List<ParserSearchToken> parserSearchTokens = null;

                    for (QueryParser queryParser : queryParserInstance) {
                        if (!queryParser.isQueryParserGroup()) {

                            if (parserSearchTokens == null) {
                                parserSearchTokens = tokenGroup
                                    .values()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .toList();
                            }

                            queryParser.accept(
                                ParserContext
                                    .builder()
                                    .tokenTypeGroup(parserSearchTokens)
                                    .mutableQuery(boolQueryBuilder)
                                    .currentTenant(tenant)
                                    .build()
                            );

                        }
                    }

                    return QueryParserResponse
                        .newBuilder()
                        .setQuery(ByteString.copyFromUtf8(boolQueryBuilder.toString()))
                        .build();

                });

        });


    }

    private Uni<Tenant> _getTenantAndFetchRelations(
        Mutiny.Session s, long tenantId) {

        return Uni.createFrom().deferred(() -> {

            CriteriaBuilder criteriaBuilder = sf.getCriteriaBuilder();

            CriteriaQuery<Tenant> criteriaQuery = criteriaBuilder.createQuery(Tenant.class);

            Root<Tenant> tenantRoot = criteriaQuery.from(Tenant.class);

            Fetch<Tenant, Datasource> datasourceRoot =
                tenantRoot.fetch(Tenant_.datasources);

            Fetch<Datasource, DataIndex> dataIndexRoot =
                datasourceRoot.fetch(Datasource_.dataIndex);

            Fetch<DataIndex, DocType> docTypeFetch =
                dataIndexRoot.fetch(DataIndex_.docTypes);

            docTypeFetch.fetch(DocType_.docTypeFields);

            criteriaQuery.where(
                criteriaBuilder.equal(tenantRoot.get(Tenant_.id), tenantId)
            );

            criteriaQuery.distinct(true);

            return s.createQuery(criteriaQuery).getSingleResult();

        });

    }

    @Inject
    Instance<QueryParser> queryParserInstance;

    @Inject
    SearcherMapper searcherMapper;

    @Inject
    Mutiny.SessionFactory sf;

}