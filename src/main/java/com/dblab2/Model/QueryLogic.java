package com.dblab2.Model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class QueryLogic implements QL_Interface{
    private final MongoDatabase db;

    public QueryLogic(MongoDatabase db) {
        this.db = db;
    }
    @Override
    public List<Book> searchBookByTitle(String title) throws DatabaseException {
        List<Book> resultBooks = new ArrayList<>();
        MongoCollection<Document> collection = db.getCollection("books");
        Bson filter = Filters.regex("title", ".*" + title + ".*", "i");

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                resultBooks.add(mapDocumentToBook(cursor.next()));
            }
        } catch (Exception e) {
            throw new DatabaseException("Error searching by title", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByISBN(String ISBN) throws DatabaseException {
        List<Book> resultBooks = new ArrayList<>();
        MongoCollection<Document> books = db.getCollection("books");
        try (MongoCursor<Document> cursor = books.find(Filters.eq("_id", ISBN)).iterator()) {
            while (cursor.hasNext()) {
                resultBooks.add(mapDocumentToBook(cursor.next()));
            }
        } catch (Exception e) {
            throw new DatabaseException("Error searching by ISBN", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws DatabaseException {
        List<Book> resultBooks = new ArrayList<>();
        MongoCollection<Document> books = db.getCollection("books");
        Bson filter = Filters.and(
                Filters.regex("authors.firstName", ".*" + firstName + ".*", "i"),
                Filters.regex("authors.lastName", ".*" + lastName + ".*", "i")
        );

        try (MongoCursor<Document> cursor = books.find(filter).iterator()) {
            while (cursor.hasNext()) {
                resultBooks.add(mapDocumentToBook(cursor.next()));
            }
        } catch (Exception e) {
            throw new DatabaseException("Error searching by author", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByRating(int rating) throws DatabaseException {
        List<Book> resultBooks = new ArrayList<>();
        MongoCollection<Document> books = db.getCollection("books");
        try (MongoCursor<Document> cursor = books.find(Filters.gte("averageRating", rating)).iterator()) {
            while (cursor.hasNext()) {
                resultBooks.add(mapDocumentToBook(cursor.next()));
            }
        } catch (Exception e) {
            throw new DatabaseException("Error searching by rating", e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByGenre(String genre) throws DatabaseException {
        List<Book> resultBooks = new ArrayList<>();
        MongoCollection<Document> books = db.getCollection("books");
        try (MongoCursor<Document> cursor = books.find(Filters.eq("genres.name", genre)).iterator()) {
            while (cursor.hasNext()) {
                resultBooks.add(mapDocumentToBook(cursor.next()));
            }
        } catch (Exception e) {
            throw new DatabaseException("Error searching by genre", e);
        }
        return resultBooks;
    }

    @Override
    public List<Author> selectAuthorsForBook(String ISBN) throws DatabaseException {
        MongoCollection<Document> books = db.getCollection("books");
        Document doc = books.find(Filters.eq("_id", ISBN)).first();
        List<Author> authors = new ArrayList<>();
        if (doc != null) {
            List<Document> authorDocs = doc.getList("authors", Document.class);
            if (authorDocs != null) {
                for (Document aDoc : authorDocs) {
                    Author a = new Author();
                    a.setFirstName(aDoc.getString("firstName"));
                    a.setLastName(aDoc.getString("lastName"));
                    authors.add(a);
                }
            }
        }
        return authors;
    }

    @Override
    public List<Genre> selectGenresForBook(String ISBN) throws DatabaseException {
        MongoCollection<Document> books = db.getCollection("books");
        Document doc = books.find(Filters.eq("_id", ISBN)).first();
        List<Genre> genres = new ArrayList<>();
        if (doc != null) {
            List<Document> genreDocs = doc.getList("genres", Document.class);
            if (genreDocs != null) {
                for (Document gDoc : genreDocs) {
                    genres.add(new Genre(0, gDoc.getString("name")));
                }
            }
        }
        return genres;
    }

    private Book mapDocumentToBook(Document doc) {
        Book book = new Book();
        book.setISBN(doc.getString("_id"));
        book.setTitle(doc.getString("title"));
        book.setPages(doc.getInteger("pages", 0));

        List<Document> authorDocs = doc.getList("authors", Document.class);
        List<Author> authors = new ArrayList<>();
        if (authorDocs != null) {
            for (Document aDoc : authorDocs) {
                Author a = new Author();
                a.setFirstName(aDoc.getString("firstName"));
                a.setLastName(aDoc.getString("lastName"));
                authors.add(a);
            }
        }
        book.setAuthors(authors);

        List<Document> genreDocs = doc.getList("genres", Document.class);
        List<Genre> genres = new ArrayList<>();
        if (genreDocs != null) {
            for (Document gDoc : genreDocs) {
                genres.add(new Genre(0, gDoc.getString("name")));
            }
        }
        book.setGenres(genres);
        return book;
    }

    @Override
    public void insertToBooks(Book book) throws DatabaseException {
        MongoCollection<Document> booksColl = db.getCollection("books");
        MongoCollection<Document> authorsColl = db.getCollection("authors");
        MongoCollection<Document> genresColl = db.getCollection("genres");

        try {
            List<Document> fullAuthorDocs = new ArrayList<>();
            for (Author a : book.getAuthors()) {
                Document foundAuthor = authorsColl.find(Filters.and(
                        Filters.eq("firstName", a.getFirstName()),
                        Filters.eq("lastName", a.getLastName())
                )).first();

                if (foundAuthor == null) {
                    throw new DatabaseException("Author not found: " + a.getFirstName() + " " + a.getLastName());
                }
                fullAuthorDocs.add(foundAuthor);
            }

            List<Document> fullGenreDocs = new ArrayList<>();
            for (Genre g : book.getGenres()) {
                Document foundGenre = genresColl.find(Filters.eq("name", g.getGenre())).first();

                if (foundGenre == null) {
                    throw new DatabaseException("Genre not found: " + g.getGenre());
                }
                fullGenreDocs.add(foundGenre);
            }

            Document bookDoc = new Document("_id", book.getISBN())
                    .append("title", book.getTitle())
                    .append("pages", book.getPages())
                    .append("authors", fullAuthorDocs)
                    .append("genres", fullGenreDocs);

            booksColl.insertOne(bookDoc);

        } catch (DatabaseException de) {
            throw de;
        } catch (Exception e) {
            throw new DatabaseException("Database error during book insertion", e);
        }
    }

    @Override
    public void insertToRatings(String ISBN, int rating) throws DatabaseException {
        MongoCollection<Document> ratings = db.getCollection("books");
        try {
            ratings.updateOne(Filters.eq("_id", ISBN),
                    Updates.push("anonymousRatings", rating));
        } catch (Exception e) {
            throw new DatabaseException("Error adding rating", e);
        }
    }

    @Override
    public void insertToUserRatings(String ISBN, String username, int userRating) throws DatabaseException {
        MongoCollection<Document> userRatings = db.getCollection("books");
        try {
            Document userRatingDoc = new Document("username", username).append("rating", userRating);
            userRatings.updateOne(Filters.eq("_id", ISBN),
                    Updates.push("userRatings", userRatingDoc));
        } catch (Exception e) {
            throw new DatabaseException("Error adding user rating", e);
        }
    }

    @Override
    public void insertToReviews(Review review) throws DatabaseException {
        MongoCollection<Document> reviews = db.getCollection("books");
        try {
            Document reviewDoc = new Document("username", review.getUser().getUsername())
                    .append("text", review.getReviewText());
            reviews.updateOne(Filters.eq("_id", review.getBook().getISBN()),
                    Updates.push("reviews", reviewDoc));
        } catch (Exception e) {
            throw new DatabaseException("Error adding review", e);
        }
    }

    @Override
    public void deleteBookByISBN(String ISBN) throws DatabaseException {
        MongoCollection<Document> books = db.getCollection("books");
        try {
            books.deleteOne(Filters.eq("_id", ISBN));
        } catch (Exception e) {
            throw new DatabaseException("Error deleting book", e);
        }
    }

    @Override
    public User login(User user, String password) throws DatabaseException {
        MongoCollection<Document> users = db.getCollection("users");
        Document found = users.find(Filters.and(
                Filters.eq("_id", user.getUsername()),
                Filters.eq("password", password)
        )).first();

        if (found == null) return null;

        User loggedIn = new User();
        loggedIn.setUsername(found.getString("_id"));
        return loggedIn;
    }
}
