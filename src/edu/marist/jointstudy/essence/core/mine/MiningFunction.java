package edu.marist.jointstudy.essence.core.mine;

import edu.marist.jointstudy.essence.core.hash.Hashcode;

/**
 * <p>How to determine if a given (block's) hash is valid.</p>
 *
 * Created by Thomas.Magnusson1 on 6/9/2017.
 */
@FunctionalInterface
public interface MiningFunction {

    /**
     * @param potentialSolution a (block's) hash that might be valid.
     * @param difficulty a value that represents how unlikely it is a {@code potentialSolution} is valid.
     * @return {@code true} if the given {@code potentialSolution} is valid, {@code false} otherwise.
     */
    boolean isSolution(Hashcode potentialSolution, int difficulty);

    /** The hash is valid if the number of leading zero bytes in the hash corresponds to the difficulty. */
    MiningFunction NUM_LEADING_ZEROS = (s, d) -> {
        if(d < 0 || d > s.toString().length() ) {
            return false;
        }
        // true if the hash starts with d many 0 bytes
        for (int i = 0; i < d; i++) {
            if (!(s.toString().charAt(i) == '0')) {
                return false;
            }
        }
        return true;
    };
}
