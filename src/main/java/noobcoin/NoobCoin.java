package noobcoin;

import noobchain.Block;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoobCoin {

    public static List<Block> blockchain = new ArrayList<>();

    public static Map<String, TransactionOutput> utxos = new HashMap<>();  // list of all unspent transactions

    public static int difficulty = 3;

    public static float minimumTransactionValue = 0.1f;

    public static Wallet walletA;

    public static Wallet walletB;

    public static TransactionImpl genesisTransaction;

    public static void main(String[] args) {
        // Setup BouncyCastle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // Create genesis transaction, which sends 100 NoobCoin to Wallet A
        genesisTransaction = new TransactionImpl(coinbase.publicKey, walletA.publicKey, 100f ,null);
        genesisTransaction.generateSignature(coinbase.privateKey);  // manually sign the genesis transaction
        genesisTransaction.transId = "0";  // manually set the transaction id

        // manually add the transactions output
        genesisTransaction.outputs.add(
                new TransactionOutput(
                        genesisTransaction.recipient,
                        genesisTransaction.value,
                        genesisTransaction.transId));

        // its important to store our first transaction in the UTXO list
        utxos.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and mining genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // Testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("\nWallet A is attempting to send funds (40) to Wallet B...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWallet A is attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWallet B is attempting to send funds (20) to Wallet A...");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("Wallet B's balance is: " + walletB.getBalance());

        isChainValid();
    }

    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        // temporary working list of unspent transactions at a given block state
        Map<String, TransactionOutput> tempUtxos = new HashMap<>();
        tempUtxos.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

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

            // loop through blockchain transactions
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                TransactionImpl currentTrans = (TransactionImpl) currentBlock.transactions.get(t);
                if (!currentTrans.verifySignature()) {
                    System.out.println("Signature on transaction(" + t + ") is invalid!");
                    return false;
                }
                if (currentTrans.getInputsValue() != currentTrans.getOutputsValue()) {
                    System.out.println("Inputs are not equal to outputs on transaction(" + t + ")!");
                    return false;
                }
                for (TransactionInput input : currentTrans.inputs) {
                    tempOutput = tempUtxos.get(input.transOutputId);
                    if (tempOutput == null) {
                        System.out.println("Referenced input on transaction(" + t + ") is missing!");
                        return false;
                    }
                    if (input.utxo.value != tempOutput.value) {
                        System.out.println("Referenced input transaction(" + t + ") value is invalid!");
                        return false;
                    }
                    tempUtxos.remove(input.transOutputId);
                }
                for (TransactionOutput output : currentTrans.outputs) {
                    tempUtxos.put(output.id, output);
                }
                if (currentTrans.outputs.get(0).recipient != currentTrans.recipient) {
                    System.out.println("Transaction(" + t + ") output \"change\" is not sender!");
                    return false;
                }
            }
        }
        System.out.println("Blockchain is valid");

        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
