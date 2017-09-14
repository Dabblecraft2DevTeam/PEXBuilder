/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import com.sun.javafx.tk.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.bukkit.configuration.file.FileConfiguration;
import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Bryce
 */
public class PEXBuilderController implements Initializable {

    @FXML
    private Stage thisStage;

    @FXML
    public Button btnClose, btnSQL, btnAddPerm, btnRemovePerm, btnAddGroup, btnOpen, btnLeft, btnRight, btnRemoveGroup, btnRightPlayer, btnLeftPlayer, btnAddPermPlayer, btnAddPlayer, btnNew, btnUpgrade;

    @FXML
    public AnchorPane anchPlugin, anchMain;

    @FXML
    public Label lblDrag, lblDescription, lblGroup, lblDescription2, lblName, lblUsername, lblRank, lblDeveloper, lblUpgraded, lblMessage, lblOnline;

    @FXML
    public TextField txtPerm, txtGroup, txtSuffix, txtPrefix, txtRank, txtPlayer, txtPermPlayer, txtPrefixPlayer, txtSuffixPlayer, txtNamePlayer, txtSearch, txtPlayerSearch;

    @FXML
    public CheckBox chkDefault;

    @FXML
    public Tab tbPlugins, tbGroupPerms, tbGroupOptions, tbPlayerPerms, tbPlayerOptions;

    @FXML
    public ImageView imgAvatar;

    @FXML
    public WebView webAdvert;

    @FXML
    public TabPane tbMain;

    @FXML
    public ListView lstPlugins, lstGroups, lstActive, lstAvailable, lstAvailableGroups, lstActiveGroups, lstPlayers, lstActivePlayer, lstAvailablePlayer;

    private FileConfiguration pluginConfig = null;

    private FileConfiguration permissionConfig = null;

    private Timeline timeline;

    public PluginMode mode;

    protected ObservableList<String> plugins = FXCollections.observableArrayList();

    protected ObservableList<String> groups = FXCollections.observableArrayList();

    protected ObservableList<String> activePerms = FXCollections.observableArrayList();

    protected ObservableList<String> globalGroups = FXCollections.observableArrayList();

    protected ObservableList<String> players = FXCollections.observableArrayList();

    protected Map<String, Permission> globalPerms = new HashMap<>();

    protected ObservableList<String> availablePerms = FXCollections.observableArrayList();

    protected Map<String, ObservableList<String>> inheritance = new HashMap<>();

    protected File permissionsFile;

    protected String jsonPlugin;

    Map<String, Plugin> mappedPlugins = new HashMap<>();

    Map<String, Group> mappedGroups = new HashMap<>();

    Map<String, Player> mappedPlayers = new HashMap<>();

    private static PEXBuilderController INSTANCE;

    private MySQLController MySQLController;

    private final PEXSQL sql = new PEXSQL();

    private PluginMode currentMode = PluginMode.PEX;

    public Backend backend = Backend.FILE;

