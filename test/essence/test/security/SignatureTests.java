package essence.test.security;

import edu.marist.jointstudy.essence.core.security.Security;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureTests {

    @Nested
    @DisplayName("Basic Signature verify")
    class BasicSignature {
        @Test
        void isVerified() throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException, InvalidKeySpecException {
            Security s = Security.INSTANCE;

            String str = "hello world";
            String signature = s.sign(str);

            assertTrue(s.isVerified(str, s.getPublicKeyHexadecimal(), signature));
        }
    }

}
