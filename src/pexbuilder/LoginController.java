/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notification;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 *
 * @author Bryce
 */
public class LoginController implements Initializable {

    @FXML
    private PEXBuilderController controller;

    @FXML
    private Stage thisStage;

    @FXML
    private JFXTextField txtLUsername, txtRUsername, txtRFirstName, txtRLastName, txtREmail;

    @FXML
    private JFXPasswordField txtLPassword, txtRPassword, txtRRepeat;

    @FXML
    private CheckBox chkStay;

    @FXML
    private Label lblGuest, lblMessage, lblMessage2;

    @FXML
    private LoginController INSTANCE;

    @FXML
    private TabPane tabPane;

    @FXML
    private ProgressBar prgStatus, prgRegister;

    private String globalResponses = "null";

    public Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());

    // Add enter to registration page
    // Add advertisements to PEXBuilder
    // Make pexbuilder utilize Logged in user.
    // Add subscription checker, make sure subscriptions cant be checked via user id, only API key.
    // Post on Spigot.
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        INSTANCE = this;
    }

    public LoginController getInstance() {
        return INSTANCE;
    }

    public void setStage(Stage input) {
        thisStage = input;
        thisStage.addEventHandler(WindowEvent.WINDOW_SHOWING, (WindowEvent window) -> {
            if (userPrefs.get("stay", "0").equalsIgnoreCase("1")) {
                verifyToken();
                chkStay.setSelected(true);
            }
        });
        txtLUsername.setText(userPrefs.get("username", ""));

    }

    public Stage getStage() {
        return thisStage;
    }

    public void lblGuestAction(Event e) {
        userPrefs.put("guest", "1");
        userPrefs.put("token", "");
        enter();
    }

    public void enter() {
        URL location = getClass().getResource("PEXBuilder.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = null;
        try {
            root = (Parent) fxmlLoader.load(location.openStream());
        } catch (IOException ex) {
            System.out.println("Null: "+ex.getMessage());
        }
        controller = fxmlLoader.getController();

        Scene scene = new Scene(root);
        thisStage.setScene(scene);
        thisStage.setResizable(false);
        thisStage.setTitle("PEXBuilder 1.0");

        scene.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        // Dropping over surface
        scene.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                for (File file : db.getFiles()) {
                    filePath = file.getAbsolutePath();
                    controller.pluginDrop(db);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        controller.setStage(thisStage);
        controller.showStage();
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

    public void btnLoginAction(ActionEvent e) {
        login();
    }

    public void login() {
        userPrefs.put("guest", "0");
        prgStatus.setVisible(true);
        String username = txtLUsername.getText();
        String password = txtLPassword.getText();
        Thread t1 = new Thread(() -> {
            globalResponses = Requests.login(username, password);
            if (!globalResponses.equalsIgnoreCase("null")) {
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(globalResponses);
                    String status = (String) obj.get("status");
                    if (status.equalsIgnoreCase("ok")) {
                        JSONObject data = (JSONObject) obj.get("data");
                        String token = (String) data.get("token");
                        if (chkStay.isSelected()) {
                            userPrefs.put("token", token);
                            userPrefs.put("username", username);
                            userPrefs.put("stay", "1");
                        } else {
                            userPrefs.put("token", token);
                            userPrefs.put("username", username);
                            userPrefs.put("stay", "0");
                        }
                        Platform.runLater(() -> {
                            notifySuccess(username + " has logged in!");
                            prgStatus.setVisible(false);
                            enter();
                        });
                    } else if (status.equalsIgnoreCase("failure")) {
                        Platform.runLater(() -> {
                            String message = (String) obj.get("message");
                            lblMessage.setVisible(true);
                            prgStatus.setVisible(false);
                            lblMessage.setText("Error logging in: " + message);
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblMessage.setVisible(true);
                            prgStatus.setVisible(false);
                            lblMessage.setText("Unknown error occured!");
                        });
                    }
                } catch (ParseException ex) {
                    System.out.println("Error logging in: " + ex.getMessage());
                    prgStatus.setVisible(false);
                }
            } else {
                lblMessage.setVisible(true);
                prgStatus.setVisible(false);
                lblMessage.setText("Unknown error occured!");
            }
        });
        t1.start();
    }

    public void verifyToken() {
        Thread t1 = new Thread(() -> {
            String token = userPrefs.get("token", "");
            if (!token.equalsIgnoreCase("")) {
                try {
                    String response = Requests.verify(token);
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(response);
                    String status = (String) obj.get("status");
                    if (status.equalsIgnoreCase("ok")) {
                        Platform.runLater(() -> {
                            enter();
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblMessage.setVisible(true);
                            lblMessage.setText("Session Expired!");
                            txtLUsername.setText(userPrefs.get("username", ""));
                        });
                    }
                } catch (ParseException e) {
                    System.out.println("Error verifying token: " + e.getMessage());
                }
            }
        });
        t1.start();
    }

    public void btnRegisterAction(ActionEvent e) {
        tabPane.getSelectionModel().select(1);
    }

    public void btnSubmitAction(ActionEvent e) {
        register();
    }

    public void register() {

        String username = txtRUsername.getText();
        String password = txtRPassword.getText();
        String repeat = txtRRepeat.getText();
        String email = txtREmail.getText();
        String first = txtRFirstName.getText();
        String last = txtRLastName.getText();

        if (password.equalsIgnoreCase(repeat)) {
            if (email.contains("@") && email.contains(".")) {
                if (!first.contains(" ") && !last.contains(" ")) {
                    userPrefs.put("guest", "0");
                    prgRegister.setVisible(true);
                    Thread t1 = new Thread(() -> {
                        globalResponses = Requests.register(username, password, first, last, email);
                        if (!globalResponses.equalsIgnoreCase("null")) {
                            try {
                                JSONParser parser = new JSONParser();
                                JSONObject obj = (JSONObject) parser.parse(globalResponses);
                                String status = (String) obj.get("status");
                                if (status.equalsIgnoreCase("ok")) {
                                    JSONObject data = (JSONObject) obj.get("data");
                                    String token = (String) data.get("token");
                                    userPrefs.put("token", token);
                                    userPrefs.put("username", username);
                                    Platform.runLater(() -> {
                                        notifySuccess(username + " has logged in!");
                                        prgRegister.setVisible(false);
                                        enter();
                                    });
                                } else if (status.equalsIgnoreCase("failure")) {
                                    Platform.runLater(() -> {
                                        String message = (String) obj.get("message");
                                        lblMessage2.setVisible(true);
                                        lblMessage2.setText("Error Registering: " + message);
                                        prgRegister.setVisible(false);
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        lblMessage2.setVisible(true);
                                        lblMessage2.setText("Unknown error occured!");
                                        prgRegister.setVisible(false);
                                    });
                                }
                            } catch (ParseException ex) {
                                System.out.println("Error logging in: " + ex.getMessage());
                                prgRegister.setVisible(false);
                            }
                        } else {

                        }
                    });
                    t1.start();
                } else {
                    lblMessage2.setVisible(true);
                    lblMessage2.setText("Error: Enter first or last name!");
                }
            } else {
                lblMessage2.setVisible(true);
                lblMessage2.setText("Error: Enter valid email!");
            }
        } else {
            lblMessage2.setVisible(true);
            lblMessage2.setText("Error: Passwords did not match!");
        }
    }

    public void btnCloseAction(ActionEvent e) {
        System.exit(1);
    }

    public void showStage() {
        thisStage.show();
    }

    public void notifySuccess(String input) {
        TrayNotification tray = new TrayNotification();
        Notification notification = Notifications.SUCCESS;
        tray.setTitle("PEXBuilder");
        tray.setMessage(input);
        tray.setRectangleFill(Paint.valueOf("#2A9A84"));
        tray.setAnimation(Animations.POPUP);
        tray.setNotification(notification);
        tray.showAndDismiss(Duration.seconds(1.5));
    }

    public void notifyFailure(String input) {
        TrayNotification tray = new TrayNotification();
        Notification notification = Notifications.ERROR;
        tray.setTitle("PEXBuilder");
        tray.setMessage(input);
        tray.setRectangleFill(Paint.valueOf("#2A9A84"));
        tray.setAnimation(Animations.POPUP);
        tray.setNotification(notification);
        tray.showAndDismiss(Duration.seconds(1.5));
    }

}
