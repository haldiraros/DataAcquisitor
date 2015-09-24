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
public class Datagram{

    private BigDecimal id;
    private final String data;
    private boolean dataSend;
    private final String hubId;
    private final String dataTimestamp;
    private String newErrorMessage;
    private String prevErrorMessage;
    
     public Datagram(BigDecimal id, String data, String hubId, String dataTimestamp, boolean dataSend) {
        this.id = id;
        this.data = data;
        this.dataSend = dataSend;
        this.dataTimestamp = dataTimestamp;
        this.hubId = hubId;
    }

    public Datagram(BigDecimal id, String data, String hubId, String dataTimestamp) {
        this(id,data,hubId,dataTimestamp,false);
    }

    public Datagram(String data, String hubId, String dataTimestamp) {
        this(null,data,hubId,dataTimestamp,false);
    }

    public boolean isDataSend() {
        return dataSend;
    }

    public void setDataSend(boolean dataSend) {
        this.dataSend = dataSend;
    }

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

    public BigDecimal getId() {
        return id;
    }

    public String getData() {
        return data;
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
     * @return the dataTimestamp
     */
    public String getDataTimestamp() {
        return dataTimestamp;
    }

}
