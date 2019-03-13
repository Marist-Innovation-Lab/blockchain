package edu.marist.jointstudy.essence.api.server;

import java.util.logging.Logger;

public enum APIConstants {
    ; // not meant to be instantiated

    private static final Logger LOG = Logger.getLogger(APIConstants.class.getName());

    public static final String apiVersion = "0.01";
    public static final String apiName    = "SecureCloud Blockchain API version " + apiVersion;

    public static void displayStartupInfo(int port) {
        LOG.info("Welcome to the " + apiName + " running on port " + port + ".");
        Runtime rt = Runtime.getRuntime();
        LOG.info(" JVM says Processors: " + rt.availableProcessors());
        LOG.info("  Total memory: " +
                java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(rt.totalMemory()) +
                " bytes.");
        LOG.info("  Free memory: " +
                java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(rt.freeMemory()) +
                " bytes.");
        LOG.info("  Used memory: " +
                java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(
                        rt.totalMemory() - rt.freeMemory()) +
                " bytes.");
    }
}
