package amordleq.elasticsearch.elastic.maptile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class MapTileApplication {

	public static void main(String[] args) {
		Schedulers.enableMetrics();
		SpringApplication.run(MapTileApplication.class, args);
	}

}
