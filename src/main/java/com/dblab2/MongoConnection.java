package com.dblab2;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    public static void main(String[] args) {
        String uri = "mongodb://localhost:27017";
        String databaseName = "";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
        }
        }
}
