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

public class AnomalyDetectionTransformer implements Transformer<String, String, KeyValue<String, JobSequenceInfo>> {
    //private LocalDateTime startT;
    private final Map<String, JobSequenceInfo> jobSequenceInfoStore = new HashMap<>();
    private final Map<String, RuntimeStats> runtimeStatsStore = new HashMap<>();

    @Override
    public void init(ProcessorContext context) {

    }

    @Override
    public KeyValue<String, JobSequenceInfo> transform(String key, String value) {
        //if (startT == null) startT = java.time.LocalDateTime.now();
        EquipmentEvent event = deserializeEvent(value);
        System.out.println(event);
        //if (event.getJobReference().equals("440")) System.out.println("Runtime: " + ChronoUnit.MILLIS.between(java.time.LocalDateTime.now(), startT));

        JobSequenceInfo jobSequenceInfo = jobSequenceInfoStore.get(event.getJobReference());
        if (jobSequenceInfo == null) jobSequenceInfo = new JobSequenceInfo(event.getJobReference(), event.getEquipmentType(), event.getEquiptment());

        String statsKey = event.getEquipmentType() + "-" + event.getEquipmentToolRecipe();
        if (event.getEiEventType().equals("JobCompleted")) statsKey += "-job";
        if (event.getEiEventType().equals("WaferCompleted")) statsKey += "-wafer";
        RuntimeStats runtimeStats = runtimeStatsStore.get(statsKey);
        if (runtimeStats == null) runtimeStats = new RuntimeStats();

        jobSequenceInfo.updateFromEvent(event, runtimeStats, runtimeStatsStore, statsKey);
        jobSequenceInfoStore.put(event.getJobReference(), jobSequenceInfo);

        return KeyValue.pair(event.getJobReference(), jobSequenceInfo);
    }

    @Override
    public void close() {
    }

    private EquipmentEvent deserializeEvent(String value) {
       return parseToEquipment(parseLogMessage(value));
    }
}

