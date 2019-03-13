package edu.marist.jointstudy.essence.core.hash;

import com.google.gson.annotations.SerializedName;

import javax.xml.bind.DatatypeConverter;

/**
 * <p>A wrapper around {@code byte[]} representing a particular hash, e.g. SHA-256.
 *
 * Created by Thomas.Magnusson1 on 6/6/2017.
 */
public interface Hashcode extends Hashable {

    /** @return the underlying byte array of this hash. */
    byte[] toByteArray();

    class SHA256 implements Hashcode {

        /** The underlying bytes that this hashcode represents. */
        private transient byte[] bytes;

        @SerializedName("hexBinary")
        private String textBytes;

        // required for json
        private SHA256() { }

        private SHA256(byte[] bytes) {
            this.bytes = bytes;
            this.textBytes = DatatypeConverter.printHexBinary(bytes).toLowerCase();
        }

        /**
         * @param hex a hex representation of a SHA256 hash.
         */
        private SHA256(String hex) {
            this(DatatypeConverter.parseHexBinary(hex));
        }

        public static SHA256 fromHex(String hex) {
            return new SHA256(hex);
        }

        /**
         * <p>Wraps a {@code byte[]} as a {@code Hashcode}.
         *
         * <p><strong>Note:</strong> this does <strong>NOT</strong> calculate the given {@code byte[]}'s hashcode.
         * This is purely a construction method, not a calculation.</p>
         *
         * @param bytes a byte array to wrap as a hashcode.
         * @return a new SHA256 hashcode with the given {@code byte[]} as its underlying byte array.
         */
        public static SHA256 wrap(byte[] bytes) {
            return new SHA256(bytes);
        }

        /** @return this class' underlying byte array representation. */
        @Override
        public byte[] toByteArray() {
            return this.bytes;
        }

        @Override
        public String toString() {
            return textBytes;
        }
    }
}
