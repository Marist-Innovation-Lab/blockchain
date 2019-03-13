package essence.test;

import edu.marist.jointstudy.essence.core.structures.Block;
import essence.test.util.Generate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenesisBlockTests {
    private Block b = Block.newBlock(Generate.singleTransactionList(), null);

    @Test
    void isMined() {
        assertTrue(b.isMined(), "Genesis block is not mined after it is constructed with fake transactions.");
    }

    @Test
    void hasExpectedHash() {
        assertEquals("000c82d62127b10120494a4eb38908b4abfa819e8071f4e7e5a346f525d2cddb",
                b.getHash().toString(),
                "Genesis block's hash is not the expected hash value.");
    }

    @Test
    void hasNullPreviousBlock() {
        assertNull(b.getPreviousBlock(), "Genesis block's previous block is not null.");
    }

    @Test
    void hasZeroId() {
        assertEquals(b.getId(), 0, "Genesis block's id is not 0.");
    }

}
