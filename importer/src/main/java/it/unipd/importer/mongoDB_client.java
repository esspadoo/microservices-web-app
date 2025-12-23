package it.unipd.importer;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

/**
 * Provides a simple, synchronous MongoDB client wrapper for connecting to a
 * local MongoDB instance, retrieving a default database and its collections,
 * and performing basic CRUD operations.
 * <p>
 * This class encapsulates a single {@link MongoClient} instance created via the
 * official MongoDB Java synchronous driver. By default, it connects to
 * {@code mongodb://localhost:27017} and operates on a database named
 * {@code "mongoDB"}.
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>Create an instance of {@code mongoDB_client} to establish the connection.</li>
 *   <li>Use {@link #getDatabase()} or {@link #getCollection(String)} to access data.</li>
 *   <li>Invoke {@link #close_connection()} when the client is no longer needed.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>
 * {@link MongoClient} instances are thread-safe. This wrapper can therefore be
 * safely shared across multiple threads, provided that
 * {@link #close_connection()} is called only once during application shutdown.
 * </p>
 *
 * <h2>Example usage</h2>
 * <pre>{@code
 * mongoDB_client client = new mongoDB_client();
 * MongoDatabase db = client.getDatabase();
 * MongoCollection<Document> collection = client.getCollection("myCollection");
 * client.close_connection();
 * }</pre>
 */
public class mongoDB_client {

    /**
     * Name of the default database used by this client.
     */
    private static final String DEFAULT_DATABASE_NAME = "documents";

    /**
     * The underlying MongoDB client instance used to connect to the server.
     */
    private final MongoClient mongoClient;

    /**
     * Constructs a new {@code mongoDB_client} and establishes a connection to
     * the MongoDB server at {@code mongodb://localhost:27017} using default
     * client settings.
     * <p>
     * If the MongoDB server is not reachable, the connection attempt may fail
     * lazily when the first operation is executed.
     * </p>
     */
    public mongoDB_client() {
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://mongodb:27017"))
                        .build()
        );
    }

    /**
     * Retrieves the default MongoDB database used by this client.
     *
     * @return a {@link MongoDatabase} instance representing the database
     *         named {@value #DEFAULT_DATABASE_NAME}
     */
    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DEFAULT_DATABASE_NAME);
    }

    /**
     * Retrieves a specific collection from the default database.
     *
     * @param collection the name of the collection to retrieve; must not be
     *                   {@code null} or blank
     * @return a {@link MongoCollection} of {@link Document} representing the
     *         requested collection
     * @throws IllegalArgumentException if {@code collection} is {@code null} or blank
     */
    public MongoCollection<Document> getCollection(String collection) {
        if (collection == null || collection.isBlank()) {
            throw new IllegalArgumentException("Collection name is null or empty");
        }
        return getDatabase().getCollection(collection);
    }

    /**
     * Inserts or updates a single document in the specified collection using an
     * <em>upsert</em> operation.
     * <p>
     * If at least one document matches the provided {@code filter}, the first
     * matching document is updated according to {@code update}. If no documents
     * match the filter, a new document is inserted.
     * </p>
     * <p>
     * This method is a thin wrapper around
     * {@link MongoCollection#updateOne(Bson, Bson, UpdateOptions)} with
     * {@code upsert} set to {@code true}.
     * </p>
     *
     * @param collection the name of the collection to operate on; must not be
     *                   {@code null} or blank
     * @param filter     the query filter used to select the document to update;
     *                   must not be {@code null}
     * @param update     the update definition describing the modifications to
     *                   apply; must not be {@code null}
     * @return an {@link UpdateResult} containing information about the operation,
     *         such as matched count, modified count, and upserted ID
     * @throws IllegalArgumentException if {@code collection} is {@code null} or blank,
     *                                  or if {@code filter} or {@code update} is {@code null}
     *
     * @see <a href="https://www.mongodb.com/docs/drivers/java/sync/current/crud/update-documents/">
     *      MongoDB Java Driver – Update Documents</a>
     */
    public UpdateResult insert_update_doc(String collection, Bson filter, Bson update) {
        if (collection == null || collection.isBlank())
            throw new IllegalArgumentException("Collection name is null or empty");

        if (filter == null)
            throw new IllegalArgumentException("Filter cannot be null");

        if (update == null)
            throw new IllegalArgumentException("Update cannot be null");

        // Instructs the driver to insert a new document if none match the query
        UpdateOptions options = new UpdateOptions().upsert(true);

        return getCollection(collection).updateOne(filter, update, options);
    }

    /**
     * Closes the underlying {@link MongoClient} connection and releases all
     * associated resources.
     * <p>
     * This method should be called during application shutdown. After invoking
     * this method, any further use of this {@code mongoDB_client} instance may
     * result in undefined behavior.
     * </p>
     */
    public void close_connection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}

