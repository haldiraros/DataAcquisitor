/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import hubGui.logging.LogTyps;
import hubGui.logging.Logger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import project.data.Datagram;

/**
 *
 * @author hp
 */
public class DatagramMenager {

    private Connection connection;

    public DatagramMenager(Connection c) {
        connection = c;
    }

    public void createDatagram(Datagram datagram)
            throws SQLException, Exception {
        if (datagram.getId() != null) {
            throw new SQLException("Storing Datagram failed, Datagram has ID:" + datagram.getId().toPlainString());
        } else {
            /*LOG*/ System.out.println("Start: createDatagram");
            String sql = "INSERT INTO DATAGRAMS(MESSAGE,HUB_ID,DATA_TIME) VALUES (?,?,?)";
            PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, datagram.getData());
            ps.setString(2, datagram.getHubId());
            ps.setString(3, datagram.getDataTimestamp());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Storing Datagram failed, no rows inserted.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    datagram.setId(generatedKeys.getBigDecimal(1));
                } else {
                    throw new SQLException("Storing Datagram failed, no ID obtained.");
                }
            }

            sql = "INSERT INTO Datagram_statistics(datagram_id) VALUES (?)";
            ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, datagram.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Storing Datagram_statistics for Datagram with id "
                        + datagram.getId().toPlainString() + " failed, no rows inserted.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Storing Datagram_statistics failed, no ID obtained.");
                }
            }
            ps.close();
            /*LOG*/ System.out.println("End: createDatagram, created Datagram ID: " + datagram.getId().toPlainString());
        }
    }

    public Datagram getDatagram(BigDecimal id)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getDatagram, with ID: " + id.toPlainString());
        Datagram datagram = null;
        String sql = "select dt.id          ID"
                + "        , dt.MESSAGE     MESSAGE"
                + "        , dt.HUB_ID      HUB_ID"
                + "        , dt.DATA_TIME   DATA_TIME"
                + "        , stat.is_send   IS_SEND"
                + "     from Datagrams dt"
                + "        , Datagram_statistics stat "
                + "    where stat.datagram_id = dt.id "
                + "      and dt.id = (?) ";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            datagram = new Datagram(rs.getBigDecimal("ID"), rs.getString("MESSAGE"), rs.getString("HUB_ID"), rs.getString("DATA_TIME"), rs.getBoolean("IS_SEND"));
            getLastDatagramError(datagram);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getDatagram, with ID: " + id.toPlainString());
        return datagram;
    }

    public void getLastDatagramError(Datagram d)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getLastDatagramError, with ID: " + d.getId().toPlainString());
        Datagram datagram = null;
        String sql = "select err.error ERROR"
                + "     from Datagram_Errors_log err "
                + "        , Datagram_statistics stat "
                + "    where stat.datagram_id = (?) "
                + "      and err.id = stat.last_log_id ";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, d.getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            datagram.setPrevErrorMessage(rs.getString("ERROR"));
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getLastDatagramError, with ID: " + d.getId().toPlainString());
    }

    public Set<Datagram> getDatagramsToSend()
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getDatagramsToSend");
        Set<Datagram> datagrams = new HashSet<Datagram>();
        String sql = "select id"
                + "        , MESSAGE "
                + "        , HUB_ID"
                + "        , DATA_TIME"
                + "     from Datagrams "
                + "    where id in "
                + "      (select datagram_id "
                + "         from Datagram_statistics"
                + "        where is_send = 'FALSE')";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Datagram datagram = new Datagram(rs.getBigDecimal("id"), rs.getString("MESSAGE"), rs.getString("HUB_ID"), rs.getString("DATA_TIME"));
            getLastDatagramError(datagram);
            datagrams.add(datagram);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getDatagramsToSend, datagrmas obtained: " + datagrams.size());
        return datagrams;
    }

    public boolean deleteDatagram(Datagram datagram)
            throws SQLException {
        if (datagram.getId() == null) {
            throw new SQLException("Deleting Datagram failed, Datagram has no ID.");
        } else {
            /*LOG*/ System.out.println("Start: deleteDatagram with id:" + datagram.getId());
            String sql = "DELETE FROM Datagram_statistics WHERE datagram_id = (?)";
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, datagram.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Deleting from Datagram_statistics failed, no rows deleted.");
            }
            sql = "DELETE FROM Datagram_Errors_log WHERE datagram_id = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, datagram.getId());
            ps.executeUpdate();
            sql = "DELETE FROM DATAGRAMS WHERE ID = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, datagram.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Deleting from Datagram_statistics failed, no rows deleted.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: deleteDatagram with id:" + datagram.getId());
            return true;
        }
    }

    public boolean reportSendErrorForDatagram(Datagram datagram)
            throws ClassNotFoundException, SQLException, Exception {
        if (datagram.getId() != null && datagram.isDataSend() != true) {
            /*LOG*/ System.out.println("Start: reportSendErrorForDatagram with id:" + datagram.getId());
            String sql = "INSERT INTO Datagram_Errors_log(DATAGRAM_ID,ERROR) VALUES (?,?)";

            PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, datagram.getId());
            ps.setString(2, datagram.getNewErrorMessage() != null ? datagram.getNewErrorMessage().substring(0, Math.min(datagram.getNewErrorMessage().length(), 2000)) : null);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Creating Datagram_Errors_log failed, no rows affected.");
            }
            BigDecimal errorId = null;
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    errorId = generatedKeys.getBigDecimal(1);
                } else {
                    throw new SQLException("Creating SEND_HISTORY failed, no ID obtained.");
                }
            }
            sql = "UPDATE Datagram_statistics "
                    + " SET "
                    + " last_log_id = (?),"
                    + " send_attempts = send_attempts + 1"
                    + " WHERE "
                    + " datagram_id = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, errorId);
            ps.setBigDecimal(2, datagram.getId());
            if (ps.executeUpdate() == 0) {
                Logger.write("datagram.getId():" + datagram.getId(), LogTyps.ERROR);
                throw new SQLException("Updating Datagram_statistics failed, no rows affected.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: reportSendErrorForDatagram with id:" + datagram.getId());
            return true;
        } else {
            throw new SQLException("reportSendErrorForDatagram failed for: " + datagram.getId().toPlainString() + "; datagram.isDataSend(): " + datagram.isDataSend());
        }
    }

    public boolean setSendOK(Datagram datagram) throws SQLException {
        if (datagram.getId() != null && datagram.isDataSend() == true) {
            /*LOG*/ System.out.println("Start: setSendOK with id:" + datagram.getId());
            String sql = "UPDATE Datagram_statistics "
                    + " SET "
                    + " is_send = 'TRUE',"
                    + " send_attempts = send_attempts + 1"
                    + " WHERE "
                    + " datagram_id = (?)";
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, datagram.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Updating Datagram_statistics failed, no rows affected.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: setSendOK with id:" + datagram.getId());
            return true;
        } else {
            throw new SQLException("setSendOK failed for datagram.getId: " + datagram.getId().toPlainString() + "; datagram.isDataSend(): " + datagram.isDataSend());
        }
    }

    public Set<Datagram> getDatagramsToRemove()
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getDatagramsToRemove");
        Set<Datagram> datagrams = new HashSet<Datagram>();
        String sql = "select ID"
                + "        , MESSAGE "
                + "        , HUB_ID"
                + "        , DATA_TIME"
                + "     from Datagrams "
                + "    where id in "
                + "      (select datagram_id "
                + "         from Datagram_statistics"
                + "        where is_send = 'TRUE')";
        PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Datagram datagram = new Datagram(rs.getBigDecimal("id"), rs.getString("MESSAGE"), rs.getString("HUB_ID"), rs.getString("DATA_TIME"), true);
            getLastDatagramError(datagram);
            datagrams.add(datagram);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getDatagramsToRemove, selected: " + datagrams.size());
        return datagrams;
    }

    public boolean removeSendDatagrams() throws SQLException, ClassNotFoundException {
        /*LOG*/ System.out.println("Start: removeSendDatagrams");
        for (Datagram datagram : getDatagramsToRemove()) {
            deleteDatagram(datagram);
        }
        /*LOG*/ System.out.println("End: removeSendDatagrams");
        return true;
    }

    public void processDatagram(Datagram datagram) throws SQLException, Exception {
        /*LOG*/ System.out.println("Start: processDatagram : " + datagram.getId().toPlainString());
        if (datagram.getId() == null) {
            createDatagram(datagram);
        } else if (datagram.getId() != null && datagram.isDataSend() == false) {
            reportSendErrorForDatagram(datagram);
        } else if (datagram.getId() != null && datagram.isDataSend() == true) {
            setSendOK(datagram);
        } else {
            throw new Exception("UNKNOWN DATAGRAM STATE! [" + datagram.toString() + "]");
        }
    }

    public boolean updateDatagram(Datagram datagram) throws Exception {
        /*LOG*/ System.out.println("Start: updateDatagram : " + datagram.getId().toPlainString());
        if (datagram.getId() != null && datagram.isDataSend() == false) {
            return reportSendErrorForDatagram(datagram);
        } else if (datagram.getId() != null && datagram.isDataSend() == true) {
            return setSendOK(datagram);
        } else {
            throw new Exception("UNKNOWN DATAGRAM STATE! [" + datagram.toString() + "]");
        }
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
