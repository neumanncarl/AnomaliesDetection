package org.example.pojo;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class Anomaly implements Serializable {

    private String eiEventJobId;
    private String equipment;
    private String equipmentType;
    private String equipmentToolRecipe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date earliestTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date nextJobStart;

    private int eventCount;

    private int sequenceAnomaly;

    private int jobDuration;
    private int jobRuntimeAnomaly;
    private int waferCount;
    private int waferRuntimeAnomaly;

    public Anomaly (){
        this(null,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                0);
    }

    public Anomaly(String eiEventJobId,
                   String equipment,
                   String equipmentType,
                   String equipmentToolRecipe,
                   Date earliestTimestamp,
                   Date nextJobStart,
                   int eventCount,
                   int sequenceAnomaly,
                   int jobDuration,
                   int jobRuntimeAnomaly,
                   int waferCount,
                   int waferRuntimeAnomaly) {
        this.eiEventJobId = eiEventJobId;
        this.equipment = equipment;
        this.equipmentType = equipmentType;
        this.equipmentToolRecipe = equipmentToolRecipe;
        this.earliestTimestamp = earliestTimestamp;
        this.nextJobStart = nextJobStart;
        this.eventCount = eventCount;
        this.sequenceAnomaly = sequenceAnomaly;
        this.jobDuration = jobDuration;
        this.jobRuntimeAnomaly = jobRuntimeAnomaly;
        this.waferCount = waferCount;
        this.waferRuntimeAnomaly = waferRuntimeAnomaly;
    }

    public String getEiEventJobId() {
        return eiEventJobId;
    }

    public void setEiEventJobId(String eiEventJobId) {
        this.eiEventJobId = eiEventJobId;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getEquipmentToolRecipe() {
        return equipmentToolRecipe;
    }

    public void setEquipmentToolRecipe(String equipmentToolRecipe) {
        this.equipmentToolRecipe = equipmentToolRecipe;
    }

    public Date getEarliestTimestamp() {
        return earliestTimestamp;
    }

    public void setEarliestTimestamp(Date earliestTimestamp) {
        this.earliestTimestamp = earliestTimestamp;
    }

    public Date getNextJobStart() {
        return nextJobStart;
    }

    public void setNextJobStart(Date nextJobStart) {
        this.nextJobStart = nextJobStart;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public int getSequenceAnomaly() {
        return sequenceAnomaly;
    }

    public void setSequenceAnomaly(int sequenceAnomaly) {
        this.sequenceAnomaly = sequenceAnomaly;
    }

    public int getJobDuration() {
        return jobDuration;
    }

    public void setJobDuration(int jobDuration) {
        this.jobDuration = jobDuration;
    }

    public int getJobRuntimeAnomaly() {
        return jobRuntimeAnomaly;
    }

    public void setJobRuntimeAnomaly(int jobRuntimeAnomaly) {
        this.jobRuntimeAnomaly = jobRuntimeAnomaly;
    }

    public int getWaferCount() {
        return waferCount;
    }

    public void setWaferCount(int waferCount) {
        this.waferCount = waferCount;
    }

    public int getWaferRuntimeAnomaly() {
        return waferRuntimeAnomaly;
    }

    public void setWaferRuntimeAnomaly(int waferRuntimeAnomaly) {
        this.waferRuntimeAnomaly = waferRuntimeAnomaly;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Anomaly{");
        sb.append("eiEventJobId='").append(eiEventJobId).append('\'');
        sb.append(", equipment='").append(equipment).append('\'');
        sb.append(", equipmentType='").append(equipmentType).append('\'');
        sb.append(", equipmentToolRecipe='").append(equipmentToolRecipe).append('\'');
        sb.append(", earliestTimestamp=").append(earliestTimestamp);
        sb.append(", nextJobStart=").append(nextJobStart);
        sb.append(", eventCount=").append(eventCount);
        sb.append(", sequenceAnomaly=").append(sequenceAnomaly);
        sb.append(", jobDuration=").append(jobDuration);
        sb.append(", jobRuntimeAnomaly=").append(jobRuntimeAnomaly);
        sb.append(", waferCount=").append(waferCount);
        sb.append(", waferRuntimeAnomaly=").append(waferRuntimeAnomaly);
        sb.append('}');
        return sb.toString();
    }
}
