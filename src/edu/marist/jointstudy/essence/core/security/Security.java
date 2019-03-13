package edu.marist.jointstudy.essence.core.security;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public enum Security {
    INSTANCE();

    // https://stackoverflow.com/questions/7224626/how-to-sign-string-with-private-key
    public static void main(String[] args) {
        try {
            String data = "Hello world";

            Security s = Security.INSTANCE;
            String signature = s.sign(data);

            System.out.println("Pub: " + s.getPublicKeyHexadecimal());
            System.out.println("Sig: " + signature);
            System.out.println("Verified: " + s.isVerified(data, s.getPublicKeyHexadecimal(), signature));

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }

    private PrivateKey priv;
    private PublicKey pub;

    public static final String STRING_CHARSET = "UTF-8";
    public static final String PROVIDER = "SUN";
    public static final String RANDOMNESS_AGLO = "SHA1PRNG";
    public static final String KEY_GEN_ALGO = "DSA";
    public static final int KEY_SIZE_BITS = 1024;
    public static final String SIG_ALGO = "SHA1withDSA";

    Security() {
        try {
            // generates key pairs (private, and public)
            // Sun microsystems (originally created Java, bought by Oracle) provides the DSA algorithm out of the box
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_GEN_ALGO, PROVIDER);
            keyGen.initialize(KEY_SIZE_BITS, SecureRandom.getInstance(RANDOMNESS_AGLO, PROVIDER));

            // make the pair
            KeyPair pair = keyGen.generateKeyPair();
            priv = pair.getPrivate();
            pub = pair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Signs the data, returns a hexadecimal representation of the signature created.
     * @param data the string to be signed
     * @return a hexadecimal string of the signature.
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     */
    public String sign(String data) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, SignatureException {
        Signature dsa = Signature.getInstance(SIG_ALGO, PROVIDER);

        // need to give the signature the private key
        dsa.initSign(priv);

        byte[] bytes = data.getBytes(STRING_CHARSET);

        dsa.update(bytes);
        byte[] signature = dsa.sign();

        return DatatypeConverter.printHexBinary(signature);
    }

    /**
     * True iff the public key signed the data with the given signature, false otherwise.
     * @param data the data to be verified
     * @param publicKey a hexadecimal representation of the public key that ostensibly created the signature with the data
     * @param signature a hexadecimal representation of the signature generated with the public key and data to be checked.
     * @return true iff the public key signed the data with the given signature, false otherwise.
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws SignatureException
     */
    public boolean isVerified(String data, String publicKey, String signature) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        try {
            byte[] sigToVerify = DatatypeConverter.parseHexBinary(signature);
            byte[] enPubKey = DatatypeConverter.parseHexBinary(publicKey);

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(enPubKey);

            KeyFactory keyFactory = KeyFactory.getInstance(KEY_GEN_ALGO, PROVIDER);
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            Signature sig = Signature.getInstance(SIG_ALGO, PROVIDER);
            sig.initVerify(pubKey);

            sig.update(data.getBytes(STRING_CHARSET));

            return sig.verify(sigToVerify);
        } catch (InvalidKeySpecException keyEx) {
            // the length for the key is invalid, fine, it's false
            return false;
        }
    }

    public PublicKey getPublicKey() {
        return pub;
    }

    public String getPublicKeyHexadecimal() {
        return DatatypeConverter.printHexBinary(this.getPublicKey().getEncoded()).toLowerCase();
    }
}
