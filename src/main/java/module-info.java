module com.dblab2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;


    opens com.dblab2 to javafx.fxml;
    exports com.dblab2;
}