package com.starchain.sdk;


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


public class SendTransfer {

	public static String SignTxAndSend (String nodeAPI,String txData,byte[] publicKeyEncoded,byte[] privateKey) {
		
		byte[] sign = Account.signatureData(txData,privateKey);
		String txRawData = Transaction.AddContract(txData , sign , publicKeyEncoded);
		return SendTransactionData(nodeAPI,txRawData);

	}

	public static String signTx(String txData,List<Account> accounts){
	    StringBuffer sb = new StringBuffer();
	    sb.append(txData).append(DataUtil.numStoreInMemory(String.valueOf(accounts.size()),2));
	    accounts.sort(new Comparator<Account>() {
            @Override
            public int compare(Account o1, Account o2) {
                for(int i=20;i>=0;i--){
                    if(o1.publicKeyEncoded[i] == o2.publicKeyEncoded[i]){
                        continue;
                    }
                    if(o1.publicKeyEncoded[i] - o2.publicKeyEncoded[i] >0){
                        return -1;
                    }else if(o1.publicKeyEncoded[i] - o2.publicKeyEncoded[i] < 0) {
                        return 1;
                    }
                }
                return 0;
            }
        });
	    for(int i = 0;i<accounts.size();i++){
            byte[] sign = Account.signatureData(txData,accounts.get(i).privateKey);
	        sb.append(Transaction.addSign(sign,accounts.get(i).publicKeyEncoded));
        }
        return sb.toString();
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
}
