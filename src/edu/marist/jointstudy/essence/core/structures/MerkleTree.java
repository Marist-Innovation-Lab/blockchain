package edu.marist.jointstudy.essence.core.structures;

import edu.marist.jointstudy.essence.Util;
import edu.marist.jointstudy.essence.core.hash.Hashcode;
import edu.marist.jointstudy.essence.core.hash.Hashable;
import edu.marist.jointstudy.essence.core.hash.Hashing;

import java.util.*;

/**
 * TODO: bloom filters?
 * TODO: lookup is only available for Transactions
 *
 * <p>A data structure whose internal nodes store a hashcode of their two children's hashes,
 * and whose leaf nodes store a hashcode of the data to which each points.</p>
 *
 * <dl>
 *     <dt>Merkle Root</dt>
 *     <dd>A <em>merkle root</em> is the hashcode of the root node in a merkle tree.
 *     It acts as a summary of all the underlying data in the tree's leaf nodes.
 *     Any change in one of the leaf nodes, and the merkle root changes dramatically.
 *     This allows easy data integrity verification.</dd>
 *
 *     <dt>Leaf Node</dt>
 *     <dd>A <em>leaf node</em> is a node that contains only a datum and can compute its hashcode.
 *     It does not have children.</dd>
 *
 *     <dt>Internal Node</dt>
 *     <dd>An <em>internal node</em> is a node that references two children nodes.
 *     An internal node does not contain a reference to a datum.
 *     It computes its hashcode using the hashes of its children.</dd>
 *
 *     <dt>Proof of Membership</dt>
 *     <dd>TODO</dd>
 *
 *     <dt>Proof of Nonmembership</dt>
 *     <dd>TODO</dd>
 * </dl>
 *
 * Created by Tom Magnusson on 5/22/2017.
 */
public final class MerkleTree<D extends Hashable> {

    /** Top level node of the tree. Its hashcode is a digest of all the transactions in the tree. */
    private Node<D> root;

    /**
     * <p>Constructs a complete, ordered Merkle Tree whose LEAF nodes contain the list of data in its original order,
     * left to right.
     *
     * <p>Each INTERNAL node contains the hashcode of its two children.
     *
     * <p>Each LEAF node contains the hashcode of its data.
     *
     * @param data a list of data that the MerkleTree stores in its {@code LEAF} nodes.
     */
    public MerkleTree(List<D> data) {
        this.root = Node.constructSubtree(Objects.requireNonNull(data));
    }

    /** Hashcode of the root node represents the structure of all the underlying transactions */
    public Hashcode getMerkleRoot() {
        return root.calculateHash();
    }

    /** @return the top level node of the tree. Its hashcode is a digest of all the transactions in the tree. */
    public Node<D> getRoot() {
        return this.root;
    }

    @Override
    public String toString() {
        return this.getRoot().toString();
    }

    /**
     * <p>Nodes in a merkle tree come in two flavors: {@code LEAF} and {@code INTERNAl}.
     *
     * <p>{@code LEAF} nodes contain a reference to a datum ({@code D}. Its children {@code left} and
     * {@code right} are null. {@code LEAF} nodes are nodes at the lowest level of the tree.</p>
     *
     * <p>{@code INTERNAL} nodes contain references to children, {@code left} and {@code right}. Its {@code datum}
     * is {@code null}. {@code INTERNAL} nodes are all the nodes not at the lowest levels of the tree.</p>
     *
     * @param <D> the type of data the {@code LEAF} nodes reference.
     */
    public static final class Node<D extends Hashable> implements Hashable {

        /**
         * INTERNAL - Contains exactly two children and {@code null} datum.
         * LEAF - Contains a datum and {@code null} children.
         */
        public enum Type {
            INTERNAL, LEAF
        }

        /** Either a {@code LEAF} node or {@code INTERNAL} node.*/
        private Type type;

        /** Left child of an {@code INTERNAL} node. */
        private Node<D> left;

        /** Right child of an {@code INTERNAL} node. */
        private Node<D> right;

        /** The datum of a {@code LEAF} node. */
        private D datum;

        // Instantiate a LEAF
        private Node(D datum) {
            this.type = Type.LEAF;
            this.left = null;
            this.right = null;
            this.datum = Objects.requireNonNull(datum);
        }

