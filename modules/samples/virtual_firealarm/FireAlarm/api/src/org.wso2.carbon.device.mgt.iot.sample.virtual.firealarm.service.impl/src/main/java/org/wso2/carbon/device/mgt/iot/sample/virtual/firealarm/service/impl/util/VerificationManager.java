package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception.VirtualFireAlarmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


public class VerificationManager {
    private static final Log log = LogFactory.getLog(VerificationManager.class);

    private static PrivateKey serverPrivateKey;
    private static final String SIGNATURE_ALG = "SHA1withRSA";
    private static final String CIPHER_PADDING = "RSA/ECB/PKCS1Padding";

    private VerificationManager() {

    }

    public void initVerificationManager() {
        serverPrivateKey = retrievePrivateKey(ConfigurationUtil.CA_CERT_ALIAS,
                                              ConfigurationUtil.KEYSTORE_CA_CERT_PRIV_PASSWORD);
    }

    public static PrivateKey retrievePrivateKey(String alias, String password){
        PrivateKey privateKey = null;
        InputStream inputStream = null;
        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance(ConfigurationUtil.getConfigEntry(ConfigurationUtil.CERTIFICATE_KEYSTORE));
            inputStream = new FileInputStream(ConfigurationUtil.getConfigEntry(
                    ConfigurationUtil.PATH_CERTIFICATE_KEYSTORE));

            keyStore.load(inputStream, ConfigurationUtil.getConfigEntry(ConfigurationUtil.CERTIFICATE_KEYSTORE_PASSWORD)
                    .toCharArray());

            privateKey = (PrivateKey) (keyStore.getKey(ConfigurationUtil.getConfigEntry(alias),
                                                       ConfigurationUtil.getConfigEntry(password).toCharArray()));

        } catch (KeyStoreException e) {
            String errorMsg = "Could not load KeyStore of given type in [certificate-config.xml] file." ;
            log.error(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file could not be loaded from path given in [certificate-config.xml] file.";
            log.error(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            log.error(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            log.error(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            log.error(errorMsg, e);
        } catch (KeystoreException e) {
            String errorMsg = "An error occurred whilst trying load Configs for KeyStoreReader";
            log.error(errorMsg, e);
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving CA private key";
            log.error(errorMsg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore input stream", e);
            }
        }

        return privateKey;
    }

    public static PrivateKey getServerPrivateKey() {
        return serverPrivateKey;
    }

    public static String encryptMessage(String message, Key encryptionKey) throws VirtualFireAlarmException {
        Cipher encrypter;
        byte[] cipherData;

        try {
            encrypter = Cipher.getInstance(CIPHER_PADDING);
            encrypter.init(Cipher.ENCRYPT_MODE, encryptionKey);
            cipherData = encrypter.doFinal(message.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + encryptionKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        }

        return Base64.encodeBase64String(cipherData);
    }


    public static String signMessage(String encryptedData, PrivateKey signatureKey) throws VirtualFireAlarmException {

        Signature signature;
        String signedEncodedString;

        try {
            signature = Signature.getInstance(SIGNATURE_ALG);
            signature.initSign(signatureKey);
            signature.update(Base64.decodeBase64(encryptedData));

            byte[] signatureBytes = signature.sign();
            signedEncodedString = Base64.encodeBase64String(signatureBytes);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + signatureKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        }

        return signedEncodedString;
    }


    public static boolean verifySignature(String data, String signedData, PublicKey verificationKey)
            throws VirtualFireAlarmException {

        Signature signature;
        boolean verified;

        try {
            signature = Signature.getInstance(SIGNATURE_ALG);
            signature.initVerify(verificationKey);
            signature.update(Base64.decodeBase64(data));

            verified = signature.verify(Base64.decodeBase64(signedData));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + verificationKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        }

        return verified;
    }


    public static String decryptMessage(String encryptedMessage, Key decryptKey) throws VirtualFireAlarmException {

        Cipher decrypter;
        String decryptedMessage;

        try {

            decrypter = Cipher.getInstance(CIPHER_PADDING);
            decrypter.init(Cipher.DECRYPT_MODE, decryptKey);
            decryptedMessage = new String(decrypter.doFinal(Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + decryptKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        }

        return decryptedMessage;
    }


}
