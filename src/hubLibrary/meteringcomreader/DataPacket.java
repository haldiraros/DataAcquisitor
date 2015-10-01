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

//import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.Arrays;
import meteringdatareader.Frame;

/**
 *
 * @author Juliusz
 */
public class DataPacket implements Serializable{
//    static int LEN=31;
//    static int START_DATA=13;
     static final int DEF_TEMP_COUNT=6;
     static private final int DATA_FRAME_SIZE=16;
     int appId;
     short fieldLength;

     long loggerNo;  //Logger identificator
     short encAlg;
     Timestamp measurmentTimeStart;
     Timestamp measurmentTimeEnd;
     int measurmentPeriod;
     int[] temperatures = new int[DEF_TEMP_COUNT];
     byte batteryVoltage;
     long loggerId; //logger datrabase id
     int rssi;
     int lqi;
     protected int tempCount=DEF_TEMP_COUNT;
     long endTime;
     byte[] encriptedData;
     byte[] decriptKey;
     private int frameSize;
     byte [] orgFrame;

    public byte[] getOrgData() {
        return orgFrame;
    }
     

     DataPacket(long loggerNo, long time, long period){
         this.loggerNo=loggerNo;
         this.measurmentPeriod=(int)period;
         this.measurmentTimeStart =  Utils.time2Timestamp(time);
         this.endTime =  time;
        
    }
      
      
      public String getLoggerHexId(){
        /*
           String hexLoggerNo=
                   String.format("%02X",(byte)(0xFF&appId))
                   +String.format("%02X",(byte)(0xFF&(appId>>8)))
                   +String.format("%08X", loggerNo); //"454D" 
                   * 
                   */
           long fullLoggerId= loggerNo | ((0xFF&(long)appId)<<(8*5)) | ((0xFF00&(long)appId)<<(8*3));
           String hexLoggerNo=
                   String.format("%12X", fullLoggerId);           
           return hexLoggerNo;
    }
    
    DataPacket(byte[] data, int frameSize) throws MeteringSessionException {
        this(data, frameSize, 0);
    }
    
    boolean decriptData(){
        boolean ret=true;
        
        return false;
    }

    
    DataPacket(byte[] data, int frameSize, int start) throws MeteringSessionException {
        this.frameSize=frameSize;
        
        Frame frame = new Frame(data, start, frameSize);
        int temp;
        
        orgFrame = frame.getCtrlAsBytes();
        
        appId = frame.getHeaderElement(Frame.headerFrameLogger, "APPID");
        fieldLength = (short)frame.getHeaderElement(Frame.headerFrameLogger, "LEN");
        loggerNo = //frame.getHeaderElement(Frame.frameTempLogger, "IDD");
              ((long)frame.getHeaderElement(Frame.headerFrameLogger, "IDD"))&0x00000000FFFFFFFFL;
        
        int infbPosion=frame.getPositionOfElement(Frame.headerFrameLogger, "INFB");
        Frame infbFrame = new Frame(data, start + infbPosion/8, 4); //zakladam, że inb rozpoczyna się od pełnego bajtu
        
        encAlg = (short)infbFrame.getHeaderElement(Frame.frameINFB, "ENCF");
        //encAlg=0; //TESTING CHANGE
        short isSPRDF = (short)infbFrame.getHeaderElement(Frame.frameINFB, "SPRDF");

        int startTMF = frame.getPositionOfNextElement(Frame.headerFrameLogger, "INFB")/8;  // zakładam, że INFF zaczyna się od pełnych bajtów
        int startIDMF=startTMF+(short)infbFrame.getHeaderElement(Frame.frameINFB, "TMF");
// nie ma w opisie       startData+=(short)infbFrame.getHeaderElement(Frame.frameINFB, "IDMF");
        int startIDRF=startIDMF+0;
        int startPKTF = startIDRF + (short)infbFrame.getHeaderElement(Frame.frameINFB, "IDRF")*5;
        int startSPRDF= startPKTF + (short)infbFrame.getHeaderElement(Frame.frameINFB, "PKTF");
        int startFNCF=startSPRDF +  (short)infbFrame.getHeaderElement(Frame.frameINFB, "SPRDF")*2;
        int startFHVF=startFNCF+    (short)infbFrame.getHeaderElement(Frame.frameINFB, "FNCF");
        int startVIDPIDF=startFHVF+ (short)infbFrame.getHeaderElement(Frame.frameINFB, "FHVF")*2;
        int startAIB=startVIDPIDF+  (short)infbFrame.getHeaderElement(Frame.frameINFB, "VIDPIDF")*4;
        int startData=startAIB +    (short)infbFrame.getHeaderElement(Frame.frameINFB, "AIBF"); //nie uwzględnia łańcucha pól AIB
      
        if (isSPRDF==1){
            measurmentPeriod= (int) Utils.bytes2long(data, start + startSPRDF, 2);
        }
        else{
            tempCount=1;            
            measurmentPeriod = 0;
        }
//        rssi=((int)((byte)frame.getHeaderElement(Frame.frameTempLogger, "RSSI")))/2-74;
//        lqi=frame.getHeaderElement(Frame.frameTempLogger, "LQI")&0x7F; //ignore b7
        rssi = ((int)Utils.bytes2long(data, start + frameSize-2, 1))/2-74;
        lqi= ((int)Utils.bytes2long(data, start + frameSize-1, 1))&0x7F; //ignore b7
        
        if (encAlg!=0){
            //Trying stuff- Haros
                encriptedData= Arrays.copyOfRange(data, start+startData, start+startData+DATA_FRAME_SIZE);
                //throw new MeteringSessionException("Decription is not supported yet");
                        //System.arraycopy(LEN, lqi, LEN, LEN, LEN)
        }
        else{
            frame = new Frame(data, start+startData, DATA_FRAME_SIZE);
            endTime = ((long)frame.getHeaderElement(Frame.frameTempLoggerData, "TIME"))&0x00000000FFFFFFFFL;
            measurmentTimeEnd =  Utils.time2Timestamp(endTime);
            //        new Timestamp(Timestamp.valueOf("2011-01-01 00:00:00").getTime()+
            //                endTime*1000);
            measurmentTimeStart =Utils.time2Timestamp(endTime-(tempCount-1)*measurmentPeriod);               
    //               new Timestamp(Timestamp.valueOf("2011-01-01 00:00:00").getTime()+
    //                       (startTime-5*measurmentPeriod)*1000);  
            for (int i=0; i<tempCount; i++){

                temp=frame.getHeaderElement(Frame.frameTempLoggerData, "TEMPERATURE"+(i+1));
                if ((temp&0x0800)==0x0800){  //sign bit in 12bits number is set
                    temp=temp |0xFFFFF800;

                    //temp=(temp&0x07FF)|0x80000000;
                }
                temperatures[i]=temp;
            }
            batteryVoltage = (byte)frame.getHeaderElement(Frame.frameTempLoggerData, "BATTERY_VOLTAGE");        
        }
    }
    
