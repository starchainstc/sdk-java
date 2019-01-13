package com.starchain.sdk.info;

import java.io.Serializable;
import java.math.BigDecimal;

import org.json.JSONArray;




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
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public void setbalance(BigDecimal balance) {
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

}