package noobcoin;

public class TransactionInput {

    public String transOutputId;  // reference to TransactionOutputs -> transactionId
    public TransactionOutput utxo;  // contains the unspent transaction output; name is a Bitcoin convention

    public TransactionInput(String transOutputId) {
        this.transOutputId = transOutputId;
    }
}
