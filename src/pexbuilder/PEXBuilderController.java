/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import com.sun.javafx.tk.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

/**
 *
 * @author Bryce
 */
public class PEXBuilderController implements Initializable {

    public Stage thisStage;

    @FXML
    public Button btnClose, btnAddPerm, btnRemovePerm;

    @FXML
    public AnchorPane anchPlugin;

    @FXML
    public Label lblDrag;

    @FXML
    public TextField txtPerm, txtGroup;

    @FXML
    public ListView lstPlugins, lstGroups, lstActive, lstAvailable;

    private FileConfiguration pluginConfig = null;

    private FileConfiguration permissionConfig = null;

    protected ObservableList<String> plugins = FXCollections.observableArrayList();

    protected ObservableList<String> groups = FXCollections.observableArrayList();

    protected ObservableList<String> activePerms = FXCollections.observableArrayList();

    protected ObservableList<String> globalPerms = FXCollections.observableArrayList();

    protected ObservableList<String> availablePerms = FXCollections.observableArrayList();

    Map<String, Plugin> mappedPlugins = new HashMap<>();

    Map<String, Group> mappedGroups = new HashMap<>();

    private static PEXBuilderController INSTANCE;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        INSTANCE = this;
        lstActive.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        EventHandler<MouseEvent> eventHandler = (event) -> {
            if (!event.isShortcutDown()) {
                Event.fireEvent(event.getTarget(), cloneMouseEvent(event));
                event.consume();
            }
        };
        lstActive.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        lstActive.addEventFilter(MouseEvent.MOUSE_RELEASED, eventHandler);

