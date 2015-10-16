/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import project.Config;

/**
 *
 * @author hp
 */
public class pathTest {
    public static void main(String[] args) throws java.io.IOException {
        System.out.println(Config.getPath("hubGui.settings.fileName"));
        System.out.println(Config.getPath("localDB.menagers.DataBaseFilePath"));
    }
}
