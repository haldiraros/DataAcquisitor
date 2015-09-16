/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.logging;

/**
 *
 * @author hp
 */
public class Logger {
    public static final String ANSI_RESET = "\u001B[0m";
//    public static final String ANSI_BLACK = "\u001B[30m";
//    public static final String ANSI_RED = "\u001B[31m";
//    public static final String ANSI_GREEN = "\u001B[32m";
//    public static final String ANSI_YELLOW = "\u001B[33m";
//    public static final String ANSI_BLUE = "\u001B[34m";
//    public static final String ANSI_PURPLE = "\u001B[35m";
//    public static final String ANSI_CYAN = "\u001B[36m";
//    public static final String ANSI_WHITE = "\u001B[37m";
    
    private static Object console;
    private static Logger logger;

    protected Logger(Object console) {
        this.console = console;
        this.write("created singleton: " + logger, LogTyps.MESSAGE);
    }

    public static Logger getInstance(Object console){
        if (logger == null) {
            logger = new Logger(console);
        }
        return logger;
    }

    public static void write(String message) {
        write(message, LogTyps.LOG);
    }
    public static void write(String message, LogTyps type){
//        if(console != null){
            if(type != null){
                System.out.println(type + message + ANSI_RESET);
            }else{
                System.out.println(message + ANSI_RESET);                
            }
//        }
    }
 
}
