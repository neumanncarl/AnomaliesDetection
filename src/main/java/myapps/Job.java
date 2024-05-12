package myapps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Job {

    // Every event related to one job
    private final ArrayList<EquipmentEvent> events = new ArrayList();
    private final String equipment;
    private final String equipmentType;
    private final long jobRef;

    // Job status
    private boolean jobStarted = false;
    private boolean waferStarted = false;
    private boolean waferCompleted = false;
    private boolean jobCompleted = false;
    private boolean materialRemoved = false;

    private Date jobStart;
    private ArrayList<Date> waferStarts = new ArrayList<>();

    private final Map<String, Integer> eventTypeCounts = new HashMap<>();

    private int jobRuntimeAnomaly = 0;
    private int waferRuntimeAnomaly = 0;

    public Job(EquipmentEvent event) {
        this.jobRef = event.getJobReference();
        this.equipment = event.getEquipment();
        this.equipmentType = event.getEquipmentType();
        events.add(event);
        this.eventTypeCounts.merge(event.getEiEventType(), 1, Integer::sum);
    }

    public void update(EquipmentEvent event, RuntimeStats runtimeStats, Map<String, RuntimeStats> runtimeStatsStore, String statsKey) {
        System.out.println(events);
        if (events.getLast().getTimestamp().equals(event.getTimestamp())) return; // Reject event if it's a duplicate (event is no anomaly but a system error)
        this.events.add(event);

        // Handle event based on its type
        long runtime;
        switch (event.getEiEventType()) {
            case "WaferStarted":
                waferStarted = true;
                waferStarts.add(event.getTimestamp());
                break;
            case "WaferCompleted":
                waferCompleted = true;
                if (waferStarts.isEmpty()) {
                    break;
                }
                runtime = ChronoUnit.SECONDS.between(waferStarts.getFirst().toInstant(), event.getTimestamp().toInstant());
                waferStarts.removeFirst();
                runtimeStats.update(runtime);
                runtimeStatsStore.put(statsKey, runtimeStats);
                if (runtimeStats.isWithinBounds(runtime) && !event.getEquipmentState().equals("UnscheduledDowntimeDefault")) {
                    waferRuntimeAnomaly++;
                }
                break;
            case "JobStarted":
                jobStarted = true;
                jobStart = event.getTimestamp();
                break;
            case "JobCompleted":
                jobCompleted = true;
                runtime = ChronoUnit.SECONDS.between(jobStart.toInstant(), event.getTimestamp().toInstant());
                runtimeStats.update(runtime);
                runtimeStatsStore.put(statsKey, runtimeStats);
                if (runtimeStats.isWithinBounds(runtime)) {
                    jobRuntimeAnomaly++;
                }
                break;
            case "MaterialRemoved":
                materialRemoved = true;
                break;
        }

        this.eventTypeCounts.merge(event.getEiEventType(), 1, Integer::sum);
    }

    public boolean hasAnomaly() {
        //if (materialPlaced && jobStarted && waferStarted && waferCompleted && !jobCompleted) return waferRuntimeAnomaly > 0;
        //if (materialPlaced && jobStarted && jobCompleted) return jobRuntimeAnomaly > 0;
        if (materialRemoved) {
            return isSequenceAnomaly() || jobRuntimeAnomaly > 0 || waferRuntimeAnomaly > 0;
        }
        return false;
    }

    private boolean isSequenceAnomaly() {
        if ("CascadingWafer".equals(equipmentType)) {
            return !(eventTypeCounts.getOrDefault("MaterialPlaced", 0) >= 1 &&
                    eventTypeCounts.getOrDefault("JobStarted", 0) >= 1 &&
                    eventTypeCounts.getOrDefault("JobCompleted", 0) >= 1 &&
                    eventTypeCounts.getOrDefault("MaterialRemoved", 0) >= 1 &&
                    waferStarted && waferCompleted &&
                    eventTypeCounts.values().stream().mapToInt(Integer::intValue).sum() >= 6);
        } else {
            return eventTypeCounts.values().stream().mapToInt(Integer::intValue).sum() != 4 ||
                    !eventTypeCounts.keySet().containsAll(Arrays.asList("MaterialPlaced", "JobStarted", "JobCompleted", "MaterialRemoved"));
        }
    }

    public boolean isJobStarted() {
        return jobStarted;
    }

    public boolean isJobCompleted() {
        return jobCompleted;
    }

    public ArrayList<EquipmentEvent> getEvents() {
        return events;
    }

    public long getJobRef() {
        return jobRef;
    }

    @Override
    public String toString() {
        return "{" +
                "jobReference:'" + jobRef + '\'' +
                ", equipment:'" + equipment + '\'' +
                ", insertTime:" + new Date() +
                ", jobRuntimeAnomaly:" + jobRuntimeAnomaly +
                ", waferRuntimeAnomaly:" + waferRuntimeAnomaly +
                '}';
    }
}
