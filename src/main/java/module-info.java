module com.dblab2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires java.desktop;
    requires org.mongodb.bson;


    opens com.dblab2 to javafx.fxml;
    exports com.dblab2;
    exports com.dblab2.Model;
    opens com.dblab2.Model to javafx.fxml;
}