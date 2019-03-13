package edu.marist.jointstudy.essence.core.structures;

import edu.marist.jointstudy.essence.core.hash.Hashable;
import edu.marist.jointstudy.essence.core.security.Security;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * TODO: include id in signature
 *
 * Created by Thomas.Magnusson1 on 6/30/2017.
 */
public class Transaction implements Hashable, Identifiable {

    private int id; // transactions increment ids regardless of the block they belong to
    private String publicKey; // the public key of the peer that signed this transaction
    private String signature; // the signature of the transaction, that the public key created with the paylod
    private String payload; // the body of the transaction

    /**
     * <b>Should only be used for parsing.</b>
     * <p>Creates an unsafe transaction, meaning that the payload, public key and signature are all manually created strings.</p>
     * <p>There are no guarantees that the transaction created {@code isVerified()}</p>
     * <p>The {@code id} of this transaction is set to 0. This is dangerous.</p>
     * @param payload payload of the unsafe transaction
     * @param publicKey public key of the unsafe transaction. No guarantee that this and the signature are verified.
     * @param signature signature of the unsafe transaction. No guarantee that this and the public key are verified.
     * @return an unsafe, existing transaction, with no guarantee of validity. Id is set to 0.
     */
    public static Transaction wrapUnsafeNoId(String payload, String publicKey, String signature) {
        return new Transaction(payload, publicKey, signature);
    }

    /**
     * <b>Should only be used for parsing.</b>
     * <p>Creates an unsafe transaction, meaning that the payload, public key and signature are all manually created strings.</p>
     * <p>There are no guarantees that the transaction created {@code isVerified()}</p>
     * @param payload payload of the unsafe transaction
     * @param publicKey public key of the unsafe transaction. No guarantee that this and the signature are verified.
     * @param signature signature of the unsafe transaction. No guarantee that this and the public key are verified.
     * @param id id of the unsafe transaction. No guarantee that this is the correct id in a given blockchain.
     * @return an unsafe, existing transaction, with no guarantee of validity.
     */
    public static Transaction wrapUnsafe(String payload, String publicKey, String signature, int id) {
        return new Transaction(payload, publicKey, signature, id);
    }

    /**
     * Creates a new transaction. New transactions are guaranteed to be verified.
     *
     * <p>The id of new transactions is 0. This is dangerous and does not necessarily reflect the id it should have
     * with respect to the blockchain to which it belongs.</p>
     *
     * <p>To set the transaction's id, use {@code setId()}</p>
     * @param payload
     * @return a new transaction, guaranteed to be verified, with its id set to 0.
     */
    public static Transaction newTransaction(String payload) {
        return new Transaction(payload);
    }

    // should not be able to create a transaction without an id.
    // Implementation of Transaction.wrapUnsafeNoId()
    private Transaction(String payload, String publicKey, String signature) {
        this.setPayload(payload);
        this.publicKey = publicKey;
        this.signature = signature;
    }

    // Implementation of Transaction.wrapUnsafe()
    private Transaction(String payload, String publicKey, String signature, int id) {
        this(payload, publicKey, signature);
        this.id = id;
    }

    // Implementation of Transaction.newTransaction()
    private Transaction(String payload) {
        this.setPayload(payload);
        this.publicKey = Security.INSTANCE.getPublicKeyHexadecimal();
        try {
            this.signature = Security.INSTANCE.sign(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** @return true iff the public key of this transaction signed its payload to create its signature. */
    public boolean isVerified() {
        try {
            return Security.INSTANCE.isVerified(this.getPayload(), this.getPublicKey(), this.getSignature());
        } catch (IllegalArgumentException iae) {
            return false; // not the right length, can ignore
        } catch (Exception e) {
            e.printStackTrace();
            return false; // better be safe than sorry
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** @return hexadecimal String representation of this transaction's signature. */
    public String getSignature() {
        return signature;
    }

    public String getPayload() {
        return payload;
    }

    /** @return hexadecimal String representation of this transaction's public key. */
    public String getPublicKey() {
        return publicKey;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(
                Integer.BYTES +
                        payload.getBytes().length +
                        (Objects.nonNull(signature) ? signature.getBytes().length : 0) +
                        (Objects.nonNull(publicKey) ? publicKey.getBytes().length : 0))
                .putInt(id)
                .put(payload.getBytes())
                .put(Objects.nonNull(signature) ? signature.getBytes() : new byte[0])
                .put(Objects.nonNull(publicKey) ? publicKey.getBytes() : new byte[0])
                .array();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(Objects.isNull(obj)) {
            return false;
        }

        if(!(obj instanceof Transaction)) {
            return false;
        }

        Transaction tx = (Transaction) obj;
        return (this.getId() == tx.getId()) &&
                (this.getPayload().equals(tx.getPayload())) &&
                (this.getPublicKey().equals(tx.getPublicKey()) &&
                (this.getSignature().equals(tx.getSignature())));
    }
}
