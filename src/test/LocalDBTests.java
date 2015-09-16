/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import java.math.BigDecimal;
import java.sql.SQLException;
import localDB.menagers.DatagramMenager;
import localDB.menagers.LocalDataBaseMenager;
import project.data.Datagram;
import project.data.Session;

/**
 *
 * @author hp
 */
public class LocalDBTests {

    public static void main(String[] args) throws Exception {
        System.out.println("LocalDBTests START:");

        LocalDataBaseMenager ldbm = new LocalDataBaseMenager();
//        if(ldbm.fullTestBDExists()){
//           Logger.write("Local DB Found!", LogTyps.WARNING); 
//        }else{
//            Logger.write("Local DB Not Found!", LogTyps.WARNING);
//        }
        if (ldbm.fullTestBDExists() == false) {
            Logger.write("Local DB Not Found!", LogTyps.WARNING);
            Logger.write("Trying to create new Local DB.", LogTyps.MESSAGE);
            try {
                ldbm.setupDataBase();
                Logger.write("New Local DB created.", LogTyps.MESSAGE);
            } catch (Exception e) {
                Logger.write("Error while creating Local DB:" + e.getMessage(), LogTyps.ERROR);
                e.printStackTrace();
            }
            if (ldbm.fullTestBDExists() == false) {
                Logger.write("Error: Local BD is not valid!", LogTyps.ERROR);
                return;
            }
        }
        Session localDBSession = new Session(ldbm, true);
        localDBSession.getLocalDataBaseMenager().getDatagramMenager().createDatagram(new Datagram("lol"));
        
        testDatagram(localDBSession.getLocalDataBaseMenager().getDatagramMenager());
        
        localDBSession.closeSession();
        
        System.out.println("LocalDBTests END");
        return;
    }

    private static void testDatagram(DatagramMenager dm) throws Exception {
        if (dm != null) {
            Datagram test = new Datagram("testowy");
            dm.createDatagram(test);
            dm.updateDatagram(test, "ERROR?");
            Datagram test2 = dm.getDatagram(test.getId());
            if (!test.getData().equals(test2.getData())) {
                Logger.write("ERROR while comparing datagram: test:[" + test.getData() + "];test2:[" + test2.getData() + "]", LogTyps.ERROR);
            } else {
                Logger.write("SUCCESS while comparing datagram: test:[" + test.getData() + "];test2:[" + test2.getData() + "]", LogTyps.MESSAGE);
            }
            Logger.write("before setting as send:", LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToSend().size()  :" + dm.getDatagramsToSend().size(), LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToRemove().size():" + dm.getDatagramsToRemove().size(), LogTyps.WARNING);
            test2.setDataSend(true);
            dm.setSendOK(test2);
            Logger.write("after setting as send:", LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToSend().size()  :" + dm.getDatagramsToSend().size(), LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToRemove().size():" + dm.getDatagramsToRemove().size(), LogTyps.WARNING);
            dm.removeSendDatagrams();
            Logger.write("after removing send datagrams:", LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToSend().size()  :" + dm.getDatagramsToSend().size(), LogTyps.WARNING);
            Logger.write("  dm.getDatagramsToRemove().size():" + dm.getDatagramsToRemove().size(), LogTyps.WARNING);
        }
    }
}
