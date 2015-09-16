/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import localDB.SetupDB;

import project.Config;
import project.data.Datagram;
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
            return c;
        }
    }

    public boolean fullTestBDExists() throws ClassNotFoundException, SQLException {
        String tables[] = {"Datagrams", "Datagram_statistics", "Errors_log", "Session_statistics"};
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

    public void createDatagram(Datagram datagram) throws Exception {
        getDatagramMenager().createDatagram(datagram);
    }

    public void updateDatagram(Datagram datagram, String error) throws Exception {
        getDatagramMenager().updateDatagram(datagram, error);
    }

    public Set<Datagram> getDatagramsToSend() throws ClassNotFoundException, SQLException {
        return getDatagramMenager().getDatagramsToSend();
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
