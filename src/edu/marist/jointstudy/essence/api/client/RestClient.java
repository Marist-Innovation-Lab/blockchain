package edu.marist.jointstudy.essence.api.client;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO: document
public class RestClient {

    /** How long the server should wait after being launched in seconds before asking its friends for their
     * blockchains. */
    private static final int INITIAL_DELAY_SECONDS = 5;

    /** How long the server should wait in seconds between download requests. */
    private static final int DOWNLOAD_PERIOD_SECONDS = 5;

    private List<Friend> friends;

    public RestClient(List<Friend> friends) {
        this.friends = friends;
    }

    // runs a given runnable every so often
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    // TODO: document
    public void start() {
        Runnable task = () -> {
            // pull each friends' blockchain (from server to memory to db)
            friends.forEach(Friend::pullBlockchain);
        };

        // wait initially 2 seconds to try to download copies of the blockchains,
        // then download them every 5 seconds
        exec.scheduleWithFixedDelay(task, INITIAL_DELAY_SECONDS, DOWNLOAD_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops the client's downloads and populates a fresh executor.
     */
    public void stop() {
        exec.shutdown();
        exec = Executors.newScheduledThreadPool(1);
    }
}
