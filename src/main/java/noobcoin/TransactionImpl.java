package noobcoin;

import noobchain.StringUtil;
import noobchain.Transaction;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TransactionImpl implements Transaction {

    public String transId;  // this is also the hash of the transaction
    public PublicKey sender;  // sender's address / public key
    public PublicKey recipient;  // recipient's address / public key
    public float value;
    public byte[] signature;  // this is to prevent anyone else from spending funds in out wallet

    public List<TransactionInput> inputs;
    public List<TransactionOutput> outputs = new ArrayList<>();

    private static int seq = 0;  // a rough count of how many transactions have been generated

    public TransactionImpl(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash() {
        seq++;  // increase seq to avoid 2 identical transactions having the same hash
        return StringUtil.applySHA256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        value +
                        seq);
    }

    /**
     * Signs all the data we don't want to be tampered with
     *
     * @param privateKey
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        this.signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * Verifies the signed data to ensure it hasn't been tampered with
     *
     * @return
     */
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("Transaction signature failed to verify");
            return false;
        }

        // Gather transaction inputs to ensure they are unspent
        for (TransactionInput input : inputs) {
            input.utxo = NoobCoin.utxos.get(input.transOutputId);
        }

        // Check if transaction is valid
        if (getInputsValue() < NoobCoin.minimumTransactionValue) {
            System.out.println("Transaction inputs too small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs
        float leftover = getInputsValue() - value;  // get value of inputs then the leftover change
        transId = calculateHash();

        // Send value to recipient
        outputs.add(new TransactionOutput(this.recipient, value, transId));

        // Send the leftover "change" back to the sender
        outputs.add(new TransactionOutput(this.sender, leftover, transId));

        // Add outputs to unspent (UTXOs) list
        for (TransactionOutput output : outputs) {
            NoobCoin.utxos.put(output.id, output);
        }

        // Remove transaction inputs from the UTXO list as spent
        for (TransactionInput input : inputs) {
            if (input.utxo == null) {  // if transaction can't be found then skip it
                continue;
            }
            NoobCoin.utxos.remove(input.utxo.id);
        }

        return true;
    }

    /**
     * Get sum of input (UTXO) values
     *
     * @return total
     */
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.utxo == null) {  // if transaction can't be found then skip it
                continue;
            }
            total += input.utxo.value;
        }
        return total;
    }

    /**
     * Get sum of outputs
     *
     * @return total
     */
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput output : outputs) {
            total += output.value;
        }
        return total;
    }
}