    public Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());

    public PluginMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(PluginMode input) {
        currentMode = input;
    }

    public void enterSQLMode() {
        btnOpen.setDisable(true);
        btnNew.setDisable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        INSTANCE = this;
        lstActive.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        btnSQL.setDisable(true);

        EventHandler<MouseEvent> eventHandler = (event) -> {
            if (!event.isShortcutDown()) {
                Event.fireEvent(event.getTarget(), cloneMouseEvent(event));
                event.consume();
            }
        };
        lstActive.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);

        lstAvailable.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);

        lstActive.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                final ListCell cell = new ColorRectCell();
                cell.setOnMouseEntered((MouseEvent event) -> {
                    if (cell.getIndex() <= (activePerms.size() - 1)) {
                        String perm = (String) lstActive.getItems().get(cell.getIndex());
                        System.out.println("Perm: " + perm);
                        if (globalPerms.get(perm).hasDescription()) {
                            lblDescription.setText(globalPerms.get(perm).getDescription());
                        } else {
                            lblDescription.setText("No description provided");
                        }
                    } else {
                        lblDescription.setText("No description provided");
                    }
                });
                return cell;
            }
        });

        lstAvailable.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                final ListCell cell = new ColorRectCell();
                cell.setOnMouseEntered((MouseEvent event) -> {
                    if (cell.getIndex() <= (availablePerms.size() - 1)) {
                        String perm = (String) lstAvailable.getItems().get(cell.getIndex());
                        System.out.println("Perm: " + perm);
                        if (globalPerms.get(perm).hasDescription()) {
                            lblDescription.setText(globalPerms.get(perm).getDescription());
                        } else {
                            lblDescription.setText("No description provided");
                        }
                    } else {
                        lblDescription.setText("No description provided");
                    }
                });
                return cell;
            }
        });

        // NEW LIST
        lstActivePlayer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstAvailablePlayer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        lstActivePlayer.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);

        lstAvailablePlayer.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);

        lstActivePlayer.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                final ListCell cell = new ColorRectCell();
                cell.setOnMouseEntered((MouseEvent event) -> {
                    if (cell.getIndex() <= (activePerms.size() - 1)) {
                        String perm = (String) lstActivePlayer.getItems().get(cell.getIndex());
                        System.out.println("Perm: " + perm);
                        if (globalPerms.get(perm).hasDescription()) {
                            lblDescription2.setText(globalPerms.get(perm).getDescription());
                        } else {
                            lblDescription2.setText("No description provided");
                        }
                    } else {
                        lblDescription2.setText("No description provided");
                    }
                });
                return cell;
            }
        });

        lstAvailablePlayer.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                final ListCell cell = new ColorRectCell();
                cell.setOnMouseEntered((MouseEvent event) -> {
                    if (cell.getIndex() <= (availablePerms.size() - 1)) {
                        String perm = (String) lstAvailablePlayer.getItems().get(cell.getIndex());
                        System.out.println("Perm: " + perm);
                        if (globalPerms.get(perm).hasDescription()) {
                            lblDescription2.setText(globalPerms.get(perm).getDescription());
                        } else {
                            lblDescription2.setText("No description provided");
                        }
                    } else {
                        lblDescription2.setText("No description provided");
                    }
                });
                return cell;
            }
        });

        disableAll();

        if (!userPrefs.get("guest", "0").equalsIgnoreCase("1") && !userPrefs.get("token", "").equalsIgnoreCase("")) {
            Thread t1 = new Thread(() -> {
                String token = userPrefs.get("token", "");
                if (!token.equalsIgnoreCase("")) {
                    String response = Requests.verify(token);
                    if (!response.equalsIgnoreCase("")) {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");
                        if (status.equalsIgnoreCase("ok")) {
                            JSONObject data = obj.getJSONObject("data");
                            Platform.runLater(() -> {
                                if (data.getInt("developer") == 1) {
                                    lblDeveloper.setText("True");
                                } else {
                                    lblDeveloper.setText("False");
                                }

                                lblUsername.setText(data.getString("username"));
                                imgAvatar.setImage(new Image(data.getString("avatar")));
                            });
                            String upgradeResponse = Requests.isUpgraded(token);
                            JSONObject upgrade = new JSONObject(upgradeResponse);
                            String status2 = upgrade.getString("status");
                            Platform.runLater(() -> {
                                if (status2.equalsIgnoreCase("ok")) {
                                    btnUpgrade.setDisable(true);
                                    lblMessage.setText("You have already upgraded! Thanks for your purchase!");
                                    lblUpgraded.setText("True");
                                    tbMain.setPrefHeight(600);
                                    anchMain.getChildren().remove(webAdvert);
                                    anchMain.setMinSize(704, 520);
                                    thisStage.setMaxHeight(570);
                                    thisStage.setMinHeight(570);
                                } else {
                                    WebEngine webEngine = webAdvert.getEngine();
                                    webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.44 (KHTML, like Gecko) Chrome/8.0 JavaFX/8.0 Safari/537.44");
                                    webEngine.setJavaScriptEnabled(true);
                                    webEngine.load("https://shulkerbox.org/ad.html");
                                    webAdvert.setZoom(0.9);
                                    webAdvert.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                                        @Override
                                        public void onChanged(Change<? extends Node> change) {
                                            Set<Node> deadSeaScrolls = webAdvert.lookupAll(".scroll-bar");
                                            deadSeaScrolls.forEach((scroll) -> {
                                                scroll.setVisible(false);
                                            });
                                        }
                                    });

                                    webEngine.locationProperty().addListener((ObservableValue<? extends String> ov, final String oldLoc, final String loc) -> {
                                        if (!loc.equalsIgnoreCase("https://shulkerbox.org/ad.html")) {
                                            System.out.println("Loc: " + loc + " Old: " + oldLoc);
                                            webEngine.load("https://shulkerbox.org/ad.html");
                                            PEXBuilder.getInstance().openURL(loc);
                                        }
                                    });

                                    webAdvert.setVisible(true);
                                    System.out.println("Loaded web engine");
                                    lblUpgraded.setText("False");
                                }
                                getOnlineUsers();
                            });
                        } else {
                            logout();
                        }
                    }
                }
            });
            t1.start();
        } else {
            WebEngine webEngine = webAdvert.getEngine();
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.44 (KHTML, like Gecko) Chrome/8.0 JavaFX/8.0 Safari/537.44");
            webEngine.setJavaScriptEnabled(true);
            webEngine.load("https://shulkerbox.org/ad.html");
            webAdvert.setVisible(true);
            lblUsername.setText("Guest");
            lblRank.setText("N/A");
            lblUpgraded.setText("N/A");
            lblDeveloper.setText("N/A");
            btnUpgrade.setDisable(true);
            lblMessage.setText("You must be logged in to upgrade to premium!");
        }
    }

    static class ColorRectCell extends ListCell<String> {

        private final Color fill = Color.BLACK;

        @Override
        public void updateItem(String item, boolean empty) {

            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setTextFill(null);
                return;
            }
            setText(String.valueOf(item));
            setTextFill(fill);
        }
    }

    public void btnLogoutAction(ActionEvent e) {
        logout();
    }

    public void getOnlineUsers() {
        timeline = new Timeline(new KeyFrame(Duration.ZERO, ev -> {
            try {
                System.out.println("Tick!");
                String response = Requests.getOnline();
                JSONObject obj = new JSONObject(response);
                JSONObject data = obj.getJSONObject("data");

                Integer online = data.getInt("total");
                lblOnline.setText(online.toString());
            } catch (JSONException e) {
                System.out.println("Error: " + e.getMessage());
            }

        }), new KeyFrame(Duration.minutes(5)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void logout() {
        LoginController controller;
        URL location = getClass().getResource("Login.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = null;
        try {
            root = (Parent) fxmlLoader.load(location.openStream());

        } catch (IOException ex) {
            Logger.getLogger(LoginController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        controller = fxmlLoader.getController();

        Scene scene = new Scene(root);
        thisStage.setScene(scene);
        thisStage.setResizable(false);
        thisStage.setTitle("PEXBuilder 1.0");

        userPrefs.put("stay", "0");

        controller.setStage(thisStage);
        controller.showStage();
    }

    public void btnUpgradeAction(ActionEvent e) {
        PEXBuilder.getInstance().openUpgrade();

    }

    public void btnAddPlayerAction(ActionEvent e) {
        if (!txtPlayer.getText().equalsIgnoreCase("")) {
            if (txtPlayer.getText().length() > 16) {
                Player player = new Player(txtPlayer.getText());
                JSONObject obj = new JSONObject(getUsername(txtPlayer.getText()));
                String username = obj.getString("name");
                player.setUUID(txtPlayer.getText());
                players.add(username);
                mappedPlayers.put(username, player);
                txtPlayer.setText("");

                lstPlayers.setItems(players);
            } else {
                System.out.println("Name Entered!");

                JSONObject obj = new JSONObject(getUUID(txtPlayer.getText()));

                String uuid1 = obj.getString("id");
                String uuid = java.util.UUID.fromString(uuid1.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")).toString();

                Player player = new Player(uuid);

                player.setUUID(uuid);
                String username = txtPlayer.getText();
                players.add(username);
                mappedPlayers.put(username, player);
                txtPlayer.setText("");

                lstPlayers.setItems(players);
            }
        }
    }

    public void txtSearchAction(Event e) {
        String search = txtSearch.getText();
        if (!txtSearch.getText().isEmpty()) {
            availablePerms.clear();
            globalPerms.keySet().stream().filter((str) -> (str.contains(search))).filter((str) -> (!availablePerms.contains(str) && !activePerms.contains(str))).forEachOrdered((str) -> {
                availablePerms.add(str);
            });

            globalPerms.values().forEach((perm) -> {
                String plugin = perm.getPlugin();
                if (plugin != null) {
                    if (plugin.contains(search)) {
                        if (!availablePerms.contains(plugin) && !activePerms.contains(plugin)) {
                            availablePerms.add(perm.getPermission());
                        }
                    }
                }
            });
            lstAvailable.setItems(availablePerms);
        } else {
            availablePerms.clear();
            loadAvailable();
        }
    }

    public void txtPlayerSearchAction(Event e) {
        String search = txtPlayerSearch.getText();
        if (!txtPlayerSearch.getText().isEmpty()) {
            availablePerms.clear();
            globalPerms.keySet().stream().filter((str) -> (str.contains(search))).filter((str) -> (!availablePerms.contains(str) && !activePerms.contains(str))).forEachOrdered((str) -> {
                availablePerms.add(str);
            });

            globalPerms.values().forEach((perm) -> {
                String plugin = perm.getPlugin();
                if (plugin != null) {
                    if (plugin.contains(search)) {
                        if (!availablePerms.contains(plugin) && !activePerms.contains(plugin)) {
                            availablePerms.add(perm.getPermission());
                        }
                    }
                }
            });
            lstAvailablePlayer.setItems(availablePerms);
        } else {
            availablePerms.clear();
            loadAvailable();
        }
    }

    public void btnAddPermPlayerAction(ActionEvent e) {
        if (!txtPermPlayer.getText().equalsIgnoreCase("")) {
            if (!globalPerms.containsKey(txtPermPlayer.getText())) {
                availablePerms.clear();
                Permission perm = new Permission(txtPermPlayer.getText(), "Manually added permission", false, "Manual");
                globalPerms.put(txtPermPlayer.getText(), perm);
                globalPerms.keySet().stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
                    availablePerms.add(str);
                });
                lstAvailablePlayer.setItems(availablePerms);
            }
            txtPermPlayer.setText("");
        }
    }

    public void btnRemovePermPlayerAction(ActionEvent e) {
        String selected = (String) lstAvailablePlayer.getSelectionModel().getSelectedItem();
        availablePerms.remove(selected);
        globalPerms.remove(selected);
        lstAvailablePlayer.setItems(availablePerms);
    }

    public void btnRemovePlayerAction(ActionEvent e) {
        String selected = (String) lstPlayers.getSelectionModel().getSelectedItem();
        players.remove(selected);
        mappedPlayers.remove(selected);
        lstPlayers.setItems(groups);
    }

    public void setPermissionsFile(File input) {
        permissionsFile = input;
    }

    public File getPermissionsFile() {
        return permissionsFile;
    }

    public void savePermissions() {
        if (backend == Backend.FILE) {
            System.out.println("Saved!");
            if (lstGroups != null) {
                String group = (String) lstGroups.getSelectionModel().getSelectedItem();

                if (group != null) {
                    if (!activePerms.isEmpty()) {
                        getPermConfig().set("groups." + group + ".permissions", activePerms);
                    } else {
                        getPermConfig().set("groups." + group + ".permissions", null);
                    }

                    if (!inheritance.isEmpty()) {
                        getPermConfig().set("groups." + group + ".inheritance", inheritance.get(group));

                    } else {
                        getPermConfig().set("groups." + group + ".inheritance", null);
                    }

                    if (!txtSuffix.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.suffix", txtSuffix.getText());
                    }

                    if (!txtPrefix.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.prefix", txtPrefix.getText());
                    }

                    if (!txtRank.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.rank", txtRank.getText());
                    }

                    getPermConfig().set("groups." + group + ".options.default", chkDefault.isSelected());
                }
            }

            if (lstPlayers != null) {
                String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
                if (player != null) {
                    Player ply = mappedPlayers.get(player);
                    if (ply.hasPermissions()) {
                        List<String> toAdd = new ArrayList<>();
                        ply.getPermissions().values().forEach((perm) -> {
                            toAdd.add(perm.getPermission());
                        });
                        getPermConfig().set("users." + ply.getUUID() + ".permissions", toAdd);

                    }

                    if (!txtSuffixPlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.suffix", txtSuffixPlayer.getText());
                        System.out.println("Setting player suffix");
                    }
                    if (!txtPrefixPlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.prefix", txtPrefixPlayer.getText());
                        System.out.println("Setting player prefix");
                    }
                    if (!txtNamePlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.name", txtNamePlayer.getText());
                        System.out.println("Setting player name!");
                    }
                }
            }

            if (getPermConfig() != null) {
                try {
                    getPermConfig().save(getPermissionsFile());
                } catch (IOException e) {

                }
            }
        } else if (backend == Backend.MYSQL) {
            if (lstGroups != null) {
                String group = (String) lstGroups.getSelectionModel().getSelectedItem();

                if (group != null) {
                    if (!activePerms.isEmpty()) {
                        getPermConfig().set("groups." + group + ".permissions", activePerms);
                    } else {
                        getPermConfig().set("groups." + group + ".permissions", null);
                    }

                    if (!inheritance.isEmpty()) {
                        getPermConfig().set("groups." + group + ".inheritance", inheritance.get(group));

                    } else {
                        getPermConfig().set("groups." + group + ".inheritance", null);
                    }

                    if (!txtSuffix.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.suffix", txtSuffix.getText());
                    }

                    if (!txtPrefix.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.prefix", txtPrefix.getText());
                    }

                    if (!txtRank.getText().isEmpty()) {
                        getPermConfig().set("groups." + group + ".options.rank", txtRank.getText());
                    }

                    getPermConfig().set("groups." + group + ".options.default", chkDefault.isSelected());
                }
            }

            if (lstPlayers != null) {
                String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
                if (player != null) {
                    Player ply = mappedPlayers.get(player);
                    if (ply.hasPermissions()) {
                        List<String> toAdd = new ArrayList<>();
                        ply.getPermissions().values().forEach((perm) -> {
                            toAdd.add(perm.getPermission());
                        });
                        getPermConfig().set("users." + ply.getUUID() + ".permissions", toAdd);

                    }

                    if (!txtSuffixPlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.suffix", txtSuffixPlayer.getText());
                        System.out.println("Setting player suffix");
                    }
                    if (!txtPrefixPlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.prefix", txtPrefixPlayer.getText());
                        System.out.println("Setting player prefix");
                    }
                    if (!txtNamePlayer.getText().isEmpty()) {
                        getPermConfig().set("users." + ply.getUUID() + ".options.name", txtNamePlayer.getText());
                        System.out.println("Setting player name!");
                    }
                }
            }
        }
    }

    public void btnSQLAction(ActionEvent e) {
        showSQL();
    }

    public void showSQL() {
        Stage stage;
        MySQLController = new MySQLController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MySQL.fxml"));
        fxmlLoader.setController(MySQLController);
        Parent root1 = null;
        try {
            root1 = (Parent) fxmlLoader.load();
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        stage = new Stage();
        stage.setScene(new Scene(root1));

        stage.show();
    }

    public void disableAll() {
        tbPlugins.setDisable(true);
        tbGroupOptions.setDisable(true);
        tbPlayerPerms.setDisable(true);
        tbPlayerOptions.setDisable(true);
        btnAddPerm.setDisable(true);
        btnRemovePerm.setDisable(true);
        btnAddGroup.setDisable(true);
        btnOpen.setDisable(false);
        btnLeft.setDisable(true);
        btnRight.setDisable(true);
        btnRemoveGroup.setDisable(true);
    }

    public void enableAll() {
        tbPlugins.setDisable(false);
        tbGroupOptions.setDisable(false);
        tbPlayerPerms.setDisable(false);
        tbPlayerOptions.setDisable(false);
        btnAddPerm.setDisable(false);
        btnRemovePerm.setDisable(false);
        btnAddGroup.setDisable(false);
        btnOpen.setDisable(false);
        btnLeft.setDisable(false);
        btnRight.setDisable(false);
        btnRemoveGroup.setDisable(false);
    }

    public static PEXBuilderController getInstance() {
        return INSTANCE;
    }

    public void btnCloseAction(ActionEvent e) {
        savePermissions();
        System.exit(1);
    }

    public void setStage(Stage input) {
        thisStage = input;
    }

    public Stage getStage() {
        return thisStage;
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
            System.out.println("File: " + filePath);
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
        InputStream is;
        System.out.println("Input path: " + path);
        String inputFile = "jar:file:/" + path + "!/plugin.yml";
        URL inputURL = null;
        Yaml yml = new Yaml();
        try {

            inputURL = new URL(inputFile);
            JarURLConnection conn = (JarURLConnection) inputURL.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            File targetFile = new File(defaultDirectory() + "PEXBuilder");
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            is = new FileInputStream(targetFile);

            Yaml yaml = new Yaml();

            Object obj = yml.load(is);

            jsonPlugin = JSONValue.toJSONString(obj);

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

        if (!mappedPlugins.containsKey(plugin.getName())) {

// Permissions are missing 1 perm essentials.gamemode is missing?
            JSONObject obj = new JSONObject(jsonPlugin);
            JSONObject perms = obj.getJSONObject("permissions");
            String desc;

            // ADD GLOBAL PERMS TO LIST, Permisison:str, perm:jsonObject
            for (String str : perms.keySet()) {
                if (perms.get(str) instanceof JSONObject) {
                    Permission p;
                    JSONObject permission = perms.getJSONObject(str);
                    JSONObject foundPerm = perms.getJSONObject(str);

                    String def = "";
                    Boolean def2 = false;

                    if (permission.has("description")) {
                        desc = permission.getString("description");
                    } else {
                        desc = "";
                    }
                    if (permission.has("default")) {

                        if (permission.get("default") instanceof String) {
                            def = permission.getString("default");
                            p = new Permission(str, desc, false, plugin.getName());
                        } else {
                            def2 = permission.getBoolean("default");
                            p = new Permission(str, desc, def2, plugin.getName());
                        }
                    } else {
                        p = new Permission(str, desc, false, plugin.getName());
                    }
                    plugin.getPermissions().put(str, p);
                    globalPerms.put(str, p);

                    System.out.println("Key: " + str);
                    System.out.println("Desc: " + desc);
                    System.out.println("Def: " + def + "def2: " + def2);

                    if (foundPerm.has("children")) {
                        JSONObject children = foundPerm.getJSONObject("children");
                        children.keySet().stream().map((child) -> {
                            Permission childPerm = new Permission(child, "", false, plugin.getName());
                            globalPerms.put(child, childPerm);
                            plugin.getPermissions().put(child, childPerm);
                            return child;
                        }).forEachOrdered((child) -> {
                            System.out.println("Child: " + child);
                        });
                    } else {
                        System.out.println("Had no children :C");
                    }
                }
            }
            plugins.add(plugin.getName());
            mappedPlugins.put(plugin.getName(), plugin);
            loadAvailable();
            lstPlugins.setItems(plugins);
        } else {
            System.out.println("Already existed!");
        }
    }

    private static String convertToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Object obj = yaml.load(yamlString);

        return JSONValue.toJSONString(obj);
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
        setPermissionsFile(file);
        permissionConfig = loadConfiguration(file);
        loadPermissionConfig();
    }

    public void loadPermissionConfig() {
        getPermConfig().getConfigurationSection("groups").getKeys(false).forEach((str) -> {
            Group group = new Group();
            group.setName(str);
            groups.add(str);
            String selectedGroup = (String) lstGroups.getSelectionModel().getSelectedItem();
            getPermConfig().getStringList("groups." + str + ".permissions").forEach((perm) -> {
                Permission permission = new Permission(perm, "", false, "");
                group.getPermissions().put(permission.getPermission(), permission);
                globalPerms.put(permission.getPermission(), permission);
            });
            mappedGroups.put(str, group);
            if (getPermConfig().contains("groups." + str + ".inheritance")) {
                ObservableList<String> inheritGroups = FXCollections.observableArrayList();

                getPermConfig().getStringList("groups." + str + ".inheritance").forEach((inherit) -> {
                    inheritGroups.add(inherit);
                });
                inheritance.put(str, inheritGroups);

            }
        });

        if (getPermConfig().contains("users")) {

            getPermConfig().getConfigurationSection("users").getKeys(false).stream().map((str) -> {
                System.out.println("Players: " + str);
                return str;
            }).map((str) -> {
                String json = getUsername(str);
                System.out.println("STR: " + str);
                JSONObject obj = new JSONObject(json);
                String username = obj.getString("name");
                System.out.println("JSON: " + obj.toString());
                Player player = new Player(username);
                player.setUUID(str);
                getPermConfig().getStringList("users." + str + ".permissions").forEach((perm) -> {
                    Permission permission = new Permission(perm, "", false, "");
                    player.getPermissions().put(perm, permission);
                    if (!globalPerms.containsKey(perm)) {
                        globalPerms.put(perm, permission);
                    }
                });
                if (getPermConfig().contains("users." + str + ".options.prefix")) {
                    player.setPrefix(getPermConfig().getString("users." + str + ".options.prefix"));
                }
                if (getPermConfig().contains("users." + str + ".options.suffix")) {
                    player.setSuffix(getPermConfig().getString("users." + str + ".options.suffix"));
                }
                mappedPlayers.put(username, player);
                return username;
            }).forEachOrdered((username) -> {
                players.add(username);
            });
        }
        lstPlayers.setItems(players);
        lstGroups.setItems(groups);
        lstActive.setItems(activePerms);
        String selected = (String) lstGroups.getSelectionModel().getSelectedItem();
        if (!inheritance.isEmpty() && inheritance.containsKey(selected)) {
            lstActiveGroups.setItems(inheritance.get(selected));
        }
        enableAll();
    }

    public void refreshList() {
        lstPlayers.setItems(players);
        lstGroups.setItems(groups);
    }

    public void saveNew(String path) {
        URL inputUrl = getClass().getResource("permissions.yml");
        File dest = new File(path);
        try {
            FileUtils.copyURLToFile(inputUrl, dest);

        } catch (IOException ex) {
            Logger.getLogger(PEXBuilderController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void btnNewAction(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Default Permissions");
        fileChooser.setInitialFileName("permissions.yml");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Permissions", "*.yml")
        );
        File file = fileChooser.showSaveDialog(thisStage);
        if (file != null) {
            saveNew(file.getAbsolutePath());
        }
        setPermissionsFile(file);
        permissionConfig = loadConfiguration(file);
        loadPermissionConfig();
    }

    public void btnLeftPlayerAction(ActionEvent e) {
        ObservableList<String> list = lstAvailablePlayer.getSelectionModel().getSelectedItems();
        String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
        System.out.println("Size: " + list.size());
        String removing = "";
        String[] toRemove;
        removing = list.stream().map((str) -> {
            System.out.println("Moving: " + str);
            return str;
        }).map((str) -> {
            activePerms.add(str);
            return str;
        }).map((str) -> {
            Permission perm = new Permission(str, "", false, "");
            mappedPlayers.get(player).getPermissions().put(str, perm);
            return str;
        }).map((str) -> str + " ").reduce(removing, String::concat);
        removing = removing.trim();
        toRemove = removing.split(" ");

        for (String str : toRemove) {
            availablePerms.remove(str);
        }
        lstAvailablePlayer.setItems(availablePerms);
        lstAvailablePlayer.getSelectionModel().clearSelection();
        lstActivePlayer.setItems(activePerms);
        savePermissions();
    }

    public void btnRightPlayerAction(ActionEvent e) {
        ObservableList<String> list = lstActivePlayer.getSelectionModel().getSelectedItems();
        String removing = "";
        String[] toRemove;
        String selected = (String) lstPlayers.getSelectionModel().getSelectedItem();

        for (String str : list) {
            mappedPlayers.get(selected).getPermissions().remove(str);
            removing += str + " ";
            availablePerms.add(str);
            if (!globalPerms.containsKey(str)) {
                Permission permission = new Permission(str, "", false, "");
                globalPerms.put(str, permission);
            }
        }
        removing = removing.trim();
        toRemove = removing.split(" ");
        for (String str : toRemove) {
            activePerms.remove(str);
        }
        lstActivePlayer.setItems(activePerms);
        lstAvailablePlayer.setItems(availablePerms);
        savePermissions();
    }

    public void lstPlayersAction(Event e) {
        String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
        lblName.setText(player);
        tbGroupOptions.setDisable(true);
        tbPlayerOptions.setDisable(false);
        lstGroups.getSelectionModel().clearSelection();
        availablePerms.clear();
        activePerms.clear();
        txtPrefixPlayer.setText("");
        txtSuffixPlayer.setText("");
        txtNamePlayer.setText("");

        if (mappedPlayers.get(player).hasPermissions()) {
            mappedPlayers.get(player).getPermissions().entrySet().stream()
                    .sorted(Map.Entry.<String, Permission>comparingByKey())
                    .forEachOrdered(x -> activePerms.add(x.getKey()));
        }
        loadAvailablePlayer();
        lstActivePlayer.setItems(activePerms);
    }

    public String getUsername(String input) {
        String body;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + input.replace("-", ""));
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
            return body;
        } catch (IOException e) {
            return "";
        }
    }

    public String getUUID(String input) {
        String body;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + input);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
            return body;
        } catch (IOException e) {
            return "";
        }
    }

    public void loadGroup(String name) {
        if (backend == Backend.FILE) {
            lblGroup.setText(name);
            ObservableList active = FXCollections.observableArrayList();
            ObservableList available = FXCollections.observableArrayList();

            if (inheritance.containsKey(name)) {
                System.out.println("Contained inheritance!");
                active = inheritance.get(name);

                for (String str : mappedGroups.keySet()) {
                    if (!active.contains(str)) {
                        System.out.println(str + " wasnt in activeGroups!");
                        available.add(str);
                    }
                }

            } else {
                mappedGroups.keySet().forEach((str) -> {
                    available.add(str);
                });
            }
            lstAvailableGroups.setItems(available);
            lstActiveGroups.setItems(active);

            if (getPermConfig().contains("groups." + name + ".options.suffix")) {
                txtSuffix.setText(getPermConfig().getString("groups." + name + ".options.suffix"));
            }

            if (getPermConfig().contains("groups." + name + ".options.prefix")) {
                txtPrefix.setText(getPermConfig().getString("groups." + name + ".options.prefix"));
            }

            if (!getPermConfig().contains("groups." + name + ".options.default")) {
                chkDefault.setSelected(false);
            } else {
                chkDefault.setSelected(getPermConfig().getBoolean("groups." + name + ".options.default"));
            }
            if (getPermConfig().contains("groups." + name + ".options.rank")) {
                txtRank.setText(getPermConfig().getString("groups." + name + ".options.rank"));
            }
        } else if (backend == Backend.MYSQL) {
            lblGroup.setText(name);
            ObservableList active = FXCollections.observableArrayList();
            ObservableList available = FXCollections.observableArrayList();

            if (inheritance.containsKey(name)) {
                System.out.println("Contained inheritance!");
                active = inheritance.get(name);

                for (String str : mappedGroups.keySet()) {
                    if (!active.contains(str)) {
                        System.out.println(str + " wasnt in activeGroups!");
                        available.add(str);
                    }
                }

            } else {
                mappedGroups.keySet().forEach((str) -> {
                    available.add(str);
                });
            }
            lstAvailableGroups.setItems(available);
            lstActiveGroups.setItems(active);
        }
    }

    public void btnLeftInheritAction(ActionEvent e) {
        ObservableList list = FXCollections.observableArrayList();
        ObservableList available;

        available = lstAvailableGroups.getItems();
        String group = (String) lstGroups.getSelectionModel().getSelectedItem();

        if (inheritance.containsKey(group)) {
            list = inheritance.get(group);
        }

        String selected = (String) lstAvailableGroups.getSelectionModel().getSelectedItem();
        available.remove(selected);
        list.add(selected);
        lstAvailableGroups.setItems(available);
        lstActiveGroups.setItems(list);
        inheritance.put(group, list);

        savePermissions();
    }

    public void btnRightInheritAction(ActionEvent e) {
        ObservableList list = FXCollections.observableArrayList();
        String group = (String) lstGroups.getSelectionModel().getSelectedItem();

        String selected = (String) lstAvailableGroups.getSelectionModel().getSelectedItem();
        list.add(selected);
        lstAvailableGroups.setItems(list);
        lstActiveGroups.setItems(inheritance.get(group));
        inheritance.get(group).remove(selected);
        savePermissions();
    }

    public void lstGroupsAction(Event e) {
        String group = (String) lstGroups.getSelectionModel().getSelectedItem();
        lstPlayers.getSelectionModel().clearSelection();
        tbPlayerOptions.setDisable(true);
        tbGroupOptions.setClosable(false);
        availablePerms.clear();
        activePerms.clear();
        txtPrefix.setText("");
        txtSuffix.setText("");
        txtRank.setText("");
        mappedGroups.get(group).getPermissions().entrySet().stream()
                .sorted(Map.Entry.<String, Permission>comparingByKey())
                .forEachOrdered(x -> activePerms.add(x.getKey()));

        loadAvailable();
        lstActive.setItems(activePerms);
        loadGroup(group);
    }

    public void txtPrefixAction(Event e) {
        savePermissions();
    }

    public void loadAvailable() {
        System.out.println("Loading available!");
        globalPerms.keySet().stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
            availablePerms.add(str);
        });
        availablePerms.sort(String::compareToIgnoreCase);

        lstAvailable.setItems(availablePerms);
    }

    public void loadAvailablePlayer() {
        if (backend == Backend.FILE) {
            System.out.println("Loading available!");
            String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
            globalPerms.keySet().stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
                availablePerms.add(str);
            });
            availablePerms.sort(String::compareToIgnoreCase);
            lstAvailablePlayer.setItems(availablePerms);
            Player ply = mappedPlayers.get(player);
            if (getPermConfig().contains("users." + ply.getUUID() + ".options.name")) {
                txtNamePlayer.setText(getPermConfig().getString("users." + ply.getUUID() + ".options.name"));
            } else {
                txtNamePlayer.setText(player);
            }

            if (getPermConfig().contains("users." + ply.getUUID() + ".options.prefix")) {
                txtPrefixPlayer.setText(getPermConfig().getString("users." + ply.getUUID() + ".options.prefix"));
            }

            if (getPermConfig().contains("users." + ply.getUUID() + ".options.suffix")) {
                txtSuffixPlayer.setText(getPermConfig().getString("users." + ply.getUUID() + ".options.suffix"));
            }
        } else if (backend == Backend.MYSQL) {
            System.out.println("Loading available!");
            String player = (String) lstPlayers.getSelectionModel().getSelectedItem();
            globalPerms.keySet().stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
                availablePerms.add(str);
            });
            availablePerms.sort(String::compareToIgnoreCase);
            lstAvailablePlayer.setItems(availablePerms);
            Player ply = mappedPlayers.get(player);

            if (ply.getPrefix() != null) {
                txtPrefixPlayer.setText(ply.getPrefix());
            }
            if (ply.getSuffix() != null) {
                txtSuffixPlayer.setText(ply.getSuffix());
            }

        }
    }

    public FileConfiguration getCustomConfig() {
        return pluginConfig;
    }

    public FileConfiguration getPermConfig() {
        return permissionConfig;
    }

    public void btnAddPermAction(ActionEvent e) {
        if (!txtPerm.getText().equalsIgnoreCase("")) {
            if (!globalPerms.containsKey(txtPerm.getText())) {
                availablePerms.clear();
                Permission perm = new Permission(txtPerm.getText(), "Manually added permission", false, "");
                globalPerms.put(txtPerm.getText(), perm);
                globalPerms.keySet().stream().filter((str) -> (!activePerms.contains(str))).forEachOrdered((str) -> {
                    availablePerms.add(str);
                });
                lstAvailable.setItems(availablePerms);
            }
            txtPerm.setText("");
        }
    }

    public void btnLeftAction(ActionEvent e) {
        ObservableList<String> list = lstAvailable.getSelectionModel().getSelectedItems();
        String group = (String) lstGroups.getSelectionModel().getSelectedItem();
        System.out.println("Size: " + list.size());
        String removing = "";
        String[] toRemove;
        removing = list.stream().map((str) -> {
            System.out.println("Moving: " + str);
            return str;
        }).map((str) -> {
            activePerms.add(str);
            return str;
        }).map((str) -> {
            Permission perm = new Permission(str, "", false, "");
            mappedGroups.get(group).getPermissions().put(str, perm);
            return str;
        }).map((str) -> str + " ").reduce(removing, String::concat);
        removing = removing.trim();
        toRemove = removing.split(" ");

        for (String str : toRemove) {
            availablePerms.remove(str);
        }
        lstAvailable.setItems(availablePerms);
        lstAvailable.getSelectionModel().clearSelection();
        lstActive.setItems(activePerms);
        savePermissions();
    }

    public void btnRightAction(ActionEvent e) {
        ObservableList<String> list = lstActive.getSelectionModel().getSelectedItems();
        String removing = "";
        String[] toRemove;

        for (String str : list) {
            String selected = (String) lstGroups.getSelectionModel().getSelectedItem();
            mappedGroups.get(selected).getPermissions().remove(str);
            removing += str + " ";
            availablePerms.add(str);
            if (!globalPerms.containsKey(str)) {
                Permission permission = new Permission(str, "", false, "");
                globalPerms.put(str, permission);
            }
        }
        removing = removing.trim();
        toRemove = removing.split(" ");
        for (String str : toRemove) {
            activePerms.remove(str);
        }

        lstActive.setItems(activePerms);
        lstAvailable.setItems(availablePerms);
        savePermissions();
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
        loadGroup((String) lstGroups.getSelectionModel().getSelectedItem());
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
