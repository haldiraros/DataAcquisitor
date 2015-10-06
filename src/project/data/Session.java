/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.data;

import REST.RestMenager;
import REST.SendStatistics;
import hubGui.i18n.Resources;
import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import localDB.managers.LocalDataBaseManager;
import project.Config;

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
    private int measuresEnqueued;
    private int measuresReceived;
    private int measuresSend_OK;
    private int measuresSend_Failures;
    private boolean closedOnLocalBD;

    private LocalDataBaseManager localDataBaseMenager;
    private RestMenager restMenager;
    private ScheduledExecutorService exec;
    private Connection connection;

    public Session() throws Exception {
        setUpStartValues(null, true);
    }

    public Session(BigDecimal id) throws Exception {
        this.setId(id);
        setUpStartValues(null, true);
    }

    public Session(LocalDataBaseManager ldbm, Boolean sessionWithLocalDB, RestMenager restMenager) throws Exception {
        setUpStartValues(ldbm, sessionWithLocalDB);
        setRestMenager(restMenager);
    }

    public Session(LocalDataBaseManager ldbm, Boolean sessionWithLocalDB) throws Exception {
        setUpStartValues(ldbm, sessionWithLocalDB);
    }

    public Session(Boolean sessionWithLocalDB) throws Exception {
        setUpStartValues(null, sessionWithLocalDB);
    }

    public void setUpStartValues(LocalDataBaseManager ldbm, Boolean sessionWithLocalDB) throws Exception {
        setDatagramsEnqueued(0);
        setDatagramsReceived(0);
        setDatagramsSend_OK(0);
        setDatagramsSend_Failures(0);
        setMeasuresEnqueued(0);
        setMeasuresReceived(0);
        setMeasuresSend_OK(0);
        setMeasuresSend_Failures(0);
        setClosedOnLocalBD(false);

        // tworzenie bez lokalnej bazy
        if (sessionWithLocalDB) {
            setupDataBaseMenager(ldbm);
        } else {
            localDataBaseMenager = null;
        }
    }

    private void setupDataBaseMenager(LocalDataBaseManager ldbm) throws Exception {
        if (ldbm == null) {
            localDataBaseMenager = new LocalDataBaseManager();
        } else {
            localDataBaseMenager = ldbm;
        }
        connection = localDataBaseMenager.getNewConnection();
        localDataBaseMenager.createSession(connection, this);
        localDataBaseMenager.removeSendData(connection);
    }

    private void createIddleSending() throws Exception {
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new DatagramsIdleSend(connection), Config.getInteger("REST.datagramsIdleSendInitialDelay"), Config.getInteger("REST.datagramsIdleSendPeriod"), TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(new MeasurementsIdleSend(connection), Config.getInteger("REST.measurementsIdleSendInitialDelay"), Config.getInteger("REST.measurementsIdleSendPeriod"), TimeUnit.SECONDS);

        //exec.scheduleAtFixedRate(new DatagramsIdleSend(localDataBaseMenager.getNewConnection()), Config.getInteger("REST.datagramsIdleSendInitialDelay"), Config.getInteger("REST.datagramsIdleSendPeriod"), TimeUnit.SECONDS);
        //exec.scheduleAtFixedRate(new MeasurementsIdleSend(localDataBaseMenager.getNewConnection()), Config.getInteger("REST.measurementsIdleSendInitialDelay"), Config.getInteger("REST.measurementsIdleSendPeriod"), TimeUnit.SECONDS);
    }

    public boolean isSessionWithLocalDB() {
        return localDataBaseMenager != null;
    }

    public void sendDatagrams() {
        sendDatagrams(null);
    }

    public void sendDatagrams(Connection c) {
        if (c == null) {
            c = connection;
        }
        if (restMenager != null) {
            try {
                Set<Datagram> datagramsToSend = localDataBaseMenager.getDatagramsToSend(c);
                SendStatistics stats = restMenager.sendDatagrams(datagramsToSend);
                DatagramsUtils.reportSendStats(stats, datagramsToSend);
                try {
                    localDataBaseMenager.updateDatagrams(c, datagramsToSend);
                    incrementCounters(stats);
                    try {
                        localDataBaseMenager.updateSession(c, this);
                    } catch (Exception ex) {
                        Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage()), LogTyps.ERROR);
                    }
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateDatagramsException", ex.getMessage()), LogTyps.ERROR);
                }
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.getDatagramsToSendException", ex.getMessage()), LogTyps.ERROR);
            }
        } else {
            Logger.write(Resources.getString("msg.session.restManagerNotFound"), LogTyps.WARNING);
        }
    }

    public boolean addDatagram(Datagram datagram) {
        if (localDataBaseMenager != null) {
            try {
                addDatagramsReceived(localDataBaseMenager.createDatagram(connection, datagram));
                try {
                    localDataBaseMenager.updateSession(connection, this);
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage()), LogTyps.ERROR);
                }
                return true;
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.createDatagramException", ex.getMessage()), LogTyps.ERROR);
            }
            return false;
        } else {
            try {
                SendStatistics stats = restMenager.sendDatagram(datagram);
                if (stats.getDatagramSendFailsCounter() > 0) {
                    Logger.write(Resources.getFormatString("msg.session.errorOnSendingDatagram", datagram.getNewErrorMessage()), LogTyps.ERROR);
                }
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.errorOnSendingDatagram", ex.getMessage()), LogTyps.ERROR);
            }
            return false;
        }
    }

    public boolean addDatagrams(Set<Datagram> datagrams) {
        if (localDataBaseMenager != null) {
            try {
                addDatagramsReceived(localDataBaseMenager.createDatagrams(connection, datagrams));
                try {
                    localDataBaseMenager.updateSession(connection, this);
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage()), LogTyps.ERROR);
                }
                return true;
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.createDatagramsException", ex.getMessage()), LogTyps.ERROR);
                return false;
            }
        } else {
            SendStatistics stats = restMenager.sendDatagrams(datagrams);
            DatagramsUtils.reportSendStats(stats, datagrams);
            return false;
        }
    }

    public void sendMeasurements() {
        sendMeasurements(connection);
    }

    public void sendMeasurements(Connection c) {
        if (restMenager != null) {
            try {
                Set<Measurement> measurementsToSend = localDataBaseMenager.getMeasurementsToSend(c);
                SendStatistics stats = restMenager.sendMeasurements(measurementsToSend);
                MeasurementUtils.reportSendStats(stats, measurementsToSend);
                try {
                    localDataBaseMenager.updateMeasurements(c, measurementsToSend);
                    incrementCounters(stats);
                    try {
                        localDataBaseMenager.updateSession(c, this);
                    } catch (Exception ex) {
                        Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage()), LogTyps.ERROR);
                    }
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateMeasurementsException", ex.getMessage()), LogTyps.ERROR);
                }
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.getMeasurementsToSendException", ex.getMessage()), LogTyps.ERROR);
            }
        } else {
            Logger.write(Resources.getString("msg.session.restManagerNotFound"), LogTyps.WARNING);
        }
    }

    public boolean addMeasurement(Measurement measurement) {
        if (localDataBaseMenager != null) {
            try {
                addMeasuresReceived(localDataBaseMenager.createMeasurement(connection, measurement));
                try {
                    localDataBaseMenager.updateSession(connection, this);
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage()), LogTyps.ERROR);
                }
                return true;
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.createMeasurementException", ex.getMessage()), LogTyps.ERROR);
                return false;
            }
        } else {
            try {
                SendStatistics stats = restMenager.sendMeasurement(measurement);
                if (stats.getMeasurementSendFailsCounter() > 0) {
                    Logger.write(Resources.getFormatString("msg.session.errorOnSendingMeasurement", measurement.getNewErrorMessage()), LogTyps.ERROR);
                }
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.errorOnSendingMeasurement", ex.getMessage(), LogTyps.ERROR));
            }
            return false;
        }
    }

    public boolean addMeasurements(Set<Measurement> measurements) {
        if (localDataBaseMenager != null) {
            try {
                addMeasuresReceived(localDataBaseMenager.createMeasurements(connection, measurements));
                try {
                    localDataBaseMenager.updateSession(connection, this);
                } catch (Exception ex) {
                    Logger.write(Resources.getFormatString("msg.session.updateSessionException", ex.getMessage(), LogTyps.ERROR));
                }
                return true;
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.createMeasurementsException", ex.getMessage(), LogTyps.ERROR));
                return false;
            }
        } else {
            SendStatistics stats = restMenager.sendMeasurements(measurements);
            MeasurementUtils.reportSendStats(stats, measurements);
            return false;
        }
    }

    public boolean closeSession() {
        if (exec != null) {
            exec.shutdown();
        }
        if (localDataBaseMenager != null) {
            try {
                localDataBaseMenager.removeSendData(connection);
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.removeSendDataException", ex.getMessage(), LogTyps.ERROR));
            }
            try {
                localDataBaseMenager.closeSession(connection, this);
            } catch (Exception ex) {
                Logger.write(Resources.getFormatString("msg.session.closeSessionException", ex.getMessage(), LogTyps.ERROR));
            }
            return true;
        }
        return false;
    }

    public void stopIddleSending() throws InterruptedException {
        exec.wait();
    }

    public void resumeIddleSending() throws InterruptedException {
        exec.notifyAll();
    }

    private void incrementCounters(SendStatistics statistics) {
        addDatagramsSend_OK(statistics.getDatagramSendOkCounter());
        addDatagramsSend_Failures(statistics.getDatagramSendFailsCounter());
        addMeasuresSend_OK(statistics.getMeasurementSendOkCounter());
        addMeasuresSend_Failures(statistics.getMeasurementSendFailsCounter());
    }

    public void sendHubStatus() {
        try {
            restMenager.sendHubStatus();
        } catch (Exception ex) {
            Logger.write(Resources.getFormatString("msg.session.sendHubStatusException", ex.getMessage(), LogTyps.ERROR));
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
    public LocalDataBaseManager getLocalDataBaseMenager() {
        return localDataBaseMenager;
    }

    /**
     * @param localDataBaseMenager the localDataBaseMenager to set
     */
    public void setLocalDataBaseMenager(LocalDataBaseManager localDataBaseMenager) {
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

    /**
     * @return the restMenager
     */
    public RestMenager getRestMenager() {
        return restMenager;
    }

    /**
     * @param restMenager the restMenager to set
     */
    public void setRestMenager(RestMenager restMenager) throws Exception {
        if (this.restMenager == null && restMenager != null) {
            createIddleSending();
        } else if (this.restMenager == null && restMenager != null && exec != null) {
            exec.shutdown();
            exec = null;
        }
        this.restMenager = restMenager;
    }

    /**
     * @return the measuresEnqueued
     */
    public int getMeasuresEnqueued() {
        return measuresEnqueued;
    }

    private void setMeasuresEnqueued(int measuresEnqueued) {
        this.measuresEnqueued = measuresEnqueued;
    }

    private void addMeasuresEnqueued(int measuresEnqueued) {
        setMeasuresEnqueued(getMeasuresEnqueued() + measuresEnqueued);
    }

    private void addMeasuresEnqueued() {
        this.addMeasuresEnqueued(1);
    }

    /**
     * @return the measuresReceived
     */
    public int getMeasuresReceived() {
        return measuresReceived;
    }

    private void setMeasuresReceived(int measuresReceived) {
        this.measuresReceived = measuresReceived;
    }

    private void addMeasuresReceived(int measuresReceived) {
        setMeasuresReceived(getMeasuresReceived() + measuresReceived);
    }

    private void addMeasuresReceived() {
        this.addMeasuresReceived(1);
    }

    /**
     * @return the measuresSend_OK
     */
    public int getMeasuresSend_OK() {
        return measuresSend_OK;
    }

    private void setMeasuresSend_OK(int measuresSend_OK) {
        this.measuresSend_OK = measuresSend_OK;
    }

    private void addMeasuresSend_OK(int measuresSend_OK) {
        setMeasuresSend_OK(getMeasuresSend_OK() + measuresSend_OK);
    }

    private void addMeasuresSend_OK() {
        this.addMeasuresSend_OK(1);
    }

    /**
     * @return the measuresSend_Failures
     */
    public int getMeasuresSend_Failures() {
        return measuresSend_Failures;
    }

    private void setMeasuresSend_Failures(int measuresSend_Failures) {
        this.measuresSend_Failures = measuresSend_Failures;
    }

    private void addMeasuresSend_Failures(int measuresSend_Failures) {
        setMeasuresSend_Failures(getMeasuresSend_Failures() + measuresSend_Failures);
    }

    private void addMeasuresSend_Failures() {
        this.addMeasuresSend_Failures(1);
    }

    private class DatagramsIdleSend implements Runnable {

        private Connection connection;

        DatagramsIdleSend(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                sendDatagrams(connection);
            } catch (Exception ex) {
                Logger.write(Resources.getString("msg.session.errorOnCreatingSendTask"), LogTyps.ERROR);
            }
        }
    }

    private class MeasurementsIdleSend implements Runnable {

        private Connection connection;

        MeasurementsIdleSend(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                sendMeasurements(connection);
            } catch (Exception ex) {
                Logger.write(Resources.getString("msg.session.errorOnCreatingSendTask"), LogTyps.ERROR);
            }
        }
    }

}
