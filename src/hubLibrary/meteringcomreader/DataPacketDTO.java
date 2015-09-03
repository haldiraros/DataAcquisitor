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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Juliusz
 */
public class DataPacketDTO implements Serializable{
     protected int appId;
     protected short fieldLength;

     protected long loggerNo;  //Logger identificator
     protected short encAlg;
     protected Timestamp measurmentTimeStart;
     protected Timestamp measurmentTimeEnd;
     protected int measurmentPeriod;
     protected int[] temperatures ;
     protected byte batteryVoltage;
     protected long loggerId; //logger datrabase id
     protected int rssi;
     protected int lqi;
     protected int tempCount;
     protected long endTime;
     protected byte[] encriptedData;
     protected byte[] decriptKey;
     protected  int frameSize;

    public DataPacketDTO(int appId, short fieldLength, long loggerNo, short encAlg, Timestamp measurmentTimeStart, Timestamp measurmentTimeEnd, int measurmentPeriod, int[] temperatures, byte batteryVoltage, long loggerId, int rssi, int lqi, int tempCount, long endTime, byte[] encriptedData, byte[] decriptKey, int frameSize) {
        this.appId = appId;
        this.fieldLength = fieldLength;
        this.loggerNo = loggerNo;
        this.encAlg = encAlg;
        this.measurmentTimeStart = measurmentTimeStart;
        this.measurmentTimeEnd = measurmentTimeEnd;
        this.measurmentPeriod = measurmentPeriod;
        this.temperatures = temperatures;
        this.batteryVoltage = batteryVoltage;
        this.loggerId = loggerId;
        this.rssi = rssi;
        this.lqi = lqi;
        this.tempCount = tempCount;
        this.endTime = endTime;
        this.encriptedData = encriptedData;
        this.decriptKey = decriptKey;
        this.frameSize = frameSize;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("appId:");sb.append(String.format("%0#8X", appId));
        sb.append(", fieldLength:");sb.append(fieldLength);
        sb.append(", loggerId:");sb.append(String.format("%0#8X", loggerNo));
        sb.append(", encAlg:");sb.append(encAlg);
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
    
    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public short getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(short fieldLength) {
        this.fieldLength = fieldLength;
    }

    public long getLoggerNo() {
        return loggerNo;
    }

    public void setLoggerNo(long loggerNo) {
        this.loggerNo = loggerNo;
    }

    public short getEncAlg() {
        return encAlg;
    }

    public void setEncAlg(short encAlg) {
        this.encAlg = encAlg;
    }

    public Timestamp getMeasurmentTimeStart() {
        return measurmentTimeStart;
    }

    public void setMeasurmentTimeStart(Timestamp measurmentTimeStart) {
        this.measurmentTimeStart = measurmentTimeStart;
    }

    public Timestamp getMeasurmentTimeEnd() {
        return measurmentTimeEnd;
    }

    public void setMeasurmentTimeEnd(Timestamp measurmentTimeEnd) {
        this.measurmentTimeEnd = measurmentTimeEnd;
    }

    public int getMeasurmentPeriod() {
        return measurmentPeriod;
    }

    public void setMeasurmentPeriod(int measurmentPeriod) {
        this.measurmentPeriod = measurmentPeriod;
    }

    public int[] getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(int[] temperatures) {
        this.temperatures = temperatures;
    }

    public byte getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(byte batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public long getLoggerId() {
        return loggerId;
    }

    public void setLoggerId(long loggerId) {
        this.loggerId = loggerId;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getLqi() {
        return lqi;
    }

    public void setLqi(int lqi) {
        this.lqi = lqi;
    }

    public int getTempCount() {
        return tempCount;
    }

    public void setTempCount(int tempCount) {
        this.tempCount = tempCount;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public byte[] getEncriptedData() {
        return encriptedData;
    }

    public void setEncriptedData(byte[] encriptedData) {
        this.encriptedData = encriptedData;
    }

    public byte[] getDecriptKey() {
        return decriptKey;
    }

    public void setDecriptKey(byte[] decriptKey) {
        this.decriptKey = decriptKey;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }
     
     
}
