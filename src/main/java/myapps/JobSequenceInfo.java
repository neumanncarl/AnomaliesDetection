package myapps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class JobSequenceInfo {

    // Every event related to one job
    private final ArrayList<EquipmentEvent> events = new ArrayList();

    // Keys
    private final String jobReference;
    private final String equipmentType;
    private final String equipment;

    // Job status
    private boolean materialPlaced = false;
    private boolean jobStarted = false;
    private boolean waferStarted = false;
    private boolean waferCompleted = false;
    private boolean jobCompleted = false;
    private boolean materialRemoved = false;

    private LocalDateTime jobStart;
    private ArrayList<LocalDateTime> waferStarts = new ArrayList<>();

    private final Map<String, Integer> eventTypeCounts = new HashMap<>();

    private boolean jobRuntimeAnomaly = false;
    private boolean waferRuntimeAnomaly = false;
    private boolean sequenceAnomaly = false;
    private int eventDeletion;
    private int eventDuplication;

    public JobSequenceInfo(String reference, String equipmentType, String equipment) {
        this.jobReference = reference;
        this.equipmentType = equipmentType;
        this.equipment = equipment;

    }

    public void updateFromEvent(EquipmentEvent event, RuntimeStats runtimeStats, Map<String, RuntimeStats> runtimeStatsStore, String statsKey) {
        if (events.contains(event)) return; // Reject event if it's a duplicate (event is no anomaly but a system error)
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
                    sequenceAnomaly = true;
                    break;
                }
                runtime = ChronoUnit.SECONDS.between(waferStarts.removeFirst(), event.getTimestamp());
                runtimeStats.update(runtime);
                runtimeStatsStore.put(statsKey, runtimeStats);
                waferRuntimeAnomaly = runtimeStats.isWithinBounds(runtime);
                break;
            case "JobStarted":
                if (!materialPlaced) {
                    sequenceAnomaly = true;
                }
                jobStarted = true;
                jobStart = event.getTimestamp();
                break;
            case "JobCompleted":
                if (!materialPlaced || !jobStarted) {
                    sequenceAnomaly = true;
                }
                jobCompleted = true;
                if (jobStart == null) {
                    sequenceAnomaly = true;
                    break;
                }
                runtime = ChronoUnit.SECONDS.between(jobStart, event.getTimestamp());
                runtimeStats.update(runtime);
                runtimeStatsStore.put(statsKey, runtimeStats);
                jobRuntimeAnomaly = runtimeStats.isWithinBounds(runtime);
                break;
            case "MaterialPlaced":
                materialPlaced = true;
                break;
            case "MaterialRemoved":
                if (!materialPlaced || !jobStarted || !jobCompleted) {
                    sequenceAnomaly = true;
                }
                materialRemoved = true;
                break;
        }

        this.eventTypeCounts.merge(event.getEiEventType(), 1, Integer::sum);
    }

    // Check every event for anomaly
    public boolean hasAnomaly() {
        if (materialPlaced && jobStarted && waferStarted && waferCompleted && !jobCompleted) return waferRuntimeAnomaly;
        if (materialPlaced && jobStarted && jobCompleted) return jobRuntimeAnomaly;
        if (materialRemoved) return isSequenceAnomaly() || sequenceAnomaly;
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

    @Override
    public String toString() {
        return "{" +
                "jobReference:'" + jobReference + '\'' +
                ", equipment:'" + equipment + '\'' +
                ", insertTime:" + new Date() +
                ", jobRuntimeAnomaly:" + jobRuntimeAnomaly +
                ", waferRuntimeAnomaly:" + waferRuntimeAnomaly +
                ", eventDeletion:" + eventDeletion +
                ", eventDuplication:" + eventDuplication +
                '}';
    }
}
