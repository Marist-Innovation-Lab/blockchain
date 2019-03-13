package edu.marist.jointstudy.essence.api;

import edu.marist.jointstudy.essence.Util;
import edu.marist.jointstudy.essence.api.client.LoggingBlockchainObserver;
import edu.marist.jointstudy.essence.api.store.Preferences;
import javafx.util.Pair;
import okhttp3.HttpUrl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static edu.marist.jointstudy.essence.Util.isPort;

/**
 * The main entry point of the command line program.
 *
 * <p>{@code $ java blockchain [-s] portNumber [friendUrl ...]}</p>
 *
 * <p>Create a sybil node running on 8082 that knows about two nodes running on 127.0.0.1 on ports 8083 and 8084:</p>
 * <pre><code>
 *     $ java blockchain -s 8082 http://127.0.0.1:8083 http://127.0.0.1:8084
 * </code></pre>
 *
 * {@code -s} makes this node a sybil attacker, meaning it will respond to its friends with a fake blockchain.
 *
 * <p>Created by Thomas.Magnusson1 on 6/30/2017.</p>
 */
public class CommandLine {

    public static void main(String[] args) {

        if(!areValidArguments(args)) {
            return;
        }

        Pair<Integer, List<HttpUrl>> prefs = setupPreferences(args);

        Peer peer = new Peer(prefs.getKey(), prefs.getValue());

        // enable logging for each blockchain download for each friend
        peer.friends().forEach((f) ->
                f.addObserver(new LoggingBlockchainObserver("Observing " + f.baseUrl().toString() + ": ")));

        // and away we go...
        peer.start();
    }

    // TODO: document
    public static Pair<Integer, List<HttpUrl>> setupPreferences(String[] args) {
        int thisPort = Integer.valueOf(args[0]);
        boolean isSybil = isSybil(args);
        List<HttpUrl> friendlyUrls = parseFriendUrls(args);

        // add the port, friendlyUrls and whether or not this peer is a sybil peer to preferences
        Preferences.setPort(thisPort);
        Preferences.setIsSybil(isSybil);
        Preferences.setFriendlyUrls(friendlyUrls);

        return new Pair<>(thisPort, friendlyUrls);
    }

    // TODO: document
    public static List<HttpUrl> decodeFriendlyUrlString(String urls) {
        // "http://url.com http://url2.com"
        // -> String[] { "http://url.com", "http://url2.com" }
        // -> HttpUrl[] { new HttpUrl("http://url.com"), new HttpUrl("http://url2.com") }
        // -> List<HttpUrl> { "" }
        return Arrays.stream(urls.split(" ")).map(HttpUrl::parse).collect(Collectors.toList());
    }

    // TODO: document
    public static boolean isSybil(String[] args) {
        if(!(args.length >= 2)) {
            return false; // need at least the second arg.
        }
        String maybeDashS = args[1]; // second argument might be "-s"
        return maybeDashS.equals("-s");
    }

    // TODO: document
    public static boolean areValidArguments(String[] args) {
        // args[0] should be the port the server wants to communicate through
        // args[1...] should be any urls of other peers who might be on the network
        if(args.length < 1) {
            throw new IllegalArgumentException("Expecting at least one argument, " + args.length + " found. The first" +
                    " argument should be the port that this server operates out of." +
                    " The remaining argument is an optional list of \"friendly\" urls that might be on the network.");
        }

        // need a valid port from which to run this server
        if(!isPort(args[0])) {
            // TODO: further restrict ports
            throw new IllegalArgumentException("Expecting the first argument to be a valid int satisfying [0, 65535]." +
                    " The argument was: " + args[0]);
        }

        // If this peer is friendless that's okay, too (we accept all kinds)
        if(args.length == 1) {
            return true;
        }
        // warn developer who started the program if they entered invalid url
        Arrays.stream(Arrays.copyOfRange(args, 1, args.length - 1)).forEachOrdered((arg) -> {
            if(arg.startsWith("-")) {
                return; // found a flag
            }
            if(HttpUrl.parse(arg) == null ) {
                System.err.println("Warning: ignoring an argument that's not a url: " + arg);
            }
        });
        return true;
    }

    // TODO: document
    public static List<HttpUrl> parseFriendUrls(String[] args) {
        return Arrays.stream(args) // String[] -> Stream<String>
                .filter((arg) -> !Util.isPort(arg))      // Remove the port from the beginning
                .map(HttpUrl::parse)                     // String -> HttpUrl (or null if invalid URL)
                .filter(Objects::nonNull)                // Remove invalid URLs
                .collect(Collectors.toList());           // Stream<Url> -> List<Url>
    }

}
