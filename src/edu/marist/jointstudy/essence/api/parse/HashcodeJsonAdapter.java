package edu.marist.jointstudy.essence.api.parse;

import com.google.gson.*;
import edu.marist.jointstudy.essence.core.hash.Hashcode;

import java.lang.reflect.Type;

public class HashcodeJsonAdapter implements JsonDeserializer<Hashcode>, JsonSerializer<Hashcode> {

    @Override
    public JsonElement serialize(Hashcode hashcode, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(hashcode.toString());
    }

    @Override
    public Hashcode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Hashcode.SHA256.fromHex(jsonElement.getAsString());
    }
}
