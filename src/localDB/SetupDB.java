/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localDB;

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
            + ",FOREIGN KEY (last_log_id) REFERENCES Errors_log (id)"
            + ")";

    private final String createDatagramsLogTable
            = "CREATE TABLE Errors_log ("
            + " id             integer        NOT NULL  PRIMARY KEY AUTOINCREMENT"
            + ",datagram_id    integer        NOT NULL"
            + ",time           datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP"
            + ",type           integer"
            + ",error          varchar(2000)"
            + ",FOREIGN KEY (datagram_id) REFERENCES Datagrams (id)"
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
            + ")";

    public SetupDB() {
    }

    public void setupDB(Connection c) throws SQLException {
        if (c != null) {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(createDatagramsTable);
            stmt.executeUpdate(createDatagramsLogTable);
            stmt.executeUpdate(createDatagramsStatisticsTable);
            stmt.executeUpdate(createSessionStatisticsTable);
            stmt.close();
        }
    }
}
