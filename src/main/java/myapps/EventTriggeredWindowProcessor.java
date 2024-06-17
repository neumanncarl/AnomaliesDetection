package myapps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.abs;

public class EventTriggeredWindowProcessor extends AbstractProcessor<String, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Map<String, RuntimeStats> statsStore;
    private Map<String, List<EquipmentEvent>> eventMap;
    private Instant startT = Instant.now();

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        super.init(context);
        statsStore = new HashMap<>();
        eventMap = new HashMap<>();
        startT = Instant.now();
    }

    @Override
    public void process(String key, String eventString) {
        EquipmentEvent event = parseLogMessage(eventString);
        if (event.getId().equals("1000000")) System.out.println("Time: " + Duration.between(startT, Instant.now()));
        // Check if the store has the key
        List<EquipmentEvent> events = eventMap.get(event.getEquipment());
        if (events == null) {
            events = new ArrayList<>();
        }

        // Add event to the list
        events.add(event);
        eventMap.put(event.getEquipment(), events);

        // Check for the trigger condition
        if (event.getEiEventType().equals("MaterialRemoved")) {
            // Trigger condition met, process the aggregated events
            JobRecord jobRecord = checkForAnomalies(events);
            context.forward(key, jobRecord);

            // Optionally, clear the events list after processing
            eventMap.put(event.getEquipment(), new ArrayList<>());
        }
    }


    private JobRecord checkForAnomalies(List<EquipmentEvent> events) {
        JobRecord jobRecord = new JobRecord();

        jobRecord.setJobReference(events.getFirst().getJobReference());
        jobRecord.setEquipment(events.getFirst().getEquipment());

        int waferRuntimeAnomaly = 0;
        int jobRuntimeAnomaly = 0;
        int eventDeletion = eventDeletion(events);
        int eventDuplication = eventDuplication(events);

        if (events.size() > 1 && events.get(1).getEquipmentToolRecipe() != null) {
            String statsKey = events.get(1).getEquipmentType() + "-" + events.get(1).getEquipmentToolRecipe();
            RuntimeStats waferStats = statsStore.get(statsKey + "-wafer");
            RuntimeStats jobStats = statsStore.get(statsKey + "-job");
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
                            statsStore.put(statsKey + "-wafer", waferStats);
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
                            statsStore.put(statsKey + "-job", jobStats);
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

    private int eventDeletion(List<EquipmentEvent> events) {
        // Maps to count occurrences of each event type
        int materialPlaced = 0;
        int jobStarted = 0;
        int waferStarted = 0;
        int waferCompleted = 0;
        int jobCompleted = 0;
        int materialRemoved = 0;

        for (EquipmentEvent event : events) {
            switch (event.getEiEventType()) {
                case "MaterialPlaced": materialPlaced++; break;
                case "JobStarted": jobStarted++; break;
                case "WaferStarted": waferStarted++; break;
                case "WaferCompleted": waferCompleted++; break;
                case "JobCompleted": jobCompleted++; break;
                case "MaterialRemoved": materialRemoved++; break;
            }
        }

        int deletions = 0;

        if (materialPlaced == 0 || materialPlaced > 1) deletions++;
        if (jobStarted == 0 || jobStarted > 1) deletions++;
        if (jobCompleted == 0 || jobCompleted > 1) deletions++;
        if (materialRemoved == 0 || materialRemoved > 1) deletions++;

        if (!events.getFirst().getEquipmentType().equals("CascadingWafer")) {
            return deletions;
        } else {
            deletions += abs(waferCompleted - waferStarted);
            return deletions;
        }
    }

    private int eventDuplication(List<EquipmentEvent> events) {
        int duplicates = 0;

        Date latestTime = null;
        String latestEventType = "";
        for (EquipmentEvent event : events) {
            if (event.getTimestamp().equals(latestTime) && event.getEiEventType().equals(latestEventType)) duplicates++;
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

    @Override
    public void close() {
        // Optional cleanup logic
    }
}
