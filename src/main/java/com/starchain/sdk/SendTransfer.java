package com.starchain.sdk;


import com.starchain.sdk.cryptography.Digest;
import com.starchain.sdk.cryptography.RipeMD160;
import com.starchain.sdk.data.DataUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class SendTransfer {

	public static String SignTxAndSend (String nodeAPI,String txData,byte[] publicKeyEncoded,byte[] privateKey) {
		
		byte[] sign = Account.signatureData(txData,privateKey);
		String txRawData = Transaction.AddContract(txData , sign , publicKeyEncoded);
		return SendTransactionData(nodeAPI,txRawData);
	}

	public static String signTx(String txData,List<Account> accounts) {
        try {
//            StringBuffer sb = new StringBuffer();
            //去重
            accounts = accounts.stream().distinct().collect(Collectors.toList());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                baos.write(DataUtil.HexStringToByteArray(txData));

            Transaction.writeLong(baos,accounts.size());

//            sb.append(txData).append(DataUtil.numStoreInMemory(Integer.toHexString(accounts.size()),2));
            accounts.sort(new Comparator<Account>() {
                @Override
                public int compare(Account o1, Account o2) {
                    byte[] hash1 = stcHash(DataUtil.HexStringToByteArray(Account.createSignatureScript(o1.publicKeyEncoded)));
                    byte[] hash2 = stcHash(DataUtil.HexStringToByteArray(Account.createSignatureScript(o2.publicKeyEncoded)));
                    for(int i=hash1.length-1;i>=0;i--){
                        Byte a = hash1[i];
                        Byte b = hash2[i];
                        Integer ia = 0xff&a;
                        Integer ib = 0xff&b;
                        int res = ia.compareTo(ib);
                        if(res == 0){
                            continue;
                        }else{
                            return res;
                        }
                    }
                    return 0;
                }
            });
            for(int i = 0;i<accounts.size();i++){
                byte[] sign = Account.signatureData(txData,accounts.get(i).privateKey);
                baos.write(DataUtil.HexStringToByteArray(Transaction.addSign(sign,accounts.get(i).publicKeyEncoded)));
//                sb.append(Transaction.addSign(sign,accounts.get(i).publicKeyEncoded));
            }
    //        return sb.toString();
            return DataUtil.bytesToHexString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

	public static String SendTransactionData(String nodeAPI,final String txRawData) {
		  
	    HttpURLConnection connection = null;
	    try {
            URL url = new URL(nodeAPI+"/api/v1/transaction");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);  
            connection.setDoOutput(true);
            connection.setDoInput(true); 
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Action", "sendrawtransaction");
            jsonObject.put("Version", "1.0.0");
            jsonObject.put("Type", ""); 
            jsonObject.put("Data", txRawData);

            OutputStream os = connection.getOutputStream();  
            os.write(jsonObject.toString().getBytes());  
            os.flush();
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();  
                ByteArrayOutputStream baos = new ByteArrayOutputStream();  
                int len = 0;  
                byte buffer[] = new byte[1024];  
                while ((len = is.read(buffer)) != -1) {  
                    baos.write(buffer, 0, len);  
                }  
                is.close();  
                baos.close();  
                  
                final String result = new String(baos.toByteArray());  
//                System.out.println("result:"+result);
                return result;
            }
        } catch (IOException e) {
	        e.printStackTrace();
	    } catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static byte[] stcHash(byte[] code){
	    byte[] temp = Digest.sha256(code);
	    byte[] rip = RipeMD160.getHash(temp);
	    return rip;
    }

    public static void main(String[] args) {
        System.out.println(DataUtil.bytesToHexString(stcHash(DataUtil.HexStringToByteArray("2103be155f6e168f17caf87d114f93d0f7a9697ec263c186be6479c4cf671f0e1752ac"))));
        System.out.println(DataUtil.bytesToHexString(stcHash(DataUtil.HexStringToByteArray("21026c236a588dd27087ad2b77782e7a370a231361ba66e519ea4330466070378c65ac"))));
        System.out.println(DataUtil.bytesToHexString(stcHash(DataUtil.HexStringToByteArray("21030ba6f3945589ff34c8ad0679ad560ead092b3ab6e2cee3725b157989a0d675feac"))));
        System.out.println(DataUtil.bytesToHexString(stcHash(DataUtil.HexStringToByteArray("21025356576dc183ca3ca859d07af63ad512e452812070b3ac9d2562078941a8b4dfac"))));
        System.out.println(DataUtil.bytesToHexString(stcHash(DataUtil.HexStringToByteArray("2103d90c5ca115e53864bf35fd934acdddb26f6f065e3f92698b42dbdea856541ceaac"))));
    }
}
