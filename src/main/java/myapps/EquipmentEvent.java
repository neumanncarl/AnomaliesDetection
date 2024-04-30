package myapps;

import java.time.LocalDateTime;

public class EquipmentEvent {

    private String jobReference;
    private LocalDateTime timestamp;
    private String area;
    private String equipment;
    private String equipmentType;
    private String equipmentState;
    private String equipmentToolRecipe;
    private String eiEventType;

    public EquipmentEvent(String jr, LocalDateTime ts, String a, String e, String et, String es, String etr, String eet) {
        area = a;
        equipment = e;
        equipmentType = et;
        equipmentState = es;
        equipmentToolRecipe = etr;
        jobReference = jr;
        timestamp = ts;
        eiEventType = eet;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public String getEquipmentToolRecipe() {
        return equipmentToolRecipe;
    }

    public String getJobReference() {
        return jobReference;
    }

    public String getEiEventType() {
        return eiEventType;
    }

    public String getEquipment() {
        return equipment;
    }

    public String getEquipmentState() {
        return equipmentState;
    }

    @Override
    public String toString() {
        return "{" +
                "jobReference='" + jobReference + '\'' +
                ", timestamp=" + timestamp +
                ", area='" + area + '\'' +
                ", equipment='" + equipment + '\'' +
                ", equipmentType='" + equipmentType + '\'' +
                ", equipmentState='" + equipmentState + '\'' +
                ", equipmentToolRecipe='" + equipmentToolRecipe + '\'' +
                ", eiEventType='" + eiEventType + '\'' +
                '}';
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
