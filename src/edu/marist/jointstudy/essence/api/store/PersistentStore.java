package edu.marist.jointstudy.essence.api.store;

import com.google.gson.Gson;
import edu.marist.jointstudy.essence.api.parse.Deserialization;
import edu.marist.jointstudy.essence.api.parse.Serialization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Map-like API for disk retrieval and storage of T within one directory.
 * @param <T> The class being transferred to and from disk
 */
public class PersistentStore<T> implements Store<T> {

    /** How to serialize and deserialize T, i.e. transfer between memory and disk */
    private Transfer<T> transfer;

    /** The directory of files where T is stored, just one file within this directory */
    private Path location;

    /**
     * Map-like API for disk retrieval and storage of T.
     * @param transfer how to serialize and deserialize T to and from a String.
     * @param directory information about where to store the files, prefixes for each file, the extension of each file, etc.
     * @throws IOException
     */
    public PersistentStore(Transfer<T> transfer, Path directory, String fileName) throws IOException {
        this.transfer = transfer;

        if(!directory.toFile().exists()) {
            if(!directory.toFile().mkdir()) {
                throw new IOException("Unable to make a store directory: " + directory);
            }
        }

        this.location = directory.resolve(fileName);
        if(!this.location.toFile().exists()) {
            // create the file
            this.location.toFile();
            if(!this.location.toFile().createNewFile()) {
                throw new IOException("Unable to create blockchain file: " + this.location);
            }
        }
    }

    /**
     * Serializes T t and saves the serialization as the fileName within the directory of the PersistentStore.
     * @param t object to be serialized and saved to disk.
     */
    public void save(T t) throws IOException {
        String s = this.transfer.serialize(t);
        Files.write(this.location, s.getBytes());
    }

    /**
     * Deserializes the object in the fileName and reads it into memory.
     * @return an optional T object, Optional.empty() if the file with the fileName doesn't exist, Optional.of(T)
     * otherwise.
     */
    public Optional<T> get() throws IOException {
        Optional<String> s = Files.readAllLines(this.location) // can throw
                .stream()
                .reduce(String::concat);

        if(!s.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(this.transfer.deserialize(s.get()));
    }

    /**
     * @return true if the underlying file for this object exists, and if getting it from disk acutally returns a valid
     * result, false otherwise.
     */
    public boolean exists() {
        try {
            return this.location.toFile().exists() && this.get().isPresent();
        } catch (IOException ignored) {
            return false; // IOException means it doesn't exist
        }
    }

    /**
     * Deletes this Store's file and encapsulating directory.
     * @return true if both the file and its encapsulating directory were deleted, false otherwise.
     */
    public boolean delete() {
        boolean didDelete = false;
        didDelete = this.location.toFile().delete();
        didDelete = this.location.getParent().toFile().delete() && didDelete;
        return didDelete;
    }

    /**
     * How to serialize and deserialize T.
     * @param <S> The class that can be serialized and deserialized from and into Strings.
     */
    public static class Transfer<S> implements Deserialization<S>, Serialization<S> {

        private Serialization<S> serialize;
        private Deserialization<S> deserialize;


        @Override
        public String serialize(S s) {
            return this.serialize.serialize(s);

        }

        @Override
        public S deserialize(String s) {
            return this.deserialize.deserialize(s);
        }

        public Transfer(Serialization<S> serialize, Deserialization<S> deserialize) {
            this.serialize = serialize;
            this.deserialize = deserialize;
        }

        public Transfer(Gson gson, Class<S> clazz) {
            this(gson::toJson, (str) -> gson.fromJson(str, clazz));
        }

    }

}
