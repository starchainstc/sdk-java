package com.starchain.sdk;


import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.cryptography.Digest;
import com.starchain.sdk.cryptography.ECC;
import com.starchain.sdk.data.DataUtil;

import java.math.BigInteger;
import java.util.Arrays;


/**
 * account info, including privatekey/publicKey and publicHash
 */
public class Account {
	/**
	 * privateKey, used for signing transaction
	 */
    public final byte[] privateKey;
    
    /**
     * publickey, used for verifying signature info
     */
    public final ECPoint publicKey;

    public final byte[] publicKeyEncoded;
    
    public final String script;
    
    /**
     * publiekeyHash, used for 用于识别合同所属的帐户
     */
    public final byte[] publicKeyHash;
    
    public final byte[] programHash;
    
    public final String address;

    public Account(byte[] privateKey) {
        if (privateKey.length != 32 && privateKey.length != 96 && privateKey.length != 104) {
        	throw new IllegalArgumentException();
        }
        this.privateKey = new byte[32];
        System.arraycopy(privateKey, privateKey.length - 32, this.privateKey, 0, 32);
        if (privateKey.length == 32) {
            this.publicKey = ECC.secp256r1.getG().multiply(new BigInteger(1, privateKey)).normalize();
        } else {
        	byte[] encoded = new byte[65];
        	encoded[0] = 0x04;
        	System.arraycopy(privateKey, 0, encoded, 1, 64);
            this.publicKey = ECC.secp256r1.getCurve().decodePoint(encoded); 
        }
        this.publicKeyEncoded = publicKey.getEncoded(true);
        
        this.publicKeyHash = Digest.hash160(publicKeyEncoded);
        
        this.script = createSignatureScript(publicKeyEncoded);
        
        this.programHash = Digest.hash160(DataUtil.HexStringToByteArray(script));
        
        this.address = getAddress(programHash);
    }
    
    public static String createSignatureScript (byte[] publicKeyEncoded) {
    	return "21" +DataUtil.bytesToHexString( publicKeyEncoded) + "ac";
    }
    
    public String getAddress(byte[] programHash) {
    	
    	byte[] data = new byte[1+programHash.length];
    	data[0] = 63;
    	for (int i=0 ; i < programHash.length ; i++) {
    		data[i+1] = programHash[i];
    	}
    	
    	byte[] programSha256_2 = Digest.hash256(data);
    	
    	byte[] datas = new byte[1+programHash.length+4];
    	for(int i=0 ; i <data.length ; i++) {
    		datas[i] = data[i];
    	}
    	for(int i = 0 ; i < 4 ; i++) {
    		datas[i+21] = programSha256_2[i];
    	}    	
    	return Base58.encode(datas);
    }

    /**
     * 签名事务无符号数据
     *
     * @param String txData
	 * @param byte[] privateKey
	 * 
 	 * @return byte[] signature
     */
	public static byte[] signatureData(String txData, byte[] privateKey) {
	   ECDSASigner signer = new ECDSASigner();
	   signer.init(true, new ECPrivateKeyParameters(new BigInteger(1, privateKey), ECC.secp256r1));
	   BigInteger[] bi = signer.generateSignature(Digest.sha256(DataUtil.HexStringToByteArray(txData)));// dna
	   byte[] signature = new byte[64];
	   System.arraycopy(BigIntegers.asUnsignedByteArray(32, bi[0]), 0, signature, 0, 32);
	   System.arraycopy(BigIntegers.asUnsignedByteArray(32, bi[1]), 0, signature, 32, 32);
	   return signature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Account account = (Account) o;
		return Arrays.equals(privateKey, account.privateKey);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(privateKey);
	}
}
