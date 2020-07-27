package hudson.plugins.ansicolor.action;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LineIdentifier implements Serializable {
    private static final String ALGORITHM = "SHA-256";
    private static final long serialVersionUID = 1;
    private transient MessageDigest messageDigest;

    private MessageDigest getMessageDigest() {
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance(ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Cannot get message digest", e);
            }
        }
        return messageDigest;
    }

    public String hash(String lineContent, long lineNo) {
        final String key = String.join("|", lineContent, String.valueOf(lineNo));
        return Base64.getEncoder().encodeToString(getMessageDigest().digest(key.getBytes(UTF_8)));
    }

    public boolean isEqual(String lineContent, long lineNo, String other) {
        return hash(lineContent, lineNo).equals(other);
    }
}
