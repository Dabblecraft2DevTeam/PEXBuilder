/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pexbuilder;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Bryce
 */
public class PEXBuilder extends Application {
    

    LoginController controller;
    
    private static PEXBuilder INSTANCE;
    
    @Override
    public void start(Stage stage) throws Exception {
        INSTANCE = this;
        URL location = getClass().getResource("Login.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = (Parent) fxmlLoader.load(location.openStream());
        controller = fxmlLoader.getController();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("PEXBuilder 1.6");
        stage.initStyle(StageStyle.UNDECORATED);
        
        
        controller.setStage(stage);
        controller.showStage();
        
    }
    
    public void openUpgrade(){
        getHostServices().showDocument("https://shulkerbox.org/market/scripts/view/79");
    }
    
    public void openURL(String input){
        getHostServices().showDocument(input);
    }
    
    public static PEXBuilder getInstance(){
        return INSTANCE;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
