/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.logging;

import java.util.ArrayList;

/**
 *
 * @author hp
 */
public class Logger {
    private static final ArrayList<ILogTarget> logTargets = new ArrayList<>();
    private static final Object lock = new Object();
    
    public static void addTarget(ILogTarget logTarget) {
        synchronized (lock) {
            logTargets.add(logTarget);
        }
    }

    public static void write(String message) {
        write(message, LogTyps.LOG);
    }
    
    public static void write(String message, LogTyps type) {
        synchronized (lock) {
            for (ILogTarget logTarget : logTargets) {
                logTarget.write(message, type);
            }
        }
    }
 
}
