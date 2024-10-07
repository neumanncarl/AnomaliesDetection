package org.example.operators;

import org.apache.flink.api.java.functions.KeySelector;
import org.example.pojo.EquipmentEvent;

public class JobIdEquipmentSelector implements KeySelector<EquipmentEvent, String> {
    @Override
    public String getKey(EquipmentEvent value) throws Exception {
        String key = String.format("{}-{}", value.getEiEventJobId(), value.getEquipment());
        return key;
    }
}
