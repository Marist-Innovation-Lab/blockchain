package essence.test;

import edu.marist.jointstudy.essence.core.hash.HashFunction;
import edu.marist.jointstudy.essence.core.hash.Hashable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashingTests {

    @Test
    void sanityCheckSHA256Works() {
        // expected value based on http://www.xorbin.com/tools/sha256-hash-calculator
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
                HashFunction.SHA_256.hash(new Hashable() {
                    @Override
                    public byte[] toByteArray() {
                        return "hello world".getBytes();
                    }
                }).toString());
    }

}
