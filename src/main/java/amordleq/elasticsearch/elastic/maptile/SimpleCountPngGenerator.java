package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("simple")
@Component
public class SimpleCountPngGenerator implements PngGenerator {

    @Override
    public byte[] generatePng(GeoGrid geoGrid) {
        Long totalCount = geoGrid.getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getDocCount)
                .reduce(0L, Long::sum);
        // TODO: make a png out of total count
        return new byte[0];
    }
}
