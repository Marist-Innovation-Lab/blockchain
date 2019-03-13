package edu.marist.jointstudy.essence.api.client;

import edu.marist.jointstudy.essence.api.client.service.BlockchainService;
import edu.marist.jointstudy.essence.api.parse.GsonSingleton;
import edu.marist.jointstudy.essence.api.store.Store;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Another peer on the network that this peer knows about. This peer periodically asks for its friends' blockchains to
 * keep in sync with the network.
 */
public class Friend {

    /** How this peer contacts its friend over http. This contains the base url of the friend.*/
    private Retrofit httpClient;

    /** Where to save blockchains once they're downloaded.*/
    private Store<Blockchain> store;

    /** A number of observer classes who wish to be notified about a given download process. */
    private List<BlockchainPullObserver> observers = new ArrayList<>();

    /**
     * Another peer on the network that this peer knows about.
     * @param url The url of the friend.
     * @param store Where to save blockchains.
     */
    public Friend(HttpUrl url, Store<Blockchain> store) {
        httpClient = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(GsonSingleton.INSTANCE.get()))
                .build();
        this.store = store;
    }

    public void addObserver(BlockchainPullObserver o) {
        observers.add(o);
        o.on(BlockchainPullEvent.ADDED); // letting the observer know it was just added
        if(lastException != null) {
            o.onFailed(lastException); // get them up to speed if the last event was a failure
        } else if(lastEvent != null){
            o.on(lastEvent); // get them up to speed
        }
    }

    public void removeObserver(BlockchainPullObserver o) {
        observers.remove(o);
    }

    public HttpUrl baseUrl() {
        return httpClient.baseUrl();
    }

    /**
     * <p>Sends a GET request for this friend's blockchain, and if the request is successful, and if the blockchain that's
     * successfully downloaded is valid, it is saved to disk.
     * <ul>
     *  <li>Observers are notified when a downloaded starts with {@code on(DOWNLOADING)}.</li>
     *  <li>Observers are notified when a download is successful with {@code on(DOWNLOADED)}.</li>
     *  <li>If the request fails, observers are notified with {@code onFailed(Exception e)}.</li>
     *  <li>If the request succeeds, observers are notified when saving starts with {@code on(SAVING}.</li>
     *  <li>If the request succeeds, but the blockchain is invalid, observers are notified with {@code on(SKIPPED)}.</li>
     *  <li>If the request succeeds, and the blockchain is valid, observers are notified with {@code on(SAVED)}.</li>
     * </ul>
     *
     */
    public void pullBlockchain() {
        try {
            notifyObservers(BlockchainPullEvent.DOWNLOADING);

            Blockchain bc = httpClient.create(BlockchainService.class).getBlockchain().execute().body();

            notifyObservers(BlockchainPullEvent.DOWNLOADED);
            notifyObservers(BlockchainPullEvent.SAVING);

            Blockchain ourBc = store.get().orElse(new Blockchain());
            if(shouldSaveBlockchain(bc, ourBc)) {
                store.save(bc);
                notifyObservers(BlockchainPullEvent.SAVED);
            } else {
                notifyObservers(BlockchainPullEvent.SKIPPED);
            }
        } catch (Exception ignored) {
            notifyObserversFailure(ignored);
        }
    }

    private BlockchainPullEvent lastEvent;
    private Exception lastException = null;

    private void notifyObservers(BlockchainPullEvent e) {
        this.lastEvent = e;
        observers.forEach((o) -> o.on(e));
        this.lastException = null; // clear out the last exception
    }

    private void notifyObserversFailure(Exception e) {
        this.lastException = e;
        observers.forEach((o) -> o.onFailed(e));
    }

    // TODO: document
    private boolean shouldSaveBlockchain(Blockchain toBeSaved, Blockchain comparedAgainst) {
        return toBeSaved.isMined() && toBeSaved.isVerified() &&
                comparedAgainst.getLastTransactionId().orElse(0) < toBeSaved.getLastTransactionId().orElse(0);
    }

}
