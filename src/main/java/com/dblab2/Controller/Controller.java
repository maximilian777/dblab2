package com.dblab2.Controller;

import javafx.stage.Stage;
import com.dblab2.Model.*;
import com.dblab2.View.UserView;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;

public class Controller {

    private final MongoDatabase db;
    private final MongoClient mongoClient;
    private final QL_Interface queryLogic;
    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;
    private final UserView userView;

    public Controller(MongoDatabase db, MongoClient mongoClient, Stage primaryStage) {
        this.db = db;
        this.mongoClient = mongoClient;

        // Initialize your MongoDB-specific query logic
        this.queryLogic = new QueryLogic(db);

        this.bookController = new BookController(queryLogic);
        this.authorController = new AuthorController(queryLogic);
        this.reviewController = new ReviewController(queryLogic);
        this.userController = new UserController(queryLogic);

        this.userView = new UserView(
                bookController,
                authorController,
                reviewController,
                userController
        );

        startUI(primaryStage);
    }

    public void startUI(Stage stage) {
        userView.showUserProfile(stage);
    }

    public void shutdown() {
        System.out.println("Shutting down MongoDB connection...");
        try {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("MongoClient successfully closed.");
            }
        } catch (Exception e) {
            System.err.println("Error during MongoDB shutdown: " + e.getMessage());
        }
    }
}