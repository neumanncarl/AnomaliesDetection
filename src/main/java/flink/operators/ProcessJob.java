package org.example.operators;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.example.pojo.Anomaly;
import org.example.pojo.EquipmentEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessJob extends KeyedProcessFunction<Long, EquipmentEvent, Anomaly> {
    Map<Long, Anomaly> jobAnomalyMap = new HashMap<>();
    Map<Long, ArrayList<String>> jobEventtype = new HashMap<>();
    @Override
    public void processElement(EquipmentEvent value, KeyedProcessFunction<Long, EquipmentEvent, Anomaly>.Context ctx, Collector<Anomaly> out) throws Exception {
        Long jobRef = ctx.getCurrentKey();
        EquipmentEvent rcvdEvent = value;
        ArrayList<String> eventtypesPerJob = jobEventtype.getOrDefault(jobRef, new ArrayList<String>());
        eventtypesPerJob.add(rcvdEvent.getEiEventType().toLowerCase());
        jobEventtype.put(jobRef, eventtypesPerJob);
        Anomaly anomaly = new Anomaly();
        anomaly.setEiEventJobId(jobRef.toString());
        anomaly.setEquipmentType(rcvdEvent.getEquipmentType());

        if (rcvdEvent.getEquipmentType().equalsIgnoreCase("CascadingWafer")){
            if (eventtypesPerJob.size() >= 6 ) {
                if (eventtypesPerJob.containsAll(List.of(new String[]{"materialplaced", "jobstarted", "jobcompleted", "materialremoved", "waferstarted", "wafercompleted"}))){
//                    if (eventtypesPerJob.get(0).equalsIgnoreCase("materialplaced") && eventtypesPerJob.get(0).equalsIgnoreCase("materialremoved")){
                       anomaly.setSequenceAnomaly(0);

//                    }

                } else{
                    anomaly.setSequenceAnomaly(1);
                }
            } else {
                int missing = 6 - eventtypesPerJob.size();
                anomaly.setSequenceAnomaly(missing);
            }

        } else if (rcvdEvent.getEquipmentType().equalsIgnoreCase("SingleProc") || rcvdEvent.getEquipmentType().equalsIgnoreCase("Batching") || rcvdEvent.getEquipmentType().equalsIgnoreCase("CascadingLot")){
            if (eventtypesPerJob.size() == 4 ) {
                if (eventtypesPerJob.containsAll(List.of(new String[]{"materialplaced", "jobstarted", "jobcompleted", "materialremoved"}))){
                    anomaly.setSequenceAnomaly(0);
                } else {
                    int missing = 4 - eventtypesPerJob.size();
                    anomaly.setSequenceAnomaly(missing);
                }
            } else {
//                anomaly.setSequenceAnomaly();
            }

        }
//        else {
//            System.out.println(String.format("***************** Found EquipmentType: {} *****************", rcvdEvent.getEquipmentType()));
//        }
        out.collect(anomaly);
    }
}
