package essence.test.api;

import com.google.gson.*;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import essence.test.util.Generate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParsingTests {

    Gson gson = Generate.defaultGson();

    @Nested
    class BlockchainParsing {
        String bcString = "{\n" +
                "  \"currentBlock\": {\n" +
                "    \"id\": 1,\n" +
                "    \"nonce\": \"8101\",\n" +
                "    \"hash\": \"0009fac2d13fee1b7dde3cd64cbb69644bc5e7fc4ee73d95a7a3e1b9a5d181e0\",\n" +
                "    \"transactions\": [\n" +
                "      {\n" +
                "        \"id\": 3,\n" +
                "        \"publicKey\": \"World\",\n" +
                "        \"signature\": \"\",\n" +
                "        \"payload\": \"Hello\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 4,\n" +
                "        \"publicKey\": \"Universe\",\n" +
                "        \"signature\": \"\",\n" +
                "        \"payload\": \"Hi\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 5,\n" +
                "        \"publicKey\": \"Space\",\n" +
                "        \"signature\": \"\",\n" +
                "        \"payload\": \"Howdy\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"previousBlock\": {\n" +
                "      \"id\": 0,\n" +
                "      \"nonce\": \"5107\",\n" +
                "      \"hash\": \"000dfd5314e82e4090b23ea949433ef65f426434690ba46f9dbead0895d2ae73\",\n" +
                "      \"transactions\": [\n" +
                "        {\n" +
                "          \"id\": 0,\n" +
                "          \"publicKey\": \"World\",\n" +
                "          \"signature\": \"\",\n" +
                "          \"payload\": \"Hello\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 1,\n" +
                "          \"publicKey\": \"Universe\",\n" +
                "          \"signature\": \"\",\n" +
                "          \"payload\": \"Hi\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 2,\n" +
                "          \"publicKey\": \"Space\",\n" +
                "          \"signature\": \"\",\n" +
                "          \"payload\": \"Howdy\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"previousBlock\": null\n" +
                "    }\n" +
                "  }\n" +
                "}";

        @Test
        void blockchainToJson() {
            Blockchain bc = Generate.twoBlockBlockchain();
            assertEquals(bcString, gson.toJson(bc));
        }

        @Test
        void blockchainFromJson() {
            Blockchain bc = gson.fromJson(bcString, Blockchain.class);
            //System.out.println(bc.getCurrentBlock().getHash());
            assertEquals(bcString, gson.toJson(bc));
        }

        @Test
        void blockchainFromJsonHasCorrectLastTxId() {
            Blockchain bc = gson.fromJson(bcString, Blockchain.class);
            assertEquals(5, bc.getLastTransactionId().getAsInt());
            assertNotNull(bc.getCurrentBlock().getTransactionsAsMerkleTree());
        }

        @Test
        void blockchainFromJsonHasMerkleTree() {
            Blockchain bc = gson.fromJson(bcString, Blockchain.class);
            assertNotNull(bc.getCurrentBlock().getTransactionsAsMerkleTree());
        }
    }
}
