package org.example.operators;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import org.example.pojo.Anomaly;
import org.example.pojo.EquipmentEvent;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EquipmentWindowProcess extends ProcessWindowFunction<EquipmentEvent, Anomaly, String, GlobalWindow> {

    @Override
    public void process(String s, ProcessWindowFunction<EquipmentEvent, Anomaly, String, GlobalWindow>.Context context, Iterable<EquipmentEvent> elements, Collector<Anomaly> out) throws Exception {
        Anomaly anomaly = new Anomaly();
        Iterator<EquipmentEvent> iter = elements.iterator();
        EquipmentEvent equipment = iter.next();

        anomaly.setEquipmentType(equipment.getEquipmentType());
        anomaly.setEquipmentToolRecipe(equipment.getEquipmentToolRecipe());
        anomaly.setEarliestTimestamp(equipment.getTimestamp());

        for (; iter.hasNext();){
//            EquipmentEvent event = iterator.next();
            try {
                EquipmentEvent event1 = iter.next();
                anomaly.setEiEventJobId(event1.getEiEventJobId());

                if (equipment.getEquipmentType().equalsIgnoreCase("CascadingWafer")){
                    long waferStarted = 0;
                    long waferCompleted = 0;
                    while (iter.hasNext()){
                        EquipmentEvent tempEvent = iter.next();
                        if (tempEvent.getEiEventType().equalsIgnoreCase("JobCompleted")){
                            break;
                        }
                        if (tempEvent.getEiEventType().equalsIgnoreCase("WaferStarted")) {
                            waferStarted++;
                        } else if (tempEvent.getEiEventType().equalsIgnoreCase("WaferCompleted")) {
                            waferCompleted++;
                        }

                    }


                    EquipmentEvent event3 = iter.next();
                    if (equipment.getEiEventType().equalsIgnoreCase("MaterialPlaced") &&
                            event1.getEiEventType().equalsIgnoreCase("JobStarted") &&
                            event3.getEiEventType().equalsIgnoreCase("MaterialRemoved") &&
                            waferStarted == waferCompleted
                    ){
                        // correct
//                        System.out.println("correct");
                    } else {
                        anomaly.setJobRuntimeAnomaly(anomaly.getJobRuntimeAnomaly()+1);
                    }


                } else {

                    try {
                        // get the next three elements
                        EquipmentEvent event2 = iter.next();
                        EquipmentEvent event3 = iter.next();
//                EquipmentEvent equipment4 = iterator.next();

                        // check the order
                        if (
                                equipment.getEiEventType().equalsIgnoreCase("MaterialPlaced") &&
                                event1.getEiEventType().equalsIgnoreCase("JobStarted") &&
                                event2.getEiEventType().equalsIgnoreCase("JobCompleted") &&
                                event3.getEiEventType().equalsIgnoreCase("MaterialRemoved")
                        ) {

                        } else {
                            // anomaly

                            anomaly.setJobRuntimeAnomaly(anomaly.getJobRuntimeAnomaly()+1);

                        }
                        if (iter.hasNext()){
                            // duplication

                            anomaly.setSequenceAnomaly(anomaly.getSequenceAnomaly()+1);
                        }
                    } catch (NoSuchElementException e){
                        // event deletion

                        anomaly.setSequenceAnomaly(anomaly.getSequenceAnomaly()+1);
                    }


                }
            } catch (NoSuchElementException e){

                anomaly.setSequenceAnomaly(anomaly.getSequenceAnomaly()+1);
            }
        }

        out.collect(anomaly);

    }
}
