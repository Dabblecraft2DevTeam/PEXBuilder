/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bryce
 */
public class Player {

    private String UUID;

    private Map<String, Permission> permissions = new HashMap<>();

    private List<String> groups = new ArrayList<>();

    private String prefix;

    private String suffix;

    public Player(String UUID) {
        this.UUID = UUID;
    }

    public void setUUID(String input) {
        UUID = input;
    }

    public String getUUID() {
        return UUID;
    }

    public void setPrefix(String input) {
        prefix = input;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setSuffix(String input) {
        suffix = input;
    }

    public String getSuffix() {
        return suffix;
    }

    public List<String> getGroups() {
        return groups;
    }

    public Map<String, Permission> getPermissions() {
        return permissions;
    }
    
    public Boolean hasPermissions(){
        if (permissions.size()==0){
            return false;
        }else{
            return true;
        }
    }

}
