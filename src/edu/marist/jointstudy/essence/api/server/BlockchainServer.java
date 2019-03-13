package edu.marist.jointstudy.essence.api.server;

import java.io.IOException;
import java.util.*;

/* NanoHTTPD
   GitHub: https://github.com/NanoHttpd/nanohttpd
   Documentation : https://jar-download.com/java-documentation-javadoc.php?a=nanohttpd&g=org.nanohttpd&v=2.2.0
 */
import com.google.gson.JsonParser;
import edu.marist.jointstudy.essence.Util;
import edu.marist.jointstudy.essence.api.parse.GsonSingleton;
import edu.marist.jointstudy.essence.api.store.PersistentStore;
import edu.marist.jointstudy.essence.api.store.Store;
import edu.marist.jointstudy.essence.core.mine.Mining;
import edu.marist.jointstudy.essence.core.structures.Block;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import okhttp3.HttpUrl;

/**
 * <p>REST stands for REpresentational State Transfer. It's an architectural style using HTTP methods. Google it if you
 * want to find out more.
 *
 * <p>This REST server serializes its resources (the things being communicated over HTTP, e.g. Blockchain) using
 * JSON.</p>
 *
 * <p>This particular class deals only with blockchain-specific logic. See {@link Server} if you want more
 * information on the underlying server architecture.</p>
 *
 * Authors Tom Magnusson, Alan and the Marist NSF-stars
 */
public class BlockchainServer extends Server {

    /**
     * Is a test sybil node. If true, this server feeds fake blockchains to its friends.
     * You thought fake news was bad.
     */
    private boolean isSybil;

    /** Where to store the blockchain. */
    private Store<Blockchain> bcStore;

    /** The blockchain this server deals with. */
    private Blockchain bc;

    /**
     * The transactions submitted by clients that have not yet made it onto the blockchain. Clients must request that
     * The blockchain be mined, which flushes the buffered transactions onto the blockchain.
     */
    private List<Transaction> transactionBuffer = new LinkedList<>();
    /** The next transaction id for the buffer. Used as a cursor to remember which ids to give to which transactions.*/
    private int nextTxIdForBuffer;

    /** Used to give anyone who asks all of the urls of this peer's friends. */
    private List<HttpUrl> friendlyUrls;

    public BlockchainServer(int port, List<HttpUrl> friendlyUrls, Store<Blockchain> store) {
        super(port, GsonSingleton.INSTANCE.get());
        this.bcStore = store;
        this.friendlyUrls = friendlyUrls;

        Util.time("parsing blockchain", () -> {
            try {
                Optional<Blockchain> maybeBc = store.get();
                this.bc = maybeBc.orElse(new Blockchain());
            } catch(IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't get the blockchain from disk.");
                System.err.println(e.getMessage());
                this.bc = new Blockchain();
            }
        });

        // set next tx id for the buffer to the lastTxId + 1, or 0 (if we have no last tx id)
        nextTxIdForBuffer = this.bc.getLastTransactionId().isPresent() ?
                this.bc.getLastTransactionId().getAsInt() + 1 :
                0;

        APIConstants.displayStartupInfo(port);

        // registers all the routes for the server, e.g. GET "/blockchain/:" where ":" stands for an id, which is
        // passed into the function passed in as the second argument to registering a path.
        Util.time("registering routes", () -> {
            registerGets();
            registerPosts();
            registerDeletes();
            registerOptions();
        });
    }

    private void registerGets() {
        // register routes

        // ":" stands for an id (number) of some kind in the url

        // get the urls of this peer's friends
        get("/friends", this::getFriends);

        // get a blockchain
        get("/blockchain", this::getBlockchain);

        get("/blockchain/length", this::getBlockchainLength);

        // get the mining difficulty and max tx per block of this server
        get("/details",
                (s, ids) -> newApiResponse(Response.Status.OK,
                        "{\"miningDifficulty\":" + Mining.difficulty +
                                ", \"maxTransactionsPerBlock\": \"infinite\"}"));

        // get the contents of this blockchain's buffer
        get("/blockchain/buffer", this::getBuffer);

        // get a transaction within a blockchain
        get("/blockchain/transaction/:", this::getTx);

        // get a transaction within a block within a blockchain
        get("/blockchain/block/:/transaction/:", this::getTxFromBlock);

        // get the merkle tree of a given block
        get("/blockchain/block/:/merkle", this::getMerkleTree);

        // lots of browsers request this
        get("/favicon.ico",
                (s, ids) -> newFixedLengthResponse(Response.Status.OK, "plain/text", "favicon"));

        // icons - these requests seem to happen a lot
        get("/apple-touch-icon",
                (s, ids) -> newApiResponse(Response.Status.OK, "apple-touch-icon."));
    }

    private void registerPosts() {

        // request mining of the blockchain
        post("/blockchain/mine", this::mineBlockchain);

        // post a transaction to a blockchain's tx buffer
        // expect "/blockchain/{id}/transaction" {transaction: some payload}
        post("/blockchain/transaction", this::createTx);
    }

    private void registerDeletes() {

        // clear the transactions in a blockchain's buffer
        delete("/blockchain/buffer", this::clearBuffer);

    }

