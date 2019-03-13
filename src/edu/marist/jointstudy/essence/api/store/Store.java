package edu.marist.jointstudy.essence.api.store;

import java.io.IOException;
import java.util.Optional;

public interface Store<T> {

    void save(T t) throws IOException;

    Optional<T> get() throws IOException;

    boolean exists();

    boolean delete();
}
