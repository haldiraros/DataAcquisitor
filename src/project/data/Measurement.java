/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.data;

import java.math.BigDecimal;

/**
 *
 * @author hp
 */
public class Measurement{
    
    private BigDecimal id;
    private final String loggerId;
    private final String hubId;
    private final String measurmentTime;
    private boolean dataSend;

    private String newErrorMessage;
    private String prevErrorMessage;
    
    private int[] measurments;
    
       
    public Measurement(String loggerId, String hubId, int[] measurments, String measurmentTime){
        this(null,loggerId,hubId,measurments,measurmentTime,false);
    }  
       
    public Measurement(BigDecimal id, String loggerId, String hubId, int[] measurments, String measurmentTime){
        this(id,loggerId,hubId,measurments,measurmentTime,false);
    }
    
    public Measurement(BigDecimal id, String loggerId, String hubId, String measurmentTime){
        this(id,loggerId,hubId,null,measurmentTime,false);
    }
    
    public Measurement(BigDecimal id, String loggerId, String hubId, String measurmentTime, boolean dataSend){
        this(id,loggerId,hubId,null,measurmentTime,dataSend);
    }

    public Measurement(BigDecimal id, String loggerId, String hubId, int[] measurments, String measurmentTime, boolean dataSend){
        this.id = id;
        this.loggerId = loggerId;
        this.hubId = hubId;
        this.measurments = measurments;
        this.measurmentTime = measurmentTime;
        this.dataSend = dataSend;
    }
    
    /**
     * @return the id
     */
    public BigDecimal getId() {
        return id;
    }

    /**
     * @param id the id to set
     * @throws java.lang.Exception
     */
    public void setId(BigDecimal id) throws Exception {
        if (this.id == null) {
            this.id = id;
        } else {
            throw new Exception("Id is already set [current id: ("
                    + this.id
                    + ") new id: ("
                    + id
                    + ")]");
        }
    }

    /**
     * @return the loggerId
     */
    public String getLoggerId() {
        return loggerId;
    }

    /**
     * @return the dataSend
     */
    public boolean isDataSend() {
        return dataSend;
    }

    /**
     * @param dataSend the dataSend to set
     */
    public void setDataSend(boolean dataSend) {
        this.dataSend = dataSend;
    }

    /**
     * @return the hubId
     */
    public String getHubId() {
        return hubId;
    }

    /**
     * @return the newErrorMessage
     */
    public String getNewErrorMessage() {
        return newErrorMessage;
    }

    /**
     * @param newErrorMessage the newErrorMessage to set
     */
    public void setNewErrorMessage(String newErrorMessage) {
        this.newErrorMessage = newErrorMessage;
    }

    /**
     * @return the prevErrorMessage
     */
    public String getPrevErrorMessage() {
        return prevErrorMessage;
    }

    /**
     * @param prevErrorMessage the prevErrorMessage to set
     */
    public void setPrevErrorMessage(String prevErrorMessage) {
        this.prevErrorMessage = prevErrorMessage;
    }

    /**
     * @return the measurments
     */
    public int[] getMeasurments() {
        return measurments;
    }

    /**
     * @param measurments the measurments to set
     */
    public void setMeasurments(int[] measurments) {
        this.measurments = measurments;
    }

    /**
     * @return the measurmentTime
     */
    public String getMeasurmentTime() {
        return measurmentTime;
    }

}