    private void registerOptions() {
        // option needed for DELETE on chrome for safety or something like that, stupid
        // https://perlmaven.com/deleting-item-using-ajax-with-delete-and-options
        options("/blockchain/buffer",
                (s, ids) -> newApiResponse(Response.Status.NO_CONTENT, ""));
    }

    /** @return a read-only copy of the server's transaction buffer. */
    public List<Transaction> transactionBuffer() {
        return Collections.unmodifiableList(this.transactionBuffer);
    }

    /** @return a read-only copy of the server's blocks in its blockchain. */
    public List<Block> blocks() {
        return Collections.unmodifiableList(this.bc.asList());
    }

    //
    // Response routines
    //

    /** GET "/friends" returns HttpUrls of this peer's friends */
    protected Response getFriends(IHTTPSession session, int[] ids) {
        return this.newApiResponse(friendlyUrls);
    }

    /** GET "/blockchain" */
    protected Response getBlockchain(IHTTPSession session, int[] ids) {
        try {
            // update bc cache as well
            this.bc = bcStore.get().get();
            return this.newApiResponse(bc);
        } catch (Exception e) {
            e.printStackTrace();
            return Failure.internalIOFailure(e).response(gson);
        }
    }

    /** GET "/blockchain/length" */
    protected Response getBlockchainLength(IHTTPSession session, int[] ids) {
        try {
            // update bc cache as well
            this.bc = bcStore.get().get();
            return newApiResponse("{\"length\": " + bc.size() + " }");
        } catch (Exception e) {
            e.printStackTrace();
            return Failure.internalIOFailure(e).response(gson);
        }
    }

    /** GET "/blockchain/buffer" */
    protected Response getBuffer(IHTTPSession session, int[] ids) {
        return this.newApiResponse(transactionBuffer);
    }

    /** GET "/blockchain/transaction" */
    protected Response getTx(IHTTPSession session, int[] ids) {
        int txId = ids[0];
        try {
            Transaction t = bc.findTransaction(txId).get(); // can throw unchecked exception
            return this.newApiResponse(t);
        } catch(Exception e) {
            return Failure.transactionLookup(e, txId).response(gson);
        }
    }

    /** GET "blockchain/block/:/merkle" */
    protected Response getMerkleTree(IHTTPSession session, int[] ids) {
        int blockId = ids[0];
        // TODO: inefficient way of searching for a block, should have getBlock(int id)
        Block potentialBlock = bc.getCurrentBlock();
        while(potentialBlock.getId() != blockId) {
            potentialBlock = potentialBlock.getPreviousBlock();
            if(Objects.isNull(potentialBlock)) {
                return Failure.invalidBlockId(blockId).response(gson);
            }
        }
        return this.newApiResponse(potentialBlock.getTransactionsAsMerkleTree());
    }

    /** GET "/blockchain/block/:/transaction" */
    protected Response getTxFromBlock(IHTTPSession session, int[] ids) {
        int blockId = ids[0];
        int txId = ids[1];
        try {
            Transaction t = bc.findTransactionWithBlockId(txId, blockId).get(); // can throw unchecked exception
            return this.newApiResponse(t);
        } catch (Exception e) {
            return Failure.transactionLookupWithBlockId(e, txId, blockId).response(gson);
        }
    }

    /** POST (MINE) "/blockchain" */
    protected Response mineBlockchain(IHTTPSession s, int[] ids) {
        try {
            return mineBlockchain() ?
                    this.newApiResponse(bc) : newApiResponse(Response.Status.NO_CONTENT, "");
        } catch (IOException e) {
            return this.newApiResponse(Failure.internalIOFailure(e));
        }
    }

    public boolean mineBlockchain() throws IOException {
        if(transactionBuffer.isEmpty()) {
           return false;
        }
        bc.add(transactionBuffer);
        transactionBuffer.clear();
        bcStore.save(bc);
        return true;
    }

    /** POST "/blockchain/transaction" */
    protected Response createTx(IHTTPSession s, int[] ids) {
        // parse body into a Transaction object (hopefully)
        try {
            String rawBody = parseBody(s).get(); // can throw

            // parse out the payload in the request body (JSON)
            JsonParser parser = new JsonParser();
            String payload = parser.parse(rawBody).getAsJsonObject().get("payload").getAsString();

            Transaction t = submitTransactionToBuffer(Transaction.newTransaction(payload));

            return this.newApiResponse(t);
        } catch(Exception e) {
            return this.newApiResponse(Failure.malformedTransactionBody(e));
        }
    }

    public Transaction submitTransactionToBuffer(Transaction t) {
        // the client won't specify the id, we do that
        t.setId(nextTxIdForBuffer++);
        transactionBuffer.add(t);
        return t;
    }

    /** DELETE "/blockchain/buffer" */
    protected Response clearBuffer(IHTTPSession session, int[] ids) {
        if(transactionBuffer.isEmpty()) {
            return getBuffer(session, ids);
        }

        int lastId = 0;
        lastId = transactionBuffer.get(transactionBuffer.size() - 1).getId();
        nextTxIdForBuffer = lastId - transactionBuffer.size() + 1;
        transactionBuffer.clear();

        return getBuffer(session, ids); // should return an empty collection
    }
}
