/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Bryce
 */
public final class PEXSQL {

    private static PEXSQL INSTANCE;

    private Connection connection;

    private Map<String, String> SQLPlayers = new HashMap<>();

    public PEXSQL() {
        INSTANCE = this;
    }

    public void connect(String hostname, String username, String database, String password, Integer port) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
            if (connection.isValid(0)) {
                System.out.println("Connected!");
                checkTables();
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
        }
    }

    public void createTables() {
        if (PEXBuilderController.getInstance().getCurrentMode() == PluginMode.PEX) {
            ScriptRunner runner = new ScriptRunner(connection, false, false);
            try {
                InputStream in = getClass().getResourceAsStream("deploy.sql");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                runner.runScript(reader);
                MySQLController.getInstance().prgStatus.setVisible(false);
                loadPermissions();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PEXSQL.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | SQLException ex) {
                Logger.getLogger(PEXSQL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void checkTables() {
        String query = "SHOW TABLES LIKE 'permissions';";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.first()) {
                createTables();
                System.out.println("Creating tables!");

            } else {
                System.out.println("Tables existed!");
                MySQLController.getInstance().prgStatus.setVisible(false);
                loadPermissions();
            }

        } catch (SQLException e) {
            System.out.println("Result error: " + e.getMessage());
        }

    }

    public void loadPermissions() {
        String query = "SELECT * from permissions;";
        List<String> checkedPlayers = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String group = rs.getString("name");
                Integer type = rs.getInt("type");
                String permission = rs.getString("permission");
                if (type == 0) {
                    Group group2 = new Group();
                    group2.setName(group);
                    if (!PEXBuilderController.getInstance().globalGroups.contains(group)) {
                        PEXBuilderController.getInstance().globalGroups.add(group);
                        PEXBuilderController.getInstance().mappedGroups.put(group, group2);
                        PEXBuilderController.getInstance().groups.add(group);
                        System.out.println("Group: " + group);
                    }

                    if (!PEXBuilderController.getInstance().globalPerms.containsKey(permission)) {
                        Permission perm = new Permission(permission, "", false, "Manual");
                        PEXBuilderController.getInstance().globalPerms.put(permission, perm);
                        group2.getPermissions().put(permission, perm);

                    }
                    // if person is player
                } else if (type == 1) {

                    if (!checkedPlayers.contains(group)) {
                        Player player = new Player(group);
                        System.out.println("New User Permission check:" + permission);
                        if (permission.equalsIgnoreCase("prefix")) {
                            System.out.println("Was prefix mode!.... Append");
                            player.setPrefix(rs.getString("value"));
                            System.out.println("Set Prefix: " + rs.getString("value"));
                        } else if (permission.equalsIgnoreCase("suffix")) {
                            player.setSuffix(rs.getString("value"));
                        } else {
                            System.out.println("Added permission: " + permission + " to player");
                            Permission perm = new Permission(permission, "", false, "Manual");
                            player.getPermissions().put(permission, perm);
                        }
                        System.out.println("Checking: " + group);

                        String json = PEXBuilderController.getInstance().getUsername(group);
                        JSONObject obj = new JSONObject(json);
                        String username = obj.getString("name");
                        System.out.println("JSON: " + obj.toString());
                        PEXBuilderController.getInstance().mappedPlayers.put(username, player);
                        PEXBuilderController.getInstance().players.add(username);
                        SQLPlayers.put(group, username);
                        checkedPlayers.add(group);
                    } else {
                        String username = SQLPlayers.get(group);
                        Player player = PEXBuilderController.getInstance().mappedPlayers.get(username);
                        System.out.println("User Existed Permission check:" + permission);
                        if (permission.equalsIgnoreCase("prefix")) {
                            player.setPrefix(rs.getString("value"));
                            System.out.println("Set Prefix: " + rs.getString("value"));
                        } else if (permission.equalsIgnoreCase("suffix")) {
                            player.setSuffix(rs.getString("value"));
                        } else {
                            System.out.println("Added permission: " + permission + " to player");
                            Permission perm = new Permission(permission, "", false, "Manual");
                            player.getPermissions().put(permission, perm);
                        }
                        PEXBuilderController.getInstance().mappedPlayers.put(username, player);
                    }
                }

            }

            PEXBuilderController.getInstance().loadAvailable();
            PEXBuilderController.getInstance().refreshList();
            PEXBuilderController.getInstance().enableAll();

        } catch (SQLException e) {
            System.out.println("Result error: " + e.getMessage());
        }
    }

    public void addPermission(String permission, String group, Integer type) {
        String query = "INSERT INTO permissions (name, type, permission, world, value) VALUES (" + group + ", " + type + ", " + permission + ",,true);";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
        } catch (Exception e) {
            System.out.println("Couldnt add permission: "+e.getMessage());
        }
    }

    public void removePermission(String permission, String group) {

    }

    public static PEXSQL getInstance() {
        return INSTANCE;
    }
}
