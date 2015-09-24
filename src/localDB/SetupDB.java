/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author hp
 */
public class SetupDB {

    private final String createDatagramsTable
            = "CREATE TABLE Datagrams( "
            + " id             integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",MESSAGE        varchar(128)   NOT NULL"
            + ",HUB_ID         varchar(128)   NOT NULL"
            + ",DATA_TIME      varchar(128)   NOT NULL"
            + ")";

    private final String createDatagramsStatisticsTable
            = "CREATE TABLE Datagram_statistics ("
            + " id             INTEGER        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",datagram_id    INTEGER        NOT NULL "
            + ",last_log_id    INTEGER                  DEFAULT NULL"
            + ",Received       DATETIME                 DEFAULT CURRENT_TIMESTAMP"
            + ",send_attempts  INTEGER                  DEFAULT 0"
            + ",is_send        BOOLEAN        NOT NULL  DEFAULT FALSE"
            + ",FOREIGN KEY (datagram_id) REFERENCES Datagrams (id)"
            + ",FOREIGN KEY (last_log_id) REFERENCES Datagram_Errors_log (id)"
            + ")";

    private final String createDatagramsLogTable
            = "CREATE TABLE Datagram_Errors_log ("
            + " id             integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",datagram_id    integer        NOT NULL"
            + ",time           datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP"
            + ",type           integer"
            + ",error          varchar(2000)"
            + ",FOREIGN KEY (datagram_id) REFERENCES Datagrams (id)"
            + ")";
    
    private final String createMeasurementsTable
            = "CREATE TABLE Measurements( "
            + " ID               integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",LOGGER_ID        varchar(128)   NOT NULL"
            + ",HUB_ID           varchar(128)   NOT NULL"
            + ",MEASUREMENT_TIME varchar(128)   NOT NULL"
            + ")";
    
    private final String createMeasurementsDataTable
            = "CREATE TABLE Measurements_Data( "
            + " ID              integer   NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",Measurement_id  integer   NOT NULL"
            + ",TAB_INDEX       integer   NOT NULL"
            + ",TAB_VALUE       integer   NOT NULL"
            + ",FOREIGN KEY (Measurement_id) REFERENCES Measurements (id)"
            + ")";

    private final String createMeasurementsStatisticsTable
            = "CREATE TABLE Measurement_statistics ("
            + " ID             INTEGER        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",Measurement_id INTEGER        NOT NULL "
            + ",last_log_id    INTEGER                  DEFAULT NULL"
            + ",received       DATETIME                 DEFAULT CURRENT_TIMESTAMP"
            + ",send_attempts  INTEGER                  DEFAULT 0"
            + ",is_send        BOOLEAN        NOT NULL  DEFAULT FALSE"
            + ",FOREIGN KEY (Measurement_id) REFERENCES Measurements (id)"
            + ",FOREIGN KEY (last_log_id)    REFERENCES Measurement_Errors_log (id)"
            + ")";

    private final String createMeasurementsLogTable
            = "CREATE TABLE Measurement_Errors_log ("
            + " id             integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",Measurement_id integer        NOT NULL"
            + ",time           datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP"
            + ",type           integer"
            + ",error          varchar(2000)"
            + ",FOREIGN KEY (Measurement_id) REFERENCES Measurements (id)"
            + ")";

    private final String createSessionStatisticsTable
            = "CREATE TABLE Session_statistics ("
            + " id              integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",time_start      datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP"
            + ",time_end        datetime                 DEFAULT NULL"
            + ",last_update     datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP"
            + ",d_enqueued      integer        NOT NULL  DEFAULT 0"
            + ",d_received      integer        NOT NULL  DEFAULT 0"
            + ",d_send_ok       integer        NOT NULL  DEFAULT 0"
            + ",d_send_failures integer        NOT NULL  DEFAULT 0"
            + ",m_enqueued      integer        NOT NULL  DEFAULT 0"
            + ",m_received      integer        NOT NULL  DEFAULT 0"
            + ",m_send_ok       integer        NOT NULL  DEFAULT 0"
            + ",m_send_failures integer        NOT NULL  DEFAULT 0"
            + ")";

    public SetupDB() {
    }

    public void setupDB(String dbPatch, Connection c) throws SQLException {
        File BDFile = new File(dbPatch);
        BDFile.delete();
        Statement stmt = null;
        try {
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            stmt.executeUpdate(createDatagramsTable);
            stmt.executeUpdate(createDatagramsLogTable);
            stmt.executeUpdate(createDatagramsStatisticsTable);
            stmt.executeUpdate(createMeasurementsTable);
            stmt.executeUpdate(createMeasurementsDataTable);
            stmt.executeUpdate(createMeasurementsLogTable);
            stmt.executeUpdate(createMeasurementsStatisticsTable);
            stmt.executeUpdate(createSessionStatisticsTable);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Tables created successfully");
    }
}
