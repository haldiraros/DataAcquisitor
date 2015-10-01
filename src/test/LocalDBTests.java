/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import REST.RestMenager;
import hubGui.i18n.Resources;
import java.util.HashSet;
import java.util.Set;
import localDB.menagers.LocalDataBaseMenager;
import localDB.menagers.MeasurementManager;
import project.data.Measurement;
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
//           System.out.println("Local DB Found!", LogTyps.WARNING); 
//        }else{
//            System.out.println("Local DB Not Found!", LogTyps.WARNING);
//        }
        if (ldbm.fullTestBDExists() == false) {
            System.out.println("Local DB Not Found!");
            System.out.println("Trying to create new Local DB.");
            try {
                ldbm.setupDataBase();
                System.out.println("New Local DB created.");
            } catch (Exception e) {
                System.out.println("Error while creating Local DB:" + e.getMessage());
                e.printStackTrace();
            }
            if (ldbm.fullTestBDExists() == false) {
                System.out.println("Error: Local BD is not valid!");
                return;
            }
        }
        Resources.setLang("pl");
        Session localDBSession = new Session(ldbm, true, new RestMenager());
        int[] tab = new int[10];
        for (int i = 0; i < 10; i++) {
            tab[i] = i;
        }
//        localDBSession.getLocalDataBaseMenager().createMeasurement(new Measurement("lol", "hub_id", "teraz", tab, 10));
//
        testMeasurement(localDBSession.getLocalDataBaseMenager().getMeasurementManager());
        long startTime2 = System.currentTimeMillis();
        System.out.println("startTime multi:" + startTime2);
        Set<Measurement> sd = new HashSet<Measurement>();
        for (int i = 0; i < 1000; i++) {
            sd.add(new Measurement("lol", "hub_id" + i/100, "teraz", tab, i));
        }
        localDBSession.addMeasurements(sd);
        System.out.println("multi:" + (System.currentTimeMillis() - startTime2));

//        long startTime = System.currentTimeMillis();
//        System.out.println("startTime single:"+startTime);
//        for (int i = 0; i < 1000; i++) {
//            localDBSession.getLocalDataBaseMenager().getMeasurementMenager().createMeasurement(new Measurement("lol", "hub_id", "pojedynczo:" + i));
//        }
//        System.out.println("single:"+(System.currentTimeMillis() - startTime));
        Thread.sleep(10000);
//        localDBSession.sendDatagrams();
//        localDBSession.sendMeasurements();
//        if (1 == 1) {
//            throw new Exception("lol");
//        }
        localDBSession.closeSession();

        System.out.println("LocalDBTests END");
        return;
    }
//1: 343 510
//2: 226 731

    private static void testMeasurement(MeasurementManager dm) throws Exception {
        if (dm != null) {
            int[] tab = new int[10];
            for (int i = 0; i < 10; i++) {
                tab[i] = i;
            }
            Measurement test = new Measurement("lol", "hub_id", "teraz", tab, 10);
            Set<Measurement> ds = new HashSet<>();
            ds.add(test);
//            System.out.println("Inserted:" + dm.createMeasurements(ds));
//            test.setNewErrorMessage("error");
//            dm.updateMeasurements(ds);
////            dm.updateMeasurement(test);
////            Measurement test2 = dm.getMeasurement(test.getId());
//            System.out.println("before setting as send:");
//            System.out.println("  dm.getMeasurementsToSend().size()  :" + dm.getMeasurementsToSend().size());
//            test.setDataSend(true);
//            dm.updateMeasurements(ds);
//            System.out.println("after setting as send:");
//            System.out.println("  dm.getMeasurementsToSend().size()  :" + dm.getMeasurementsToSend().size());
//            dm.deleteSendMeasurements();
//            System.out.println("after removing send datagrams:");
//            System.out.println("  dm.getMeasurementsToSend().size()  :" + dm.getMeasurementsToSend().size());

        }
    }
}
