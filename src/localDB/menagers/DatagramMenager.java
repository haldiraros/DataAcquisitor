/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
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

    public boolean createDatagram(Datagram datagram)
            throws ClassNotFoundException, SQLException, Exception {
        if (datagram.getId() == null) {
            String sql = "INSERT INTO DATAGRAMS(DATA) VALUES (?)";

            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setString(1, datagram.getData());
            cs.execute();
            cs.close();

            cs = getConnection().prepareCall(
                    "select last_insert_rowid()");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                datagram.setId(rs.getBigDecimal(1));
            }
            rs.close();
            cs.close();

            sql = "INSERT INTO Datagram_statistics(datagram_id) VALUES (?)";
            cs = getConnection().prepareCall(sql);
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
        String sql = "select dt.id"
                + "        , dt.MESSAGE"
                + "        , stat.is_send "
                + "     from Datagrams dt"
                + "        , Datagram_statistics stat "
                + "    where stat.datagram_id = dt.id "
                + "      and dt.id = (?) ";
        CallableStatement cs = getConnection().prepareCall(sql);
        cs.setBigDecimal(1, id);
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            BigDecimal datagramId = rs.getBigDecimal(1);
            String data = rs.getString(2);
            Boolean sendStat = rs.getBoolean(3);
            datagram = new Datagram(datagramId, data, sendStat);
        }
        rs.close();
        cs.close();
        return datagram;
    }

    public Set<Datagram> getDatagramsToSend()
            throws ClassNotFoundException, SQLException {
        Set<Datagram> datagrams = new HashSet<Datagram>();
        String sql = "select id"
                + "        , MESSAGE "
                + "     from Datagrams "
                + "    where id in "
                + "      (select datagram_id "
                + "         from Datagram_statistics"
                + "        where is_send = FALSE)";
        CallableStatement cs = getConnection().prepareCall(sql);
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
        if (datagram.getId() != null && datagram.isDataSend() != true) {
            String sql = "INSERT INTO SEND_HISTORY(DATAGRAM_ID,ERROR) VALUES (?,?)";

            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.setString(2, error.substring(0, 200));
            cs.execute();
            cs.close();

            BigDecimal errorId = null;
            cs = getConnection().prepareCall(
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

    public boolean setSendOK(Datagram datagram) throws SQLException {
        if (datagram.getId() != null && datagram.isDataSend() == true) {
            String sql = "UPDATE Datagram_statistics "
                    + " SET "
                    + " is_send = TRUE,"
                    + " send_attemps = send_attemps + 1"
                    + " WHERE "
                    + " datagram_id = (?)";
            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, datagram.getId());
            cs.execute();
            cs.close();
            return true;
        } else {
            return false;
        }
    }

    public Set<Datagram> getDatagramsToRemove()
            throws ClassNotFoundException, SQLException {
        Set<Datagram> datagrams = new HashSet<Datagram>();
        String sql = "select id"
                + "        , MESSAGE "
                + "     from Datagrams "
                + "    where id in "
                + "      (select datagram_id "
                + "         from Datagram_statistics"
                + "        where is_send = TRUE)";
        CallableStatement cs = getConnection().prepareCall(sql);
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            BigDecimal id = rs.getBigDecimal(1);
            String data = rs.getString(2);
            datagrams.add(new Datagram(id, data, true));
        }
        rs.close();
        cs.close();
        return datagrams;
    }

    public boolean removeSendDatagrams() throws SQLException, ClassNotFoundException {
        for(Datagram datagram : getDatagramsToRemove()){
            deleteDatagram(datagram);
        }
        return true;
     }
    
    public boolean processDatagram(Datagram datagram, String error) throws SQLException, Exception{
        if (datagram.getId() == null) {
            return createDatagram(datagram);
        }else if(datagram.getId() != null && datagram.isDataSend() == false){
            return reportSendErrorForDatagram(datagram, error);
        }else if(datagram.getId() != null && datagram.isDataSend() == true){
            return setSendOK(datagram);
        }
        else{
            throw new Exception("UNKNOWN DATAGRAM STATE! ["+datagram.toString()+"]"); 
        }
    }

    public boolean updateDatagram(Datagram datagram, String error) throws Exception{
        if(datagram.getId() != null && datagram.isDataSend() == false){
            return reportSendErrorForDatagram(datagram, error);
        }else if(datagram.getId() != null && datagram.isDataSend() == true){
            return setSendOK(datagram);
        }
        else{
            throw new Exception("UNKNOWN DATAGRAM STATE! ["+datagram.toString()+"]"); 
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
