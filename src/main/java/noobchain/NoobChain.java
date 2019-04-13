package noobchain;

import java.util.ArrayList;
import java.util.List;

public class NoobChain {

    public static List<Block> blockchain = new ArrayList<>();

    public static int difficulty = 5;  // takes around 3 seconds for 3 blocks

    private static void addBlock(String desc) {
        Block newBlock;
        if (blockchain.isEmpty()) {
            newBlock = new Block(desc, "0");
        } else {
            newBlock = new Block(desc, blockchain.get(blockchain.size() - 1).hash);
        }
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        // loop through blockchain to check hashes
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // compare stored hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current hashes not equal");
                return false;
            }

            // compare previous hash and stored previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous hashes not equal");
                return false;
            }

            // check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        /*
	    Block genesisBlock = new Block("Hi, I'm the first block", "0");
	    System.out.println("Hash for block 1: " + genesisBlock.hash);

	    Block secondBlock = new Block("Yo, I'm the second block", genesisBlock.hash);
	    System.out.println("Hash for block 2: " + secondBlock.hash);

	    Block thirdBlock = new Block("Hey, I'm the third block", secondBlock.hash);
	    System.out.println("Hash for block 3: " + thirdBlock.hash);

         */
        long start = System.currentTimeMillis();

        System.out.println("Mining block 1...");
        addBlock("Hi, I'm the first block");

        System.out.println("Mining block 2...");
        addBlock("Yo, I'm the second block");

        System.out.println("Mining block 3...");
        addBlock("Hey, I'm the third block");

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        System.out.println("Chain is valid: " + isChainValid());
        System.out.println("Execution time in ms: " + elapsed);

        String blockchainJson = StringUtil.getJson(blockchain);
        System.out.println(blockchainJson);
    }
}
