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
        this.setDBPatch(Config.getPath("localDB.menagers.DataBaseFilePath"));
        datagramMenager = new DatagramMenager();
        sessionMenager = new SessionMenager();
        measurementManager = new MeasurementManager();
    }

    public Connection getNewConnection() throws SQLException, ClassNotFoundException {
        Connection c = null;
        Class.forName(Config.getString("localDB.menagers.jdbcClass"));
        c = DriverManager.getConnection(Config.getString("localDB.menagers.jdbcPath")
                + getDBPatch());
        c.setAutoCommit(false);
        return c;
    }

    public boolean fullTestBDExists() throws ClassNotFoundException, SQLException {
        String tables[] = {"Datagrams", "Datagram_statistics", "Datagram_Errors_log", "Session_statistics", "Measurements", "Measurement_statistics", "Measurement_Errors_log"};
        for (String table : tables) {
            try {
                String sql = "select count(*) from " + table + ";";
                Statement stmt = getNewConnection().createStatement();
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
            new SetupDB().setupDB(getDBPatch(), getNewConnection());
            return fullTestBDExists();
        } else {
            throw new SQLException("LocalDB already exists!");
        }
    }

    public void createSession(Connection c, Session session) throws Exception {
        getSessionMenager().createSession(c,session);
    }

    public void updateSession(Connection c, Session session) throws Exception {
        getSessionMenager().updateSession(c,session);
    }

    public void closeSession(Connection c, Session session) throws Exception {
        getSessionMenager().closeSession(c,session);
    }

    public int createDatagram(Connection c, Datagram datagram) throws SQLException, Exception {
        Set<Datagram> ds = new HashSet<>();
        ds.add(datagram);
        return getDatagramMenager().createDatagrams(c,ds);
    }

    public int createDatagrams(Connection c, Set<Datagram> datagrams) throws SQLException, Exception {
        return getDatagramMenager().createDatagrams(c,datagrams);
    }

    public Set<Datagram> getDatagramsToSend(Connection c) throws ClassNotFoundException, SQLException {
        return getDatagramMenager().getDatagramsToSend(c);
    }

    public int updateDatagrams(Connection c, Set<Datagram> datagrams) throws SQLException, Exception {
        return getDatagramMenager().updateDatagrams(c,datagrams);
    }

    public int createMeasurement(Connection c, Measurement measurement) throws SQLException, Exception {
        Set<Measurement> ms = new HashSet<>();
        ms.add(measurement);
        return getMeasurementManager().createMeasurements(c,ms);
    }

    public int createMeasurements(Connection c, Set<Measurement> measurements) throws SQLException, Exception {
        return getMeasurementManager().createMeasurements(c,measurements);
    }

    public Set<Measurement> getMeasurementsToSend(Connection c) throws ClassNotFoundException, SQLException, JSONException {
        return getMeasurementManager().getMeasurementsToSend(c);
    }

    public int updateMeasurements(Connection c, Set<Measurement> measurements) throws Exception {
        return getMeasurementManager().updateMeasurements(c,measurements);
    }

    public int removeSendData(Connection c) throws SQLException, ClassNotFoundException {
        return getDatagramMenager().deleteSendDatagrams(c) + getMeasurementManager().deleteSendMeasurements(c);
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
