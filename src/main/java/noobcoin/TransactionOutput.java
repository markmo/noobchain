package noobcoin;

import noobchain.StringUtil;

import java.security.PublicKey;

public class TransactionOutput {

    public String id;
    public PublicKey recipient;  // aka new owner of these coins
    public float value;  // amount of coins
    public String parentTransId;  // id of the transaction this output was created in

    public TransactionOutput(PublicKey recipient, float value, String parentTransId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransId = parentTransId;
        this.id = StringUtil.applySHA256(StringUtil.getStringFromKey(recipient) + value + parentTransId);
    }

    /**
     * Check if coin belongs to you
     *
     * @param publicKey
     * @return
     */
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
