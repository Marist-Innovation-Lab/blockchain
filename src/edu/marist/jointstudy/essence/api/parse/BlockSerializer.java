package edu.marist.jointstudy.essence.api.parse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.marist.jointstudy.essence.core.mine.Nonce;
import edu.marist.jointstudy.essence.core.structures.Block;
import edu.marist.jointstudy.essence.core.structures.Transaction;

import java.lang.reflect.Type;
import java.util.List;

public class BlockSerializer implements JsonDeserializer<Block> {
    @Override
    public Block deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Type txListToken = new TypeToken<List<Transaction>>(){}.getType();
        List<Transaction> txs = jsonDeserializationContext.deserialize(
                jsonElement
                        .getAsJsonObject()
                        .get("transactions")
                        .getAsJsonArray(), txListToken
        );

        Nonce nonce = jsonDeserializationContext.deserialize(
                jsonElement.getAsJsonObject().get("nonce"),
                Nonce.class
        );

        if(jsonElement.getAsJsonObject().get("previousBlock").isJsonNull()) {
            return Block.newUnsafeBlock(txs, null, nonce);
        }
        return Block.newUnsafeBlock(
                txs,
                jsonDeserializationContext.deserialize(
                        jsonElement.getAsJsonObject().get("previousBlock").getAsJsonObject(),
                        Block.class),
                nonce
        );
    }
}