        lstAvailable.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        lstAvailable.addEventFilter(MouseEvent.MOUSE_RELEASED, eventHandler);

    }

    public static PEXBuilderController getInstance() {
        return INSTANCE;
    }

    public void btnCloseAction(ActionEvent e) {
        System.exit(1);
    }

    public void setStage(Stage input) {
        thisStage = input;
    }

    public void showStage() {
        thisStage.show();
    }

    public Double initialX, initialY;

    public void mouseDragAction(MouseEvent e) {
        thisStage.getScene().getWindow().setX(e.getScreenX() - initialX);
        thisStage.getScene().getWindow().setY(e.getScreenY() - initialY);
    }

    public void dragAction(MouseEvent event) {
        System.out.println("Set drag!");
        initialX = event.getSceneX();
        initialY = event.getSceneY();
    }

    public void pluginDrop(Dragboard db) {
        String filePath;
        String fileType;
        for (File file : db.getFiles()) {
            filePath = file.getAbsolutePath();
            fileType = filePath.substring(filePath.lastIndexOf("."), filePath.length());
            if (fileType.equalsIgnoreCase(".jar")) {
                lblDrag.setText("Drag and Drop a plugin here!");
                lblDrag.setTextFill(Color.web("#000000"));
                loadPlugin(filePath);
            } else {
                lblDrag.setText("Please use a valid plugin!");
                lblDrag.setTextFill(Color.web("#ed3838"));
            }
        }
    }

    public void loadPlugin(String path) {
        InputStream in;
        System.out.println("Input path: " + path);
        String inputFile = "jar:file:/" + path + "!/plugin.yml";
        URL inputURL = null;
        try {
            inputURL = new URL(inputFile);
            JarURLConnection conn = (JarURLConnection) inputURL.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            File targetFile = new File(defaultDirectory() + "PEXBuilder");
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            pluginConfig = loadConfiguration(targetFile);
        } catch (MalformedURLException e1) {
            System.err.println("Malformed input URL: " + inputURL);
        } catch (IOException e1) {
            System.err.println("IO error open connection");
        } catch (Exception ex) {
            System.out.println("Exception! " + ex.getMessage());
        }

        String[] list = {"name", "description", "version", "author", "main", "depend"};
        Plugin plugin = new Plugin();
        for (String str : list) {
            if (getCustomConfig().contains(str)) {
                if (str.equalsIgnoreCase("name")) {
                    plugin.setName(getCustomConfig().getString(str));
                }
                if (str.equalsIgnoreCase("description")) {
                    plugin.setDescription(getCustomConfig().getString(str));
                }
                if (str.equalsIgnoreCase("version")) {
                    plugin.setVersion(getCustomConfig().getString(str));
                }
                if (str.equalsIgnoreCase("author")) {
                    plugin.setAuthor(getCustomConfig().getString(str));
                }
                if (str.equalsIgnoreCase("main")) {
                    plugin.setMain(getCustomConfig().getString(str));
                }
                if (str.equalsIgnoreCase("depend")) {
                    String depend = getCustomConfig().getString(str);
                    depend = depend.replace("[", "").replace("]", "").replace(" ", "").trim();
                    plugin.setDepend(depend.split(","));
                }
            }
        }

        plugins.add(plugin.getName());
// Check for children, add them to a list.
        getCustomConfig().getConfigurationSection("permissions").getKeys(true).stream().filter((str) -> (!str.contains(".description") && !str.contains(".default") && str.contains("."))).map((str) -> str).forEachOrdered((permission) -> {
            System.out.println("Keys: " + permission);
            String description = getCustomConfig().getString("permissions." + permission + ".description");
            if (description != null) {
                Boolean def = getCustomConfig().getBoolean("permissions." + permission + ".default");
                Permission perm = new Permission(permission, description, def);
                System.out.println("Permission: " + permission + " with description: " + description + " added.");
                plugin.permissions.put(permission, perm);
                if ((!globalPerms.contains(perm.getPermission()))) {
                    globalPerms.add(perm.getPermission());
                }
            }else if (permission.contains(".children")){
                String child = StringUtils.substringAfter(permission, ".children");
                System.out.println("Child: "+child);
            }
        });

        mappedPlugins.put(plugin.getName(), plugin);
        lstPlugins.setItems(plugins);

    }

    public void lstPluginAction(Event e) {
        PluginInfoController controller = new PluginInfoController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PluginInfo.fxml"));
        fxmlLoader.setController(controller);
        Parent root1 = null;
        try {
            root1 = (Parent) fxmlLoader.load();
        } catch (IOException ex) {

        }
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        controller.setStage(stage);
        controller.loadPlugin((String) lstPlugins.getSelectionModel().getSelectedItem());
    }

    public String defaultDirectory() {
        String OS = getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            return getenv("APPDATA");
        } else if (OS.contains("MAC")) {
            return getProperty("user.home") + "/Library/Application "
                    + "Support";
        } else if (OS.contains("NUX")) {
            return getProperty("user.home");
        }
        return getProperty("user.dir");
    }

    public void btnOpenAction(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Permissions", "permissions.yml")
        );
        File file = fileChooser.showOpenDialog(thisStage);
        permissionConfig = loadConfiguration(file);
        loadPermissionConfig();
    }

    public void loadPermissionConfig() {
        getPermConfig().getConfigurationSection("groups").getKeys(false).forEach((str) -> {
            Group group = new Group();
            group.setName(str);
            groups.add(str);
            getPermConfig().getStringList("groups." + str + ".permissions").forEach((perm) -> {
                Permission permission = new Permission(perm, "", false);
                group.getPermissions().put(permission.getPermission(), permission);
            });
            mappedGroups.put(str, group);

        });
        lstGroups.setItems(groups);
        lstActive.setItems(activePerms);
    }

    public void lstGroupsAction(Event e) {
        String group = (String) lstGroups.getSelectionModel().getSelectedItem();
        availablePerms.clear();
        activePerms.clear();
        mappedGroups.get(group).getPermissions().entrySet().stream()
                .sorted(Map.Entry.<String, Permission>comparingByKey())
                .forEachOrdered(x -> activePerms.add(x.getKey()));

        globalPerms.stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
            availablePerms.add(str);
        });
        lstActive.setItems(activePerms);
        lstAvailable.setItems(availablePerms);

    }

    public FileConfiguration getCustomConfig() {
        return pluginConfig;
    }

    public FileConfiguration getPermConfig() {
        return permissionConfig;
    }

    public void btnAddPermAction(ActionEvent e) {
        if (!txtPerm.getText().equalsIgnoreCase("")) {
            if (!globalPerms.contains(txtPerm.getText())) {
                availablePerms.clear();
                globalPerms.add(txtPerm.getText());
                globalPerms.stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
                    availablePerms.add(str);
                });
                lstAvailable.setItems(availablePerms);
            }
            txtPerm.setText("");
        }
    }

    public void btnLeftAction(ActionEvent e) {
        ObservableList<String> list = lstAvailable.getSelectionModel().getSelectedItems();
        list.stream().map((str) -> {
            availablePerms.remove(str);
            return str;
        }).forEachOrdered((str) -> {
            activePerms.add(str);
            Permission perm = new Permission(str, "", false);
            mappedGroups.get((String) lstGroups.getSelectionModel().getSelectedItem()).getPermissions().put(str, perm);
        });
        lstActive.setItems(activePerms);
        lstAvailable.setItems(availablePerms);
    }

    public void btnRightAction(ActionEvent e) {
        ObservableList<String> list = lstActive.getSelectionModel().getSelectedItems();
        list.forEach((str) -> {
            String selected = (String) lstGroups.getSelectionModel().getSelectedItem();
            mappedGroups.get(selected).getPermissions().remove(str);
            activePerms.remove(str);
            availablePerms.add(str);
            if (!globalPerms.contains(str)) {
                globalPerms.add(str);
            }
        });
        lstActive.setItems(activePerms);
        lstAvailable.setItems(availablePerms);
    }

    public void btnAddGroupAction(ActionEvent e) {
        if (!txtGroup.getText().equalsIgnoreCase("")) {
            Group group = new Group();
            groups.add(txtGroup.getText());
            group.setName(txtGroup.getText());
            mappedGroups.put(txtGroup.getText(), group);
            txtGroup.setText("");
            lstGroups.setItems(groups);
        }
    }

    public void btnRemoveGroupAction(ActionEvent e) {
        String selected = (String) lstGroups.getSelectionModel().getSelectedItem();
        groups.remove(selected);
        mappedGroups.remove(selected);
        lstGroups.setItems(groups);
    }

    public void btnRemovePermAction(ActionEvent e) {
        String selected = (String) lstAvailable.getSelectionModel().getSelectedItem();
        availablePerms.remove(selected);
        globalPerms.remove(selected);
        lstAvailable.setItems(availablePerms);
    }

    private MouseEvent cloneMouseEvent(MouseEvent event) {
        switch (Toolkit.getToolkit().getPlatformShortcutKey()) {
            case SHIFT:
                return new MouseEvent(
                        event.getSource(),
                        event.getTarget(),
                        event.getEventType(),
                        event.getX(),
                        event.getY(),
                        event.getScreenX(),
                        event.getScreenY(),
                        event.getButton(),
                        event.getClickCount(),
                        true,
                        event.isControlDown(),
                        event.isAltDown(),
                        event.isMetaDown(),
                        event.isPrimaryButtonDown(),
                        event.isMiddleButtonDown(),
                        event.isSecondaryButtonDown(),
                        event.isSynthesized(),
                        event.isPopupTrigger(),
                        event.isStillSincePress(),
                        event.getPickResult()
                );

            case CONTROL:
                return new MouseEvent(
                        event.getSource(),
                        event.getTarget(),
                        event.getEventType(),
                        event.getX(),
                        event.getY(),
                        event.getScreenX(),
                        event.getScreenY(),
                        event.getButton(),
                        event.getClickCount(),
                        event.isShiftDown(),
                        true,
                        event.isAltDown(),
                        event.isMetaDown(),
                        event.isPrimaryButtonDown(),
                        event.isMiddleButtonDown(),
                        event.isSecondaryButtonDown(),
                        event.isSynthesized(),
                        event.isPopupTrigger(),
                        event.isStillSincePress(),
                        event.getPickResult()
                );

            case ALT:
                return new MouseEvent(
                        event.getSource(),
                        event.getTarget(),
                        event.getEventType(),
                        event.getX(),
                        event.getY(),
                        event.getScreenX(),
                        event.getScreenY(),
                        event.getButton(),
                        event.getClickCount(),
                        event.isShiftDown(),
                        event.isControlDown(),
                        true,
                        event.isMetaDown(),
                        event.isPrimaryButtonDown(),
                        event.isMiddleButtonDown(),
                        event.isSecondaryButtonDown(),
                        event.isSynthesized(),
                        event.isPopupTrigger(),
                        event.isStillSincePress(),
                        event.getPickResult()
                );

            case META:
                return new MouseEvent(
                        event.getSource(),
                        event.getTarget(),
                        event.getEventType(),
                        event.getX(),
                        event.getY(),
                        event.getScreenX(),
                        event.getScreenY(),
                        event.getButton(),
                        event.getClickCount(),
                        event.isShiftDown(),
                        event.isControlDown(),
                        event.isAltDown(),
                        true,
                        event.isPrimaryButtonDown(),
                        event.isMiddleButtonDown(),
                        event.isSecondaryButtonDown(),
                        event.isSynthesized(),
                        event.isPopupTrigger(),
                        event.isStillSincePress(),
                        event.getPickResult()
                );

            default: // well return itself then
                return event;

        }
    }

}
