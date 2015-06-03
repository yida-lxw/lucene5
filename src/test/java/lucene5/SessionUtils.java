package lucene5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
/**
 * Session ID生成算法测试
 * @author Administrator
 *
 */
public class SessionUtils {
	private MessageDigest digest;
	private Random random;
	private String entropy;
	private String DEFAULT_ALGORITHM = "MD5";
	protected String randomClass = "java.security.SecureRandom";
	protected static final int SESSION_ID_BYTES = 16;
	
	public static void main(String[] args) {
		SessionUtils sessionUtils = new SessionUtils();
		String sessionID = sessionUtils.generateSessionId();
		for (int i = 0; i < 1000; i++) {
			System.out.println("sessionID-->" + sessionID);
		}
	}
	
	protected synchronized String generateSessionId() {

        // Generate a byte array containing a session identifier
        byte bytes[] = new byte[SESSION_ID_BYTES];
        getRandom().nextBytes(bytes);
        bytes = getDigest().digest(bytes);

        // Render the result as a String of hexadecimal digits
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0x0f);
            if (b1 < 10)
                result.append((char) ('0' + b1));
            else
                result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                result.append((char) ('0' + b2));
            else
                result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());

    }
	
	protected synchronized MessageDigest getDigest() {

        if (this.digest == null) {
            try {
                this.digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
            	this.digest = null;
            }
        }
        return (this.digest);
    }
	
	public void setEntropy(String entropy) {

        this.entropy = entropy;

    }
	
	protected synchronized Random getRandom() {

        if (this.random == null) {
            try {
                Class<?> clazz = Class.forName(randomClass);
                this.random = (Random) clazz.newInstance();
                long seed = System.currentTimeMillis();
                char entropy[] = getEntropy().toCharArray();
                for (int i = 0; i < entropy.length; i++) {
                    long update = ((byte) entropy[i]) << ((i % 8) * 8);
                    seed ^= update;
                }
                this.random.setSeed(seed);
            } catch (Exception e) {
                this.random = new java.util.Random();
            }
        }

        return (this.random);

    }
	
	public String getEntropy() {
        if (this.entropy == null)
            setEntropy(this.toString());

        return (this.entropy);

    }
}
