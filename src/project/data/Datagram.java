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
public class Datagram {

    private BigDecimal id;
    private final String data;
    private boolean dataSend;
    private final String hubId;
    
     public Datagram(BigDecimal id, String data, String hubId, boolean dataSend) {
        this.id = id;
        this.data = data;
        this.dataSend = dataSend;
        this.hubId = hubId;
    }

    public Datagram(BigDecimal id, String data, String hubId) {
        this(id,data,hubId,false);
    }

    public Datagram(String data, String hubId) {
        this(null,data,hubId,false);
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

}
