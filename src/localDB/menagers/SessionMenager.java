/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import project.data.Session;

/**
 *
 * @author hp
 */
public class SessionMenager {

    private Connection connection;

    public SessionMenager(Connection c) {
        connection = c;
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

    public boolean createSession(Session session)
            throws ClassNotFoundException, SQLException, Exception {
        if (session.getId() == null) {
            String sql = "INSERT INTO Session(d_enqueued,d_received,d_send_ok,d_send_failures) VALUES (?,?,?,?)";

            PreparedStatement cs = getConnection().prepareCall(sql);
            cs.setInt(1, session.getDatagramsEnqueued());
            cs.setInt(2, session.getDatagramsReceived());
            cs.setInt(3, session.getDatagramsSend_OK());
            cs.setInt(4, session.getDatagramsSend_Failures());
            cs.execute();
            cs.close();

            cs = getConnection().prepareStatement(
                    "select last_insert_rowid()");
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                session.setId(rs.getBigDecimal(1));
            }
            rs.close();
            cs.close();

            return true;
        } else {
            return false;
        }
    }


    public boolean updateSession(Session session) throws SQLException{
        if (session.getId() != null) {
            String sql = "UPDATE Session_statistics SET"
                    + " d_enqueued = (?),"
                    + " d_received = (?),"
                    + " d_send_ok = (?),"
                    + " d_send_failures = (?), "
                    + " last_update = CURRENT_TIMESTAMP "
                    + " where id = (?)";
            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setInt(1, session.getDatagramsEnqueued());
            cs.setInt(2, session.getDatagramsReceived());
            cs.setInt(3, session.getDatagramsSend_OK());
            cs.setInt(4, session.getDatagramsSend_Failures());
            cs.setBigDecimal(5, session.getId());
            cs.execute();
            cs.close();
            
            return true;
        } else {
            return false;
        }
    }
    
    public boolean closeSession(Session session)
            throws ClassNotFoundException, SQLException {
        if (session.getId() != null) {
            String sql = "UPDATE Session_statistics SET "
                    + " time_end = CURRENT_TIMESTAMP,"
                    + " last_update = CURRENT_TIMESTAMP "
                    + " where id = (?)";
            CallableStatement cs = getConnection().prepareCall(sql);
            cs.setBigDecimal(1, session.getId());
            cs.execute();
            cs.close();
            
            return true;
        } else {
            return false;
        }
    }

}
