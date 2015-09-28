/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.data;

import REST.SendStatistics;
import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
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

    static void reportSendStats(SendStatistics stats, Set<Measurement> measurementsToSend) {

        if (stats.getMeasurementSendFailsCounter() == 0 && stats.getMeasurementSendOkCounter() == 0) {
            return;
        } else if (stats.getMeasurementSendFailsCounter() == 0 && stats.getMeasurementSendOkCounter() > 0) {
            Logger.write(Resources.getFormatString("msg.MeasurementUtils.MeasurementSendToRestAllOK", stats.getMeasurementSendOkCounter()), LogTyps.LOG);
            return;
        } else if (stats.getMeasurementSendFailsCounter() > 0) {
            Map<String, Integer> sortedMeasurements = new HashMap<String, Integer>();
            for (Measurement d : measurementsToSend) {
                String key = d.getNewErrorMessage();
                if (key != null) {
                    if (sortedMeasurements.containsKey(key)) {
                        sortedMeasurements.replace(key, sortedMeasurements.get(key) + 1);
                    } else {
                        sortedMeasurements.put(key, 1);
                    }
                }
            }
            String errors = null;
            for (String e : sortedMeasurements.keySet()) {
                errors = errors + e + ":" + sortedMeasurements.get(e);
            }
            if (stats.getMeasurementSendOkCounter() > 0) {
                Logger.write(Resources.getFormatString("msg.MeasurementUtils.MeasurementSendToRestSomeError", stats.getMeasurementSendOkCounter(), errors), LogTyps.ERROR);
            } else {
                Logger.write(Resources.getFormatString("msg.MeasurementUtils.MeasurementSendToRestAllError", errors), LogTyps.ERROR);
            }
        }
    }
}