        // Instantiate an INTERNAL node with two children
        private Node(Node<D> left, Node<D> right) {
            this.type = Type.INTERNAL;
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
            this.datum = null;
        }

        /**
         * <p>Creates a new leaf node with the given datum.</p>
         *
         * @param datum the datum the leaf references.
         * @param <D> a datum.
         * @return a new {@code LEAF} node.
         */
        public static <D extends Hashable> Node<D> newLeaf(D datum) {
            return new Node<>(datum);
        }

        /**
         * <p>Creates a new internal node with the given children.</p>
         *
         * @param left the left node child
         * @param right the right node child
         * @param <D> a serializable type that the {@code LEAF} nodes reference.
         * @return a new {@code INTERNAL} node.
         */
        public static <D extends Hashable> Node<D> newInternal(Node<D> left, Node<D> right) {
            return new Node<>(left, right);
        }

        /**
         * <p>Constructs a complete, ordered tree whose LEAF nodes contain the list of data in its original order,
         * left to right.
         *
         * <p>More specifically:
         * <ul>
         *   <li>Each leaf node contains one datum in the list of data
         *   <li>The leaves maintain the order of the list passed into the constructor.
         *   <li>The tree produced is complete, meaning:
         *   <ul>
         *     <li>All INTERNAL nodes have two children
         *     <li>All nodes are built as left as possible while still being strictly balanced.
         *     <li>It is strictly balanced, meaning:
         *     <ul>
         *       <li>The balance factor between any two leaf nodes is: -1 <= bf <= 1.
         *       <li>Leaf nodes are not more than one level away from each other.
         *
         * @param data a nonnull, nonemtpy list of data
         * @param <D> the type of data that the LEAF nodes hold.
         * @return a complete, ordered tree
         */
        public static <D extends Hashable> Node<D> constructSubtree(List<D> data) {

            if(Objects.requireNonNull(data).size() == 0) {
                throw new IllegalArgumentException("Data must contain at least one datum. The data given is empty: "
                        + data);
            }

            int size = data.size();

            // easy single case
            if(size == 1) {
                return Node.newLeaf(data.get(0));
            }

            // 2^(log[2](floor(size)))
            // the next closest power of two greater than or equal to the size of the list of data
            int nextPowerOfTwo = (int) Math.pow(2, Math.ceil( Math.log(size) / Math.log(2) ));

            // the amount of nodes that would be needed to be added
            // to the size to make it that next power of two
            int exremainder = nextPowerOfTwo - size;

            // TODO: better name for `whenToStop`
            // the index at which we stop pairing the bottom level nodes
            int whenToStop = size - exremainder;

            List<Node<D>> leaves = new ArrayList<>(exremainder);
            // create all the LEAF nodes
            for(int i = whenToStop; i < size; i++) {
                leaves.add(Node.newLeaf(data.get(i)));
            }
            // TODO: figure out nice `map` function that works with generics (lambdas don't like generics)

            // The capacity is the next lowest power of two.
            // Contains nodes on the second from the bottom level (to create a full "super" tree out of).
            List<Node<D>> penultimateLevel = new ArrayList<>(nextPowerOfTwo / 2);

            // Helpful debug information
            // System.out.println("Construction Iteration:\nSize: " + size + "\nnextPowerOfTwo: "
            // + nextPowerOfTwo + "\nexremainder: " + exremainder + "\nwhenToStop: " + whenToStop + "\n");

            // skip by two to pair all the nodes that are
            // supposed to be on the bottom-most level
            for(int i = 0; i < whenToStop; i += 2) {
                Node<D> left = Node.newLeaf(data.get(i));
                Node<D> right = Node.newLeaf(data.get(i + 1));
                Node<D> internal = Node.newInternal(left, right);
                penultimateLevel.add(internal);
            }

            // add the rest of the leaves to the second from the bottom level
            penultimateLevel.addAll(leaves);
            // postcondition: the size of level is a power of two: |level| = 2^k | k is an element of *N*

            // recursively create the binary tree based on the penultimate level.
            // this is easy because the penultimate level has a size that is a power of two
            return Node.constructFullBinaryTree(penultimateLevel);
        }

        /** @return {@code true} if {@code x} is a power of two, {@code false} otherwise.*/
        private static boolean isPowerOfTwo(int x) {
            return ((double) x ) == Math.pow(2, Math.log(x) / Math.log(2));
        }

