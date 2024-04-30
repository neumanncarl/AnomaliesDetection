package myapps;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static myapps.AnomaliesDetection.parseLogMessage;
import static myapps.AnomaliesDetection.parseToEquipment;

public class AnomalyDetectionTransformer implements Transformer<String, String, KeyValue<String, Job>> {
    private final Map<String, RuntimeStats> statsStore = new HashMap<>();
    private final Map<String, Job> jobStore = new HashMap<>();

    private LocalDateTime startT;

    @Override
    public void init(ProcessorContext context) {
        startT = LocalDateTime.now();
    }

    @Override
    public KeyValue<String, Job> transform(String key, String value) {
       // if (startT == null) startT = java.time.LocalDateTime.now();
        EquipmentEvent event = deserializeEvent(value);

       // if (event.getJobReference().equals("2555113")) System.out.println("Runtime: " + ChronoUnit.MILLIS.between(java.time.LocalDateTime.now(), startT));
       // if (event.getJobReference().equals("525")) System.out.println(event);
        System.out.println(event);
        String statsKey = event.getEquipmentType() + "-" + event.getEquipmentToolRecipe();
        if (event.getEiEventType().equals("JobCompleted")) statsKey += "-job";
        if (event.getEiEventType().equals("WaferCompleted")) statsKey += "-wafer";
        RuntimeStats stats = statsStore.get(statsKey);
        if (stats == null) stats = new RuntimeStats();

        String equipmentId = event.getEquipment();

        Job job;
        if (event.getEiEventType().equals("MaterialPlaced")) {
            // Start a new job
            job = new Job(event);
            jobStore.put(equipmentId, job);
        } else if (event.getEiEventType().equals("MaterialRemoved")) {
            // End and remove the job
            job = jobStore.get(equipmentId);
            if (job != null) {
                job.update(event, stats, statsStore, statsKey);
            }
            jobStore.remove(equipmentId);
        } else {
            // Associate event with existing job
            job = jobStore.get(equipmentId);
            if (job != null) {
                job.update(event, stats, statsStore, statsKey);
                jobStore.put(equipmentId, job); // Update the job with the new event
            }
        }

        return KeyValue.pair(event.getJobReference(), job);
    }

    @Override
    public void close() {
    }

    private EquipmentEvent deserializeEvent(String value) {
       return parseToEquipment(parseLogMessage(value));
    }
}

