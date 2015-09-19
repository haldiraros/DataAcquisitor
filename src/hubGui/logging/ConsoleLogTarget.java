/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.logging;

/**
 *
 * @author Marcin
 */
public class ConsoleLogTarget implements ILogTarget {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String DEFAULT_COLOR = "\u001B[30m";
    private static final String ERROR_COLOR = "\u001B[31m";
    private static final String WARNING_COLOR = "\u001B[33m";
    private static final String SUCCESS_COLOR = "\u001B[32m";

    @Override
    public void write(String message, LogTyps type) {
        System.out.println(getColor(type) + message + ANSI_RESET);
    }
    
    private String getColor(LogTyps type) {
        switch (type) {
            case LOG:
                return DEFAULT_COLOR;
            case ERROR:
                return ERROR_COLOR;
            case WARNING:
                return WARNING_COLOR;
            case SUCCESS:
                return SUCCESS_COLOR;
        }
        throw new RuntimeException("Unknown log type " + type);
    }
    
}
