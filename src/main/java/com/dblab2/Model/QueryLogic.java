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
    public List<Book> searchBookByTitle(String title) throws SearchException {
        List<Book> resultBooks = new ArrayList<>();
        try {
            MongoCollection<Document> collection = db.getCollection("books");
            Bson filter = Filters.regex("title", ".*" + title + ".*", "i");
            try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    resultBooks.add(mapDocumentToBook(cursor.next()));
                }
            }
        } catch (Exception e) {
            throw new SearchException("Database error during title search", "Title", title, e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByISBN(String ISBN) throws SearchException {
        List<Book> resultBooks = new ArrayList<>();
        try {
            MongoCollection<Document> books = db.getCollection("books");
            try (MongoCursor<Document> cursor = books.find(Filters.eq("_id", ISBN)).iterator()) {
                while (cursor.hasNext()) {
                    resultBooks.add(mapDocumentToBook(cursor.next()));
                }
            }
        } catch (Exception e) {
            throw new SearchException("Database error during ISBN search", "ISBN", ISBN, e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByAuthor(String firstName, String lastName) throws SearchException {
        List<Book> resultBooks = new ArrayList<>();
        String fullName = firstName + " " + lastName;
        try {
            MongoCollection<Document> books = db.getCollection("books");
            Bson filter = Filters.and(
                    Filters.regex("authors.firstName", ".*" + firstName + ".*", "i"),
                    Filters.regex("authors.lastName", ".*" + lastName + ".*", "i")
            );
            try (MongoCursor<Document> cursor = books.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    resultBooks.add(mapDocumentToBook(cursor.next()));
                }
            }
        } catch (Exception e) {
            throw new SearchException("Database error during author search", "Author", fullName, e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByRating(int rating) throws SearchException {
        List<Book> resultBooks = new ArrayList<>();
        try {
            MongoCollection<Document> books = db.getCollection("books");
            try (MongoCursor<Document> cursor = books.find(Filters.gte("averageRating", rating)).iterator()) {
                while (cursor.hasNext()) {
                    resultBooks.add(mapDocumentToBook(cursor.next()));
                }
            }
        } catch (Exception e) {
            throw new SearchException("Database error during rating search", "Rating", String.valueOf(rating), e);
        }
        return resultBooks;
    }

    @Override
    public List<Book> searchBookByGenre(String genre) throws SearchException {
        List<Book> resultBooks = new ArrayList<>();
        try {
            MongoCollection<Document> books = db.getCollection("books");
            try (MongoCursor<Document> cursor = books.find(Filters.eq("genres.name", genre)).iterator()) {
                while (cursor.hasNext()) {
                    resultBooks.add(mapDocumentToBook(cursor.next()));
                }
            }
        } catch (Exception e) {
            throw new SearchException("Database error during genre search", "Genre", genre, e);
        }
        return resultBooks;
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
    public void insertToBooks(Book book) throws InsertException {
        MongoCollection<Document> books = db.getCollection("books");
        MongoCollection<Document> authors = db.getCollection("authors");
        MongoCollection<Document> genres = db.getCollection("genres");

        try {
            List<Document> authorList = new ArrayList<>();
            for (Author a : book.getAuthors()) {
                Document author = authors.find(Filters.and(
                        Filters.eq("firstName", a.getFirstName()),
                        Filters.eq("lastName", a.getLastName())
                )).first();

                if (author == null) {
                    throw new InsertException("Author not found", "Book", book.getISBN(), a.getFirstName() + " " + a.getLastName());
                }
                authorList.add(author);
            }

            List<Document> genreList = new ArrayList<>();
            for (Genre g : book.getGenres()) {
                Document foundGenre = genres.find(Filters.eq("name", g.getGenre())).first();

                if (foundGenre == null) {
                    throw new InsertException("Genre not found", "Book", book.getISBN(), g.getGenre());
                }
                genreList.add(foundGenre);
            }

            Document bookDoc = new Document("_id", book.getISBN())
                    .append("title", book.getTitle())
                    .append("pages", book.getPages())
                    .append("authors", authorList)
                    .append("genres", genreList);

            books.insertOne(bookDoc);

        } catch (InsertException ie) {
            throw ie;
        } catch (Exception e) {
            throw new InsertException("Database error during book insertion", "Book", book.getISBN(), e);
        }
    }

    @Override
    public void insertToRatings(String ISBN, int rating) throws InsertException {
        MongoCollection<Document> ratings = db.getCollection("books");
        try {
            ratings.updateOne(Filters.eq("_id", ISBN),
                    Updates.push("anonymousRatings", rating));
        } catch (Exception e) {
            throw new InsertException("Error adding rating", "Rating", ISBN, e);
        }
    }

    @Override
    public void insertToUserRatings(String ISBN, String username, int userRating) throws InsertException {
        MongoCollection<Document> userRatings = db.getCollection("books");
        try {
            Document userRatingDoc = new Document("username", username).append("rating", userRating);
            userRatings.updateOne(Filters.eq("_id", ISBN),
                    Updates.push("userRatings", userRatingDoc));
        } catch (Exception e) {
            throw new InsertException("Error adding user rating", "UserRating", ISBN, e);
        }
    }

    @Override
    public void insertToReviews(Review review) throws InsertException {
        MongoCollection<Document> reviews = db.getCollection("books");
        String isbn = review.getBook().getISBN();
        try {
            Document reviewDoc = new Document("username", review.getUser().getUsername())
                    .append("text", review.getReviewText());
            reviews.updateOne(Filters.eq("_id", isbn),
                    Updates.push("reviews", reviewDoc));
        } catch (Exception e) {
            throw new InsertException("Error adding review", "Review", isbn, e);
        }
    }

    @Override
    public void deleteBookByISBN(String ISBN) throws DeleteException {
        MongoCollection<Document> books = db.getCollection("books");
        try {
            long count = books.deleteOne(Filters.eq("_id", ISBN)).getDeletedCount();
            if (count == 0) {
                throw new DeleteException("No book found to delete", ISBN);
            }
        } catch (DeleteException de) {
            throw de;
        } catch (Exception e) {
            throw new DeleteException("Error deleting book", ISBN, e);
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
