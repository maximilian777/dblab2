package com.dblab2.Controller;

import javafx.stage.Stage;
import com.dblab2.Model.*;
import com.dblab2.View.UserView;

public class Controller {

    private final BookController bookController;
    private final AuthorController authorController;
    private final ReviewController reviewController;
    private final UserController userController;
    private final UserView userView;

    public Controller(Stage primaryStage, QL_Interface queryLogic) {
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
}