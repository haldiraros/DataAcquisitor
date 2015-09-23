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
public class DatagramsSendStatistics {
    private int sendOkCounter;
    private int sendFailsCounter;   

    public DatagramsSendStatistics(){
        sendOkCounter = 0;
        sendFailsCounter = 0;
    }
    
    /**
     * @return the sendOkCounter
     */
    public int getSendOkCounter() {
        return sendOkCounter;
    }

    /**
     * @param sendOkCounter the sendOkCounter to set
     */
    public void setSendOkCounter(int sendOkCounter) {
        this.sendOkCounter = sendOkCounter;
    }

    public void addSendOkCounter(int sendOk) {
        this.sendOkCounter += sendOk;
    }

    public void addSendOkCounter() {
        this.sendOkCounter += 1;
    }

    /**
     * @return the sendFailsCounter
     */
    public int getSendFailsCounter() {
        return sendFailsCounter;
    }

    /**
     * @param sendFailsCounter the sendFailsCounter to set
     */
    public void setSendFailsCounter(int sendFailsCounter) {
        this.sendFailsCounter = sendFailsCounter;
    }
    
    public void addSendFailsCounter(int sendFails) {
        this.sendFailsCounter += sendFails;
    }    
    
    public void addSendFailsCounter() {
        this.sendFailsCounter += 1;
    }
}
