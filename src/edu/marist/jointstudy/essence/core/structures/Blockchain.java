package edu.marist.jointstudy.essence.core.structures;

import java.util.*;

/**
 * A linked list of hash pointers to blocks, which contain transactions.
 *
 * Created by Thomas.Magnusson1 on 6/30/2017.
 */
public class Blockchain {

    /** The block that was last added to the chain. Null if this blockchain is empty. */
    private Block currentBlock;

    /** Create a new blockchain with null current block. */
    public Blockchain() {
        size();
    }

    /**
     * Adds a list of transactions to the blockchain, automatically setting the ids of the transactions to pick up from
     * the last transaction id of this blockchain.
     *
     * This method mines a new block when it is created, which is a blocking operation.
     *
     * @param transactions a list of transactions to be added to the blockchain.
     */
    public void add(List<Transaction> transactions) {
        int lastTxId = this.getLastTransactionId().orElse(-1);
        for(Transaction tx: transactions) {
            tx.setId(++lastTxId);
        }
        this.currentBlock = Block.newBlock(transactions, this.getCurrentBlock());
        this.size++;
    }

    /** @return the last added block of this blockchain. */
    public Block getCurrentBlock() {
        return this.currentBlock;
    }

    /**
     * Finds a transaction based on a transaction id.
     *
     * @param id the tx to be found.
     * @return the transaction with the matching id, or Optional.empty() if not present.
     */
    public Optional<Transaction> findTransaction(int id) {
        // search through each block
        Block searchBlock = this.currentBlock;
        while (searchBlock != null) {
            Optional<Transaction> tx = searchBlock.findTransaction(id);
            if (tx.isPresent()) {
                return tx;
            }
            searchBlock = searchBlock.getPreviousBlock();
        }
        return Optional.empty();
    }

    public Optional<Transaction> findTransactionWithBlockId(int transactionId, int blockId) {
        Block searchBlock = this.currentBlock;
        while (searchBlock != null) {
            if (searchBlock.getId() == blockId) {
                return searchBlock.findTransaction(transactionId);
            }
            searchBlock = searchBlock.getPreviousBlock();
        }
        return Optional.empty();
    }

    /** @return true if currentBlock is null, or if all the blocks in this blockchain are mined, False otherwise. */
    public boolean isMined() {
        if(this.currentBlock == null) return true;
        for(Block current = this.currentBlock; current != null; current = current.getPreviousBlock()) {
            if(!current.isMined()) {
                return false;
            }
        }
        return true;
    }

    /** @return true iff all the transactions in this blockchain have verified signatures. */
    public boolean isVerified() {
        if(this.currentBlock == null) return true;
        for(Block current = this.currentBlock; current != null; current = current.getPreviousBlock()) {
            if(!current.isVerified()) {
                return false;
            }
        }
        return true;
    }

    /** @return the last transaction id officially a part of this blockchain, OptionalInt.empty() if the blockchain
     * is empty.*/
    public OptionalInt getLastTransactionId() {
        Block b = this.getCurrentBlock();
        if(Objects.isNull(b)) {
            return OptionalInt.empty(); // a bc with no txs should not have a last txid
        }
        List<Transaction> txs = this.getCurrentBlock().getTransactions();
        return OptionalInt.of(txs.get(txs.size() - 1).getId());
    }

    /** The number of blocks in this blockchain. */
    private transient int size = 0;

    /** @return the number of blocks in this blockchain. */
    public int size() {
        // FIXME: inefficient
        return this.asList().size();
    }

    /** @return the blocks in this blockchain as a list. Each block still contains a reference to the previous one.*/
    public List<Block> asList() {
        List<Block> blockList = new ArrayList<>();
        for (Block current = this.currentBlock; current != null; current = current.getPreviousBlock()) {
            blockList.add(current);
        }
        return blockList;
    }
}
