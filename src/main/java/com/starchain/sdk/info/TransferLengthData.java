package com.starchain.sdk.info;


import java.io.Serializable;

/**
 * 转账数据
 * 
 * @author 12146
 *
 */
public class TransferLengthData implements Serializable {

	private static final long serialVersionUID = -9089152100304450645L;
	private String firstVal;
	private int len = 0;
	private String inputNum;

	public TransferLengthData() {
		super();
	}

	public TransferLengthData(String firstVal, int len, String inputNum) {
		super();
		this.firstVal = firstVal;
		this.len = len;
		this.inputNum = inputNum;
	}

	public void setfirstVal(String firstVal) {
		this.firstVal = firstVal;
	}

	public void setlen(int len) {
		this.len = len;
	}

	public void setInputNum(String inputNum) {
		this.inputNum = inputNum;
	}

	public String getfirstVal() {
		return this.firstVal;
	}

	public int getlen() {
		return this.len;
	}

	public String getInputNum() {
		return this.inputNum;
	}

}