        /**
         * Precondition: the list of nodes must have a size of 2^k where k is a natural number.
         *
         * @param nodes, nonnull, nonempty list of nodes having a size of a power of two.
         * @return a full binary tree, whose lowest level contains the list of nodes passed in.
         */
        public static <D extends Hashable> Node<D> constructFullBinaryTree(final List<Node<D>> nodes) {
            if(Objects.requireNonNull(nodes).size() == 0) {
                throw new IllegalArgumentException("Data must contain at least one datum. Nodes given: " + nodes);
            }

            int size = nodes.size();

            if(!isPowerOfTwo(nodes.size())) {
                throw new IllegalArgumentException(
                        "The list of nodes must have a size that is a power of two. Its size is: " + size);
            }

            // Base case: single node
            if(size == 1) {
                return nodes.get(0);
            }

            // halve the list
            int i = size / 2;

            // Recursive case: pair the subtrees
            List<Node<D>> left = nodes.subList(0, i);
            List<Node<D>> right = nodes.subList(i, size);

            return Node.newInternal(
                    Node.constructFullBinaryTree(left),
                    Node.constructFullBinaryTree(right));
        }

        /* ACCESSORS */

        /** Lazy instantiation cache used by {@code getHash()}. */
        private Hashcode hashcode = null;

        /**
         * Lazily computes the hashcode of this node using the following information:
         * <code>
         * <pre>
         * For some cryptographically secure hashing algorithm, H()
         * where || denotes concatenation,
         * for a leaf node,
         *
         * calculateHash() = H(datum)
         *
         * or for an INTERNAL node,
         *
         * calculateHash() = H(left.getHash() || right.getHash())
         *
         * </pre>
         * </code>
         *
         * This method draws from an in-memory cache of the hashcode.
         *
         * @return if this is a LEAF node, the hashcode of its datum. If this is an INTERNAL node,
         * the hashcode of its two children's hashes.
         */
        public Hashcode calculateHash() {
            // check if the cache is empty
            if(hashcode == null) {
                this.hashcode = Hashing.hashFunction.hash(this);
            }
            return hashcode;
        }

        /**
         * <p>Allows a {@code Hashing} type to hashcode this object using the appropriate instance variables.
         *
         * <p>If this node is a {@code LEAF}, the byte array consists of the {@code toByteArray()} of its datum.</p>
         * <p>If this node is {@code INTERNAL}, the byte array consists of the left child's hashcode,
         * followed by the right child's hashcode.</p>
         *
         * @return a byte array of (1) the datum if the node is a {@code LEAF}, or
         * (2) the a byte array of the left child's hashcode and the right child's hashcode,
         * in that order, if the node is {@code INTERNAL}.
         */
        @Override
        public byte[] toByteArray() {
            if(this.getType() == Type.LEAF) {
                return this.getDatum().toByteArray();
            } else {
                return Util.combine(
                        this.getLeft().calculateHash().toByteArray(),
                        this.getRight().calculateHash().toByteArray());
            }
        }

        /**
         * <p>LEAF nodes have no children ({@code null} references to {@code left} and {@code right})
         * and contain a reference to a single datum of type {@code D}.
         * <p>INTERNAL nodes have exactly two children, and their datum reference is {@code null}.
         *
         * @return either LEAF or INTERNAL
         */
        public Type getType() {
            return this.type;
        }

        /**
         * @return the right child node of this node if it is {@code INTERNAL},
         * {@code null} if this node is a {@code LEAF}.
         */
        public Node<D> getRight() {
            return this.right;
        }

        /**
         * @return the left child node of this node if it is {@code INTERNAL},
         * {@code null} if this node is a {@code LEAF}.
         */
        public Node<D> getLeft() {
            return this.left;
        }

        /**
         * @return the datum of this merkle tree if this node is a {@code LEAF},
         * {@code null} if this node is {@code INTERNAL}.
         */
        public D getDatum() {
            return this.datum;
        }

        @Override
        public String toString() {
            if(this.getType() == Type.LEAF) {
                return "Leaf Node: (" + this.getDatum() + ") [" + this.calculateHash() + "]";
            } else {
                // type is INTERNAL
                return "Internal Node [" + this.calculateHash()
                        + "] - Left: \n" + this.getLeft().toString()
                        + "\nRight:\n" + this.getRight().toString();
            }
        }
    }
}
