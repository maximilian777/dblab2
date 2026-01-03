package com.dblab2;

import javafx.application.Platform;
import com.dblab2.Controller.*;
import javafx.application.Application;
import javafx.stage.Stage;
import com.mongodb.client.MongoDatabase;

public class Main extends Application {

    private MongoConnection mongoConnection;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            mongoConnection = new MongoConnection("dbAdmin", "123password", "dblab2");
            MongoDatabase db = mongoConnection.connect();

            controller = new Controller(db, mongoConnection.getMongoClient(), primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection to database denied! Please check if login-input is correct.");
            if (mongoConnection != null) {
                mongoConnection.close();
            }

            System.out.println("Exiting after exception thrown!");
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}