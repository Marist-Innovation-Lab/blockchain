package edu.marist.jointstudy.essence.core.structures;

import com.google.gson.annotations.SerializedName;
import edu.marist.jointstudy.essence.core.hash.Hashable;
import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.hash.Hashing;
import edu.marist.jointstudy.essence.core.mine.Mining;
import edu.marist.jointstudy.essence.core.mine.Nonce;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Created by Thomas.Magnusson1 on 6/30/2017.
 */
public class Block implements Hashable {

    private int id;
    private transient MerkleTree<Transaction> transactionsTree;

    private Nonce nonce = Nonce.newZeroNonce();

    // for json serialization
    private Hashcode hash = null;

    // for json serialization
    @SerializedName("transactions") private List<Transaction> txs;

    private Block previousBlock;

    public static Block newUnsafeBlock(List<Transaction> transactions, Block previousBlock, Nonce nonce) {
        Block b = new Block(transactions, previousBlock, nonce);
        b.getHash(); // trigger lazy hash instantiation
        return b;
    }

    public static Block newBlock(List<Transaction> transactions, Block previousBlock) {
        Block b = new Block(transactions, previousBlock);

        // mine
        while(!b.isMined()) {
            b.setNonce(b.getNonce().incremenented());
        }
        return b;
    }

    private Block(List<Transaction> transactions, Block previousBlock) {
        if(Objects.nonNull(previousBlock)) {
            this.previousBlock = previousBlock;
            this.id = previousBlock.id + 1;
        } else {
            // genesis case
            this.id = 0;
        }
        this.txs = new ArrayList<>(transactions); // json serialization
        this.transactionsTree = new MerkleTree<>(transactions);
    }

    private Block(List<Transaction> transactions, Block previousBlock, Nonce nonce) {
        this(transactions, previousBlock);
        this.nonce = nonce;
    }

    // ========= Hashing ==========

    public Hashcode getHash() {
        this.hash = Hashing.hashFunction.hash(this);
        return this.hash;
    }

    // [byte].of(prevHash || transactions.merkleroot || id || nonce || source)
    // or [byte].of(transactions.merkleroot || id || nonce || source) if prevHash == null
    @Override
    public byte[] toByteArray() {
        byte[] prevHashBytes = null; // stays null if this is genesis block
        if(nonNull(this.getPreviousBlock())) {
            prevHashBytes = this.getPreviousBlock().getTransactionsAsMerkleTree().getMerkleRoot().toByteArray();
        }
        int prevHashBytesLength = prevHashBytes != null ? prevHashBytes.length : 0;
        byte[] nonceBytes = this.getNonce().toByteArray();
        byte[] merkleRootBytes = this.getTransactionsAsMerkleTree().getMerkleRoot().toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(
                merkleRootBytes.length + prevHashBytesLength + Integer.BYTES + nonceBytes.length
        );

        if(nonNull(prevHashBytes)) {
            buffer.put(prevHashBytes);
        }
        return buffer
                .put(merkleRootBytes)
                .putInt(id)
                .put(nonceBytes)
                .array();
    }

    // ========= Mining ============

    public boolean isMined() {
        return Mining.isMined(this);
    }

    public boolean isVerified() {
        return this.txs.stream().allMatch(Transaction::isVerified);
    }

    // ========= Searching ==========

    public Optional<Transaction> findTransaction(int id) {
        // filter by id (should only match one)
        List<Transaction> txs = this.txs.stream().filter((tx) -> tx.getId() == id).collect(Collectors.toList());
        return txs.isEmpty() ? Optional.empty() : Optional.of(txs.get(0)); // should only be one, if there are any in there, they'd be identical
    }

    // ========= Accessors and mutators =========

    public MerkleTree<Transaction> getTransactionsAsMerkleTree() {
        return transactionsTree;
    }

    public List<Transaction> getTransactions() {
        return txs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Nonce getNonce() {
        return nonce;
    }

    public void setNonce(Nonce nonce) {
        this.nonce = nonce;
    }

    public Block getPreviousBlock() {
        return previousBlock;
    }

    public Optional<Hashcode> getPreviousBlockHash() {
        Block prev = this.getPreviousBlock();
        return (prev == null) ? Optional.empty() : Optional.of(prev.getHash());
    }

}
