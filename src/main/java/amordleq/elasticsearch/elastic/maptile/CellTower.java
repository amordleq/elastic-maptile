package amordleq.elasticsearch.elastic.maptile;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "cell-towers")
public class CellTower {
    String radio;
    Integer mcc;
    Integer net;
    Integer area;
    Integer cell;
    Integer range;
    String countryCode;
    String countryName;
    String operator;
    String status;
    Location location;
}
