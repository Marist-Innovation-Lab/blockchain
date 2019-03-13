package edu.marist.jointstudy.essence.core.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>A hash function takes a hashable input and converts it into a hashcode.
 *
 * <p>This interface also offers concrete implementations of the {@code hash()} method, namely the {@code SHA_256}
 * implementation.</p>
 *
 * Created by Thomas.Magnusson1 on 6/9/2017.
 */
@FunctionalInterface
public interface HashFunction<H extends Hashable, C extends Hashcode> {

    C hash(H hashable);

    /**
     * A concrete {@code HashFunction} that computes the SHA 256 hash of a given {@code Hashable}.
     */
    HashFunction<Hashable, Hashcode> SHA_256 = (t) -> {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(t.toByteArray());
            return Hashcode.SHA256.wrap(md.digest());
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            // should not happen
        }
        // should not happen
        throw new IllegalStateException("SHA-256 is not available, consider using another HashFunction.");
    };
}
