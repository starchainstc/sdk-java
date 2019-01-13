package com.starchain.sdk.service;

import com.starchain.sdk.Account;
import com.starchain.sdk.AccountAsset;
import com.starchain.sdk.NodeMsg;
import com.starchain.sdk.SendTransfer;
import com.starchain.sdk.Transaction;
import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.cryptography.ECC;
import com.starchain.sdk.info.AssetInfo;

import java.math.BigDecimal;

public class ClientService {
	
	/**
	 * 创建账户
	 * @return
	 */
	public static Account createAccount() {
        byte[] privateKey = ECC.generateKey();
        Account account = createAccount(privateKey);
        return account;
    }

	/**
	 * 加载账户
	 * @param privateKey
	 * @return
	 */
    public static Account createAccount(byte[] privateKey) {
        Account account = new Account(privateKey);
        return account;
    }
    
    /**
	 * 加载账户
	 * @param privateKey
	 * @return
	 */
    public static Account createAccount(String privateKey) {
        Account account = new Account(Base58.decode(privateKey));
        return account;
    }
    
    /**
     * 获取账户合约列表
     * @param nodeAPI 节点地址
     * @param account 账户信息
     * @return
     */
    public static AssetInfo[] getUpspentList(final String nodeAPI ,final Account account){
    	return AccountAsset.getUpspent(nodeAPI, account);
    }

    /**
     * 获取账户合约信息
     * @param nodeAPI 节点地址
     * @param account 账户信息
     * @return
     */
    public static AssetInfo getUpspentInfo(final String nodeAPI ,final Account account,String assetId){
    	AssetInfo[] assetList = AccountAsset.getUpspent(nodeAPI, account);
    	AssetInfo aInfo = null;
    	if (assetList != null) {
    		for (AssetInfo assetInfo : assetList) {
    			if (assetInfo.getAssetId().equals(assetId)) {
    				aInfo = assetInfo;
    			}
    		}
		}
    	return aInfo;
    }
    
    /**
     * 获取节点高度
     * @param nodeAPI
     * @return
     */
    public static String getNodeHeight(final String nodeAPI ){
    	return NodeMsg.getNodeHeight(nodeAPI);
    }
    
    /**
     * 获取交易体
     * @param Asset 合约信息
     * @param publicKeyEncoded 账户公钥
     * @param toAddress 接收地址
     * @param transferAssetAmount 发送数量
     * @param Desc 备注描述
     * @return
     */
    public static String makeTransferTransaction(AssetInfo Asset, byte[] publicKeyEncoded, String toAddress, BigDecimal transferAssetAmount, String Desc){
    	return Transaction.makeTransferTransaction(Asset, publicKeyEncoded, toAddress, transferAssetAmount, Desc);
    }    	
    
    /**
     * 离线签名并发送事务
     * @param nodeAPI 节点地址
     * @param txData 交易体
     * @param publicKeyEncoded 账户公钥
     * @param privateKey 账户私钥
     * @return
     */
    public static String SignTxAndSend (String nodeAPI,String txData,byte[] publicKeyEncoded,byte[] privateKey){
    	return SendTransfer.SignTxAndSend(nodeAPI, txData, publicKeyEncoded, privateKey);
    }
    
    
}
