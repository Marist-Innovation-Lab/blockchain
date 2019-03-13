package edu.marist.jointstudy.essence.api.client.service;

import edu.marist.jointstudy.essence.core.structures.Blockchain;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Represents getting a blockchain from a peer, parsing it, and getting it into a model class.
 */
public interface BlockchainService {

    @GET("blockchain")
    Call<Blockchain> getBlockchain();

}
