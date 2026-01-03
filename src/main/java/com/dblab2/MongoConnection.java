package com.dblab2;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoConnection {
    private final String uri;
    private final String dbName;
    private MongoClient mongoClient;

    public MongoConnection(String username, String password, String database) {
        this.uri = "mongodb://" + username + ":" + password + "@localhost:27017/?authSource=admin";
        this.dbName = database;
    }
    public MongoDatabase connect() {
        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(uri))
                        .build());

        return mongoClient.getDatabase(dbName);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}