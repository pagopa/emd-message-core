package it.gov.pagopa.common.utils;

import it.gov.pagopa.message.core.exception.custom.EmdEncryptionException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class Utils {

    private Utils(){}
    public static String createSHA256(String fiscalCode)  {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = md.digest(fiscalCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Something went wrong creating SHA256");
            throw new EmdEncryptionException("Something went wrong creating SHA256",true,e);
        }
    }
}
