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

package hubLibrary.meteringcomreader.callback;

/**
 *
 * @author Juliusz
 */
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import hubLibrary.meteringcomreader.ComReadDispatch;
import hubLibrary.meteringcomreader.DBUtils;
import hubLibrary.meteringcomreader.Hub;
import hubLibrary.meteringcomreader.HubRequest;
import hubLibrary.meteringcomreader.HubResponse;
import hubLibrary.meteringcomreader.HubSessionDBManager;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionCRCException;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.dcn.QueryChangeDescription;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.TableChangeDescription;
import oracle.sql.ROWID;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;
 
public class DBChangeNotification implements DatabaseChangeListener
{
      /**
     * Utworzenie loggera systemowego
     */
    private static final org.slf4j.Logger lgr = LoggerFactory.getLogger(DBChangeNotification.class);
    
    private static final String dbcQuerySQL="select HR_HS_NUMBER from hub_requests where HR_HS_NUMBER=?";
    private static final String getHubReqestsSQL
            ="select HR_P1,HR_P2,HR_P3,HR_P4,HR_P5,HR_P6,HR_P7,HR_P8,HR_P9,HR_P10, HR_HS_NUMBER,HR_COMMAND from hub_requests where rowid=?";
    private static  OraclePreparedStatement getHubReqestsPS=null;
    private static final String putHubReqestsSQL
            ="insert into hub_responses (HP_P1,HP_P2,HP_P3,HP_P4,HP_P5,HP_HR_HS_NUMBER,HP_ERR_MSG) values (?,?,?,?,?,?,?)";
    private static  PreparedStatement putHubReqestsPS=null;
    
    private static final String testReverseConnSQL ="{ ? = call hub_service.test_reverse_connection(?, ?)}";
    private static Random rnd;

  
  static public void unregisterForCallback(Hub hub, Connection connection) throws MeteringSessionException{
      OracleConnection conn=null;
      if (connection instanceof OracleConnection)
          conn=(OracleConnection)connection;
      else
          throw new MeteringSessionException("Connection is not instance of OracleConnection");
      DatabaseChangeRegistration dcr=hub.getDCR();
      try {
          conn.unregisterDatabaseChangeNotification(dcr);
      } catch (SQLException ex) {
          throw new MeteringSessionException(ex);
      }
  }
  
