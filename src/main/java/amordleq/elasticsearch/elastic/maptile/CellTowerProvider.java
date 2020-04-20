package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
public class CellTowerProvider {

    @Autowired
    private ReactiveElasticsearchTemplate elasticsearchTemplate;

    public Flux<CellTower> findNear(double latitude, double longitude, String distance) {
        return findNear(latitude, longitude, distance, null);
    }

    public Flux<CellTower> findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter) {
        return findNear(latitude, longitude, distance, additionalFilter, -1);
    }

    public Flux<CellTower>  findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter, Integer maximumNumberOfResults) {
        BoolQueryBuilder searchQuery = QueryBuilders.boolQuery()
                .filter(geoDistanceQuery(latitude, longitude, distance));
        if (additionalFilter != null) {
            searchQuery.filter(additionalFilter);
        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(searchQuery);
        if (maximumNumberOfResults != null && maximumNumberOfResults > 0) {
            queryBuilder.withPageable(PageRequest.of(0, maximumNumberOfResults)); // FIXME: use query.setMaxResults() in future when available
        }

        return elasticsearchTemplate.find(queryBuilder.build(), CellTower.class)
                .retryBackoff(2, Duration.ofMillis(250));
    }

    private GeoDistanceQueryBuilder geoDistanceQuery(double latitude, double longitude, String distance) {
        return QueryBuilders.geoDistanceQuery("location").point(latitude, longitude).distance(distance);
    }
}
