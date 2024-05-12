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
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection-application2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put("auto.offset.reset", "earliest"); // for testing purposes only

        Instant start = Instant.now();
        // Set-up
        final StreamsBuilder builder = new StreamsBuilder();
        StoreBuilder<KeyValueStore<String, List<EquipmentEvent>>> storeBuilder1 = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore("event-store"),
                Serdes.String(),
                new ListEquipmentEventSerde()
        );

        StoreBuilder<KeyValueStore<String, RuntimeStats>> storeBuilder2 = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore("stats-store"),
                Serdes.String(),
                new RuntimeStatsSerde()
        );

        builder.addStateStore(storeBuilder1);
        builder.addStateStore(storeBuilder2);
//
//        KStream<String, String> logMessages = builder.stream("small-first", Consumed.with(Serdes.String(), Serdes.String()));
//
//
////        Serde<EquipmentEvent> equipmentEventSerde = new EquipmentEventSerde();
//
//        KStream<String, EquipmentEvent> equipmentEvents = logMessages
//                .mapValues(AnomaliesDetection::parseLogMessage);
//
//        equipmentEvents
//                .process(EventTriggeredWindowProcessor::new, "event-store", "stats-store");
//        equipmentEvents.peek((key, value) -> System.out.println("value + "+value));

       // equipmentEvents.to("small-first-out", Produced.with(new JobRecordSerde()));

//
//        KGroupedStream<String, EquipmentEvent> groupedStream = equipmentEvents
//                .groupBy((key, event) -> {
//                            String equipmentKey = event.getEquipment();
//                            System.out.println("Grouping by Equipment: " + equipmentKey);
//                            return equipmentKey;
//                        },
//                        Grouped.with(Serdes.String(), equipmentEventSerde));
//
////        groupedStream
////                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
////                .count(Materialized.as("windowed-count-store"))
////                .toStream()
////                .peek((windowedId, count) -> System.out.println("Window: " + windowedId + " Count: " + count));
//
//        KTable<String, List<EquipmentEvent>> aggregatedTable = groupedStream
//                .aggregate(
//                        ArrayList::new, // Initializer
//                        (key, value, aggregate) -> {
//                            try {
//                                if (value == null) {
//                                    System.out.println("Received null value for key: " + key);
//                                    return aggregate; // skip null values
//                                }
//
//                                // Check for specific properties if necessary
//                                if (value.getEquipment() == null || value.getTimestamp() == null) {
//                                    System.out.println("Invalid EquipmentEvent detected: " + value);
//                                    return aggregate; // skip invalid events
//                                }
//
//                                if (value.getEiEventType().equals("MaterialPlaced")) aggregate = new ArrayList<>();
//
//                                aggregate.add(value);
//                                if (value.getId().equals("10000")) System.out.println("Duration: " + Duration.between(start, Instant.now()));
//                                System.out.println("Aggregating: Key = " + key + ", Event = " + value.getId());
//                            } catch (Exception e) {
//                                System.err.println("Error during aggregation for key: " + key);
//                                e.printStackTrace(); // Log exception stack trace
//                            }
//                            return aggregate;
//                        },
//                        Materialized
////                        Materialized.<String, List<EquipmentEvent>, KeyValueStore<Bytes, byte[]>>as("events-store")
//                                .with(Serdes.String(), new ListEquipmentEventSerde())
////                                .withValueSerde(new ListEquipmentEventSerde())
//                );

