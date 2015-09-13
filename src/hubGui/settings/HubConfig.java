/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hubGui.settings;

/**
 *
 * @author Marcin
 */
public class HubConfig {

    private String id;
    private String key;
    
    public HubConfig() {
        this(null, null);
    }
    
    public HubConfig(String id, String key) {
        this.id = id;
        this.key = key;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
}
