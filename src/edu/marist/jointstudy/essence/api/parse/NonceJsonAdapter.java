package edu.marist.jointstudy.essence.api.parse;

import com.google.gson.*;
import edu.marist.jointstudy.essence.core.mine.Nonce;

import java.lang.reflect.Type;

public class NonceJsonAdapter implements JsonSerializer<Nonce>, JsonDeserializer<Nonce> {
    @Override
    public JsonElement serialize(Nonce nonce, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(nonce.toString());
    }

    @Override
    public Nonce deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Nonce.newNonce(jsonElement.getAsLong());
    }
}
