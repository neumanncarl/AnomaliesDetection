package myapps;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class JobRecord implements Serializable {

    private long id;

    private long jobReference;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date timestamp;

    private String equipment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date insertTime;

    private int jobRuntimeAnomaly;

    private int waferRuntimeAnomaly;

    private int eventDeletion;

    private int eventDuplication;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJobReference() {
        return jobReference;
    }

    public void setJobReference(long jobReference) {
        this.jobReference = jobReference;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public int getJobRuntimeAnomaly() {
        return jobRuntimeAnomaly;
    }

    public void setJobRuntimeAnomaly(int jobRuntimeAnomaly) {
        this.jobRuntimeAnomaly = jobRuntimeAnomaly;
    }

    public int getWaferRuntimeAnomaly() {
        return waferRuntimeAnomaly;
    }

    public void setWaferRuntimeAnomaly(int waferRuntimeAnomaly) {
        this.waferRuntimeAnomaly = waferRuntimeAnomaly;
    }

    public int getEventDeletion() {
        return eventDeletion;
    }

    public void setEventDeletion(int eventDeletion) {
        this.eventDeletion = eventDeletion;
    }

    public int getEventDuplication() {
        return eventDuplication;
    }

    public void setEventDuplication(int eventDuplication) {
        this.eventDuplication = eventDuplication;
    }

    public JobRecord() {}


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JobRecord{");
        sb.append("id=").append(id);
        sb.append(", jobReference=").append(jobReference);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", equipment='").append(equipment).append('\'');
        sb.append(", insertTime=").append(insertTime);
        sb.append(", jobRuntimeAnomaly=").append(jobRuntimeAnomaly);
        sb.append(", waferRuntimeAnomaly=").append(waferRuntimeAnomaly);
        sb.append(", eventDeletion=").append(eventDeletion);
        sb.append(", eventDuplication=").append(eventDuplication);
        sb.append('}');
        return sb.toString();
    }
}
