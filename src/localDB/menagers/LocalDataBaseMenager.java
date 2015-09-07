/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import localDB.SetupDB;

import project.Settings;
import project.data.Datagram;
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
        this.setDBPatch(Settings.getString("DBPath"));
        datagramMenager = new DatagramMenager(getConnection());
        sessionMenager = new SessionMenager(getConnection());
    }

    public Connection getConnection() throws SQLException {
        if (c != null) {
            return c;
        } else {
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + getDBPatch());
            return c;
        }
    }

    protected boolean testBDExists() throws ClassNotFoundException {
        Class.forName(Settings.getString("sqliteJDBC"));
        File BDFile = new File(getDBPatch());
        if (BDFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public void setupDataBase() throws ClassNotFoundException, SQLException {
        if (testBDExists() == false) {
            new SetupDB().setupDB(getConnection());
        }
    }

    public boolean createDatagram(Datagram datagram)
            throws Exception {
        if (testBDExists() == true) {
            return getDatagramMenager().createDatagram(datagram);
        }
        return false;
    }
    
    public boolean updateDatagram(Datagram datagram, String error)
            throws Exception {
        if (testBDExists() == true) {
            return getDatagramMenager().updateDatagram(datagram, error);
        }
        return false;
    }

    public Set<Datagram> getDatagramsToSend()
            throws ClassNotFoundException, SQLException {
        if (testBDExists() == true) {
            return getDatagramMenager().getDatagramsToSend();
        }
        return null;
    }  

    public boolean createSession(Session session)
            throws Exception {
        if (testBDExists() == true) {
            return getSessionMenager().createSession(session);
        }
        return false;
    }

    public boolean updateSession(Session session)
            throws Exception {
        if (testBDExists() == true) {
            return getSessionMenager().updateSession(session);
        }
        return false;
    }

    public boolean closeSession(Session session)
            throws Exception {
        if (testBDExists() == true) {
            return getSessionMenager().closeSession(session);
        }
        return false;
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
