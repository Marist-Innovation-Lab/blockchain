package essence.test.api;

import com.google.gson.Gson;
import edu.marist.jointstudy.essence.api.store.PersistentStore;
import edu.marist.jointstudy.essence.api.store.Store;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import essence.test.util.Generate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class StoreTests {

    Store<Blockchain> bcStore;

    @BeforeEach
    void setupBcStore() {
        Gson gson = Generate.defaultGson();
        PersistentStore.Transfer<Blockchain> transfer = new PersistentStore.Transfer<>(gson, Blockchain.class);
        try {
            bcStore = new PersistentStore<>(transfer, Paths.get("./blockchain"), "bc.json");
        } catch (IOException e) {
            fail(e);
        }
    }

    @AfterEach
    void deleteBcStore() {
        if(!bcStore.delete()) {
            fail("Could not delete the bcStore");
        }
    }

    @Test
    void testsForExistingFolder() {
        Gson gson = Generate.defaultGson();
        PersistentStore.Transfer<Blockchain> transfer = new PersistentStore.Transfer<>(gson, Blockchain.class);

        String testDirectory = "./directory";
        File f = Paths.get(testDirectory).toFile();
        if(!f.mkdir()) fail(testDirectory + ".mkdir()=false");
        try {
            Store<Blockchain> s = new PersistentStore<>(transfer, Paths.get(testDirectory), "blockchain.json");
            if(!s.delete()) {
                fail("could not delete store.");
            }
        } catch(IOException e){
            fail(e);
        }

    }

    @Test
    void emptyThrows() {
        assertThrows(NoSuchElementException.class, () -> {
            try{
                bcStore.get().get(); // no blockchain actually in there
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail(ioe);
            }
        });
    }

    @Test
    void doesNotExist() {
        assertTrue(!bcStore.exists());
    }

    @Test
    void addedBcExists() {
        try {
            bcStore.save(Generate.twoBlockBlockchain());
            assertTrue(bcStore.exists());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void addedBcCanBeRetrieved() {
        try {
            bcStore.save(Generate.twoBlockBlockchain());
            Blockchain bc = bcStore.get().get();
        } catch (Exception e) {
            fail(e);
        }
    }
}
