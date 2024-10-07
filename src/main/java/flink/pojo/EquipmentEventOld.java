package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

//@JsonPropertyOrder({"timestamp", "EQName", "EQState", "EQType", "EQGroup", "EQArea", "eventType", "eventJobId"})
@JsonIgnoreProperties(ignoreUnknown = true)

public class EquipmentEventOld implements Serializable {

    private long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date insertTime;

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


    public EquipmentEventOld(){
        this(0, null, null, null, null, null, null, null, null, null, null, 0);
    }
    public EquipmentEventOld(long id,
                          Date timestamp,
                          Date insertTime,
                          String area,
                          String equipment,
                          String equipmentToolRecipe,
                          String equipmentState,
                          String equipmentType,
                          String equipmentGroup,
                          String eiEventType,
                          String eiEventJobId,
                          long jobReference) {
        this.id = id;
        this.timestamp = timestamp;
        this.insertTime = insertTime;
        this.area = area;
        this.equipment = equipment;
        this.equipmentToolRecipe = equipmentToolRecipe;
        this.equipmentState = equipmentState;
        this.equipmentType = equipmentType;
        this.equipmentGroup = equipmentGroup;
        this.eiEventType = eiEventType;
        this.eiEventJobId = eiEventJobId;
        this.jobReference = jobReference;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EIEvent{");
        sb.append("id=").append(id);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", insertTime=").append(insertTime);
        sb.append(", area='").append(area).append('\'');
        sb.append(", equipment='").append(equipment).append('\'');
        sb.append(", equipmentToolRecipe='").append(equipmentToolRecipe).append('\'');
        sb.append(", equipmentState='").append(equipmentState).append('\'');
        sb.append(", equipmentType='").append(equipmentType).append('\'');
        sb.append(", equipmentGroup='").append(equipmentGroup).append('\'');
        sb.append(", eiEventType='").append(eiEventType).append('\'');
        sb.append(", eiEventJobId='").append(eiEventJobId).append('\'');
        sb.append(", jobReference=").append(jobReference);
        sb.append('}');
        return sb.toString();
    }
}
