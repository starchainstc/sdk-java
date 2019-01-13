package com.starchain.sdk;

import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.cryptography.ECC;
import com.starchain.sdk.info.AssetInfo;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;

public class RpcTest {

	public static void main(String[] args) {
//		byte[] privateKey = ECC.generateKey();
//		String s = Base58.encode(privateKey);
//		System.out.println(s);
//		System.out.println(Base58.decode(s));

//		byte[] privateKey = Base58.decode("E8UDcvYZNJzm6CQsxkdD8dAZi4Log8pxfrvuYrynYsVj");
		byte[] privateKey = Hex.decode("751e6a1f85a486dd206ee9e6cf8c7b592c876b271be18fec814e49983544d9c1");
		Account account = new Account(privateKey);
		System.out.println(account.address);
		AssetInfo[] assetList = AccountAsset.getUpspent("http://47.52.44.156:25884", account);
		AssetInfo aInfo = null;
		for (AssetInfo assetInfo : assetList) {
			if (assetInfo.getAssetId().equals("4ca6f87e7bfaf1a62545c3ebf6091b3f13ccd249396a27dd8aee0531ba8322cb")) {
				aInfo = assetInfo;
			}
		}
		String txData = Transaction.makeTransferTransaction(aInfo, account.publicKeyEncoded, "SNtx1q6LqqtUXeF8BiNWKzNttWxLGFi6mW", BigDecimal.valueOf(0.202), "测试");
		String txIdString = SendTransfer.SignTxAndSend("http://127.0.0.1:25884",txData, account.publicKeyEncoded, privateKey);
		System.out.println(txIdString);
//		String string = DataUtil.bytesToHexString("测试".getBytes());
//		System.out.println(string);
//		System.out.println(new String(DataUtil.HexStringToByteArray(string)));
//		String hash = "ecf62caa8f9c9d37a91ab53c5fc7dd2b9f090ac01e82e5f069b6d104a4ea3d62";
	}

}
