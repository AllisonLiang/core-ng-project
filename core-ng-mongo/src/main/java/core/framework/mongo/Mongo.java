package core.framework.mongo;

import com.mongodb.client.model.IndexOptions;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public interface Mongo {
    default void createIndex(String collection, Bson keys) {
        createIndex(collection, keys, new IndexOptions());
    }

    // refer to com.mongodb.client.model.Indexes for building keys
    void createIndex(String collection, Bson keys, IndexOptions options);

    void dropCollection(String collection);

    void runCommand(Bson command);
}
