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
public class Plugin {
    
    private String name;
    
    private String main;
    
    private String version;
    
    private String author;
    
    private String description;
    
    private String[] depend;
    
    Map<String, Permission> permissions = new HashMap<>();
    
    public void setName(String input){
        name = input;
    }
    
    public String getName(){
        return name;
    }
    
    public void setMain(String input){
        main = input;
    }
    
    public String getMain(){
        return main;
    }
    
    public void setVersion(String input){
        version = input;
    }
    
    public String getVersion(){
        return version;
    }
    
    public void setAuthor(String input){
        author = input;
    }
    
    public String getAuthor(){
        return author;
    }
    
    public void setDescription(String input){
        description = input;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDepend(String[] input){
        depend = input;
    }
    
    public String[] getDepend(){
        return depend;
    }
    
    public Map<String, Permission> getPermissions(){
        return permissions;
    }
    
}
