/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import project.data.Measurement;

/**
 *
 * @author hp
 */
public class MeasurementManager {

    public int createMeasurements(Connection connection, Set<Measurement> measurements) throws SQLException, Exception {
        int inserted = 0;
        String sql = "INSERT INTO Measurements(LOGGER_ID,HUB_ID,MEASUREMENT_TIME,PERIOD,DATA) VALUES (?,?,?,?,?)";
        //connection.setAutoCommit(false);
        synchronized (connection) {
            for (Measurement m : measurements) {
                if (m.getId() == null) {
                    /*LOG*/ //System.out.println("Start: createDatagram");
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, m.getLoggerId());
                    ps.setString(2, m.getHubId());
                    ps.setString(3, m.getMeasurmentTime());
                    ps.setInt(4, m.getPeriod());
                    ps.setString(5, m.getData().toString());
                    int insert = ps.executeUpdate();
                    inserted += insert;
                    if (insert == 0) {
                        throw new SQLException("Storing Measurement failed, no rows inserted.");
                    }
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            m.setId(generatedKeys.getBigDecimal(1));
                        } else {
                            throw new SQLException("Storing Measurement failed, no ID obtained.");
                        }
                    }
                    /*LOG*/ //System.out.println("End: createMeasurement, created Measurement ID: " + m.getId().toPlainString());
                    ps.close();
                }
            }

            connection.commit();
        }
        //connection.setAutoCommit(true);
        return inserted;
    }

    public Set<Measurement> getMeasurementsToSend(Connection connection)
            throws ClassNotFoundException, SQLException, JSONException {
        /*LOG*/ //System.out.println("Start: getMeasurementsToSend");
        Set<Measurement> measurements = new HashSet<>();
        String sql = "select ms.id                  ID"
                + "        , ms.LOGGER_ID           LOGGER_ID"
                + "        , ms.HUB_ID              HUB_ID"
                + "        , ms.MEASUREMENT_TIME    MEASUREMENT_TIME"
                + "        , ms.DATA                DATA"
                + "        , ms.PERIOD              PERIOD"
                + "        , (select err.error from measurement_Errors_log err where err.id = stat.last_log_id) ERROR "
                + "     from Measurements ms"
                + "        , measurement_statistics stat"
                + "    where ms.id = stat.measurement_id "
                + "      and stat.is_send = 'FALSE'";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Measurement measurement = new Measurement(rs.getBigDecimal("ID"), rs.getString("LOGGER_ID"), rs.getString("HUB_ID"), rs.getString("MEASUREMENT_TIME"), rs.getString("DATA"), rs.getInt("PERIOD"));
            measurement.setPrevErrorMessage(rs.getString("ERROR"));
            measurements.add(measurement);
        }
        rs.close();
        ps.close();
        /*LOG*/ //System.out.println("End: getMeasurementToSend, Measurements obtained: " + measurements.size());
        return measurements;
    }

    public int deleteSendMeasurements(Connection connection) throws SQLException {
        /*LOG*/ //System.out.println("Start: deleteSendMeasurements");
        //connection.setAutoCommit(false);
        String sql = "DELETE FROM Measurements WHERE ID in (select Measurement_id from Measurement_statistics where is_send = 'TRUE')";
        int deleted;
        synchronized (connection) {
            PreparedStatement ps = connection.prepareStatement(sql);
            deleted = ps.executeUpdate();
            ps.close();

            connection.commit();
        }
        //connection.setAutoCommit(true);
        return deleted;
        /*LOG*/ // System.out.println("End: deleteSendMeasurements");
    }

    private int reportSendErrorForMeasurements(Connection connection, Set<Measurement> measurements) throws SQLException {
        int updated = 0;
        //connection.setAutoCommit(false);
        String sql = "INSERT INTO Measurement_Errors_log(Measurement_ID,ERROR) VALUES (?,?)";
        synchronized (connection) {
            for (Measurement measurement : measurements) {
                if (measurement.getId() != null && measurement.isDataSend() != true && measurement.getNewErrorMessage() != null) {
                    /*LOG*/ // System.out.println("Start: reportSendErrorForMeasurement with id:" + measurement.getId());
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setBigDecimal(1, measurement.getId());
                    ps.setString(2, measurement.getNewErrorMessage() != null ? measurement.getNewErrorMessage().substring(0, Math.min(measurement.getNewErrorMessage().length(), 2000)) : null);
                    updated += ps.executeUpdate();
                    measurement.setPrevErrorMessage(measurement.getNewErrorMessage());
                    measurement.setNewErrorMessage(null);
                    ps.close();
                    /*LOG*/ // System.out.println("End: reportSendErrorForMeasurement with id:" + measurement.getId());
                }
            }

            connection.commit();
        }
        //connection.setAutoCommit(true);
        return updated;
    }

    private int setSendOK(Connection connection, Set<Measurement> measurements) throws SQLException {
        int updated = 0;
        //connection.setAutoCommit(false);
        String sql = "UPDATE Measurement_statistics "
                + " SET "
                + " is_send = 'TRUE',"
                + " send_attempts = send_attempts + 1"
                + " WHERE "
                + " measurement_id = (?)";
        synchronized (connection) {
            for (Measurement measurement : measurements) {
                if (measurement.getId() != null && measurement.isDataSend() == true) {
                    /*LOG*/ // System.out.println("Start: setSendOK with id:" + measurement.getId());
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setBigDecimal(1, measurement.getId());
                    updated += ps.executeUpdate();
                    ps.close();
                    /*LOG*/ // System.out.println("End: setSendOK with id:" + measurement.getId());
                }
            }

            connection.commit();
        }
        //connection.setAutoCommit(true);
        return updated;
    }

    public int updateMeasurements(Connection connection, Set<Measurement> measurements) throws SQLException {
        return setSendOK(connection, measurements) + reportSendErrorForMeasurements(connection, measurements);
    }

}
