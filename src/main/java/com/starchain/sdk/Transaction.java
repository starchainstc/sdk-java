package com.starchain.sdk;

import com.starchain.sdk.data.BigDecimalUtil;
import com.starchain.sdk.info.*;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.cryptography.Digest;
import com.starchain.sdk.data.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;


public class Transaction {

	private static Logger log = LoggerFactory.getLogger("transfer");

	/**
	 * Make transfer transaction and get transaction unsigned data.
	 * 发起一个转账交易和获取交易数据（十六进制）。
	 *
	 * 数据格式：
	 * 字节            内容
	 * 1              type ： 80
	 * 1              version  ： 00
	 * 1              交易属性个数：01
	 * 1              交易属性中的用法
	 * 8              交易属性中的数据长度
	 * 数据实际长度     交易属性中的数据
	 * 1              引用交易的输入个数：个数为0时，则无
	 * 32             引用交易的hash：个数为0时，则无
	 * 2              引用交易输出的索引：个数为0时，则无
	 * 1              交易输出类型: 01为全部转账；02位有找零
	 * 32             转账资产ID
	 * 8              转账资产数量
	 * 20             转账资产ProgramHash
	 * 32             找零转账资产ID，仅在交易输出类型为02时有
	 * 8              找零转账资产数量，仅在交易输出类型为02时有
	 * 20             找零转账资产ProgramHash，仅在交易输出类型为02时有
	 * 1              Program长度：0x01
	 * 1              参数长度 parameter
	 * 参数实际长度 	  参数：签名
	 * 1			  代码长度 code
	 * 代码实际长度     代码：公钥
	 *
	 * @param publicKeyEncoded
	 * @param toAddress
	 * @param Desc
	 *
	 * @returns {*} : TxUnsignedData
	 */

	public static String makeTransferTransaction(AssetInfo Asset, byte[] publicKeyEncoded, String toAddress, BigDecimal transferAssetAmount,String Desc){
		
		byte[] ProgramHash = Base58.decode(toAddress);
		try {
			byte[] ProgramHashBuffer = new byte[21];		
			for (int i = 0 ; i < 21; i ++) {
				ProgramHashBuffer[i] = ProgramHash[i];
			}
			byte[] ProgramSha256Buffer = Digest.hash256(ProgramHashBuffer);
			
			byte[] ProgramSha256Buffer_part = new byte[4];
			byte[] ProgramHash_part = new byte[4];
			for(int i = 0 ; i < 4 ; i ++) {
				ProgramSha256Buffer_part[i] = ProgramSha256Buffer[i];
			}
			for(int i = 0 ; i < 4 ; i ++) {
				ProgramHash_part[i] = ProgramHash[i+21];
			}
			if(!DataUtil.bytesToHexString(ProgramSha256Buffer_part).equals(DataUtil.bytesToHexString(ProgramHash_part))) {
				log.error("dest addr is error");
				return "-1";
			}
		} catch (Exception e) {  
			//address verify failed.
            e.printStackTrace();  
            return "-1";
		}
		
		byte[] programHash = new byte[20];
		for(int i = 0 ; i < 20 ; i ++) {
			programHash[i] = ProgramHash[i+1];
		}
		
		String SignatureScript = Account.createSignatureScript(publicKeyEncoded);
		byte[] myProgramHash = Digest.hash160(DataUtil.HexStringToByteArray(SignatureScript));

		//Input Construct
		TransferInputData inputData = makeTransferInputData(Asset,transferAssetAmount);
		if(inputData == null) {
			log.error("parse input data error");
			return "-2";
		}
		
		BigDecimal inputAmount = inputData.getCoin_amount();
		
		//Adjust the accuracy.
		BigDecimal accuracyVal = BigDecimal.valueOf(100000000);
//		BigInteger newOutputAmount = BigDecimalUtil.mul(transferAssetAmount,accuracyVal) ;
		BigInteger newOutputAmount = BigDecimalUtil.mul(transferAssetAmount,accuracyVal).toBigInteger() ;
		BigInteger input = BigDecimalUtil.mul(inputAmount,accuracyVal).toBigInteger();
//		BigInteger newInputAmount = BigDecimalUtil.sub(BigDecimalUtil.mul(inputAmount,accuracyVal) , newOutputAmount);
		BigInteger newInputAmount = input.subtract(newOutputAmount);

		/**
	     * data
	     * @type {string}
	     */
		String type = "80";
		String version = "00";
		//Custom Attributes
		String transactionAttrNum = "01";
		String transactionAttrUsage = "00";
//		String transactionAttrData =  DataUtil.bytesToHexString(Integer.toString((int)(Math.random()*99999999)).getBytes());
		String transactionAttrData =  DataUtil.bytesToHexString(Desc.getBytes());
		String transactionAttrDataLen = DataUtil.prefixInteger(Integer.toHexString(transactionAttrData.length()/2), 2);
		String referenceTransactionData = DataUtil.bytesToHexString(inputData.getData());
		
		String data = type + version + transactionAttrNum + transactionAttrUsage + transactionAttrDataLen+ transactionAttrData + referenceTransactionData ;
		
		//OUTPUT
		String transactionOutputNum = "01";//No change
		String transactionOutputAssetID = DataUtil.bytesToHexString(DataUtil.reverseArray(DataUtil.HexStringToByteArray(Asset.getAssetId())));
		String transactionOutputValue = DataUtil.numStoreInMemory(newOutputAmount.toString(16),16);
		String transactionOutputProgramHash = DataUtil.bytesToHexString(programHash);
		
		if(inputAmount.compareTo(transferAssetAmount) == 0) {
			data =data + transactionOutputNum +  transactionOutputAssetID + transactionOutputValue + transactionOutputProgramHash;
			//System.out.println(data);
		}else {
			transactionOutputNum = "02" ; //Have the change
			//Transfer to someone
			data =data + transactionOutputNum +  transactionOutputAssetID + transactionOutputValue + transactionOutputProgramHash;

			//Change to yourself
			String transactionOutputValue_me  =DataUtil.numStoreInMemory(newInputAmount.toString(16),16);
			String transactionOutputProgramHash_me = DataUtil.bytesToHexString(myProgramHash);
			data = data + transactionOutputAssetID + transactionOutputValue_me + transactionOutputProgramHash_me;
			
		}
		return data;
	}

