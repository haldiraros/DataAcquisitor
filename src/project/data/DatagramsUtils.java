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
        return splitedDatagrams;
    }
}
