package org.example.operators;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.example.pojo.EquipmentEvent;

public class EquipmentWatermarkGenerator implements WatermarkGenerator<EquipmentEvent> {
    @Override
    public void onEvent(EquipmentEvent event, long eventTimestamp, WatermarkOutput output) {

        if (event.getEiEventType().equalsIgnoreCase("MaterialRemoved")) {
            output.emitWatermark(new Watermark(eventTimestamp));
        }

    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {

    }
}
