package edu.marist.jointstudy.essence.api.parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.mine.Nonce;
import edu.marist.jointstudy.essence.core.structures.Block;

import java.util.Objects;

/**
 * Lazily instantiated gson singleton, shared throughout the application.
 */
public enum GsonSingleton {
    INSTANCE();

    private Gson gson;

    public Gson get() {
        if(Objects.isNull(gson)) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .registerTypeAdapter(Hashcode.class, new HashcodeJsonAdapter())
                    .registerTypeAdapter(Nonce.class, new NonceJsonAdapter())
                    .registerTypeAdapter(Block.class, new BlockSerializer())
                    .create();
        }
        return gson;
    }
}
