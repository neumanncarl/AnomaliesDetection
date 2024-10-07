package org.example.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.example.pojo.EquipmentEvent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EquipmentEventDeserializer implements DeserializationSchema<EquipmentEvent> {
    ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public EquipmentEvent deserialize(byte[] message) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(message);

            String id = node.get("id").asText();
            String timestampStr = node.get("timestamp").asText();
            String inserttimeStr = node.get("inserttime").asText();
            String area = node.get("area").asText();
            String equipment = node.get("equipment").asText();
            String equipmenttoolrecipe = node.get("equipmenttoolrecipe").asText(null);
            String equipmentstate = node.get("equipmentstate").asText();
            String equipmenttype = node.get("equipmenttype").asText();
            String equipmentgroup = node.get("equipmentgroup").asText();
            String eieventtype = node.get("eieventtype").asText();
            String eieventjobid = node.get("eieventjobid").asText(null);
            String jobreferenceStr = node.get("jobreference").asText(null);

            Date timestamp = parseDate(timestampStr);
            Date inserttime = parseDate(inserttimeStr);

            Long jobreference = null;
            if (jobreferenceStr != null && !jobreferenceStr.trim().isEmpty()) {
                try {
                    jobreference = Long.parseLong(jobreferenceStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid jobreference value: " + jobreferenceStr);
                    jobreference = 0L; // Default value (optional)
                }
            }


            EquipmentEvent event = new EquipmentEvent();
            event.setId(id);
            event.setTimestamp(timestamp);
            event.setInserttime(inserttime);
            event.setArea(area);
            event.setEquipment(equipment);
            event.setEquipmentToolRecipe(equipmenttoolrecipe);
            event.setEquipmentState(equipmentstate);
            event.setEquipmentType(equipmenttype);
            event.setEquipmentGroup(equipmentgroup);
            event.setEiEventType(eieventtype);
            event.setEiEventJobId(eieventjobid);
            event.setJobReference(jobreference);

            return event;

        } catch (JsonProcessingException e) {
            throw new IOException("Failed to deserialize JSON", e);
        }
    }

    private Date parseDate(String dateStr) throws IOException {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new IOException("Failed to parse date: " + dateStr, e);
        }
    }


    @Override
    public boolean isEndOfStream(EquipmentEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<EquipmentEvent> getProducedType() {
        return TypeInformation.of(EquipmentEvent.class);
    }
}
