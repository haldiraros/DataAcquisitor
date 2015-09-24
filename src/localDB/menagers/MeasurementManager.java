/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB.menagers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import project.data.Measurement;

/**
 *
 * @author hp
 */
public class MeasurementManager {

    private Connection connection;

    public MeasurementManager(Connection c) {
        connection = c;
    }

    public void createMeasurement(Measurement measurement)
            throws SQLException, Exception {
        if (measurement.getId() != null) {
            throw new SQLException("Storing Measurement failed, Measurement has ID:" + measurement.getId().toPlainString());
        } else {
            /*LOG*/ System.out.println("Start: createDatagram");
            String sql = "INSERT INTO Measurements(LOGGER_ID,HUB_ID,MEASUREMENT_TIME) VALUES (?,?,?)";
            PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, measurement.getLoggerId());
            ps.setString(2, measurement.getHubId());
            ps.setString(3, measurement.getMeasurmentTime());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Storing Measurement failed, no rows inserted.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    measurement.setId(generatedKeys.getBigDecimal(1));
                } else {
                    throw new SQLException("Storing Measurement failed, no ID obtained.");
                }
            }
            sql = "INSERT INTO Measurements_Data(Measurement_id,TAB_INDEX,TAB_VALUE) VALUES (?,?,?)";
            for (int i = 0; i < measurement.getMeasurments().length; i++) {
                ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setBigDecimal(1, measurement.getId());
                ps.setInt(2, i);
                ps.setInt(3, measurement.getMeasurments()[i]);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Storing Measurements_Data for Measurement with id "
                            + measurement.getId().toPlainString() + " failed.");
                }
            }
            sql = "INSERT INTO Measurement_statistics(Measurement_id) VALUES (?)";
            ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, measurement.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Storing Measurement_statistics for Measurement with id "
                        + measurement.getId().toPlainString() + " failed, no rows inserted.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Storing Measurement_statistics failed, no ID obtained.");
                }
            }
            ps.close();
            /*LOG*/ System.out.println("End: createMeasurement, created Measurement ID: " + measurement.getId().toPlainString());
        }
    }

    public Measurement getMeasurement(BigDecimal id)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getMeasurement, with ID: " + id.toPlainString());
        Measurement m = null;
        String sql = "select ms.id          ID"
                + "        , ms.LOGGER_ID   LOGGER_ID"
                + "        , ms.HUB_ID      HUB_ID"
                + "        , ms.MEASUREMENT_TIME   MEASUREMENT_TIME"
                + "        , stat.is_send   IS_SEND"
                + "     from Measurements ms"
                + "        , Measurement_statistics stat "
                + "    where stat.datagram_id = ms.id "
                + "      and ms.id = (?) ";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            m = new Measurement(rs.getBigDecimal("ID"), rs.getString("LOGGER_ID"), rs.getString("HUB_ID"), rs.getString("MEASUREMENT_TIME"), rs.getBoolean("IS_SEND"));
            getLastMeasurementError(m);
            getMeasurementData(m);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getMeasurement, with ID: " + id.toPlainString());
        return m;
    }

    public void getLastMeasurementError(Measurement m)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getLastMeasurementError, with ID: " + m.getId().toPlainString());
        String sql = "select err.error ERROR"
                + "     from Measurement_Errors_log err "
                + "        , Measurement_statistics stat "
                + "    where stat.measurement_id = (?) "
                + "      and err.id = stat.last_log_id ";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, m.getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            m.setPrevErrorMessage(rs.getString("ERROR"));
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getLastMeasurementError, with ID: " + m.getId().toPlainString());
    }

    public Set<Measurement> getMeasurementsToSend()
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getMeasurementsToSend");
        Set<Measurement> ms = new HashSet<Measurement>();
        String sql = "select id"
                + "        , LOGGER_ID "
                + "        , HUB_ID"
                + "        , MEASUREMENT_TIME"
                + "     from Measurements "
                + "    where id in "
                + "      (select measurement_id "
                + "         from measurement_statistics"
                + "        where is_send = 'FALSE')";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Measurement m = new Measurement(rs.getBigDecimal("id"), rs.getString("LOGGER_ID"), rs.getString("HUB_ID"), rs.getString("MEASUREMENT_TIME"));
            getLastMeasurementError(m);
            getMeasurementData(m);
            ms.add(m);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getMeasurementToSend, Measurements obtained: " + ms.size());
        return ms;
    }

    public boolean deleteMeasurement(Measurement m)
            throws SQLException {
        if (m.getId() == null) {
            throw new SQLException("Deleting Measurement failed, Measurement has no ID.");
        } else {
            /*LOG*/ System.out.println("Start: deleteMeasurement with id:" + m.getId());
            String sql = "DELETE FROM Measurement_statistics WHERE Measurement_id = (?)";
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, m.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Deleting from Measurement_statistics failed, no rows deleted.");
            }
            sql = "DELETE FROM Measurement_Errors_log WHERE Measurement_id = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, m.getId());
            ps.executeUpdate();
            sql = "DELETE FROM Measurements_Data WHERE Measurement_id = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, m.getId());
            ps.executeUpdate();
            sql = "DELETE FROM Measurements WHERE ID = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, m.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Deleting from Measurements failed, no rows deleted.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: deleteMeasurement with id:" + m.getId());
            return true;
        }
    }

    public boolean reportSendErrorForMeasurement(Measurement m)
            throws ClassNotFoundException, SQLException, Exception {
        if (m.getId() != null && m.isDataSend() != true) {
            /*LOG*/ System.out.println("Start: reportSendErrorForMeasurement with id:" + m.getId());
            String sql = "INSERT INTO Measurement_Errors_log(Measurement_id,ERROR) VALUES (?,?)";

            PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, m.getId());
            ps.setString(2, m.getNewErrorMessage() != null ? m.getNewErrorMessage().substring(0, Math.min(m.getNewErrorMessage().length(), 2000)) : null);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Creating Measurement_Errors_log failed, no rows affected.");
            }
            BigDecimal errorId = null;
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    errorId = generatedKeys.getBigDecimal(1);
                } else {
                    throw new SQLException("Creating SEND_HISTORY failed, no ID obtained.");
                }
            }
            sql = "UPDATE Measurement_statistics "
                    + " SET "
                    + " last_log_id = (?),"
                    + " send_attempts = send_attempts + 1"
                    + " WHERE "
                    + " Measurement_id = (?)";
            ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, errorId);
            ps.setBigDecimal(2, m.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Updating Measurement_statistics failed, no rows affected.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: reportSendErrorForMeasurement with id:" + m.getId());
            return true;
        } else {
            throw new SQLException("reportSendErrorForMeasurement failed for: " + m.getId().toPlainString() + "; Measurement.isDataSend(): " + m.isDataSend());
        }
    }

    public boolean setSendOK(Measurement m) throws SQLException {
        if (m.getId() != null && m.isDataSend() == true) {
            /*LOG*/ System.out.println("Start: setSendOK with id:" + m.getId());
            String sql = "UPDATE Measurement_statistics "
                    + " SET "
                    + " is_send = 'TRUE',"
                    + " send_attempts = send_attempts + 1"
                    + " WHERE "
                    + " Measurement_id = (?)";
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setBigDecimal(1, m.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Updating Measurement_statistics failed, no rows affected.");
            }
            ps.close();
            /*LOG*/ System.out.println("End: setSendOK with id:" + m.getId());
            return true;
        } else {
            throw new SQLException("setSendOK failed for: " + m.getId().toPlainString() + "; Measurement.isDataSend(): " + m.isDataSend());
        }
    }

    public Set<Measurement> getMeasurementsToRemove()
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getMeasurementsToRemove");
        Set<Measurement> ms = new HashSet<Measurement>();
        String sql = "select id"
                + "        , LOGGER_ID "
                + "        , HUB_ID"
                + "        , MEASUREMENT_TIME"
                + "     from Measurements "
                + "    where id in "
                + "      (select measurement_id "
                + "         from measurement_statistics"
                + "        where is_send = 'TRUE')";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Measurement m = new Measurement(rs.getBigDecimal("id"), rs.getString("LOGGER_ID"), rs.getString("HUB_ID"), rs.getString("MEASUREMENT_TIME"), true);
            getLastMeasurementError(m);
            getMeasurementData(m);
            ms.add(m);
        }
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getMeasurementToRemove, Measurements obtained: " + ms.size());
        return ms;
    }

    public boolean removeSendMeasurements() throws SQLException, ClassNotFoundException {
        /*LOG*/ System.out.println("Start: removeSendMeasurements");
        for (Measurement m : getMeasurementsToRemove()) {
            deleteMeasurement(m);
        }
        /*LOG*/ System.out.println("End: removeSendMeasurements");
        return true;
    }

    public boolean updateMeasurement(Measurement m) throws Exception {
        /*LOG*/ System.out.println("Start: updateMeasurement : " + m.getId().toPlainString());
        if (m.getId() != null && m.isDataSend() == false) {
            return reportSendErrorForMeasurement(m);
        } else if (m.getId() != null && m.isDataSend() == true) {
            return setSendOK(m);
        } else {
            throw new Exception("UNKNOWN DATAGRAM STATE! [" + m.toString() + "]");
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

    private void getMeasurementData(Measurement m)
            throws ClassNotFoundException, SQLException {
        /*LOG*/ System.out.println("Start: getMeasurementdata, with ID: " + m.getId().toPlainString());
        Integer rows = null;
        String sql = "select max(TAB_INDEX) TAB_INDEX, count(TAB_VALUE) COUNT_VALUES"
                + "     from Measurements_Data "
                + "    where Measurement_id = (?) ";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, m.getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Integer TAB_INDEX = rs.getInt("TAB_INDEX");
            Integer COUNT_VALUES = rs.getInt("COUNT_VALUES");
            if (TAB_INDEX + 1 == COUNT_VALUES) {
                rows = COUNT_VALUES;
            } else {
                throw new SQLException("getMeasurementData failed for Measurement with id=" + m.getId().toPlainString() + ";"
                        + "\n No of rows in DB="+COUNT_VALUES+"; Max TAB_INDEX="+TAB_INDEX);
            }
        }
        int[] data = new int[rows];
        sql = "select TAB_INDEX"
            + "     , TAB_VALUE"
            + "  from Measurements_Data "
            + " where Measurement_id = (?)";
        ps = getConnection().prepareStatement(sql);
        ps.setBigDecimal(1, m.getId());
        rs = ps.executeQuery();
        while (rs.next()) {
            data[rs.getInt("TAB_INDEX")] = rs.getInt("TAB_VALUE");
        }
        m.setMeasurments(data);
        rs.close();
        ps.close();
        /*LOG*/ System.out.println("End: getMeasurementdata, with ID: " + m.getId().toPlainString());
    }

}
