package edu.marist.jointstudy.essence.core.mine;

import edu.marist.jointstudy.essence.core.structures.Block;

/**
 * <p>A static settings class (interface) to tweak with the mining functionality from a centralized place.</p>
 *
 * Created by Thomas.Magnusson1 on 6/22/2017.
 */
public interface Mining {

    /** default difficulty (not very difficult, is it?) */
    int difficulty = 3;

    /** the target has has to have the difficulty number many leading zeros. */
    MiningFunction miningFunction = MiningFunction.NUM_LEADING_ZEROS;

    static boolean isMined(Block block) {
        return miningFunction.isSolution(block.getHash(), difficulty);
    }

}
