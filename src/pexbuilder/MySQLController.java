/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Bryce
 */
public class MySQLController implements Initializable {

    @FXML
    private TextField txtUsername, txtPassword, txtHost, txtPort, txtDatabase;

    @FXML
    public ProgressIndicator prgStatus;
    
    private static MySQLController INSTANCE;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        INSTANCE = this;
    }
    
    public static MySQLController getInstance(){
        return INSTANCE;
    }

    public void btnConnectAction(ActionEvent e) {
        prgStatus.setVisible(true);
        Thread t1 = new Thread(() -> {
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            String database = txtDatabase.getText();
            Integer port = Integer.parseInt(txtPort.getText());
            String host = txtHost.getText();
            PEXSQL.getInstance().connect(host, username, database, password, port);
            PEXBuilderController.getInstance().backend = Backend.MYSQL;
        });
        t1.start();
    }

}
