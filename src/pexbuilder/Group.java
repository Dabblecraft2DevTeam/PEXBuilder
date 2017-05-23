/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bryce
 */
public class Group {
    
    private String name;
    
    private Options options;
    
    private Map<String, Permission> permissions = new HashMap<>();
    
    public Group(){
        
    }
    
    public Group(String name, Options options){
        this.name = name;
        this.options = options;
    }
    
    public Map<String, Permission> getPermissions(){
        return permissions;
    }
    
    public void setName(String input){
        name = input;
    }
    
    public String getName(){
        return name;
    }
    
    public void setOptions(Options input){
        options = input;
    }
    
    public Options getOptions(){
        return options;
    }
    
}
