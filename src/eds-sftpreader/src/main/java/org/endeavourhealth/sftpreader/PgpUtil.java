package org.endeavourhealth.sftpreader;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Iterator;

public class PgpUtil
{
    public static void decryptAndVerify(String inputFileName, String publicKeyFileName, String secretKeyFileName, String secretKeyPassword, String outputFileName) throws IOException, NoSuchProviderException, PGPException, SignatureException
    {
        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(inputFileName));
             InputStream publicKeyIn = new BufferedInputStream(new FileInputStream(publicKeyFileName));
             InputStream secretKeyIn = new BufferedInputStream(new FileInputStream(secretKeyFileName));
             OutputStream fileOut = new FileOutputStream(outputFileName);)
        {
            decryptAndVerify(fileIn, fileOut, publicKeyIn, secretKeyIn, secretKeyPassword);
        }
    }

    private static void decryptAndVerify(InputStream fileIn, OutputStream fileOut, InputStream publicKeyIn, InputStream secretKeyIn, String secretKeyPassword) throws IOException, SignatureException, PGPException, NoSuchProviderException
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
        ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

        while (message != null)
        {
            if (message instanceof PGPCompressedData)
            {
                compressedData = (PGPCompressedData) message;
                plainFact = new PGPObjectFactory(compressedData.getDataStream(), fingerCalc);
                message = plainFact.nextObject();
            }

            if (message instanceof PGPLiteralData)
                Streams.pipeAll(((PGPLiteralData) message).getInputStream(), actualOutput);  // have to read it and keep it somewhere.
            else if (message instanceof PGPOnePassSignatureList)
                onePassSignatureList = (PGPOnePassSignatureList) message;
            else if (message instanceof PGPSignatureList)
                signatureList = (PGPSignatureList) message;
            else
                throw new PGPException("message unknown message type.");

            message = plainFact.nextObject();
        }

        actualOutput.close();

        PGPPublicKey publicKey = null;

        byte[] output = actualOutput.toByteArray();

        if (onePassSignatureList == null || signatureList == null)
        {
            throw new PGPException("Poor PGP. Signatures not found.");
        }
        else
        {
            for (int i = 0; i < onePassSignatureList.size(); i++)
            {
                PGPOnePassSignature ops = onePassSignatureList.get(0);
                PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn), fingerCalc);
                publicKey = pgpRing.getPublicKey(ops.getKeyID());

                if (publicKey != null)
                {
                    ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
                    ops.update(output);
                    PGPSignature signature = signatureList.get(i);

                    if (ops.verify(signature))
                    {
                        Iterator<?> userIds = publicKey.getUserIDs();

                        while (userIds.hasNext())
                        {
                            String userId = (String) userIds.next();
                        }
                    }
                    else
                    {
                        throw new SignatureException("Signature verification failed");
                    }
                }
            }
        }

        if (pgpPublicKeyEncryptedData.isIntegrityProtected() && !pgpPublicKeyEncryptedData.verify())
        {
            throw new PGPException("Data is integrity protected but integrity is lost.");
        }
        else if (publicKey == null)
        {
            throw new SignatureException("Signature not found");
        }
        else
        {
            fileOut.write(output);
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
