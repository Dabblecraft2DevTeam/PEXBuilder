/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Bryce
 */
public class PluginInfoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Stage thisStage;

    @FXML
    public Label lblName, lblVersion, lblAuthor, lblDesc, lblMain;

    @FXML
    public ListView lstDepends, lstPerms;

    protected ObservableList<String> depends = FXCollections.observableArrayList();

    protected ObservableList<String> permissions = FXCollections.observableArrayList();

    public void btnCloseAction(ActionEvent e) {
        thisStage.close();
    }

    public void setStage(Stage input) {
        thisStage = input;
    }

    public Double initialX, initialY;

    public void mouseDragAction(MouseEvent e) {
        thisStage.getScene().getWindow().setX(e.getScreenX() - initialX);
        thisStage.getScene().getWindow().setY(e.getScreenY() - initialY);
    }

    public void dragAction(MouseEvent event) {
        initialX = event.getSceneX();
        initialY = event.getSceneY();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void loadPlugin(String input) {
        Plugin plugin = PEXBuilderController.getInstance().mappedPlugins.get(input);
        lblName.setText(plugin.getName());
        lblAuthor.setText(plugin.getAuthor());
        lblMain.setText(plugin.getMain());
        lblDesc.setText(plugin.getDescription());
        lblVersion.setText(plugin.getVersion());
        
        if (plugin.getDepend() != null) depends.addAll(Arrays.asList(plugin.getDepend()));

        plugin.getPermissions().entrySet().forEach((entry) -> {
            System.out.println("Added permission: "+entry.getKey());
            permissions.add(entry.getKey());
        });
        lstDepends.setItems(depends);
        lstPerms.setItems(permissions);
    }

}
