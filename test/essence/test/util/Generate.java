package essence.test.util;

import com.google.gson.Gson;
import edu.marist.jointstudy.essence.api.parse.GsonSingleton;
import edu.marist.jointstudy.essence.core.structures.Blockchain;
import edu.marist.jointstudy.essence.core.structures.Transaction;

import java.util.Arrays;
import java.util.List;

public enum Generate {
    ;

    public static Gson defaultGson() {
        return GsonSingleton.INSTANCE.get();
    }

    public static List<Transaction> singleTransactionList() {
        return Arrays.asList(Transaction.wrapUnsafeNoId("Hello", "World", ""));
    }

    public static List<Transaction> singleTransactionListWithId(int id) {
        return Arrays.asList(Transaction.wrapUnsafe("Hello", "World", "", id));
    }

    public static List<Transaction> twoTransactionListWithIds(int id1, int id2) {
        return Arrays.asList(
                Transaction.wrapUnsafe("Hello", "World", "", id1),
                Transaction.wrapUnsafe("Hi", "Universe", "", id2)
        );
    }

    public static List<Transaction> threeTransactionList() {
        return Arrays.asList(
                Transaction.wrapUnsafeNoId("Hello", "World", ""),
                Transaction.wrapUnsafeNoId("Hi", "Universe", ""),
                Transaction.wrapUnsafeNoId("Howdy", "Space", "")
        );
    }

    public static List<Transaction> threeTransactionListWithIds(int id1, int id2, int id3) {
        return Arrays.asList(
                Transaction.wrapUnsafe("Hello", "World", "", id1),
                Transaction.wrapUnsafe("Hi", "Universe", "", id2),
                Transaction.wrapUnsafe("Howdy", "Space", "", id3)
        );
    }

    public static List<Transaction> sixTransactionListWithIds(int id1, int id2, int id3, int id4, int id5, int id6) {
        return Arrays.asList(
                Transaction.wrapUnsafe("Hello", "World", "", id1),
                Transaction.wrapUnsafe("Hi", "Universe", "", id2),
                Transaction.wrapUnsafe("Howdy", "Space", "", id3),
                Transaction.wrapUnsafe("Hello", "World", "", id4),
                Transaction.wrapUnsafe("Hi", "Universe", "", id5),
                Transaction.wrapUnsafe("Howdy", "Space", "", id6)
        );
    }

    public static Blockchain twoBlockBlockchain() {
        Blockchain bc = new Blockchain();
        bc.add(threeTransactionList());
        bc.add(threeTransactionList());
        return bc;
    }
}
