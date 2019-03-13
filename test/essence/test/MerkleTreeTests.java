package essence.test;

import edu.marist.jointstudy.essence.core.structures.MerkleTree;
import edu.marist.jointstudy.essence.core.structures.Transaction;
import essence.test.util.Generate;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreeTests {

    private MerkleTree<Transaction> tree;
    private MerkleTree.Node<Transaction> root;

    @Nested
    @DisplayName("Empty tree")
    class Empty {
        @Test
        void cantBeInstantiated() {
            assertThrows(IllegalArgumentException.class, () -> new MerkleTree<>(Collections.emptyList()));
        }
    }

    @Nested
    @DisplayName("Tree with one transaction")
    class OneTransaction {

        @BeforeEach
        void setup() {
            tree = new MerkleTree<>(Generate.singleTransactionListWithId(0));
            root = tree.getRoot();
        }

        @Test
        void hasOneNode() {
            assertNotNull(root);
        }

        @Test
        void expectedHash() {
            assertEquals("c6bd12fcfb8529e306bbff93477e16d1e8a45a80e101f824c3678c6eb4875e08",
                    root.calculateHash().toString());
        }

        @Test
        void isLeaf() {
            assertEquals(MerkleTree.Node.Type.LEAF ,root.getType());
        }

        @Test
        void hasNoChildren() {
            assertNull(root.getLeft());
            assertNull(root.getRight());
        }

        @Test
        void containsCorrectDatum() {
            assertEquals(Generate.singleTransactionList().get(0), root.getDatum());
        }

        @Test
        void nodeExpectedHash() {
            assertEquals("c6bd12fcfb8529e306bbff93477e16d1e8a45a80e101f824c3678c6eb4875e08",
                    root.calculateHash().toString());
        }
    }

    /**
     * <pre>
     * <code>
     * Tree:
     *
     *      (root)
     *     /      \
     *    /        \
     * (leaf)   (leaf)
     *    |        |
     *  (tx0)    (tx1)
     * </code>
     * </pre>
     */
    @Nested
    @DisplayName("Tree with two transactions")
    class TwoTransactions {

        @BeforeEach
        void setup() {
            tree = new MerkleTree<>(Generate.twoTransactionListWithIds(0, 1));
            root = tree.getRoot();
        }

        @Test
        void hasRootInternalNode() {
            assertNotNull(root);
            assertEquals(MerkleTree.Node.Type.INTERNAL, root.getType());
            assertNull(root.getDatum());
        }

        @Test
        void rootHasTwoChildren() {
            assertNotNull(root.getLeft());
            assertNotNull(root.getRight());
        }

        @Test
        void childrenAreOrderedLeftToRightAscending() {
            assertEquals(0, root.getLeft().getDatum().getId());
            assertEquals(1, root.getRight().getDatum().getId());
        }
    }

    /**
     * <pre>
     * <code>
     * Tree:
     *
     *           (root)
     *          /      \
     *      (int)    (leaf)
     *     /      \      |
     *    /        \   (tx2)
     * (leaf)   (leaf)
     *    |        |
     *  (tx0)    (tx1)
     * </code>
     * </pre>
     */
    @Nested
    @DisplayName("Tree with three transactions")
    class ThreeTransactions {

        @BeforeEach
        void setup() {
            tree = new MerkleTree<>(Generate.threeTransactionListWithIds(0, 1, 2));
            root = tree.getRoot();
        }

        @Test
        void hasRootInternalNode() {
            assertNotNull(root);
            assertEquals(MerkleTree.Node.Type.INTERNAL, root.getType());
            assertNull(root.getDatum());
        }

        @Test
        void leftIsInternal() {
            assertEquals(MerkleTree.Node.Type.INTERNAL, root.getLeft().getType());
        }

        @Test
        void leftLeftIsLeaf() {
            assertEquals(MerkleTree.Node.Type.LEAF, root.getLeft().getLeft().getType());
        }

        @Test
        void leftLeftHasCorrectTransaction() {
            assertEquals(Generate.threeTransactionListWithIds(0, 1, 2).get(0),
                    root.getLeft().getLeft().getDatum());
        }

        @Test
        void leftRightIsLeaf() {
            assertEquals(MerkleTree.Node.Type.LEAF, root.getLeft().getRight().getType());
        }

        @Test
        void leftRightHasCorrectTransaction() {
            assertEquals(Generate.threeTransactionListWithIds(0, 1, 2).get(1),
                    root.getLeft().getRight().getDatum());
        }

        @Test
        void rightIsLeaf() {
            MerkleTree.Node<Transaction> right = root.getRight();
            assertEquals(MerkleTree.Node.Type.LEAF, right.getType());
            assertNull(right.getLeft());
            assertNull(right.getLeft());
        }

        @Test
        void rightHasCorrectTransaction() {
            assertEquals(Generate.threeTransactionListWithIds(0, 1, 2).get(2),
                    root.getRight().getDatum());
        }
    }

    /**
     * <pre>
     * <code>
     * Tree:
     *                           (root)
     *                        /        \
     *                      /            \
     *               (int0)             (int4)
     *             /         \           /    \
     *          /             \        /       \
     *      (int1)          (int3)  (leaf)  (leaf)
     *     /      \        /    \       |       |
     *    /        \      /      \    (tx4)   (tx5)
     * (leaf)  (leaf)  (leaf)  (leaf)
     *    |       |       |       |
     *  (tx0)   (tx1)   (tx2)   (tx3)
     * </code>
     * </pre>
     */
    @Nested
    @DisplayName("Tree with six transactions")
    class SixTransactions {
        @BeforeEach
        void setup() {
            List<Transaction> sixTransactions =
                    Generate.sixTransactionListWithIds(0, 1, 2, 3 ,4 ,5);
            tree = new MerkleTree<>(sixTransactions);
            root = tree.getRoot();
        }

        @Test
        void hasCorrectTransactionOrdering() {
            // root -> int0 -> int1 -> data from both leaves
            assertEquals(0, root.getLeft().getLeft().getLeft().getDatum().getId());
            assertEquals(1, root.getLeft().getLeft().getRight().getDatum().getId());

            // root -> int0 -> int3 -> data from both leaves
            assertEquals(2, root.getLeft().getRight().getLeft().getDatum().getId());
            assertEquals(3, root.getLeft().getRight().getRight().getDatum().getId());

            // root -> int4 -> data from both leaves
            assertEquals(4, root.getRight().getLeft().getDatum().getId());
            assertEquals(5, root.getRight().getRight().getDatum().getId());
        }

    }

}