    public DataPacketDTO generateDTO(){
        return new DataPacketDTO( appId,  fieldLength,  loggerNo,  encAlg,  measurmentTimeStart,  measurmentTimeEnd,  measurmentPeriod, 
                temperatures, batteryVoltage, loggerId, rssi, lqi, tempCount, endTime, encriptedData, decriptKey, frameSize);
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("appId:");sb.append(String.format("%0#8X", appId));
        sb.append(", fieldLength:");sb.append(fieldLength);
        sb.append(", loggerId:");sb.append(String.format("%0#8X", loggerNo));
        sb.append(", encAlg:");sb.append(encAlg);
        if(encriptedData !=null){
        sb.append(", encryptedData:");
            for (byte b : encriptedData) {
            sb.append(String.format("%02X ",b));//changes
            }
        }
        sb.append(", measurmentTime:");sb.append(measurmentTimeStart);
        sb.append(", measurmentPeriod:");sb.append(measurmentPeriod);
        sb.append(", temperatures:(");
        for (int i=0; i<tempCount; i++){
            sb.append(temperatures[i]);
            sb.append(" ,");
        }       
        sb.append("), batteryVoltage:");sb.append(batteryVoltage);
        
        return sb.toString();
        
    }

    long getRSSI() {
        return rssi;
    }

    void setTemperatures(int[] temperatures, int tempCount) {
       this.temperatures= new int [tempCount];
       this.tempCount=tempCount;
       for (int i=0; i<tempCount; i++){
           this.temperatures[i]=temperatures[i];
       }
       this.endTime=this.endTime+(tempCount-1)*this.measurmentPeriod;
       this.measurmentTimeEnd= Utils.time2Timestamp(endTime);
    }

    public long getLoggerNo() {
        return loggerNo;
    }

    public void setLoggerNo(long loggerNo) {
        this.loggerNo = loggerNo;
    }    
    public static void main(String[]args) throws MeteringSessionException, FileNotFoundException, IOException, ClassNotFoundException{
        int intData[]={0X4D,0X45,0X4D,0X45, 0X1A, 0X30,0XDC,0X89,0X23, 0X30,0X8,0X11,0X8, 0XA,0X0,0XD5,0XD3,0X68,0X52,0XDA,0XA0,0XD,0XDA,0XA0,0XD,0XDA,0XA0,0XD,0X1D,0X3C,0XCA,0X1F,0XB3};
//        int intData[]=  {0X4D,0X45,0X4D,0X45,0X1A,0XA7,  0XDE,0X89,0X23,0X30,0X8,0X11,0X8,0XB4,0X0,0XC0,0X16,0X69,0X52,0XF4,0X50,0XF,0XF5,0X60,0XF,0XF6,0X70,0XF,0X1D,0X77,0X18,0X56,0XB3};
//        int intData[]=  {0X4D,0X45,0X4D,0X45,0X1A,0XA7,  0XDE,0X89,0X23,0X30,0X8,0X11,0X8,0XB4,0X0,0XC0,0X16,0X69,0X52,0XF4,0X50,0XF,0XF5,0X60,0XF,0XF6,0X70,0XF,0X1D,0X77,0X18,0X56,0XB3};
//        int intData[]= {0X4D,0X45,0X4D,0X45,0X1A,0XF,0XDA,    0X89,0X23,0X30,0X8,0X11,0X8,0XB4,0X0,0X2C,0X1F,0X69,0X52,0X8,0X51,0X10,0XA,0X1,0X11,0X10,0X51,0X11,0X1E,0XD1,0XDF,0X69,0XB2};
        byte data[] = new byte[intData.length];
        System.out.println(data.length);
        for (int i=0; i<intData.length; i++)
            data[i]=(byte)(intData[i]);
       DataPacket dp = new DataPacket(data, data.length);
       System.out.println(dp);
       
/*       
       FileOutputStream fout = new FileOutputStream("c:\\temp\\dp.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(dp);
        fout.close();
*/
/*       
        FileInputStream fin = new FileInputStream("c:\\temp\\dp.ser");
        ObjectInputStream ois = new ObjectInputStream(fin);
        DataPacket newdp;
        
        do{
            newdp = (DataPacket) ois.readObject();
            System.out.println(newdp);
        }while (newdp!=null);

       fin.close();
  */      
    }
}
