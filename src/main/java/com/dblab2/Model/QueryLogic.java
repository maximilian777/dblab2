package com.dblab2.Model;

import com.mongodb.client.MongoDatabase;

import java.util.List;

public class QueryLogic implements QL_Interface{
    private final MongoDatabase db;

    public QueryLogic(MongoDatabase db) {
        this.db = db;
    }
    @Override
    public List<Book> searchBookByTitle(String title) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Book> searchBookByISBN(String ISBN) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Book> searchBookByRating(int rating) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Book> searchBookByGenre(String genre) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Author> selectAuthorsForBook(String ISBN) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<Genre> selectGenresForBook(String ISBN) throws DatabaseException {
        return List.of();
    }

    @Override
    public void insertToBooks(Book book) throws DatabaseException {

    }

    @Override
    public void insertToRatings(String ISBN, int rating) throws DatabaseException {

    }

    @Override
    public void insertToUserRatings(String ISBN, String username, int userRating) throws DatabaseException {

    }

    @Override
    public void insertToReviews(Review review) throws DatabaseException {

    }

    @Override
    public void deleteBookByISBN(String ISBN) throws DatabaseException {

    }

    @Override
    public User login(User user, String password) throws DatabaseException {
        return null;
    }
}
