package essence.test;

import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.mine.MiningFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MiningTests {

    @Test
    void zeroDifficulty() {
        int difficulty = 0;
        Hashcode solution = Hashcode.SHA256.fromHex("8143ac992b0c6053c5eed88049c95939798b299b88489b3fedc5fd13df06f0c1");
        assertTrue(MiningFunction.NUM_LEADING_ZEROS.isSolution(solution, difficulty));
    }

    @Test
    void oneDifficulty() {
        int difficulty = 1;
        Hashcode solution = Hashcode.SHA256.fromHex("0143ac992b0c6053c5eed88049c95939798b299b88489b3fedc5fd13df06f0c1");
        assertTrue(MiningFunction.NUM_LEADING_ZEROS.isSolution(solution, difficulty));
    }

    @Test
    void threeDifficulty() {
        int difficulty = 3;
        Hashcode solution = Hashcode.SHA256.fromHex("0003ac992b0c6053c5eed88049c95939798b299b88489b3fedc5fd13df06f0c1");
        assertTrue(MiningFunction.NUM_LEADING_ZEROS.isSolution(solution, difficulty));
    }

    @Test
    void sixtyFourDifficutly() {
        int difficulty = 64;
        Hashcode solution = Hashcode.SHA256.fromHex("0000000000000000000000000000000000000000000000000000000000000000");
        assertTrue(MiningFunction.NUM_LEADING_ZEROS.isSolution(solution, difficulty));
    }

    @Test
    void sixtyFiveDifficultyIsInvalid() {
        int difficulty = 65;
        Hashcode nonSolution =
                Hashcode.SHA256.fromHex("0000000000000000000000000000000000000000000000000000000000000000");
        assertFalse(MiningFunction.NUM_LEADING_ZEROS.isSolution(nonSolution, difficulty));
    }

    @Test
    void threeDifficultyFailing() {
        int difficulty = 3;
        Hashcode solution = Hashcode.SHA256.fromHex("0a13ac992b0c6053c5eed88049c95939798b299b88489b3fedc5fd13df06f0c1");
        assertFalse(MiningFunction.NUM_LEADING_ZEROS.isSolution(solution, difficulty));
    }
}
