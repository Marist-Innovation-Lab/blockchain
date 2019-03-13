package essence.test;

import edu.marist.jointstudy.essence.core.structures.Blockchain;
import essence.test.util.Generate;
import org.junit.jupiter.api.*;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

public class BlockchainTests {

    private Blockchain bc = new Blockchain();

    @Nested
    @DisplayName("Empty (just created)")
    class Empty {
        @Test
        void hasNullCurrentBlock() {
            assertNull(bc.getCurrentBlock());
        }

        @Test
        void isMined() {
            assertTrue(bc.isMined(), "Empty blockchains should be considered mined.");
        }

        @Test
        void getLastTransactionIdReturnsEmpty() {
            assertEquals(OptionalInt.empty(), bc.getLastTransactionId());
        }
    }

    @Nested
    @DisplayName("Genesis block added")
    class GenesisBlock {

        @BeforeEach
        void addBlock() {
            bc.add(Generate.singleTransactionList());
        }

        @Test
        void hasCurrentBlock() {
            assertNotNull(bc.getCurrentBlock());
        }

        @Test
        void isMined() {
            assertTrue(bc.isMined());
        }

        @Test
        void previousBlockNull() {
            assertNull(bc.getCurrentBlock().getPreviousBlock(),
                    "Currently only the genesis block is added, the previous block of the genesis should be null.");
        }

        @Test
        void expectedHash() {
            assertEquals("000c82d62127b10120494a4eb38908b4abfa819e8071f4e7e5a346f525d2cddb",
                    bc.getCurrentBlock().getHash().toString());
        }

        @Test
        void getLastTxId() {
            // the block only has one transaction
            assertEquals(0, bc.getLastTransactionId().getAsInt());
        }
    }

    @Nested
    @DisplayName("Two blocks added")
    class TwoBlocks {

        @BeforeEach
        void addTwoBlocks() {
            bc.add(Generate.singleTransactionList());
            bc.add(Generate.threeTransactionList());
        }

        @Test
        void hasCurrentBlock() {
            assertNotNull(bc.getCurrentBlock());
        }

        @Test
        void hasPreviousBlock() {
            assertNotNull(bc.getCurrentBlock().getPreviousBlock());
        }

        @Test
        void isMined() {
            assertTrue(bc.isMined());
        }

        @Test
        void expectedHash() {
            assertEquals("000be659874ac33e21aff7b5126907860447f177321f7a289fc0972ae5a7cd3a",
                    bc.getCurrentBlock().getHash().toString());
        }

        @Test
        void getLastTxId() {
            // the second block has three transactions
            assertEquals(3, bc.getLastTransactionId().getAsInt());
        }

    }

    @Nested
    @DisplayName("Two single transaction blocks")
    class TwoSingleTransactionBlockBlockchain {
        @BeforeEach
        void addTwoBlocks() {
            bc.add(Generate.singleTransactionList());
            bc.add(Generate.singleTransactionList());
        }

        @Test
        void addingSingleTransactionBlockHasDifferentLastTxIds() {
            int oldLastTxId = bc.getLastTransactionId().getAsInt();
            bc.add(Generate.singleTransactionList());
            int newLastTxId = bc.getLastTransactionId().getAsInt();
            assertNotEquals(oldLastTxId, newLastTxId);
        }
    }
}
