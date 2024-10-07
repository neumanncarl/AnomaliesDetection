package org.example.operators;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.example.pojo.Anomaly;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProcessAnomaly extends ProcessWindowFunction<Anomaly, Anomaly, Long, TimeWindow> {
    Map<String, Anomaly> jobAnamolyMap = new HashMap<>();
    @Override
    public void process(Long aLong, ProcessWindowFunction<Anomaly, Anomaly, Long, TimeWindow>.Context context, Iterable<Anomaly> elements, Collector<Anomaly> out) throws Exception {
        Iterator<Anomaly> iterator = elements.iterator();
        while (iterator.hasNext()){
            Anomaly anomaly = iterator.next();
            jobAnamolyMap.put(anomaly.getEiEventJobId(), anomaly);
        }

        jobAnamolyMap.forEach((s, anomaly) -> {
            if (anomaly.getSequenceAnomaly() > 0){
                out.collect(anomaly);
            }
        });

    }
}
