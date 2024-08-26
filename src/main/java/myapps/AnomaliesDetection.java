package myapps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class AnomaliesDetection {
    private static final Map<String, RuntimeStats> runtimeStats = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    public static void main(String[] args) throws IOException, SQLException {

        // Config
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put("auto.offset.reset", "earliest"); // for testing purposes only

        final Topology topology = new Topology();
        topology.addSource("small-topic", "all");
        topology.addProcessor("anomaly-processor",() -> new EventTriggeredWindowProcessor(),  "small-topic");
        topology.addSink("Sink1","huge-dump-out13", Serdes.String().serializer(), new JobRecordSerde().serializer(), "anomaly-processor");
        try (KafkaStreams streams = new KafkaStreams(topology, props)) {
            streams.setStateListener((newState, oldState) -> {
                System.out.println("State changed from " + oldState + " to " + newState);
            });
            streams.setUncaughtExceptionHandler(e -> {
                System.out.println(e.getMessage());
                return null;
            });
            final CountDownLatch latch = new CountDownLatch(1);

            Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
                @Override
                public void run() {
                    streams.close();
                    latch.countDown();
                }
            });

            try {
                streams.start();
                latch.await();
            } catch (Throwable e) {
                System.exit(1);
            }
        } catch (Throwable e) {
            System.err.println("Exception in thread " + e);
        }

        System.exit(0);
    }
}
