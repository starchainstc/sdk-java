package com.starchain.sdk;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.starchain.sdk.data.BigDecimalUtil;
import com.starchain.sdk.http.HttpUtils;
import com.starchain.sdk.info.AssetInfo;
import com.starchain.sdk.info.Utxo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class AccountAsset {

	private static Logger log = LoggerFactory.getLogger("asset");
	
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
                JSONObject object = JSON.parseObject(result);
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
		AssetInfo []assetInfo = new AssetInfo[AssetResult.size()];
		
		for (int i = 0 ; i < AssetResult.size() ; i ++) {
			JSONObject assetResultObj =  AssetResult.getJSONObject(i);
			
			JSONArray Utxo = assetResultObj.getJSONArray("Utxo");
			BigDecimal amount = BigDecimal.ZERO;
			for (int j = 0 ; j < Utxo.size() ; j ++) {
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

	public static AssetInfo getAsset(List<String> addrs,String assetId,final String nodeAPI){
		AssetInfo info = new AssetInfo();
		info.setAssetId(assetId);
		List<Utxo> utxos = new ArrayList<>();
		JSONArray arrJson = new JSONArray();
		for(String addr : addrs){
			String res = HttpUtils.get(nodeAPI+"/api/v1/asset/utxo/"+addr+"/"+assetId);
			if(res != null){
				JSONObject json = JSON.parseObject(res);
				int errno = json.getInteger("Error");
				if( errno == 0 && json.get("Result") != null){
					JSONArray arr = json.getJSONArray("Result");
					arr.forEach(item->{
						arrJson.add(item);
						String txid = ((JSONObject) item).getString("Txid");
						BigDecimal value = ((JSONObject) item).getBigDecimal("Value");
						int index = ((JSONObject) item).getInteger("Index");
						utxos.add(new Utxo(txid,value,index));
					});
				}else{
					log.error("地址：{} 余额为0",addr);
					return null;
				}
			}
		}
		info.setUtxo(arrJson);
		info.setUtxos(utxos);
		return info;
	}
		
}
	