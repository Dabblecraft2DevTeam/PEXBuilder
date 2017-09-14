package pexbuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Bryce
 */
public class Permission {

    private String permission;

    private String description;

    private Boolean def = false;
    
    private String plugin;

    public Permission(String perm, String descrip, Boolean defau, String plugin) {
        permission = perm;
        description = descrip;
        def = defau;
    }
    
    public void setPlugin(String input){
        plugin = input;
    }
    
    public String getPlugin(){
        return plugin;
    }

    public void setPermission(String input) {
        permission = input;
    }

    public String getPermission() {
        return permission;
    }

    public void setDescription(String input) {
        description = input;
    }

    public String getDescription() {
        return description;
    }

    public void setDefault(Boolean input) {
        def = input;
    }

    public Boolean hasDescription() {
        if (description.equalsIgnoreCase("") || description.equalsIgnoreCase(" ") || description == null) {
            return false;
        } else {
            return true;
        }
    }

}
