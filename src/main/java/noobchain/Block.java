package noobchain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot = "";
    public List<Transaction> transactions = new ArrayList<>();
    private String data = "";  // data will be a simple message
    private long timestamp;  // number ms since 1/1/1970
    private int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();  // do this after setting the other values
    }

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();  // do this after setting the other values
    }

    public String calculateHash() {
        return StringUtil.applySHA256(previousHash + timestamp + nonce + data + merkleRoot);
    }

    /**
     * The `mineBlock` method takes in an int called difficulty, this is the number of 0’s
     * they must solve for. Low difficulty like 1 or 2 can be solved nearly instantly on most
     * computers, i’d suggest something around 4–6 for testing. At the time of writing Litecoin’s
     * difficulty is around 442,592.
     *
     * @param difficulty number 0's that must be solved for
     */
    public void mineBlock(int difficulty) {
        // Create a string with difficulty * "0"
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }

    /**
     * Add a transaction to this block
     *
     * @param trans
     * @return
     */
    public boolean addTransaction(Transaction trans) {
        if (trans == null) {
            return false;
        }
        if (!previousHash.equals("0")) {
            if (!trans.processTransaction()) {
                System.out.println("Transaction failed to process. Transaction discarded!");
                return false;
            }
        }
        transactions.add(trans);
        System.out.println("Transaction successfully added to the block");
        return true;
    }
}
