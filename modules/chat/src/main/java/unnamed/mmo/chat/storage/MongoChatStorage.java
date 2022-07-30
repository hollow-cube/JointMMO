package unnamed.mmo.chat.storage;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import unnamed.mmo.chat.ChatMessage;
import unnamed.mmo.chat.ChatQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.descending;

class MongoChatStorage implements ChatStorage {
    //todo need to change this. Would prefer to use an async driver but the reactive streams one seems horrible.
    //todo perhaps worth writing a small wrapper around MongoClient to use CompletableFuture
    private static final Executor THREAD_POOL = Executors.newSingleThreadExecutor();

    // todo eventually db name needs to be a configurable value (at minimum by env var, perhaps also file)
    private static final String DB_NAME = "mmo";
    private static final String CHAT_COLLECTION = "chat";

    //todo should be config value
    private static final int CHAT_QUERY_MAX_RESULT_WINDOW = 15;

    public static final MongoClientSettings BASE_CLIENT_SETTINGS = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build();
    public static final CodecRegistry DEFAULT_CODEC_REGISTRY = CodecRegistries.withUuidRepresentation(
            BASE_CLIENT_SETTINGS.getCodecRegistry(), BASE_CLIENT_SETTINGS.getUuidRepresentation());

    private final MongoClient client;

    public MongoChatStorage(@NotNull MongoClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<Void> recordChatMessage(@NotNull ChatMessage message) {
        return CompletableFuture.runAsync(() -> collection().insertOne(message), THREAD_POOL);
    }

    @Override
    public CompletableFuture<List<ChatMessage>> queryChatMessages(@NotNull ChatQuery query) {
        return CompletableFuture.supplyAsync(() -> {
            List<ChatMessage> results = new ArrayList<>();
            collection().find(chatQueryToBson(query))
                    .projection(excludeId())
                    .sort(descending("timestamp"))
                    .limit(CHAT_QUERY_MAX_RESULT_WINDOW)
                    .into(results);
            return results;
        }, THREAD_POOL);
    }

    /**
     * Convert a chat query to a bson query for mongodb.
     * todo don't love the use of TestOnly here
     *
     * @param query a chat query to convert to bson
     * @return A BSON query of the given {@link ChatQuery}.
     */
    @TestOnly
    public @NotNull BsonDocument chatQueryToBson(@NotNull ChatQuery query) {
        List<Bson> conditions = new ArrayList<>();

        // Currently the generated query uses the form
        // { $and: [ { <field>: { $in: <values> } } ], ... }
        // but an optimization would be to use the form
        // { $and: [ { <field>: <value> }, ... ]
        // if there is only a single query value.

        if (!query.serverIds().isEmpty())
            conditions.add(in("serverId", query.serverIds()));

        if (!query.channelIds().isEmpty())
            conditions.add(in("channelId", query.channelIds()));

        if (!query.senders().isEmpty())
            conditions.add(in("sender", query.senders()));

        if (query.message() != null)
            conditions.add(eq("message", query.message()));

        // Return all conditions $and-ed together, or an empty query if there are no conditions
        // Must check this because mongo does not allow an empty $and
        if (conditions.isEmpty()) return new BsonDocument();
        return and(conditions).toBsonDocument(Document.class, DEFAULT_CODEC_REGISTRY);
    }

    private MongoCollection<ChatMessage> collection() {
        return client.getDatabase(DB_NAME).getCollection(CHAT_COLLECTION, ChatMessage.class);
    }
}
