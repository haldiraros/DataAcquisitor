/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.data;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import localDB.menagers.LocalDataBaseMenager;

/**
 *
 * @author hp
 */
public class Session {

    private BigDecimal id;
    private int datagramsEnqueued;
    private int datagramsReceived;
    private int datagramsSend_OK;
    private int datagramsSend_Failures;
    private LocalDataBaseMenager localDataBaseMenager;
    private ScheduledExecutorService exec;
    private Boolean sessionWithLocalDB;

    public Session() throws Exception {
        SetUpStartValues(null, true);
    }

    public Session(BigDecimal id) throws Exception {
        this.setId(id);
        SetUpStartValues(null, true);
    }

    public Session(LocalDataBaseMenager ldbm, Boolean sessionWithLocalDB) throws Exception {
        SetUpStartValues(ldbm, sessionWithLocalDB);
    }

    // to_do - dodać tutaj menager wysyłania datagramów
    // Menager menager ??, tkóry będzie wysyła i przy poprawnym wyłaniu ustawi:
    //        datagram.setDataSend(true);
    // a np przy niepoprawnym zwróci błąd jako stringa
    // dodatkowo do rozstrzygnięcia kiedy będzie wołana funkcja setupDataBase() 
    // z LocalDataBaseMenager która tworzy bazę danych, bo nie wiem czy
    // to powinno być przy każdym odpalaniu programu wołane i logowane jak nastąpi stworzebnie bazy, 
    // czy tylko w jakiś szczególnych przypadkach ??
    public void sendDatagrams() throws Exception {
        for (Datagram datagram : localDataBaseMenager.getDatagramsToSend()) {
            String error = null;
            //error = menager.send(datagram);
            localDataBaseMenager.updateDatagram(datagram, error);
            if (datagram.isDataSend()) {
                addDatagramsSend_OK();
            } else {
                addDatagramsSend_Failures();
            }
        }
        localDataBaseMenager.updateSession(this);
    }

    public void SetUpStartValues(LocalDataBaseMenager ldbm, Boolean sessionWithLocalDB) throws Exception {
        setDatagramsEnqueued(0);
        setDatagramsReceived(0);
        setDatagramsSend_OK(0);
        setDatagramsSend_Failures(0);
        this.sessionWithLocalDB = sessionWithLocalDB;
        System.out.println("sessionWithLocalDB:"+sessionWithLocalDB);

        if (sessionWithLocalDB) {
            if (ldbm == null) {
                localDataBaseMenager = new LocalDataBaseMenager();
            } else {
                localDataBaseMenager = ldbm;
            }
            localDataBaseMenager.createSession(this);
            localDataBaseMenager.getDatagramMenager().removeSendDatagrams();

            exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendDatagrams();
                    } catch (Exception ex) {
                        Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, 0, 20, TimeUnit.SECONDS);
        }
    }

    public boolean addDatagram(Datagram datagram) throws Exception {
        if(sessionWithLocalDB){
        Boolean status = localDataBaseMenager.createDatagram(datagram);
        if (status) {
            addDatagramsReceived();
            localDataBaseMenager.updateSession(this);
        }
        return status;
        }else{
            String error = null;
            //error = menager.send(datagram);
            return false;
        }
    }

    public boolean closeSession() throws Exception {
        if (sessionWithLocalDB) {
            localDataBaseMenager.getDatagramMenager().removeSendDatagrams();
            return localDataBaseMenager.closeSession(this);
        }else{
            return false;
        }
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) throws Exception {
        if (this.id == null) {
            this.id = id;
        } else {
            throw new Exception("Id is already set [current id: ("
                    + this.id
                    + ") new id: ("
                    + id
                    + ")]");
        }
    }

    public int getDatagramsEnqueued() {
        return datagramsEnqueued;
    }

    public void setDatagramsEnqueued(int datagramsEnqueued) {
        this.datagramsEnqueued = datagramsEnqueued;
    }

    public void addDatagramsEnqueued(int datagramsEnqueued) {
        setDatagramsEnqueued(getDatagramsEnqueued() + datagramsEnqueued);
    }

    public void addDatagramsEnqueued() {
        this.addDatagramsEnqueued(1);
    }

    public int getDatagramsReceived() {
        return datagramsReceived;
    }

    public void setDatagramsReceived(int datagramsReceived) {
        this.datagramsReceived = datagramsReceived;
    }

    public void addDatagramsReceived(int datagramsReceived) {
        setDatagramsReceived(getDatagramsReceived() + datagramsReceived);
    }

    public void addDatagramsReceived() {
        this.addDatagramsReceived(1);
    }

    public int getDatagramsSend_OK() {
        return datagramsSend_OK;
    }

    public void setDatagramsSend_OK(int datagramsSend_OK) {
        this.datagramsSend_OK = datagramsSend_OK;
    }

    public void addDatagramsSend_OK(int datagramsSend_OK) {
        setDatagramsSend_OK(getDatagramsSend_OK() + datagramsSend_OK);
    }

    public void addDatagramsSend_OK() {
        this.addDatagramsSend_OK(1);
    }

    public int getDatagramsSend_Failures() {
        return datagramsSend_Failures;
    }

    public void setDatagramsSend_Failures(int datagramsSend_Failures) {
        this.datagramsSend_Failures = datagramsSend_Failures;
    }

    public void addDatagramsSend_Failures(int datagramsSend_Failures) {
        setDatagramsSend_Failures(getDatagramsSend_Failures() + datagramsSend_Failures);
    }

    public void addDatagramsSend_Failures() {
        this.addDatagramsSend_Failures(1);
    }

    /**
     * @return the localDataBaseMenager
     */
    public LocalDataBaseMenager getLocalDataBaseMenager() {
        return localDataBaseMenager;
    }

    /**
     * @param localDataBaseMenager the localDataBaseMenager to set
     */
    public void setLocalDataBaseMenager(LocalDataBaseMenager localDataBaseMenager) {
        this.localDataBaseMenager = localDataBaseMenager;
    }

    /**
     * @return the sessionWithLocalDB
     */
    public Boolean getSessionWithLocalDB() {
        return sessionWithLocalDB;
    }

    /**
     * @param sessionWithLocalDB the sessionWithLocalDB to set
     */
    public void setSessionWithLocalDB(Boolean sessionWithLocalDB) {
        this.sessionWithLocalDB = sessionWithLocalDB;
    }

}
