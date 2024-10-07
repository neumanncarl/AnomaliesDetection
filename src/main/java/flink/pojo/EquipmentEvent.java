package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

//public class EquipmentEvent {
//
//    private String jobReference;
//    private LocalDateTime timestamp;
//    private String area;
//    private String equipment;
//    private String equipmentType;
//    private String equipmentState;
//    private String equipmentToolRecipe;
//    private String eiEventType;
//
//    public EquipmentEvent(String jr, LocalDateTime ts, String a, String e, String et, String es, String etr, String eet) {
//        area = a;
//        equipment = e;
//        equipmentType = et;
//        equipmentState = es;
//        equipmentToolRecipe = etr;
//        jobReference = jr;
//        timestamp = ts;
//        eiEventType = eet;
//    }
//
//    public String getEquipmentType() {
//        return equipmentType;
//    }
//
//    public String getEquipmentToolRecipe() {
//        return equipmentToolRecipe;
//    }
//
//    public String getJobReference() {
//        return jobReference;
//    }
//
//    public String getEiEventType() {
//        return eiEventType;
//    }
//
//    public String getEquipment() {
//        return equipment;
//    }
//
//    public String getEquipmentState() {
//        return equipmentState;
//    }
//
//    @Override
//    public String toString() {
//        return "{" +
//                "jobReference='" + jobReference + '\'' +
//                ", timestamp=" + timestamp +
//                ", area='" + area + '\'' +
//                ", equipment='" + equipment + '\'' +
//                ", equipmentType='" + equipmentType + '\'' +
//                ", equipmentState='" + equipmentState + '\'' +
//                ", equipmentToolRecipe='" + equipmentToolRecipe + '\'' +
//                ", eiEventType='" + eiEventType + '\'' +
//                '}';
//    }
//
//    public LocalDateTime getTimestamp() {
//        return timestamp;
//    }
//}

public class EquipmentEvent implements Serializable {
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inserttime;

    private String area;
    private String equipment;

    @JsonProperty("equipmenttoolrecipe")
    private String equipmentToolRecipe;

    @JsonProperty("equipmentstate")
    private String equipmentState;

    @JsonProperty("equipmenttype")
    private String equipmentType;

    @JsonProperty("equipmentgroup")
    private String equipmentGroup;

    @JsonProperty("eieventtype")
    private String eiEventType;

    @JsonProperty("eieventjobid")
    private String eiEventJobId;

    @JsonProperty("jobreference")
    private long jobReference;

    private JobRecord jobRecord;

    // Getters and setters
    // Add getters and setters for all fields here...
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getInserttime() {
        return inserttime;
    }

    public void setInserttime(Date inserttime) {
        this.inserttime = inserttime;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getEquipmentToolRecipe() {
        return equipmentToolRecipe;
    }

    public void setEquipmentToolRecipe(String equipmentToolRecipe) {
        this.equipmentToolRecipe = equipmentToolRecipe;
    }

    public String getEquipmentState() {
        return equipmentState;
    }

    public void setEquipmentState(String equipmentState) {
        this.equipmentState = equipmentState;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getEquipmentGroup() {
        return equipmentGroup;
    }

    public void setEquipmentGroup(String equipmentGroup) {
        this.equipmentGroup = equipmentGroup;
    }

    public String getEiEventType() {
        return eiEventType;
    }

    public void setEiEventType(String eiEventType) {
        this.eiEventType = eiEventType;
    }

    public String getEiEventJobId() {
        return eiEventJobId;
    }

    public void setEiEventJobId(String eiEventJobId) {
        this.eiEventJobId = eiEventJobId;
    }

    public long getJobReference() {
        return jobReference;
    }

    public void setJobReference(long jobReference) {
        this.jobReference = jobReference;
    }

    public JobRecord getJobRecord() {
        return jobRecord;
    }

    public void setJobRecord(JobRecord jobRecord) {
        this.jobRecord = jobRecord;
    }

    public String toJobRecord() {
        return jobRecord.toString();
    }

    @Override
    public String toString() {
        return "EquipmentEvent{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", inserttime=" + inserttime +
                ", area='" + area + '\'' +
                ", equipment='" + equipment + '\'' +
                ", equipmentToolRecipe='" + equipmentToolRecipe + '\'' +
                ", equipmentState='" + equipmentState + '\'' +
                ", equipmentType='" + equipmentType + '\'' +
                ", equipmentGroup='" + equipmentGroup + '\'' +
                ", eiEventType='" + eiEventType + '\'' +
                ", eiEventJobId='" + eiEventJobId + '\'' +
                ", jobReference='" + jobReference + '\'' +
                '}';
    }
}