	public static String makeTransferWithMulti(AssetInfo assetInfo,String to,String change, BigDecimal amount,String desc){
		if(verifyAddr(to)){
			byte[] program = Base58.decode(to);
			byte[] changes = Base58.decode(change);
			byte[] programHash = new byte[20];
			byte[] changeHash = new byte[20];
			for(int i = 0 ; i < 20 ; i ++) {
				programHash[i] = program[i+1];
				changeHash[i] = changes[i+1];
			}
			//Input Construct
			TransferInputData inputData = makeTransferInputData(assetInfo,amount);
			if(inputData == null) {
				log.error("合成输入数据出错");
				return null;
			}
			BigDecimal inputAmount = inputData.getCoin_amount();

			//Adjust the accuracy.
			BigDecimal accuracyVal = BigDecimal.valueOf(100000000);
			BigInteger newOutputAmount = BigDecimalUtil.mul(amount,accuracyVal).toBigInteger() ;
			BigInteger input = BigDecimalUtil.mul(inputAmount,accuracyVal).toBigInteger();
			BigInteger newInputAmount = input.subtract(newOutputAmount);

			/**
			 * data
			 * @type {string}
			 */
			String type = "80";
			String version = "00";
			//Custom Attributes
			String transactionAttrNum = "01";
			String transactionAttrUsage = "00";
			String transactionAttrData =  DataUtil.bytesToHexString(desc.getBytes());
			String transactionAttrDataLen = DataUtil.prefixInteger(Integer.toHexString(transactionAttrData.length()/2), 2);
			String referenceTransactionData = DataUtil.bytesToHexString(inputData.getData());
			String data = type + version + transactionAttrNum + transactionAttrUsage + transactionAttrDataLen+ transactionAttrData + referenceTransactionData ;

			//OUTPUT
			String transactionOutputNum = "01";//No change
			String transactionOutputAssetID = DataUtil.bytesToHexString(DataUtil.reverseArray(DataUtil.HexStringToByteArray(assetInfo.getAssetId())));
			String transactionOutputValue = DataUtil.numStoreInMemory(newOutputAmount.toString(16),16);
			String transactionOutputProgramHash = DataUtil.bytesToHexString(programHash);

			if(inputAmount.compareTo(amount) == 0) {
				data =data + transactionOutputNum +  transactionOutputAssetID + transactionOutputValue + transactionOutputProgramHash;
			}else {
				transactionOutputNum = "02" ; //Have the change
				data =data + transactionOutputNum +  transactionOutputAssetID + transactionOutputValue + transactionOutputProgramHash;
				//Change to yourself
				String transactionOutputValue_me  =DataUtil.numStoreInMemory(newInputAmount.toString(16),16);
				String transactionOutputProgramHash_me = DataUtil.bytesToHexString(changeHash);
				data = data + transactionOutputAssetID + transactionOutputValue_me + transactionOutputProgramHash_me;
			}
			return data;
		}
		return null;
	}

