package com.starchain.sdk.info;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 资产信息
 * 
 * @author 12146
 *
 */
public class AssetInfo implements Serializable {
	private static final long serialVersionUID = 4525713704222657287L;
	private String assetId; // 资产编号
	private String assetName; // 资产名称
	private BigDecimal balance; // 资产余额
	private JSONArray Utxo; // 资产区块json
	private List<Utxo> utxos;

	public AssetInfo() {
		super();
	}

	public AssetInfo(String assetId, String assetName, BigDecimal balance,
			JSONArray Utxo) {
		super();
		this.assetId = assetId;
		this.assetName = assetName;
		this.balance = balance;
		this.Utxo = Utxo;
		this.utxos = new ArrayList<>();
		Utxo.forEach(item ->{
			if( item instanceof JSONObject){
				String txid = ((JSONObject) item).getString("Txid");
				BigDecimal value = ((JSONObject) item).getBigDecimal("Value");
				int index = ((JSONObject) item).getIntValue("Index");
				this.utxos.add(new com.starchain.sdk.info.Utxo(txid,value,index));
			}
		});
	}


	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public void setUtxo(JSONArray Utxo) {
		this.Utxo = Utxo;
	}

	public String getAssetId() {
		return this.assetId;
	}

	public String getAssetName() {
		return this.assetName;
	}

	public BigDecimal getbalance() {
		return this.balance;
	}

	public JSONArray getUtxo() {
		return this.Utxo;
	}

	public List<com.starchain.sdk.info.Utxo> getUtxos() {
		return utxos;
	}

	public void setUtxos(List<com.starchain.sdk.info.Utxo> utxos) {
		this.utxos = utxos;
	}
}