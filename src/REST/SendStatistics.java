/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package REST;

/**
 *
 * @author hp
 */
public class SendStatistics {
    private int datagramSendOkCounter;
    private int datagramSendFailsCounter;   
    private int measurementSendOkCounter;
    private int measurementSendFailsCounter;   

    public SendStatistics(){
        datagramSendOkCounter = 0;
        datagramSendFailsCounter = 0;
        measurementSendOkCounter = 0;
        measurementSendFailsCounter = 0;
    }
    
    /**
     * @return the datagramSendOkCounter
     */
    public int getDatagramSendOkCounter() {
        return datagramSendOkCounter;
    }

    /**
     * @param datagramSendOkCounter the datagramSendOkCounter to set
     */
    public void setDatagramSendOkCounter(int datagramSendOkCounter) {
        this.datagramSendOkCounter = datagramSendOkCounter;
    }

    public void addDatagramSendOkCounter(int sendOk) {
        this.datagramSendOkCounter += sendOk;
    }

    public void addDatagramSendOkCounter() {
        this.datagramSendOkCounter += 1;
    }

    /**
     * @return the datagramSendFailsCounter
     */
    public int getDatagramSendFailsCounter() {
        return datagramSendFailsCounter;
    }

    /**
     * @param datagramSendFailsCounter the datagramSendFailsCounter to set
     */
    public void setDatagramSendFailsCounter(int datagramSendFailsCounter) {
        this.datagramSendFailsCounter = datagramSendFailsCounter;
    }
    
    public void addDatagramSendFailsCounter(int sendFails) {
        this.datagramSendFailsCounter += sendFails;
    }    
    
    public void addDatagramSendFailsCounter() {
        this.datagramSendFailsCounter += 1;
    }

    /**
     * @return the measurementSendOkCounter
     */
    public int getMeasurementSendOkCounter() {
        return measurementSendOkCounter;
    }

    /**
     * @param measurementSendOkCounter the measurementSendOkCounter to set
     */
    public void setMeasurementSendOkCounter(int measurementSendOkCounter) {
        this.measurementSendOkCounter = measurementSendOkCounter;
    }
    
    public void addMeasurementSendOkCounter(int sendOk) {
        this.measurementSendOkCounter += sendOk;
    }    
    
    public void addMeasurementSendOkCounter() {
        this.measurementSendOkCounter += 1;
    }

    /**
     * @return the measurementSendFailsCounter
     */
    public int getMeasurementSendFailsCounter() {
        return measurementSendFailsCounter;
    }

    /**
     * @param measurementSendFailsCounter the measurementSendFailsCounter to set
     */
    public void setMeasurementSendFailsCounter(int measurementSendFailsCounter) {
        this.measurementSendFailsCounter = measurementSendFailsCounter;
    }
    
    public void addMeasurementSendFailsCounter(int sendFails) {
        this.measurementSendFailsCounter += sendFails;
    }    
    
    public void addMeasurementSendFailsCounter() {
        this.measurementSendFailsCounter += 1;
    }
}
