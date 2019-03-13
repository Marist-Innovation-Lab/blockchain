package essence.test;

import edu.marist.jointstudy.essence.core.hash.HashFunction;
import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTests {

    private Transaction tx = Transaction.wrapUnsafeNoId("Hello", "World", "");

    @Test
    void equality() {
        tx.setId(1);

        Transaction newTx = Transaction.wrapUnsafeNoId("Hello", "World", "");
        newTx.setId(1);

        assertEquals(newTx, tx);
    }

    @Test
    void differentParameterLengthsHashes() {
        Transaction newTx1 = Transaction.wrapUnsafeNoId("hello", "small", "very large");
        Transaction newTx2 = Transaction.wrapUnsafeNoId("hello", "very large", "small");
        Transaction newTx3 = Transaction.wrapUnsafeNoId("xs", "very large", "small");
        Transaction newTx4 = Transaction.wrapUnsafeNoId("xs", "small", "very large");

        Hashcode h1 = HashFunction.SHA_256.hash(newTx1);
        Hashcode h2 = HashFunction.SHA_256.hash(newTx2);
        Hashcode h3 = HashFunction.SHA_256.hash(newTx3);
        Hashcode h4 = HashFunction.SHA_256.hash(newTx4);
    }

    @Test
    void nullEquality() {
        Transaction tx = null;
        Transaction tx2 = null;
        assertEquals(tx, tx2);
    }

    @Test
    void setId() {
        tx.setId(4);
        assertEquals(4, tx.getId());
    }

    @Test
    void setPayload() {
        tx.setPayload("Something else");
        assertEquals("Something else", tx.getPayload());
    }

    @Test
    void isNotVerified() {
        assertTrue(!tx.isVerified());
    }

    @Nested
    @DisplayName("New Transaction with proper signature and public key")
    class NewTransaction {

        Transaction trueTx =  Transaction.newTransaction("Hello world");

        @Test
        void isVerified() {
            assertTrue(trueTx.isVerified());
        }

        @Test
        void hasPublicKey() {
            assertNotNull(trueTx.getPublicKey());
        }

        @Test
        void hasSignature() {
            assertNotNull(trueTx.getSignature());
        }

    }




}
