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
public enum LogTyps {

//    ANSI_BLACK = "\u001B[30m";
//    ANSI_RED = "\u001B[31m";
//    ANSI_GREEN = "\u001B[32m";
//    ANSI_YELLOW = "\u001B[33m";
//    ANSI_BLUE = "\u001B[34m";
//    ANSI_PURPLE = "\u001B[35m";
//    ANSI_CYAN = "\u001B[36m";
//    ANSI_WHITE = "\u001B[37m";

    /**
     * Default log Type, in console displayed in black color 
     */
    LOG,
    
    /**
     * For reporting errors, in console displayed in red color 
     */
    ERROR,

    /**
     * For reporting possible warnings, in console displayed in yellow color 
     */
    WARNING,

    /**
     * For reporting events, in console displayed in green color 
     */
    SUCCESS
}
