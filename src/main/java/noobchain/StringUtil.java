package noobchain;

import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class StringUtil {

    // Applies SHA256 to a string and returns the result
    public static String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();  // hash as hexadecimal
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies ECDSA Signature and returns the result (as bytes).
     *
     * @param privateKey
     * @param input
     * @return
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * Verifies a string signature.
     *
     * @param publicKey
     * @param data
     * @param signature
     * @return
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper to turn an object into a JSON string
     *
     * @param obj to convert
     * @return json representation
     */
    public static String getJson(Object obj) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(obj);
    }

    /**
     * Returns difficulty string target to compare with hash.
     *
     * For example, difficulty of 5 will return "00000".
     *
     * @param difficulty number 0's that must be solved for
     * @return difficulty string target
     */
    public static String getDifficultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Tacks in array of transactions and returns a merkle root.
     *
     * A Merkle root is the hash of all the hashes of all the transactions
     * that are part of a block in a blockchain network.
     *
     * A Merkle Tree is also referred to as a Binary Hash Tree.
     *
     * Each transaction is hashed, then each pair of transactions is concatenated
     * and hashed together, and so on until there is one hash for the entire block.
     * (If there is an odd number of transactions, one transaction is doubled and
     * its hash is concatenated with itself.)
     *
     * The Merkle root is combined with other information (the software version, the
     * previous block's hash, the timestamp, the difficulty target and the nonce) and
     * then run through a hash function to produce the block's unique hash. This hash
     * is not actually included in the relevant block, but the next one; it is distinct
     * from the Merkle root.
     *
     * @param transactions
     * @return
     */
    public static String getMerkleRoot(List<Transaction> transactions) {
        int count = transactions.size();
        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction trans : transactions) {
            previousTreeLayer.add(trans.transId);
        }
        List<String> treeLayer = previousTreeLayer;
        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(applySHA256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}