//        aggregatedTable.toStream()
                //.mapValues(AnomaliesDetection::checkForAnomalies)
               // .to("small-first-out", Produced.with(Serdes.String(), new JobRecordSerde()));

        final Topology topology = new Topology();
        topology.addSource("small-topic", "huge");
        topology.addProcessor("anomaly-processor",() -> new EventTriggeredWindowProcessor(),  "small-topic"); // connect source->X
        topology.addStateStore(storeBuilder1, "anomaly-processor");
        topology.addStateStore(storeBuilder2, "anomaly-processor");
        topology.addSink("Sink1","huge-out1", Serdes.String().serializer(), new JobRecordSerde().serializer(), "anomaly-processor"); // connect X->Y
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

    private static JobRecord checkForAnomalies(List<EquipmentEvent> events) {
        JobRecord jobRecord = new JobRecord();

        jobRecord.setJobReference(events.getFirst().getJobReference());
        jobRecord.setEquipment(events.getFirst().getEquipment());

        int waferRuntimeAnomaly = 0;
        int jobRuntimeAnomaly = 0;
        int eventDeletion = eventDeletion(events);
        int eventDuplication = eventDuplication(events);

        if (events.size() > 1 && events.get(1).getEquipmentToolRecipe() != null) {
            String statsKey = events.get(1).getEquipmentType() + "-" + events.get(1).getEquipmentToolRecipe();
            RuntimeStats waferStats = runtimeStats.get(statsKey + "-wafer");
            RuntimeStats jobStats = runtimeStats.get(statsKey + "-job");
            if (waferStats == null) waferStats = new RuntimeStats();
            if (jobStats == null) jobStats = new RuntimeStats();

            ArrayList<Date> waferStarts = new ArrayList<>();
            Date jobStart = null;

            if (eventDeletion == 0 && eventDuplication == 0) { // If an event gets deleted or duplicated, runtime calculations aren't useful
                for (EquipmentEvent event : events) {
                    long runtime;
                    switch (event.getEiEventType()) {
                        case "WaferStarted":
                            waferStarts.add(event.getTimestamp());
                            break;
                        case "WaferCompleted":
                            if (waferStarts.isEmpty()) break;
                            runtime = ChronoUnit.SECONDS.between(waferStarts.getFirst().toInstant(), event.getTimestamp().toInstant());
                            waferStarts.removeFirst();
                            waferStats.update(runtime);
                            runtimeStats.put(statsKey + "-wafer", waferStats);
                            if (waferStats.isWithinBounds(runtime) && !event.getEquipmentState().equals("UnscheduledDowntimeDefault")) {
                                waferRuntimeAnomaly++;
                            }
                            break;
                        case "JobStarted":
                            jobStart = event.getTimestamp();
                            break;
                        case "JobCompleted":
                            if (jobStart == null) break;
                            runtime = ChronoUnit.SECONDS.between(jobStart.toInstant(), event.getTimestamp().toInstant());
                            jobStats.update(runtime);
                            runtimeStats.put(statsKey + "-job", jobStats);
                            if (jobStats.isWithinBounds(runtime)) {
                                jobRuntimeAnomaly++;
                            }
                            break;
                    }
                }
            }
        }



        jobRecord.setJobRuntimeAnomaly(jobRuntimeAnomaly);
        jobRecord.setWaferRuntimeAnomaly(waferRuntimeAnomaly);
        jobRecord.setEventDeletion(eventDeletion);
        jobRecord.setEventDuplication(eventDuplication);

        return jobRecord;
    }

    private static int eventDeletion(List<EquipmentEvent> events) {
        // Maps to count occurrences of each event type
        Map<String, Long> eventTypeCounts = events.stream()
                .collect(Collectors.groupingBy(EquipmentEvent::getEiEventType, Collectors.counting()));
        System.out.println("deleteion");
        System.out.println(eventTypeCounts.values());

        boolean materialPlaced = false;
        boolean jobStarted = false;
        int waferStarted = 0;
        int waferCompleted = 0;
        boolean jobCompleted = false;
        boolean materialRemoved = false;

        for (EquipmentEvent event : events) {
            switch (event.getEiEventType()) {
                case "MaterialPlaced": materialPlaced = true; break;
                case "JobStarted": jobStarted = true; break;
                case "WaferStarted": waferStarted++; break;
                case "WaferCompleted": waferCompleted++; break;
                case "JobCompleted": jobCompleted = true; break;
                case "MaterialRemoved": materialRemoved = true; break;
            }
        }

        int deletions = 0;

        if (!materialPlaced) deletions++;
        if (!jobStarted) deletions++;
        if (!jobCompleted) deletions++;
        if (!materialRemoved) deletions++;

        if (!events.getFirst().getEquipmentType().equals("CascadingWafer")) {
            return  deletions;
        } else {
            deletions += abs(waferCompleted - waferStarted);
            return deletions;
        }
    }

    private static int eventDuplication(List<EquipmentEvent> events) {
        int duplicates = 0;

        Date latestTime = null;
        String latestEventType = "";
        for (EquipmentEvent event : events) {
            if (latestTime != null) {
                if (event.getTimestamp().equals(latestTime) && event.getEiEventType().equals(latestEventType)) duplicates++;
            }
            latestTime = event.getTimestamp();
            latestEventType = event.getEiEventType();
        }

        return duplicates;
    }

    public static EquipmentEvent parseLogMessage(String message) {
        try {
            return objectMapper.readValue(message, EquipmentEvent.class);
            //return objectMapper.readTree(message);
        } catch (Exception e) {
            e.printStackTrace();
            return new EquipmentEvent();
           // return objectMapper.createObjectNode();
        }
    }
}
