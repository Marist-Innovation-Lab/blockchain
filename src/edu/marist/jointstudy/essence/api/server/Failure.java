package edu.marist.jointstudy.essence.api.server;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.util.logging.Logger;

/**
 * Represents failure response (in its various forms in a REST API).
 *
 * Created by Thomas.Magnusson1 on 7/11/2017.
 */
public class Failure {

    private static Logger LOG = Logger.getLogger(Failure.class.getName());

    // a message for developers, usually including extra technical information
    private String developerMessage;

    // a message designed for display on a GUI for a user
    private String userMessage;

    private NanoHTTPD.Response.IStatus status;

    /**
     * Represents failure, usually from some stupid developer who thinks she or he understands how to program. Key word
     * "thinks."
     *
     * @param status the status of the request (usually 4xx).
     * @param devMessage a developer-friendly message with technical information about the failure.
     * @param userMessage a user-friendly message meant to be displayed directly to the GUI.
     */
    public Failure(
            NanoHTTPD.Response.IStatus status,
            String devMessage,
            String userMessage) {
        this.status = status;
        this.developerMessage = devMessage;
        this.userMessage = userMessage;
    }

    /**
     * Returns a ready-to-return response containing the failure's information.
     *
     * <p>Note: this method also logs a the failure as a warning to the its log.</p>
     *
     * @param gson the json serializer to serialize the failure.
     * @return a json serialized failure response
     */
    public NanoHTTPD.Response response(Gson gson) {
        LOG.warning(gson.toJson(this));
        return BlockchainServer.newApiResponse(this.status, gson.toJson(this));
    }

    // ========== Static Instances of Errors ============

    public static Failure notFound() {
        return new Failure(
                NanoHTTPD.Response.Status.NOT_FOUND,
                "The requested resource cannot be found. Try \"/home\" to see the list of available commands",
                "Doesn't look like we have what you're looking for."
        );
    }

    // Internal IO error

    public static Failure internalIOFailure(Exception e) {
        return new Failure(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "Couldn't access the requested resource.",
                "Something went wrong on our end, please try again later."
        );
    }

    // Tx Lookups

    public static Failure transactionLookup(Exception e, int txId){
        return new Failure(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                "The requested transaction with id " + txId +
                        " cannot be found. Exception: " + e.getMessage(),
                "We couldn't find a transaction with an id matching " +
                        txId + "."
        );
    }

    public static Failure transactionLookupWithBlockId(Exception e, int txId, int blockId) {
        return new Failure(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                "Failed to find the transaction with tx id: " + txId +
                        " from block with block id: " + blockId +
                        ". Exception message: " + e.getMessage(),
                "We couldn't find a transaction with an id matching " +
                        txId + " from a block with id " + blockId + "."
        );
    }

    public static Failure malformedTransactionBody(Exception e) {
        return new Failure(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                "Could not parse the body of the request correctly into a transaction. Check your syntax " +
                        "and make sure it's formatted as such: { \"payload\" : \"hello world\" }\nException: "
                        + e.getMessage(),
                "We couldn't process your transaction."
        );
    }

    // Invalid Block id

    public static Failure invalidBlockId(int blockId) {
        return new Failure(
                NanoHTTPD.Response.Status.BAD_REQUEST,
                "Failed to find the block with block id: " + blockId,
                "We couldn't find a block with an id matching " + blockId
        );
    }
}
