/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hp
 */
public class MeasurementUtils {

    public static Map<String, Set<Measurement>> sortMeasurements(Collection<Measurement> measurements) {
        Map<String, Set<Measurement>> sortedMeasurements = new HashMap<String, Set<Measurement>>();
        for (Measurement m : measurements) {
            if (sortedMeasurements.containsKey(m.getHubId())) {
                sortedMeasurements.get(m.getHubId()).add(m);
            } else {
                Set<Measurement> newSet = new HashSet<Measurement>();
                newSet.add(m);
                sortedMeasurements.put(m.getHubId(), newSet);
            }
        }
        return sortedMeasurements;
    }

    public static Set<Set<Measurement>> splitMeasurements(Collection<Measurement> measurements, int maxSize) {
        Set<Set<Measurement>> splitedMeasurements = new HashSet<Set<Measurement>>();
        Set<Measurement> currentSet = new HashSet<Measurement>();

        for (Measurement m : measurements) {
            currentSet.add(m);
            if (currentSet.size() >= maxSize) {
                splitedMeasurements.add(currentSet);
                currentSet = new HashSet<Measurement>();
            }
        }
        if (currentSet.size() > 0) {
            splitedMeasurements.add(currentSet);
        }
        return splitedMeasurements;
    }
}
