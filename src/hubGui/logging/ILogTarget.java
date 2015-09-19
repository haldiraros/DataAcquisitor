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
public interface ILogTarget {
    void write(String message, LogTyps type);
}
