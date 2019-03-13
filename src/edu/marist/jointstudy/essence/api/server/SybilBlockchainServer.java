package edu.marist.jointstudy.essence.api.server;

import edu.marist.jointstudy.essence.api.store.Store;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SybilBlockchainServer extends BlockchainServer {

    public SybilBlockchainServer(int port, List<HttpUrl> friendlyUrls, Store<Blockchain> store) {
        super(port, friendlyUrls, store);
        // keep generating fake transactions... wasting cpu but demonstrating creating another branch
        generateTransactions();
    }

    private void generateTransactions() {
        ExecutorService service = Executors.newSingleThreadScheduledExecutor();
        Future<?> infiniteMining = service.submit(() -> {
            BigInteger counter = BigInteger.ZERO;
            while(true) {
                super.submitTransactionToBuffer(
                        Transaction.wrapUnsafeNoId(counter.toString(), "fake", "transaction"));
                super.mineBlockchain();
                counter = counter.add(BigInteger.ONE);
            }
        });
    }

    /** Simulate no transactions to be mined. */
    @Override
    public boolean mineBlockchain() throws IOException {
        return false;
    }

    /** Simulate malformed transaction body for all submitted transactions. */
    @Override
    protected Response createTx(IHTTPSession s, int[] ids) {
        return this.newApiResponse(Failure.malformedTransactionBody(new Exception()));
    }

    /** Don't allow anyone to clear the buffer, always show it's "cleared". */
    @Override
    protected Response clearBuffer(IHTTPSession session, int[] ids) {
        return this.newApiResponse(Collections.emptyList());
    }
}
