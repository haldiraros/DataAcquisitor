/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import localDB.SetupDB;
import org.json.JSONException;
import project.Config;
import project.data.Datagram;
import project.data.Measurement;
import project.data.Session;

/**
 *
 * @author hp
 */
public class LocalDataBaseMenager {

    private Connection c;

    private String DBPatch;
    private DatagramMenager datagramMenager;
    private SessionMenager sessionMenager;
    private MeasurementManager measurementManager;

    private String getDBPatch() {
        return DBPatch;
    }

    private void setDBPatch(String dBPatch) {
        DBPatch = dBPatch;
    }

    public LocalDataBaseMenager() throws SQLException, ClassNotFoundException {
        this.setDBPatch(Config.getString("DBPath"));
        datagramMenager = new DatagramMenager(getConnection());
        sessionMenager = new SessionMenager(getConnection());
        measurementManager = new MeasurementManager(getConnection());
    }

    public Connection getConnection() throws SQLException {
        if (c != null) {
            return c;
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:"
                        + getDBPatch());
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            c.setAutoCommit(false);
            return c;
        }
    }

    public boolean fullTestBDExists() throws ClassNotFoundException, SQLException {
        String tables[] = {"Datagrams", "Datagram_statistics", "Datagram_Errors_log", "Session_statistics", "Measurements", "Measurement_statistics", "Measurement_Errors_log"};
        for (String table : tables) {
            try {
                String sql = "select count(*) from " + table + ";";
                Statement stmt = getConnection().createStatement();
                System.out.println(sql);
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    rs.getBigDecimal(1);
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean setupDataBase() throws ClassNotFoundException, SQLException {
        if (fullTestBDExists() == false) {
            new SetupDB().setupDB(getDBPatch(), getConnection());
            return fullTestBDExists();
        } else {
            throw new SQLException("LocalDB already exists!");
        }
    }

    public void createSession(Session session) throws Exception {
        getSessionMenager().createSession(session);
    }

    public void updateSession(Session session) throws Exception {
        getSessionMenager().updateSession(session);
    }

    public void closeSession(Session session) throws Exception {
        getSessionMenager().closeSession(session);
    }

    public int createDatagram(Datagram datagram) throws SQLException, Exception {
        Set<Datagram> ds = new HashSet<>();
        ds.add(datagram);
        return getDatagramMenager().createDatagrams(ds);
    }

    public int createDatagrams(Set<Datagram> datagrams) throws SQLException, Exception {
        return getDatagramMenager().createDatagrams(datagrams);
    }

    public Set<Datagram> getDatagramsToSend() throws ClassNotFoundException, SQLException {
        return getDatagramMenager().getDatagramsToSend();
    }

    public int updateDatagrams(Set<Datagram> datagrams) throws SQLException, Exception {
        return getDatagramMenager().updateDatagrams(datagrams);
    }

    public int createMeasurement(Measurement measurement) throws SQLException, Exception {
        Set<Measurement> ms = new HashSet<>();
        ms.add(measurement);
        return getMeasurementManager().createMeasurements(ms);
    }

    public int createMeasurements(Set<Measurement> measurements) throws SQLException, Exception {
        return getMeasurementManager().createMeasurements(measurements);
    }

    public Set<Measurement> getMeasurementsToSend() throws ClassNotFoundException, SQLException, JSONException {
        return getMeasurementManager().getMeasurementsToSend();
    }

    public int updateMeasurements(Set<Measurement> measurements) throws Exception {
        return getMeasurementManager().updateMeasurements(measurements);
    }

    public int removeSendData() throws SQLException, ClassNotFoundException {
        return getDatagramMenager().deleteSendDatagrams() + getMeasurementManager().deleteSendMeasurements();
    }

    /**
     * @return the measurementManager
     */
    public MeasurementManager getMeasurementManager() {
        return measurementManager;
    }

    /**
     * @param measurementManager the measurementManager to set
     */
    public void setMeasurementManager(MeasurementManager measurementManager) {
        this.measurementManager = measurementManager;
    }

    /**
     * @return the datagramMenager
     */
    public DatagramMenager getDatagramMenager() {
        return datagramMenager;
    }

    /**
     * @param datagramMenager the datagramMenager to set
     */
    public void setDatagramMenager(DatagramMenager datagramMenager) {
        this.datagramMenager = datagramMenager;
    }

    /**
     * @return the sessionMenager
     */
    public SessionMenager getSessionMenager() {
        return sessionMenager;
    }

    /**
     * @param sessionMenager the sessionMenager to set
     */
    public void setSessionMenager(SessionMenager sessionMenager) {
        this.sessionMenager = sessionMenager;
    }

}
