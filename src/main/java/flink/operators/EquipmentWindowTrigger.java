package org.example.operators;

import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.example.pojo.EquipmentEvent;

public class EquipmentWindowTrigger extends Trigger<EquipmentEvent, GlobalWindow> {
    @Override
    public TriggerResult onElement(EquipmentEvent element, long timestamp, GlobalWindow window, TriggerContext ctx) throws Exception {
        if (element.getEiEventType().equalsIgnoreCase("MaterialRemoved")) {
            return TriggerResult.FIRE_AND_PURGE;
        }
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, GlobalWindow window, TriggerContext ctx) throws Exception {
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, GlobalWindow window, TriggerContext ctx) throws Exception {
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(GlobalWindow window, TriggerContext ctx) throws Exception {

    }
}
