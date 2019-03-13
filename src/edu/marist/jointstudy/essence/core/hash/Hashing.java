package edu.marist.jointstudy.essence.core.hash;

/**
 * <p>Static settings class (interface) that holds the global hash function.</p>
 *
 * Created by Thomas.Magnusson1 on 6/22/2017.
 */
public interface Hashing {
    HashFunction<Hashable, Hashcode> hashFunction = HashFunction.SHA_256;
}
