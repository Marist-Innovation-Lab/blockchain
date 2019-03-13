package edu.marist.jointstudy.essence.api.store;

import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

public enum Preferences {
    ; // not meant to be instantiated

    /** The port of the server that's running. Default 8081.*/
    private static int port = 8081;

    /** The urls that this peer knows about on the network. */
    private static List<HttpUrl> friendlyUrls = new ArrayList<>();

    /**
     * For testing purposes, whether this peer is a sybil node or not. If isSybil is true, this peer will serve
     * #fakeBlockchains.
     */
    private static boolean isSybil = false;

    public static void setPort(int port) {
        Preferences.port = port;
    }

    public static int getPort() {
        return Preferences.port;
    }

    public static void setFriendlyUrls(List<HttpUrl> urls) {
        Preferences.friendlyUrls = urls;
    }

    public static List<HttpUrl> getFriendlyUrls() {
        return friendlyUrls;
    }

    public static void setIsSybil(boolean isSybil) {
        Preferences.isSybil = isSybil;
    }

    public static boolean isSybil() {
        return Preferences.isSybil;
    }

}
