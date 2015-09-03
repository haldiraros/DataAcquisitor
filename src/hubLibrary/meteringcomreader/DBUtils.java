/*
 * Copyright (C) 2015 Juliusz Jezierski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package hubLibrary.meteringcomreader;

import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zawiera pomocnicze funkcje statyczne operujące na bazie danych
 * @author Juliusz Jezierski
 */
public class DBUtils {
        /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(DBUtils.class);
    
    protected static ResourceBundle rb = ResourceBundle.getBundle ("meteringcomreader.db");
    /**
     * Tekst polecenia ustawiającego strefę czasową polecenia na UTC.
     */
    protected static String setTimezoneToUTC="alter session set time_zone='00:00'";

    /**
     * Opis połączenia do bazy danych.
     */
    public static void setConnDesc(String connDesc) {
        DBUtils.connDesc = connDesc;
    }

    public static void setUser(String user) {
        DBUtils.user = user;
    }

    public static void setPass(String pass) {
        DBUtils.pass = pass;
    }

    protected static String connDesc = rb.getString("connDesc");
    /**
     * Nazwa użytkownika bazy danych.
     */
    protected static String user = rb.getString("user");
    /**
     * Hasło użytkownika bazy danych.
     */
    protected static String pass = rb.getString("pass");

    /**
     * Tworzy połączenie do bazy danych i ustawia strefę czasową na 
     * UTC.
     * @return zwraca utworzone połączenie do bazy danych
     * @throws MeteringSessionException w przypadku nieznalezienia klasy drivera JDBC
     * lub zgłoszenia SQLException
     */
    public static Connection createDBConnection() throws MeteringSessionException{
        Connection conn=null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            lgr.info("Trying to connect to:"+connDesc+",user:"+user+",pwd:"+pass);
            conn = DriverManager.getConnection(connDesc, user, pass);
            lgr.info("Connected to:"+connDesc);
            conn.setAutoCommit(false);
            conn.createStatement().execute(setTimezoneToUTC);
        } catch (SQLException ex) {
            throw new MeteringSessionException(ex);
        } catch (ClassNotFoundException ex) {
            throw new MeteringSessionException(ex);
        }
        return conn;
    }
    
}
