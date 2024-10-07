package org.example.operators;

import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.example.pojo.EquipmentEvent;

import java.util.Iterator;

public class EquipmentWindowEvictor implements Evictor<EquipmentEvent, GlobalWindow> {
    @Override
    public void evictBefore(Iterable<TimestampedValue<EquipmentEvent>> elements, int size, GlobalWindow window, EvictorContext evictorContext) {

    }

    @Override
    public void evictAfter(Iterable<TimestampedValue<EquipmentEvent>> elements, int size, GlobalWindow window, EvictorContext evictorContext) {
        for (Iterator<TimestampedValue<EquipmentEvent>> iterator = elements.iterator(); iterator.hasNext(); ) {
            TimestampedValue<EquipmentEvent> equipmentEvent = iterator.next();
            Long eventTS = equipmentEvent.getTimestamp();
            if (eventTS < evictorContext.getCurrentWatermark()){
                iterator.remove();
            }
        }

    }
}
