package edu.marist.jointstudy.essence.core.mine;

import edu.marist.jointstudy.essence.core.hash.Hashable;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * <p>Wraps a {@code long} to represent mining functionality, namely incrementing and converting to byte array.
 *
 * <p>A nonce is a random number to change the hash of the block to match a certain threshold.</p>
 *
 * Created by Thomas.Magnusson1 on 6/9/2017.
 */
public final class Nonce implements Comparable<Nonce>, Hashable {

    /** The underlying value this nonce represents.*/
    private long value;

    /** @return a new {@code Nonce} whose {@code value} is 0.*/
    public static Nonce newZeroNonce() {
        return new Nonce(0L);
    }

    /** @return a new {@code Nonce} whose {@code value} is the argument. */
    public static Nonce newNonce(long n) {
        return new Nonce(n);
    }

    /** Private constructor for a nonce, whose underlying value is a {@code long}.*/
    public Nonce(long value) {
        this.value = value;
    }

    /**
     * <p>Returns the current nonce plus one.</p>
     *
     * <p>Note: mining difficult target hashes could potentially cause an overflow to the underlying {@code long}'s
     * value. This method performs a check to make sure it does not exceed the {@code Long.MAX_VALUE}. If it throws
     * the {@code ArithmeticException} please consider refreshing the timestamp on the given block and resetting
     * the nonce to zero, to produce fresh batches of hashes.</p>
     *
     * @return this {@code Nonce} with its {@code value} incremented by one.
     */
    public Nonce incremenented() {
        if(this.value == Long.MAX_VALUE) {
            throw new ArithmeticException("The current nonce is at its maximum value " + Long.MAX_VALUE
                    + ". Please consider refreshing the timestamp and resetting the nonce to zero.");
        }
        return new Nonce(value + 1);
    }

    /**
     * @param n a nonce to compare to this one.
     * @return 1 if this nonce's value is greater, -1 if this nonce's value is lesser, and 0 if their values are equal.
     */
    @Override
    public int compareTo(Nonce n) {
        if(this.value > n.value) {
            return 1;
        }
        if (this.value < n.value) {
            return -1;
        }
        return 0;
    }

    /**
     * @param obj another nonce.
     * @return {@code true} if the two if the underlying nonces' values are equal, or if both nonces are null, otherwise
     * {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(Objects.isNull(obj) || this.getClass() != obj.getClass()) {
            return false;
        }
        Nonce n = (Nonce) obj;
        return this.value == n.value;
    }

    /** @return the {@code byte[]} equivalent of the underlying {@code long}'s value. */
    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(Long.BYTES).putLong(this.value).array();
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
