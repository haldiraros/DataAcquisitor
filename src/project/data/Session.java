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
    private boolean closedOnLocalBD;

    private LocalDataBaseMenager localDataBaseMenager;
    private ScheduledExecutorService exec;

    public Session() throws Exception {
        setUpStartValues(null, true);
    }

    public Session(BigDecimal id) throws Exception {
        this.setId(id);
        setUpStartValues(null, true);
    }

    public Session(LocalDataBaseMenager ldbm, Boolean sessionWithLocalDB) throws Exception {
        setUpStartValues(ldbm, sessionWithLocalDB);
    }

    public Session(Boolean sessionWithLocalDB) throws Exception {
        setUpStartValues(null, sessionWithLocalDB);
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
                addDatagramSend_OK();
            } else {
                addDatagramSend_Failures();
            }
        }
        localDataBaseMenager.updateSession(this);
    }

    public void setUpStartValues(LocalDataBaseMenager ldbm, Boolean sessionWithLocalDB) throws Exception {
        setDatagramsEnqueued(0);
        setDatagramsReceived(0);
        setDatagramsSend_OK(0);
        setDatagramsSend_Failures(0);
        setClosedOnLocalBD(false);

        // tworzenie bez lokalnej bazy
        if (sessionWithLocalDB) {
            setupDataBaseMenager(ldbm);
        } else {
            localDataBaseMenager = null;
        }
    }

    private void setupDataBaseMenager(LocalDataBaseMenager ldbm) throws Exception {
        if (ldbm == null) {
            localDataBaseMenager = new LocalDataBaseMenager();
        } else {
            localDataBaseMenager = ldbm;
        }
        localDataBaseMenager.createSession(this);
        localDataBaseMenager.getDatagramMenager().removeSendDatagrams();
        createIddleSending();
    }

    private void createIddleSending() {
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

    public boolean isSessionWithLocalDB() {
        return localDataBaseMenager != null;
    }

    public boolean addDatagram(Datagram datagram) throws Exception {
        if (localDataBaseMenager != null) {
            localDataBaseMenager.createDatagram(datagram);
            addDatagramReceived();
            localDataBaseMenager.updateSession(this);
            return true;
        } else {
            String error = null;
            //error = menager.send(datagram);
            return false;
        }
    }

    public boolean closeSession() throws Exception {
        if (localDataBaseMenager != null) {
            localDataBaseMenager.getDatagramMenager().removeSendDatagrams();
            localDataBaseMenager.closeSession(this);
            return true;
        }
        return false;
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

    private void setDatagramsEnqueued(int datagramsEnqueued) {
        this.datagramsEnqueued = datagramsEnqueued;
    }

    private void addDatagramsEnqueued(int datagramsEnqueued) {
        setDatagramsEnqueued(getDatagramsEnqueued() + datagramsEnqueued);
    }

    private void addDatagramEnqueued() {
        this.addDatagramsEnqueued(1);
    }

    public int getDatagramsReceived() {
        return datagramsReceived;
    }

    private void setDatagramsReceived(int datagramsReceived) {
        this.datagramsReceived = datagramsReceived;
    }

    private void addDatagramsReceived(int datagramsReceived) {
        setDatagramsReceived(getDatagramsReceived() + datagramsReceived);
    }

    private void addDatagramReceived() {
        this.addDatagramsReceived(1);
    }

    public int getDatagramsSend_OK() {
        return datagramsSend_OK;
    }

    private void setDatagramsSend_OK(int datagramsSend_OK) {
        this.datagramsSend_OK = datagramsSend_OK;
    }

    private void addDatagramsSend_OK(int datagramsSend_OK) {
        setDatagramsSend_OK(getDatagramsSend_OK() + datagramsSend_OK);
    }

    private void addDatagramSend_OK() {
        this.addDatagramsSend_OK(1);
    }

    public int getDatagramsSend_Failures() {
        return datagramsSend_Failures;
    }

    private void setDatagramsSend_Failures(int datagramsSend_Failures) {
        this.datagramsSend_Failures = datagramsSend_Failures;
    }

    private void addDatagramsSend_Failures(int datagramsSend_Failures) {
        setDatagramsSend_Failures(getDatagramsSend_Failures() + datagramsSend_Failures);
    }

    private void addDatagramSend_Failures() {
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
     * @return the closed
     */
    public Boolean isClosedOnLocalBD() {
        return closedOnLocalBD;
    }

    /**
     * @param closed the closed to set
     */
    private void setClosedOnLocalBD(Boolean closed) {
        this.closedOnLocalBD = closed;
    }

    /**
     * Closes the session, so it can't be changed
     */
    public void closeOnLocalBD() {
        this.closedOnLocalBD = true;
    }
}
