package edu.marist.jointstudy.essence.api;

import edu.marist.jointstudy.essence.api.client.Friend;
import edu.marist.jointstudy.essence.api.client.RestClient;
import edu.marist.jointstudy.essence.api.server.BlockchainServer;
import edu.marist.jointstudy.essence.api.server.SybilBlockchainServer;
import edu.marist.jointstudy.essence.api.store.PersistentStoreSingleton;
import edu.marist.jointstudy.essence.api.store.Preferences;
import edu.marist.jointstudy.essence.api.store.Store;
import edu.marist.jointstudy.essence.core.structures.Block;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * <p>A blockchain peer on the Essence network. A peer is both a server and client.
 *
 * <p>It is a server because it receives and responds to requests as specified by its REST API.
 *
 * <p>It is a client because it sends requests to other peers on the network asking to download their blockchains.
 * After a peer, say Alice, downloads the blockchain of another peer, say Bob, Alice checks if Bob's blockchain is both
 * <em>valid</em> and <em>longer</em> than hers. If both these conditions are true, she saves Bob's blockchain in place
 * of her own.</p>
 */
public class Peer {

    /** The server aspect of this peer. It receives requests and responds to them according to the REST API. */
    private BlockchainServer server;

    /** Handles server's concurrency. */
    private ExecutorService serverService = Executors.newCachedThreadPool();

    /** Sends requests and receives responses. Used to request blockchains from friends periodically. */
    private RestClient client;

    /** A list of this peer's {@link Friend}s.*/
    private List<Friend> friends;

    /**
     * This peer uses a {@code PersistentStore<Blockchain>} as its store.
     *
     * @param port the port this peer runs out of.
     * @param friendUrls A list of urls of this peer's friends on the network. Two peers are <em>friends</em> if they
     *                   store each others' urls and communicate to each other regularly (over HTTP), just like real
     *                   friends do.
     */
    public Peer(int port, List<HttpUrl> friendUrls) {
        Store<Blockchain> bcStore = PersistentStoreSingleton.INSTANCE.get();

        // map urls to friends
        this.friends = friendUrls.stream().map((url) -> new Friend(url, bcStore)).collect(Collectors.toList());

        // Make a sybil server if the peer is marked as a sybil peer.
        this.server = Preferences.isSybil() ?
                new SybilBlockchainServer(port, friendUrls, bcStore)
                :
                new BlockchainServer(port, friendUrls, bcStore);
        this.client = new RestClient(friends);
    }

    /**
     * Starts the peer's server as well as its periodic blockchain downloads from its friends.
     */
    public void start() {
        this.serverService.execute(() -> {
            try {
                server.start();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.err.println("Couldn't start the server.");
            }
        });

        client.start();
    }

    /**
     * Stops the peer's server as well as the periodic blockchain downloads from its friends.
     *
     * <p>Populates a new executor for the server service.</p>
     */
    public void stop() {
        this.serverService.submit(server::stop);
        serverService.shutdown();
        this.serverService = Executors.newCachedThreadPool();
        client.stop();
    }

    /** @return a read-only copy of the peer's friend urls. */
    public List<HttpUrl> friendUrls() {
        return Collections.unmodifiableList(this.friends.stream().map(Friend::baseUrl).collect(Collectors.toList()));
    }

    /**
     * @return this peer's friends
     * @see Friend
     */
    public List<Friend> friends() {
        return this.friends;
    }

    /** @return a read-only copy of the server's transaction buffer */
    public List<Transaction> transactionBuffer() {
        return this.server.transactionBuffer();
    }

    /** @return a read-only copy of the server's blocks in its blockchain. */
    public List<Block> blocks() {
        return this.server.blocks();
    }

    /**
     * <p>Requests the server mine its blockchain.</p>
     * <p>Note: the server's service handles the threading. The mining will happen on the server's thread.</p>
     * @return a completable future representing a request to the server to mine its blockchain.
     * {@code null} if the server's transaction buffer is empty (no unmined transactions).
     */
    public CompletableFuture<Void> requestMining() {
        if(this.server.transactionBuffer().isEmpty()) {
            return null;
        }
        return CompletableFuture.runAsync(() -> {
            try {
                this.server.mineBlockchain();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }, serverService);
    }

    public Transaction submitTransactionToBuffer(Transaction t) {
        return this.server.submitTransactionToBuffer(t);
    }
}
