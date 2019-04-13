package noobcoin;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    public PrivateKey privateKey;

    public PublicKey publicKey;

    // UTXO is Unspent Transaction Outputs
    public Map<String, TransactionOutput> utxos = new HashMap<>();  // only UTXOs owned by this wallet

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecspec = new ECGenParameterSpec("prime192v1");

            // Initialise the key generator and generate a KeyPair
            keygen.initialize(ecspec, random);  // 256 bytes provides an acceptable security level
            KeyPair keyPair = keygen.generateKeyPair();

            // Set the public and private keys from the KeyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns balance and stores the UTXO's owned by this wallet in `this.utxos`
     *
     * @return balance
     */
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : NoobCoin.utxos.entrySet()) {
            TransactionOutput utxo = item.getValue();
            if (utxo.isMine(publicKey)) {  // if output (i.e. coins) belong to me
                utxos.put(utxo.id, utxo);  // add it to our list of unspent transactions
                total += utxo.value;
            }
        }
        return total;
    }

    public TransactionImpl sendFunds(PublicKey recipient, float value) {
        if (getBalance() < value) {  // gather balance and check funds
            System.out.println("Not enough funds to send transaction. Transaction discarded!");
            return null;
        }

        // create list of inputs
        List<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : utxos.entrySet()) {
            TransactionOutput utxo = item.getValue();
            total += utxo.value;
            inputs.add(new TransactionInput(utxo.id));
            if (total > value) {
                break;
            }
        }
        TransactionImpl newTransaction = new TransactionImpl(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            utxos.remove(input.transOutputId);
        }

        return newTransaction;
    }
}
