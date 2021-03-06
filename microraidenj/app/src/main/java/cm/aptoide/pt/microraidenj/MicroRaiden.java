package cm.aptoide.pt.microraidenj;

import android.support.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import org.spongycastle.jcajce.provider.digest.Keccak.Digest256;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;

public class MicroRaiden {

  public static final String MY_ADDRESS = "0x";
  private static final String TAG = MicroRaiden.class.getSimpleName();

  private final Web3j web3;
  private final MicroChannel microChannel;

  public MicroRaiden(Web3j web3, MicroChannel microChannel) {
    this.web3 = web3;
    this.microChannel = microChannel;
  }

  private String signMessage(String msg) {
    if (!microChannel.isValid()) {
      throw new IllegalStateException("No valid channelInfo");
    }
    byte[] hex = Utils.encodeHex(msg);
    System.out.println("Signing " + msg);

    try {
      return web3.ethSign(MY_ADDRESS, Utils.sha3Hash(hex))
          .send()
          .getSignature();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Failed to sign " + msg);
    }
  }

  private MicroProof signNewProof(@Nullable MicroProof microProof) {
     /* Ask user for signing a channel balance.
     * Notice it's the final balance, not the increment, and that the new
     * balance is set in channel data in next_*, requiring confirmPayment
     * call to persist it, after successful request.
     * Returns promise to signed data */
    if (!microChannel.isValid()) {
      throw new IllegalStateException("No valid channelInfo");
    }

    System.out.println("signNewProof " + microProof + ", channel" + this.microChannel);
    if (microProof == null) {
      microProof = this.microChannel.getProof();
    }

    if (microProof.getSign() != null) {
      return microProof;
    }

    if ((microProof == this.microChannel.getProof()) && (this.microChannel.getProof() != null)) {
      return microChannel.getProof();
    }

    // FIXME: 29-11-2017 call contract getBalanceMessage with parms to sign the new balance
    return null;
  }

  byte[] signBalance() {
    //# Balance message
    //bytes32 balance_message_hash = keccak256(
    //    keccak256('address receiver', 'uint32 block_created', 'uint192 balance', 'address contract'),
    //    keccak256(_receiver_address, _open_block_number, _balance, address(this))
    //);

    Digest256 keccak256 = new Digest256();
    String receiverAddress = microChannel.getReceiver();
    String openBlockNumber = microChannel.getBlock()
        .toString();
    String balance = Integer.valueOf(0)
        .toString();

    String argumentSignatures = Hex.toHexString(
        digestKeccak("address receiver", "uint32 block_created", "uint192 balance",
            "address contract"));
    String arguments =
        Hex.toHexString(digestKeccak(receiverAddress, openBlockNumber, balance, getAddress()));
    return digestKeccak(argumentSignatures, arguments);
  }

  private String getAddress() {
    return "0x0000000000000000000000000000000000000000";
  }

  public byte[] digestKeccak(String... args) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String arg : args) {
      stringBuilder.append(arg);
    }

    // TODO: 13-12-2017 fix defaultCharset
    return new Digest256().digest(stringBuilder.toString()
        .getBytes(Charset.defaultCharset()));
  }

  MicroProof incrementBalanceAndSign(BigDecimal amount, Runnable callback) {
     /* Ask user for signing a new balance, which is previous balance added
     * of a given amount.
     * Notice that it doesn't replace signed balance proof, but next_* balance
     * proof. You must call confirmPayment with the signature after confirming
     * successful request, to persist it.
     * Returns promise to signed data */
    if (!microChannel.isValid()) {
      throw new IllegalStateException("No valid channelInfo");
    }

    MicroProof proof = microChannel.getProof();
    // get current deposit
    MicroChannelInfo info = getChannelInfo();

    if (!getChannelInfo().getState()
        .equals("opened")) {
      throw new IllegalStateException("Tried signing on closed channel");
    } else if (proof.getBalance()
        .compareTo(getChannelInfo().getDeposit()) == 1) {
      throw new IllegalStateException("Insuficient funds: current = "
          + getChannelInfo().getDeposit()
          + ", required = "
          + proof.getBalance());
    }

    // get hash for new balance proof
    return signNewProof(proof);
  }

  private MicroChannelInfo getChannelInfo() {
    return null;
  }

  private interface MsgParam {

    String getType();

    String getName();

    String getValue();
  }
}