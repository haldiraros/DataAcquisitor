/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

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

    public int createDatagrams(Connection connection, Set<Datagram> datagrams) throws SQLException, Exception {
        int inserted = 0;
        String sql = "INSERT INTO DATAGRAMS(MESSAGE,HUB_ID,DATA_TIME) VALUES (?,?,?)";
        //connection.setAutoCommit(false);
        synchronized(connection){
        for (Datagram d : datagrams) {
            if (d.getId() == null) {
                /*LOG*/ // System.out.println("Start: createDatagram");
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, d.getData());
                ps.setString(2, d.getHubId());
                ps.setString(3, d.getDataTimestamp());
                int insert = ps.executeUpdate();
                inserted += insert;
                if (insert == 0) {
                    throw new SQLException("Storing Datagram failed, no rows inserted.");
                }
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        d.setId(generatedKeys.getBigDecimal(1));
                    } else {
                        throw new SQLException("Storing Datagram failed, no ID obtained.");
                    }
                }
                /*LOG*/ // System.out.println("End: createDatagram, created Datagram ID: " + datagram.getId().toPlainString());
                ps.close();
            }
        }
        connection.commit(); }
        //connection.setAutoCommit(true);
        return inserted;
    }

    public Set<Datagram> getDatagramsToSend(Connection connection)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ // System.out.println("Start: getDatagramsToSend");
        Set<Datagram> datagrams = new HashSet<>();
        String sql = "select dt.id          ID"
                + "        , dt.MESSAGE     MESSAGE"
                + "        , dt.HUB_ID      HUB_ID"
                + "        , dt.DATA_TIME   DATA_TIME"
                + "        , (select err.error from Datagram_Errors_log err where err.id = stat.last_log_id) ERROR "
                + "     from Datagrams dt"
                + "        , Datagram_statistics stat "
                + "    where stat.datagram_id = dt.id "
                + "      and stat.is_send = 'FALSE' ";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Datagram datagram = new Datagram(rs.getBigDecimal("id"), rs.getString("MESSAGE"), rs.getString("HUB_ID"), rs.getString("DATA_TIME"));
            datagram.setPrevErrorMessage(rs.getString("ERROR"));
            datagrams.add(datagram);
        }
        rs.close();
        ps.close();
        /*LOG*/ // System.out.println("End: getDatagramsToSend, datagrmas obtained: " + datagrams.size());
        return datagrams;
    }

    public int deleteSendDatagrams(Connection connection) throws SQLException {
        /*LOG*/ // System.out.println("Start: deleteSendDatagrams");
        //connection.setAutoCommit(false);
        String sql = "DELETE FROM DATAGRAMS WHERE ID in (select datagram_id from Datagram_statistics where is_send = 'TRUE')";
        int deleted =0;
        synchronized(connection){
        PreparedStatement ps = connection.prepareStatement(sql);
        deleted = ps.executeUpdate();
        ps.close();
        connection.commit();}
        //connection.setAutoCommit(true);
        return deleted;
        /*LOG*/ // System.out.println("End: deleteSendDatagrams");
    }

    private int reportSendErrorForDatagram(Connection connection, Set<Datagram> datagrams) throws SQLException {
        int updated = 0;
        //connection.setAutoCommit(false);
        String sql = "INSERT INTO Datagram_Errors_log(DATAGRAM_ID,ERROR) VALUES (?,?)";
        synchronized(connection){
        for (Datagram datagram : datagrams) {
            if (datagram.getId() != null && datagram.isDataSend() != true && datagram.getNewErrorMessage() != null) {
                /*LOG*/ // System.out.println("Start: reportSendErrorForDatagram with id:" + datagram.getId());
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setBigDecimal(1, datagram.getId());
                ps.setString(2, datagram.getNewErrorMessage() != null ? datagram.getNewErrorMessage().substring(0, Math.min(datagram.getNewErrorMessage().length(), 2000)) : null);
                updated += ps.executeUpdate();
                datagram.setPrevErrorMessage(datagram.getNewErrorMessage());
                datagram.setNewErrorMessage(null);
                ps.close();
                /*LOG*/ // System.out.println("End: reportSendErrorForDatagram with id:" + datagram.getId());
            }
        }
        connection.commit();}
        //connection.setAutoCommit(true);
        return updated;
    }

    private int setSendOK(Connection connection, Set<Datagram> datagrams) throws SQLException {
        int updated = 0;
        //connection.setAutoCommit(false);
        String sql = "UPDATE Datagram_statistics "
                + " SET "
                + " is_send = 'TRUE',"
                + " send_attempts = send_attempts + 1"
                + " WHERE "
                + " datagram_id = (?)";
        synchronized(connection){
        for (Datagram datagram : datagrams) {
            if (datagram.getId() != null && datagram.isDataSend() == true) {
                /*LOG*/ // System.out.println("Start: setSendOK with id:" + datagram.getId());
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setBigDecimal(1, datagram.getId());
                updated += ps.executeUpdate();
                ps.close();
                /*LOG*/ // System.out.println("End: setSendOK with id:" + datagram.getId());
            }
        }
        connection.commit();}
        //connection.setAutoCommit(true);
        return updated;
    }

    public int updateDatagrams(Connection connection, Set<Datagram> datagrams) throws SQLException {
        return setSendOK(connection, datagrams) + reportSendErrorForDatagram(connection, datagrams);
    }

}
