package org.endeavourhealth.sftpreader.utilities;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class PgpUtil {

    private static final int BUFFER_SIZE = 10000000; //use approx 10MB for buffered streams, as a fair balance between disk IO and memory

    public static void decryptAndVerify(String inputFileName, String publicKey, String secretKey, String secretKeyPassword, String outputFileName) throws IOException, NoSuchProviderException, PGPException, SignatureException
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(inputFileName), BUFFER_SIZE);
             InputStream publicKeyIn = new BufferedInputStream(new ByteArrayInputStream(publicKey.getBytes()));
             InputStream secretKeyIn = new BufferedInputStream(new ByteArrayInputStream(secretKey.getBytes()));
             BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFileName), BUFFER_SIZE); )
        {
            decryptAndVerify(fileIn, fileOut, publicKeyIn, secretKeyIn, secretKeyPassword);
        }
    }

    private static void decryptAndVerify(InputStream fileIn,
                                         OutputStream fileOut,
                                         InputStream publicKeyIn,
                                         InputStream secretKeyIn,
                                         String secretKeyPassword) throws IOException, SignatureException, PGPException, NoSuchProviderException
    {
        fileIn = PGPUtil.getDecoderStream(fileIn);

        KeyFingerPrintCalculator fingerCalc = new JcaKeyFingerprintCalculator();

        PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(fileIn, fingerCalc);
        PGPEncryptedDataList pgpEncryptedDataList;

        Object pgpObject = pgpObjectFactory.nextObject();

        if (pgpObject instanceof PGPEncryptedDataList)
            pgpEncryptedDataList = (PGPEncryptedDataList)pgpObject;
        else
            pgpEncryptedDataList = (PGPEncryptedDataList)pgpObjectFactory.nextObject();

        Iterator<PGPPublicKeyEncryptedData> encryptedDataIterator = pgpEncryptedDataList.getEncryptedDataObjects();
        PGPPrivateKey pgpPrivateKey = null;
        PGPPublicKeyEncryptedData pgpPublicKeyEncryptedData = null;
        PGPSecretKeyRingCollection pgpSecretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(secretKeyIn), new JcaKeyFingerprintCalculator());

        while (pgpPrivateKey == null && encryptedDataIterator.hasNext())
        {
            pgpPublicKeyEncryptedData = encryptedDataIterator.next();

            pgpPrivateKey = findSecretKey(pgpSecretKeyRingCollection, pgpPublicKeyEncryptedData.getKeyID(), secretKeyPassword.toCharArray());
        }

        if (pgpPrivateKey == null)
            throw new IllegalArgumentException("Unable to find secret key to decrypt the message");

        InputStream clear = pgpPublicKeyEncryptedData.getDataStream(new BcPublicKeyDataDecryptorFactory(pgpPrivateKey));

        PGPObjectFactory plainFact = new PGPObjectFactory(clear, fingerCalc);

        Object message;

        PGPOnePassSignatureList onePassSignatureList = null;
        PGPSignatureList signatureList = null;
        PGPCompressedData compressedData;

        message = plainFact.nextObject();
        //ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

        InputStream input = null;

        while (message != null)
        {
            if (message instanceof PGPCompressedData)
            {
                compressedData = (PGPCompressedData) message;
                plainFact = new PGPObjectFactory(compressedData.getDataStream(), fingerCalc);
                message = plainFact.nextObject();
            }

            if (message instanceof PGPLiteralData)
                input = ((PGPLiteralData) message).getInputStream();
                //Streams.pipeAll(((PGPLiteralData) message).getInputStream(), fileOut);  // have to read it and keep it somewhere.
            else if (message instanceof PGPOnePassSignatureList)
                onePassSignatureList = (PGPOnePassSignatureList) message;
            else if (message instanceof PGPSignatureList)
                signatureList = (PGPSignatureList) message;
            else
                throw new PGPException("message unknown message type.");

            message = plainFact.nextObject();
        }

        //actualOutput.close();

        PGPPublicKey publicKey = null;

        //byte[] output = actualOutput.toByteArray();

        if (onePassSignatureList == null || signatureList == null) {

            //if there's no signature to check, just stream all data from the input to the output
            Streams.pipeAll(input, fileOut);

            //EMIS PGP data is encrypted but not signed, so don't throw this
            //throw new PGPException("Poor PGP. Signatures not found.");
        }
        else {

            //if we have to verify the signature, we need to read the intput into a byte[] before writing to our output
            byte[] buffer = new byte[BUFFER_SIZE]; //use the same size array as the buffered streams

            for (int i = 0; i < onePassSignatureList.size(); i++) {

                PGPOnePassSignature ops = onePassSignatureList.get(0);
                PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn), fingerCalc);
                publicKey = pgpRing.getPublicKey(ops.getKeyID());

                if (publicKey != null) {

                    ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);

                    //read blocks of content from the input, updating the signature and then writing to the output
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) > 0) {

                        ops.update(buffer, 0, bytesRead);
                        fileOut.write(buffer, 0, bytesRead);
                    }

                    PGPSignature signature = signatureList.get(i);
                    if (ops.verify(signature)) {
                        Iterator<?> userIds = publicKey.getUserIDs();

                        while (userIds.hasNext()) {
                            String userId = (String) userIds.next();
                        }
                    }
                    else {
                        throw new SignatureException("Signature verification failed");
                    }
                }
            }

            //moved here, from below, so we only check for the signing key if it was signed
            if (publicKey == null) {
                throw new SignatureException("Signature not found");
            }
        }

        //verify() actually calls isIntegrityProtected(), so extra call is redundant
        //if (pgpPublicKeyEncryptedData.isIntegrityProtected() && !pgpPublicKeyEncryptedData.verify())
        if (!pgpPublicKeyEncryptedData.verify()) {
            throw new PGPException("Data is integrity protected but integrity is lost.");
        }
        //moving to apply this check only if the file was signed
        /*else if (publicKey == null)
        {
            throw new SignatureException("Signature not found");
        }*/
        else {
            //fileOut.write(output);
            fileOut.flush();
            fileOut.close();
        }
    }

    private static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException, NoSuchProviderException
    {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null)
            return null;


        return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
    }
}
