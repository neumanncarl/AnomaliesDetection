package org.example.operators;

import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.example.pojo.EquipmentEvent;

public class EquipmentWatermarkGeneratorSupplier implements WatermarkGeneratorSupplier<EquipmentEvent> {
    @Override
    public WatermarkGenerator<EquipmentEvent> createWatermarkGenerator(Context context) {
        return new EquipmentWatermarkGenerator();
    }
}
