package com.dblab2;

import com.dblab2.Model.MongoConnection;
import com.dblab2.Model.QL_Interface;
import com.dblab2.Model.QueryLogic;
import javafx.application.Platform;
import com.dblab2.Controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import com.mongodb.client.MongoDatabase;

public class Main extends Application {

    private MongoConnection mongoConnection;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            mongoConnection = new MongoConnection("bookwiz_charles", "password", "dblab2");
            MongoDatabase db = mongoConnection.connect();
            QL_Interface queryLogic = new QueryLogic(db);
            primaryStage.setOnCloseRequest(e -> this.shutdown());
            controller = new Controller(primaryStage, queryLogic);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection to database denied! Please check if login-input is correct.");
            shutdown();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void shutdown() {
        System.out.println("Shutting down.");
        if (mongoConnection != null) {
            mongoConnection.close();
            System.out.println("Connection successfully closed towards MongoDB.");
        }
    }
}