  static public void registerForCallback(Hub hub, Connection connection) throws MeteringSessionException{
      OracleConnection conn=null;
      if (connection instanceof OracleConnection)
          conn=(OracleConnection)connection;
      else
          throw new MeteringSessionException("Connection is not instance of OracleConnection");
    Properties prop = new Properties();
    prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS,"true");
    prop.setProperty(OracleConnection.NTF_LOCAL_TCP_PORT, "21212");
    prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION,"true");
    prop.setProperty(OracleConnection.DCN_IGNORE_DELETEOP,"true");
    prop.setProperty(OracleConnection.DCN_IGNORE_UPDATEOP,"true");        
    DatabaseChangeRegistration dcr=null;
    try
    {
      dcr = conn.registerDatabaseChangeNotification(prop);
      hub.setDCR(dcr);
      // add the listenerr:
      DBChangeNotification list = new DBChangeNotification();
      dcr.addListener(list);
       
      // second step: add objects in the registration:
//      String query=dbcQuery+"'"+ hub.getHubHexId()+"'";
//       Statement stmt = conn.createStatement();
      PreparedStatement stmt = conn.prepareStatement(dbcQuerySQL);
      // associate the statement with the registration:
      ((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
      stmt.setString(1, hub.getHubHexId());
            
//      ResultSet rs = stmt.executeQuery(query);      
        ResultSet rs = stmt.executeQuery();      
//      while (rs.next())
//      {}
      String[] tableNames = dcr.getTables();
      for(int i=0;i<tableNames.length;i++)
          lgr.debug(tableNames[i]+" is part of the registration.");
      rs.close();
      stmt.close();
      lgr.debug("Registred for query:"+dbcQuerySQL+" ?="+ hub.getHubHexId());
      lgr.debug(Long.toString(dcr.getRegId()));
    }
    catch(SQLException ex)
    {
      lgr.warn("Database can not comunicate to Logger Agent due to locked 21212 port on logger machine, "+
              " and database will not succesfully send commands to Logger Agent", ex);
      // if an exception occurs, we need to close the registration in order
      // to interrupt the thread otherwise it will be hanging around.
      if(conn != null)
        try {
          conn.unregisterDatabaseChangeNotification(dcr);
      } catch (SQLException ex1) {
          lgr.debug(ex1.getMessage());
      }
        //throw new MeteringSessionException(ex);
    }               
}


  


    @Override
    public void onDatabaseChangeNotification(DatabaseChangeEvent dce) {
        lgr.debug("kolejne wywołanie powiadomienia");
        lgr.debug(dce.toString());        
        if (! HubSessionDBManager.getHubSessionManager().isRegisteredForDCN(dce.getRegId()))
            return;
        Connection conn = null;
        try {
            conn = DBUtils.createDBConnection();
            getHubReqestsPS = (OraclePreparedStatement) conn.prepareStatement(getHubReqestsSQL);
            putHubReqestsPS = conn.prepareStatement(putHubReqestsSQL);
            QueryChangeDescription[] queryChangeDescription = dce.getQueryChangeDescription();
            if (queryChangeDescription==null){
                lgr.error("queryChangeDescription is null");
                return;
            }
            for(QueryChangeDescription qcd:queryChangeDescription){
                TableChangeDescription[] tableChangeDescription = qcd.getTableChangeDescription();
                if (tableChangeDescription==null){
                    lgr.error("tableChangeDescription is null");
                    return;
                }       
                for(TableChangeDescription tcd:tableChangeDescription){
                      RowChangeDescription[] rowChangeDescription = tcd.getRowChangeDescription();                      
                      if (tableChangeDescription==null){
                        lgr.error("tableChangeDescription is null");
                        return;
                      }
                
                      for(RowChangeDescription rcd:rowChangeDescription){
                          RowChangeDescription.RowOperation rowOperation = rcd.getRowOperation();
                          if (rowOperation!=RowChangeDescription.RowOperation.INSERT)
                              return;
                          ROWID rowid = rcd.getRowid();
                          HubRequest hr = getHubRequestFromDB(getHubReqestsPS, rowid);
                          HubResponse hp=serviceHubRequest(hr);
                          insertHubResponseIntoDB(putHubReqestsPS, hp);      
                          conn.commit();
                      }
                    
                }
            }
        } catch (MeteringSessionException ex) {
            lgr.error(ex.getMessage());
        } catch (SQLException ex) {
            lgr.error(ex.getMessage());
        }
        finally{
            if (conn!=null)
                try {
                conn.close();
            } catch (SQLException ex) {
                ; //ignore it
            }
        }
        
    }

    protected HubResponse serviceHubRequest(HubRequest hr) {
        HubResponse hp = new HubResponse();
        hp.setHexHubId(hr.getHexHubId());
        hp.getParameters()[0]="OK";
        lgr.debug("requested Command:"+hr.getCommand());
        try {
            if ("downloadMeasurmentsFromHub".equals(hr.getCommand())){
                Date date = parseDate(hr.getParameters()[0]);
//                HubSessionService.downloadMeasurmentsFromHub(hr.getHexHubId(), date);
            }
            else if("downloadMeasurmentsFromLogger".equals(hr.getCommand())){
                Date date = parseDate(hr.getParameters()[0]);
                int newMeasuments = HubSessionService.downloadMeasurmentsFromLogger(hr.getHexHubId(), date); 
                hp.getParameters()[1]=Integer.toString(newMeasuments);
            }
            else if("intervalHubFlashMemoryMode".equals(hr.getCommand())){
                Date startTime = parseDate(hr.getParameters()[0]);
                Date endTime = parseDate(hr.getParameters()[1]);
                boolean enable = parseBool(hr.getParameters()[2]);
//                HubSessionService.intervalHubFlashMemoryMode(hr.getHexHubId(), startTime, endTime, enable);
            }
            else if("overwriteHubFlashMemoryMode".equals(hr.getCommand())){
                boolean enable = parseBool(hr.getParameters()[0]);
//                HubSessionService.overwriteHubFlashMemoryMode(hr.getHexHubId(), enable);                
            }
            else if("registerMeasurer".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
//                HubSessionService.registerMeasurer(hr.getHexHubId(), measurerNo);
            }
            else if("unregisterMeasurer".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
//                HubSessionService.unregisterMeasurer(hr.getHexHubId(), measurerNo);
            }
            else if("measurerRadio".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
                boolean enable = parseBool(hr.getParameters()[1]);
//                HubSessionService.measurerRadio(hr.getHexHubId(), measurerNo, enable);
            }
            else if("TEST".equals(hr.getCommand())){
                ; //OK
            }                
            else {
                hp.getParameters()[0]="ERR";
                hp.setErrMsg("Uknow command");
            }
        } catch (MeteringSessionException ex) {
            lgr.error(ex.getMessage());
            hp.getParameters()[0]="ERR";
            String errMsg=ex.getMessage();
            if (errMsg.length()>2000)
                errMsg = errMsg.substring(1, 2000);
            hp.setErrMsg(errMsg);
            
        }
        return hp;
    }
    
    protected static Date parseDate(String dateString) throws MeteringSessionException{
        if (dateString==null || "".equals(dateString))
            return null;
        SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date parsed =null;
        // See if we can parse the output of Date.toString()
        try {
             parsed = format.parse(dateString);
        }
        catch(ParseException pe) {
            throw new MeteringSessionException(pe);
        }
        return parsed;
    }

    protected static boolean parseBool(String bool) throws MeteringSessionException{
        if ("TRUE".equals(bool))
            return true;
        else if ("FALSE".equals(bool))
            return false;
        else 
            throw new MeteringSessionException("Can't parse bool value:"+bool);
    }

    private HubRequest getHubRequestFromDB(OraclePreparedStatement getHubReqestsPS, ROWID rowid) throws SQLException {
        getHubReqestsPS.setROWID(1, rowid);
        HubRequest hr =new HubRequest();
        ResultSet rs = getHubReqestsPS.executeQuery();
        if (rs.next()){
          hr.setHexHubId(rs.getString("HR_HS_NUMBER"));
          hr.setCommand(rs.getString("HR_COMMAND"));
          String[]parameters=hr.getParameters();
          for (int i=1; i<=10; i++)
              parameters[i-1]=rs.getString(i);
        }
        return hr;
    }

    private void insertHubResponseIntoDB(PreparedStatement putHubReqestsPS, HubResponse hp) throws SQLException {
        for (int i=1; i<=5; i++)
            putHubReqestsPS.setString(i, hp.getParameters()[i-1]);
        putHubReqestsPS.setString(6, hp.getHexHubId());
        putHubReqestsPS.setString(7, hp.getErrMsg());
        putHubReqestsPS.executeUpdate();
    }


    
    public static boolean testReverseConnection(Hub hub, Connection conn) throws MeteringSessionException{
        boolean ret=true;
        boolean testHub=false;
        String testHubStr="false";
        
        if (hub==null){
            hub=new Hub(Integer.toHexString(rnd.nextInt()), "test");
            testHub=true;
            testHubStr="true";
        }
        
        if (testHub){
            DBChangeNotification.registerForCallback(hub, conn);
            HubSessionDBManager.getHubSessionManager().registerTestHub(hub);
        }
        try {Thread.sleep(1000*2);} catch (InterruptedException ex) {/*ignore it*/}
        try{
            CallableStatement testReverseConnPS = conn.prepareCall(testReverseConnSQL);
            String hubid=hub.getHubHexId();
            testReverseConnPS.registerOutParameter(1, Types.VARCHAR);
            testReverseConnPS.setString(2, hubid);
            testReverseConnPS.setString(3, testHubStr);
            testReverseConnPS.execute();
            String retStr = testReverseConnPS.getString(1);
            ret="true".equals(retStr);
            conn.commit();
//            testReverseConnPS.close();
//            testReverseConnPS=null;
        }
        catch (SQLException ex) {
            lgr.warn(ex.getMessage());
            throw new MeteringSessionException(ex);
        }        
        finally{                    
            if (testHub){
                unregisterForCallback(hub, conn);
                HubSessionDBManager.getHubSessionManager().unregisterTestHub(hub);
            }
        }
        return ret;
    }
        
    public static void  main(String[]arg) throws MeteringSessionException, InterruptedException{
        PropertyConfigurator.configure(HubSessionDBManager.class.getResource("/meteringcomreader/log4j.properties"));

        Connection conn = DBUtils.createDBConnection();
        Hub hub = new Hub("1", "ComX");
        
        DBChangeNotification.registerForCallback(hub, conn);
        Thread.sleep(1000*120);
        DBChangeNotification.unregisterForCallback(hub, conn);
        
    }
    
    static{
        rnd= new Random((new Date()).getTime());
    }
    
}
