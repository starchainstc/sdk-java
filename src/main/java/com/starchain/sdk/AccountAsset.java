package com.starchain.sdk;


import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import com.starchain.sdk.data.BigDecimalUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.starchain.sdk.http.HttpUtils;
import com.starchain.sdk.info.AssetInfo;


public class AccountAsset {
	
	public static AssetInfo[] getUpspent(final String nodeAPI ,final Account account) {		
		  
        HttpURLConnection connection = null;  
        try {  
    		
    		URL url = new URL(nodeAPI+"/api/v1/asset/utxos/"+account.address);
            connection = (HttpURLConnection) url.openConnection();  
            connection.setRequestMethod("GET");  
            connection.setConnectTimeout(8000);  
            connection.setReadTimeout(8000);  
  		            	
    		int statusCode = connection.getResponseCode();

            if (statusCode == 200) {
                InputStream is = connection.getInputStream();  
                String result = HttpUtils.readMyInputStream(is);  
//                System.out.println(result);
                JSONObject object = new JSONObject(result);
	            return AnalyzeCoins(object);
	            
            }else {  
            }  

        } catch (Exception e) {  
            e.printStackTrace();  
  
        } finally {  
                if (connection != null) {  
                    connection.disconnect();  
                }  
        }
        return null;
	}

	protected static AssetInfo[] AnalyzeCoins(JSONObject response) throws JSONException {
		
		JSONArray AssetResult = response.getJSONArray("Result");
		AssetInfo []assetInfo = new AssetInfo[AssetResult.length()];
		
		for (int i = 0 ; i < AssetResult.length() ; i ++) {
			JSONObject assetResultObj =  AssetResult.getJSONObject(i);
			
			JSONArray Utxo = assetResultObj.getJSONArray("Utxo");
			BigDecimal amount = BigDecimal.ZERO;
			for (int j = 0 ; j < Utxo.length() ; j ++) {
				JSONObject utxoObj = Utxo.getJSONObject(j);
				amount = BigDecimalUtil.add(amount,utxoObj.getBigDecimal("Value"));
//				amount = amount + utxoObj.getDouble("Value");
			}
			
			String AssetId = assetResultObj.getString("AssetId");
			String AssetName = assetResultObj.getString("AssetName");
			AssetInfo Asset = new AssetInfo(AssetId,AssetName,amount,Utxo);
			assetInfo[i] = Asset;

		}
		return assetInfo;

		
		
	}
		
}
	