	public static String makeMtoMTransfer(AssetInfo assetInfo, List<DestAddr> addrs,String change,String attribute) throws IOException {
		BigDecimal sum = BigDecimal.ZERO;
		for(DestAddr addr : addrs){
			sum = BigDecimalUtil.add(sum,addr.getValue());
			if(!verifyAddr(addr.getAddr())){
				return "";
			}
		}
		byte[] changes = Base58.decode(change);
		byte[] changeHash = ByteUtils.subArray(changes,1,21);
		//Input Construct
		TransferInputData inputData = makeInputData(assetInfo,sum);
		if(inputData == null) {
			return "";
		}
		BigDecimal inputAmount = inputData.getCoin_amount();

		BigDecimal accuracyVal = BigDecimal.valueOf(100000000);
		//Adjust the accuracy.
		BigInteger changeValue = BigDecimalUtil.mul(BigDecimalUtil.sub(inputAmount,sum),accuracyVal).toBigInteger();

//		BigInteger newOutputAmount = BigDecimalUtil.mul(sum,accuracyVal).toBigInteger() ;
//		BigInteger input = BigDecimalUtil.mul(inputAmount,accuracyVal).toBigInteger();
//		BigInteger newInputAmount = input.subtract(newOutputAmount);


		StringBuffer sb = new StringBuffer();
		sb.append("80").append("00").append("01").append("00");
		String attriNum = DataUtil.numStoreInMemory(String.valueOf(attribute.getBytes().length),2);
		sb.append(attriNum);
		String attriData = DataUtil.bytesToHexString(attribute.getBytes());
		sb.append(attriData);
		String inputStr = DataUtil.bytesToHexString(inputData.getData());
		sb.append(inputStr);

		int outputnumber = addrs.size();
		if(changeValue.compareTo(BigInteger.ZERO)>0) {
			outputnumber++;
		}
		String outputNum = DataUtil.numStoreInMemory(String.valueOf(outputnumber),2);
		sb.append(outputNum);

		String outputAssetId = DataUtil.bytesToHexString(DataUtil.reverseArray(DataUtil.HexStringToByteArray(assetInfo.getAssetId())));

		for(DestAddr addr: addrs) {
			BigInteger outputValue = BigDecimalUtil.mul(addr.getValue(),accuracyVal).toBigInteger();
			sb.append(outputAssetId).append(DataUtil.numStoreInMemory(outputValue.toString(16),16));
			sb.append(DataUtil.bytesToHexString(addr.getProgramHash()));
		}
		//change
		if(changeValue.compareTo(BigInteger.ZERO)>0) {
			sb.append(outputAssetId).append(DataUtil.numStoreInMemory(changeValue.toString(16),16));
			sb.append(DataUtil.bytesToHexString(changeHash));
		}
		return sb.toString();
	}

