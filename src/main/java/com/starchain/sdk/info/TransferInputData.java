package com.starchain.sdk.info;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 转账数据
 * 
 * @author 12146
 *
 */
public class TransferInputData implements Serializable {

	private static final long serialVersionUID = -6771314949334646060L;
	
	private BigDecimal coin_amount;
	private byte[] data;

	public TransferInputData() {
		super();
	}

	public TransferInputData(BigDecimal coin_amount, byte[] data) {
		super();
		this.coin_amount = coin_amount;
		this.data = data;
	}

	public void setCoin_amount(BigDecimal coin_amount) {
		this.coin_amount = coin_amount;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public BigDecimal getCoin_amount() {
		return this.coin_amount;
	}

	public byte[] getData() {
		return this.data;
	}

}