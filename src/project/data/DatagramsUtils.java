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
public class DatagramsUtils {

    public static Map<String, Set<Datagram>> sortDatagrams(Collection<Datagram> datagrams) {
        Map<String, Set<Datagram>> sortedDatagrams = new HashMap<String, Set<Datagram>>();
        for (Datagram d : datagrams) {
            if (sortedDatagrams.containsKey(d.getHubId())) {
                sortedDatagrams.get(d.getHubId()).add(d);
            } else {
                Set<Datagram> newSet = new HashSet<Datagram>();
                newSet.add(d);
                sortedDatagrams.put(d.getHubId(), newSet);
            }
        }
        return sortedDatagrams;
    }

    public static Set<Set<Datagram>> splitDatagrams(Collection<Datagram> datagrams, int maxSize) {
        Set<Set<Datagram>> splitedDatagrams = new HashSet<Set<Datagram>>();
        Set<Datagram> currentSet = new HashSet<Datagram>();

        for (Datagram d : datagrams) {
            currentSet.add(d);
            if (currentSet.size() >= maxSize) {
                splitedDatagrams.add(currentSet);
                currentSet = new HashSet<Datagram>();
            }
        }
        if (currentSet.size() > 0) {
            splitedDatagrams.add(currentSet);
        }
        return splitedDatagrams;
    }

    static void reportSendStats(SendStatistics stats, Set<Datagram> datagramsToSend) {
        if (stats.getDatagramSendFailsCounter() == 0 && stats.getDatagramSendOkCounter() == 0) {
            return;
        } else if (stats.getDatagramSendFailsCounter() == 0 && stats.getDatagramSendOkCounter() > 0) {
            Logger.write(Resources.getFormatString("msg.DatagramsUtils.DatagramSendToRestAllOK", stats.getDatagramSendOkCounter()), LogTyps.LOG);
            return;
        } else if (stats.getDatagramSendFailsCounter() > 0) {
            Map<String, Integer> sortedDatagrams = new HashMap<String, Integer>();
            for (Datagram d : datagramsToSend) {
                String key = d.getNewErrorMessage();
                if (key != null) {
                    if (sortedDatagrams.containsKey(key)) {
                        sortedDatagrams.replace(key, sortedDatagrams.get(key) + 1);
                    } else {
                        sortedDatagrams.put(key, 1);
                    }
                }
            }
            String errors = null;
            for (String e : sortedDatagrams.keySet()) {
                errors = errors + e + ": " + sortedDatagrams.get(e) + "\n";
            }
            if (stats.getDatagramSendOkCounter() > 0) {
                Logger.write(Resources.getFormatString("msg.DatagramsUtils.DatagramSendToRestSomeError", stats.getDatagramSendOkCounter(), errors), LogTyps.ERROR);
            } else {
                Logger.write(Resources.getFormatString("msg.DatagramsUtils.DatagramSendToRestAllError", errors), LogTyps.ERROR);
            }
        }
    }
}