	private static TransferInputData makeInputData(AssetInfo info,BigDecimal outsum) throws IOException {
		List<Utxo> utxos = info.getUtxos();
		if(utxos != null && utxos.size()>0){
			utxos.sort(new Comparator<Utxo>() {
				@Override
				public int compare(Utxo o1, Utxo o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});

			BigDecimal total = utxos.stream().map(item->{
				return item.getValue();
			}).reduce(BigDecimal.ZERO,BigDecimal::add);

			if(total.compareTo(outsum)<0){
				return null;
			}
			BigDecimal amount = outsum;
			int k = 0;
			while(utxos.get(k).getValue().compareTo(amount)<=0) {
				amount = BigDecimalUtil.sub(amount,utxos.get(k).getValue());
				if (amount.compareTo(BigDecimal.ZERO) <= 0){
					break;
				}
				k = k+1 ;
			}

			TransferLengthData lengthData  = InputDataLength(k);
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			if(lengthData.getlen() ==1) {
				bais.write(DataUtil.HexStringToByteArray(lengthData.getInputNum()));
			}else {
				byte[] firstVal = DataUtil.HexStringToByteArray(lengthData.getfirstVal());
				byte[] inputNum = DataUtil.HexStringToByteArray(lengthData.getInputNum());
				bais.write(firstVal);
				bais.write(inputNum);
			}

			//input coins  programhash
			for( int x = 0 ; x < k+1 ; x++) {
				byte[] txid = DataUtil.reverseArray(DataUtil.HexStringToByteArray(utxos.get(x).getTxid()));
				bais.write(txid);
				bais.write(DataUtil.HexStringToByteArray(DataUtil.numStoreInMemory(Integer.toHexString(utxos.get(x).getIndex()), 4)));
			}

			//calc coin_amount
			BigDecimal balance = BigDecimal.ZERO;
			for(int i = 0 ; i < k+1 ; i ++) {
				balance = BigDecimalUtil.add(balance , utxos.get(i).getValue());
			}

			TransferInputData inputData = new TransferInputData();
			inputData.setCoin_amount(balance);
			inputData.setData(bais.toByteArray());
			return inputData;
		}
		return null;
	}


	
	private static TransferInputData makeTransferInputData(AssetInfo Asset , BigDecimal transferAssetAmount) {
		JSONArray Utxo = Asset.getUtxo();
		BigDecimal[] coin_value = new BigDecimal[Utxo.length()] ;
		String[] coin_txid = new String[Utxo.length()];
		int[] coin_index = new int[Utxo.length()];
		try {
			for (int i = 0 ; i < Utxo.length() ; i ++) {
				JSONObject utxoObj = Utxo.getJSONObject(i);
				coin_value[i] = utxoObj.getBigDecimal("Value");
				coin_txid[i] = utxoObj.getString("Txid");
				coin_index[i] = utxoObj.getInt("Index");
			}
		} catch (Exception e) {  
            e.printStackTrace();  				  
        }

        //排序
	    for (int i = 0 ; i < coin_value.length - 1 ; i++) {
	        for (int j = 0 ; j < coin_value.length - 1 - i ; j++) {
	            if (coin_value[j].compareTo(coin_value[j + 1])>0) {
	            	BigDecimal temp = coin_value[j];
	            	coin_value[j] = coin_value[j + 1];
	            	coin_value[j + 1] = temp;

	            	String temp2 = coin_txid[j];
	            	coin_txid[j] = coin_txid[j+1];
	            	coin_txid[j+1] = temp2;

	            	int temp3 = coin_index[j];
	            	coin_index[j] = coin_index[j+1];
	            	coin_index[j+1] = temp3;
	            }
	        }
	    }
	    
	    BigDecimal sum = BigDecimal.ZERO;
	    for(int i = 0 ; i < coin_value.length ; i ++) {
	    	sum = BigDecimalUtil.add(sum,coin_value[i]);
	    }
		
	    if(sum.compareTo(transferAssetAmount)<0) {
	    	//总余额小于转帐金额
			log.error("balance is not enought");
	    	return null;
	    }
	    
	    BigDecimal amount = transferAssetAmount;
	    int k = 0;
	    while(coin_value[k].compareTo(amount)<=0) {
	    	amount = BigDecimalUtil.sub(amount,coin_value[k]);
	    	if (amount.compareTo(BigDecimal.ZERO) <= 0){
	    		break;
	    	}
	    	k = k+1 ;
	    }    
	    
	    TransferLengthData lengthData  = InputDataLength(k);
	    //coin[0] - coin[k]
	    byte[] data = new byte[lengthData.getlen()+34*(k+1)];
	    //input num
	    int m = 0;
	    if(lengthData.getlen() ==1) {
	    	byte[] inputNum = DataUtil.HexStringToByteArray(lengthData.getInputNum());
	    	for( int i = 0 ; i<inputNum.length ; i++) {
	    		data[i] = inputNum[i];
	    	}
	    	m = inputNum.length;
	    }else {
	    	byte[] firstVal = DataUtil.HexStringToByteArray(lengthData.getfirstVal());
	    	byte[] inputNum = DataUtil.HexStringToByteArray(lengthData.getInputNum());
	    	for(int i = 0 ; i < firstVal.length ; i++) {
	    		data[i] = firstVal[i];
	    	}
	    	m = inputNum.length;
	    	for(int i = 0 ; i<inputNum.length ; i++) {
	    		data[m + i] = inputNum[i];
	    	}
	    	m = m + inputNum.length;
	    }
	    
	    //input coins  programhash
	    for( int x = 0 ; x < k+1 ; x++) {
	    	//txid programhash
	    	int pos = m + (x * 34);
	    	byte[] txid = DataUtil.reverseArray(DataUtil.HexStringToByteArray(coin_txid[x]));
	    	for(int i = 0 ; i < txid.length ; i++) {
	    		data[pos+i] = txid[i];
	    	}
	    	
	    	//index pos
	    	pos = pos + 32;
	    	byte[] index = DataUtil.HexStringToByteArray(DataUtil.numStoreInMemory(Integer.toHexString(coin_index[x]), 4));
	    	for(int i = 0 ; i < index.length ; i++) {
	    		data[pos+i] = index[i];
	    	}
	    }
	    
    	//calc coin_amount
    	BigDecimal balance = BigDecimal.ZERO;
    	for(int i = 0 ; i < k+1 ; i ++) {
    		balance = BigDecimalUtil.add(balance , coin_value[i]);
    	}
	    
    	TransferInputData inputData = new TransferInputData();
    	inputData.setCoin_amount(balance);
    	inputData.setData(data);
    
    	
		return inputData;
		
	}

	private void writeVarUint(OutputStream os,long value){
		byte[] buf = new byte[9];
		int len  = 0;
		if(value < 0xfd){
			buf[0] = (byte)value;
			len = 1;
		}else if( value <= 0xffff){
			buf[0] = Byte.valueOf("fd",16);

		}
	}

	private static TransferLengthData InputDataLength(int orderNum) {
		int firstVal = orderNum+1;
		int len = 0 ;
		int inputNum = orderNum+1;
		String inputNumString;
		
		if(orderNum < 253) { //0xFD
			len = 1;
			 inputNumString = DataUtil.numStoreInMemory(Integer.toHexString(inputNum),2);
		} else if (orderNum < 65535) { //0xFFFF
			firstVal = 253;
			len = 3;
			inputNumString = DataUtil.numStoreInMemory(Integer.toHexString(inputNum),4);
		} else if (orderNum < 4294967295L) { //0xFFFFFFFF
			firstVal = 254;
			len = 5;
			inputNumString = DataUtil.numStoreInMemory(Integer.toHexString(inputNum),8);
		} else {
			firstVal = 255;
			len = 9;
			inputNumString = DataUtil.numStoreInMemory(Integer.toHexString(inputNum),16);
		}
		String firstValString = DataUtil.numStoreInMemory(Integer.toHexString(firstVal),2);
		
		TransferLengthData lengthData = new TransferLengthData( firstValString,len,inputNumString);
		
		return lengthData;

		
	}
	


	/**
	 * Signature structure
	 * 构成签名结构
	 *
	 *  * 数据格式：
	 * 字节            内容
	 * 文本数据长度    文本数据
	 * 1              标识 ： 01
	 * 1              结构长度  ： 41
	 * 1              数据长度  ：40
	 * 40             数据内容
	 * 1              协议数据长度
	 * 脚本数据长度   签名脚本数据
	 *
	 * @param txData
	 * @param sign
	 * @param publicKeyEncoded
	 * @return {string}
	 * @constructor
	 */
	public static String AddContract(String txData, byte[] sign, byte[] publicKeyEncoded) {

		//sign num
		String Num = "01";
		//sign struct len
		String structLen = "41";
		//sign data len
		String dataLen = "40";
		//sign data
		String data = DataUtil.bytesToHexString(sign);
		//Contract data len
		String contractDataLen = "23";
		//script data
		String signatureScript = Account.createSignatureScript(publicKeyEncoded);
		
		return txData + Num + structLen + dataLen + data + contractDataLen + signatureScript;
	}

	public static String addSign(byte[] sign,byte[] publickeyEncode){
		StringBuffer sb = new StringBuffer();
		sb.append("4140").append(DataUtil.bytesToHexString(sign))
				.append("23").append(Account.createSignatureScript(publickeyEncode));
		return sb.toString();
	}


	public static boolean verifyAddr(String addr){
		byte[] ProgramHash = Base58.decode(addr);
		byte[] ProgramHashBuffer = new byte[21];
		for (int i = 0 ; i < 21; i ++) {
			ProgramHashBuffer[i] = ProgramHash[i];
		}
		byte[] ProgramSha256Buffer = Digest.hash256(ProgramHashBuffer);

		byte[] ProgramSha256Buffer_part = new byte[4];
		byte[] ProgramHash_part = new byte[4];
		for(int i = 0 ; i < 4 ; i ++) {
			ProgramSha256Buffer_part[i] = ProgramSha256Buffer[i];
		}
		for(int i = 0 ; i < 4 ; i ++) {
			ProgramHash_part[i] = ProgramHash[i+21];
		}
		if(!DataUtil.bytesToHexString(ProgramSha256Buffer_part).equals(DataUtil.bytesToHexString(ProgramHash_part))) {
			//address verify failed.
			return false;
		}
		return true;
	}
	
}
