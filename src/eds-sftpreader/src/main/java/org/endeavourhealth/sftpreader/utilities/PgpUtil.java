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

    public static void decryptAndVerify(String inputFileName,
                                        String secretKey,
                                        String secretKeyPassword,
                                        String outputFileName,
                                        String publicKey) throws IOException, NoSuchProviderException, PGPException, SignatureException
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(inputFileName), BUFFER_SIZE);
             InputStream secretKeyIn = new BufferedInputStream(new ByteArrayInputStream(secretKey.getBytes()));
             BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFileName), BUFFER_SIZE);
             InputStream publicKeyStream = new ByteArrayInputStream(publicKey.getBytes()); )
        {
            decryptAndVerify(fileIn, fileOut, secretKeyIn, secretKeyPassword, publicKeyStream);
        }
    }

    private static void decryptAndVerify(InputStream fileIn,
                                         OutputStream fileOut,
                                         InputStream secretKeyIn,
                                         String secretKeyPassword,
                                         InputStream publicKeyStream) throws IOException, SignatureException, PGPException, NoSuchProviderException
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
        //InputStream input = null;
        PGPOnePassSignature ops = null;

        while (message != null) {
            if (message instanceof PGPCompressedData) {
                compressedData = (PGPCompressedData) message;
                plainFact = new PGPObjectFactory(compressedData.getDataStream(), fingerCalc);
                message = plainFact.nextObject();
            }

            if (message instanceof PGPLiteralData) {
                InputStream input = ((PGPLiteralData) message).getInputStream();

                if (onePassSignatureList == null) {
                    //if there's no signature list, just stream to the destination file
                    Streams.pipeAll(input, fileOut);

                } else {
                    //if we've got signed content, we need to generate our signature as we stream our input to disk
                    ops = streamOutAndCalculateSignature(onePassSignatureList, publicKeyStream, fingerCalc, input, fileOut);
                }

                //Streams.pipeAll(((PGPLiteralData) message).getInputStream(), fileOut);  // have to read it and keep it somewhere.
            } else if (message instanceof PGPOnePassSignatureList) {
                onePassSignatureList = (PGPOnePassSignatureList)message;

            } else if (message instanceof PGPSignatureList) {
                signatureList = (PGPSignatureList)message;

                //if we've got a signature list, we now verify the signature we generated
                verifySignature(signatureList, ops);

            } else {
                throw new PGPException("message unknown message type.");
            }

            message = plainFact.nextObject();
        }

        //verify() actually calls isIntegrityProtected(), so extra call is redundant
        //if (pgpPublicKeyEncryptedData.isIntegrityProtected() && !pgpPublicKeyEncryptedData.verify())
        if (!pgpPublicKeyEncryptedData.verify()) {
            throw new PGPException("Data is integrity protected but integrity is lost.");
        }

        //neither of these are probably necessary, since they'll be automatically closed down when
        //they go out of the try block scope, but it can't hurt to do it
        fileOut.flush();
        fileOut.close();
    }



    private static PGPOnePassSignature streamOutAndCalculateSignature(PGPOnePassSignatureList onePassSignatureList, InputStream publicKeyStream,
                                                                      KeyFingerPrintCalculator fingerCalc, InputStream fileInput,
                                                                      OutputStream fileOutput) throws PGPException, IOException, SignatureException {

        PGPOnePassSignature ops = onePassSignatureList.get(0);

        PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyStream), fingerCalc);
        PGPPublicKey publicKey = pgpRing.getPublicKey(ops.getKeyID());
        if (publicKey == null) {
            throw new SignatureException("Signature not found");
        }

        ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);

        //read blocks of content from the input, updating the signature and then writing to the output
        byte[] buffer = new byte[BUFFER_SIZE]; //use the same size array as the buffered streams

        int bytesRead;
        while ((bytesRead = fileInput.read(buffer)) > 0) {

            ops.update(buffer, 0, bytesRead);
            fileOutput.write(buffer, 0, bytesRead);
        }

        return ops;
    }

    private static void verifySignature(PGPSignatureList signatureList, PGPOnePassSignature onePassSignature) throws PGPException, SignatureException {
        PGPSignature signature = signatureList.get(0);
        if (!onePassSignature.verify(signature)) {
            throw new SignatureException("Signature verification failed");
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
