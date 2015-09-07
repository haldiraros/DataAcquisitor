/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public boolean saveDatagram(Datagram datagram)
            throws ClassNotFoundException, SQLException, Exception {
        if (datagram.getId() == null) {
            String sql = "INSERT INTO DATAGRAMS(DATA) VALUES (?)";

            PreparedStatement cs = getConnection().prepareCall(sql);
            cs.setString(1, datagram.getData());
            cs.execute();
            cs.close();

            cs = getConnection().prepareStatement(
                    "select last_insert_rowid()");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                datagram.setId(rs.getBigDecimal(1));
            }
            rs.close();
            cs.close();

            sql = "INSERT INTO Datagram_statistics(datagram_id) VALUES (?)";
            cs = getConnection().prepareStatement(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.execute();
            cs.close();
            
            return true;
        } else {
            return false;
        }
    }

    public Datagram getDatagram(BigDecimal id)
            throws ClassNotFoundException, SQLException {
        Datagram datagram = null;
        String sql = "select * from Datagrams where id = ?";
        PreparedStatement cs = getConnection().prepareStatement(sql);
        cs.setBigDecimal(1, id);
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            BigDecimal datagramId = rs.getBigDecimal(1);
            String data = rs.getString(2);
            datagram = new Datagram(datagramId, data);
        }
        rs.close();
        cs.close();
        return datagram;
    }

    public Set<Datagram> getDatagramsToSend()
            throws ClassNotFoundException, SQLException {
        Set<Datagram> datagrams = new HashSet<Datagram>();
        String sql = "select * from Datagrams";
        PreparedStatement cs = getConnection().prepareStatement(sql);
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            BigDecimal id = rs.getBigDecimal(1);
            String data = rs.getString(2);
            datagrams.add(new Datagram(id, data));
        }
        rs.close();
        cs.close();
        return datagrams;
    }

    public boolean deleteDatagram(Datagram datagram)
            throws ClassNotFoundException, SQLException {
        if (datagram.getId() != null) {
            String sql = "DELETE FROM Datagram_statistics WHERE datagram_id = (?)";
            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.execute();
            cs.close();

            sql = "DELETE FROM Errors_log WHERE datagram_id = (?)";
            cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.execute();
            cs.close();

            sql = "DELETE FROM DATAGRAMS WHERE ID = (?)";
            cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.execute();
            cs.close();

            return true;
        } else {
            return false;
        }
    }

    public boolean reportSendErrorForDatagram(Datagram datagram, String error)
            throws ClassNotFoundException, SQLException, Exception {
        if (datagram.getId() != null) {
            String sql = "INSERT INTO SEND_HISTORY(DATAGRAM_ID,ERROR) VALUES (?,?)";

            PreparedStatement cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.setString(2, error.substring(0, 200));
            cs.execute();
            cs.close();

            BigDecimal errorId = null;
            cs = getConnection().prepareStatement(
                    "select last_insert_rowid()");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                errorId = rs.getBigDecimal(1);
            }
            rs.close();
            cs.close();

            sql = "UPDATE Datagram_statistics "
                    + " SET "
                    + " last_log_id = (?),"
                    + " send_attemps = send_attemps + 1"
                    + " WHERE "
                    + " datagram_id = (?)";
            cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, errorId);
            cs.setBigDecimal(2, datagram.getId());
            cs.execute();
            cs.close();
           
            return true;
        } else {
            return false;
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
