package edu.marist.jointstudy.essence.api.store;

import com.google.gson.Gson;
import edu.marist.jointstudy.essence.api.parse.GsonSingleton;
import edu.marist.jointstudy.essence.core.structures.Blockchain;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * <p>The default store for this API. The client and server portions of the peer save their blockchains to this one object.
 *
 * <p>Detects from the preferences if the node is a sybil and sets the appropriate "./sybil" folder to serve the
 * blockchain there.</p>
 */
public enum PersistentStoreSingleton {
    INSTANCE();

    private Gson gson = GsonSingleton.INSTANCE.get();
    private Store<Blockchain> instance;

    public Store<Blockchain> get() {
        if(Objects.isNull(this.instance)) {
            // how to serialize and deserialize a blockchain
            PersistentStore.Transfer<Blockchain> transfer =
                    new PersistentStore.Transfer<>(gson::toJson, (str) -> gson.fromJson(str, Blockchain.class));

            try {
                boolean isSybil = Preferences.isSybil();
                int port = Preferences.getPort();
                instance = new PersistentStore<>(
                        transfer,
                        Paths.get(isSybil ? "./sybil" : "./blockchain" + port),
                        "bc.json"
                );
            } catch (IOException e) {
                System.err.println("Unable to create a new store.");
                System.err.println(e.getMessage());
                System.exit(-1); // can't do much without persistence
            }
        }
        return instance;
    }
}
