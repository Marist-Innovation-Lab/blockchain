package edu.marist.jointstudy.essence.api.server;

import fi.iki.elonen.NanoHTTPD;

/**
 * A response to a given resource request from an REST API client.
 */
@FunctionalInterface
public interface ResourceResponse {

    /**
     * @param session the request that was sent.
     * @param ids the ids in order of appearance in the original url. E.g. /blockchain/3/transaction/5 would have an id
     *            array filled with int[] {3, 5}.
     * @return a response to the client, typically with the requested resource or a {@link Failure} error if something
     * went wrong.
     */
    NanoHTTPD.Response respond(NanoHTTPD.IHTTPSession session, int[] ids);

}
