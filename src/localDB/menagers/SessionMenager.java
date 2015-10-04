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
import project.data.Session;

/**
 *
 * @author hp
 */
public class SessionMenager {

    public void createSession(Connection connection, Session session) throws SQLException, Exception {
        if (session.getId() == null) {
            /*LOG*/ // System.out.println("Start: createSession");
            //connection.setAutoCommit(true);
            String sql = "INSERT INTO Session_statistics(d_enqueued,d_received,d_send_ok,d_send_failures,m_enqueued,m_received,m_send_ok,m_send_failures) VALUES (?,?,?,?,?,?,?,?)";
            synchronized(connection){
            PreparedStatement cs = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            cs.setInt(1, session.getDatagramsEnqueued());
            cs.setInt(2, session.getDatagramsReceived());
            cs.setInt(3, session.getDatagramsSend_OK());
            cs.setInt(4, session.getDatagramsSend_Failures());
            cs.setInt(5, session.getMeasuresEnqueued());
            cs.setInt(6, session.getMeasuresReceived());
            cs.setInt(7, session.getMeasuresSend_OK());
            cs.setInt(8, session.getMeasuresSend_Failures());
            if (cs.executeUpdate() == 0) {
                throw new SQLException("Creating session failed, no rows created.");
            }
            try (ResultSet generatedKeys = cs.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getBigDecimal(1));
                } else {
                    throw new SQLException("Creating session failed, no ID obtained.");
                }
            }
            cs.close();
            
            connection.commit();
            }
            /*LOG*/ // System.out.println("End: createSession, session id: " + session.getId().toPlainString());
        } else {
            throw new SQLException("Creating session failed, session already has id: " + session.getId().toPlainString());
        }
    }

    public void updateSession(Connection connection, Session session) throws SQLException {
        if (session.isClosedOnLocalBD()) {
            throw new SQLException("Udating session failed, session is alrady closed");
        } else if (session.getId() == null) {
            throw new SQLException("Udating session failed, session has no ID!");
        }
        if (session.getId() != null && !session.isClosedOnLocalBD()) {
            /*LOG*/ // System.out.println("Start: updateSession, session id: " + session.getId().toPlainString());
            //connection.setAutoCommit(true);
            String sql = "UPDATE Session_statistics SET"
                    + " d_enqueued = (?),"
                    + " d_received = (?),"
                    + " d_send_ok = (?),"
                    + " d_send_failures = (?), "
                    + " m_enqueued = (?),"
                    + " m_received = (?),"
                    + " m_send_ok = (?),"
                    + " m_send_failures = (?), "
                    + " last_update = CURRENT_TIMESTAMP "
                    + " where id = (?)";
            synchronized(connection){
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getDatagramsEnqueued());
            ps.setInt(2, session.getDatagramsReceived());
            ps.setInt(3, session.getDatagramsSend_OK());
            ps.setInt(4, session.getDatagramsSend_Failures());
            ps.setInt(5, session.getMeasuresEnqueued());
            ps.setInt(6, session.getMeasuresReceived());
            ps.setInt(7, session.getMeasuresSend_OK());
            ps.setInt(8, session.getMeasuresSend_Failures());
            ps.setBigDecimal(9, session.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Udating session failed, no rows affected.");
            }
            ps.close();
            connection.commit();
            }
            /*LOG*/ // System.out.println("End: updateSession, session id: " + session.getId().toPlainString());
        }
    }

    public void closeSession(Connection connection, Session session) throws SQLException {
        if (session.isClosedOnLocalBD()) {
            throw new SQLException("Closing session failed, session is alrady closed");
        } else if (session.getId() == null) {
            throw new SQLException("Closing session failed, session has no ID.");
        }
        if (session.getId() != null && !session.isClosedOnLocalBD()) {
            /*LOG*/ // System.out.println("Start: closeSession, session id: " + session.getId().toPlainString());
            //connection.setAutoCommit(true);
            String sql = "UPDATE Session_statistics SET "
                    + " d_enqueued = (?),"
                    + " d_received = (?),"
                    + " d_send_ok = (?),"
                    + " d_send_failures = (?), "
                    + " m_enqueued = (?),"
                    + " m_received = (?),"
                    + " m_send_ok = (?),"
                    + " m_send_failures = (?), "
                    + " time_end = CURRENT_TIMESTAMP,"
                    + " last_update = CURRENT_TIMESTAMP "
                    + " where id = (?)";
            synchronized(connection){
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getDatagramsEnqueued());
            ps.setInt(2, session.getDatagramsReceived());
            ps.setInt(3, session.getDatagramsSend_OK());
            ps.setInt(4, session.getDatagramsSend_Failures());
            ps.setInt(5, session.getMeasuresEnqueued());
            ps.setInt(6, session.getMeasuresReceived());
            ps.setInt(7, session.getMeasuresSend_OK());
            ps.setInt(8, session.getMeasuresSend_Failures());
            ps.setBigDecimal(9, session.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Udating session failed, no rows affected.");
            }
            session.closeOnLocalBD();
            ps.close();
            connection.commit(); }
            /*LOG*/ // System.out.println("End: closeSession, session id: " + session.getId().toPlainString());
        }
    }

}
