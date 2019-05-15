package com.starchain.sdk;
import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.cryptography.ECC;
import com.starchain.sdk.info.AssetInfo;
import com.starchain.sdk.service.ClientService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RpcTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        Account acc1 = new Account(Base58.decode("MhyrF91vM5QNQSThi3iW1Y2sXDqLwtQm4s5DuRA1GZi"));
        Account acc2 = new Account(Base58.decode("FvyheEFLYTLFXG6G1Ch1aLxZvs6bn2CLA8eQB3FjuVPL"));
        Account acc3 = new Account(Base58.decode("vtRYaaKUpSBcagU9CGnSme572voN7L6dasd8Zti7yWF"));
        Account acc4 = new Account(Base58.decode("4o1TyR6LeVKSstpx1bUkbdB8mmrrFhpVzkmobs9upWtB"));
        Account acc5 = new Account(Base58.decode("5nTNUQ8eGkCpsos1LJQsoKXr5yQA5i7WsFwykJdsCwQr"));
        String txid = ClientService.sendStc(Arrays.asList( acc1,acc2,acc3,acc4,acc5), "Sj5umn1P2XKTcNvL3Cmw1Dv52Uzsh3aYVZ", "SWdJAbR5caRvHd2hdkha4yBGDrixCWuLXA", BigDecimal.valueOf(2), "test", true);
        System.out.println(txid);


//        List<String> addrs1 = new ArrayList<>();
//        addrs1.add("ScteTm85EQdxKcbXoQGxdKkib8seJoKWZM");
//        addrs1.add("Se1cLWmZeU7jAncW2DFVKPLMyTfcMBrcCy");
//        AssetInfo info1 = AccountAsset.getAsset(addrs1, "4ca6f87e7bfaf1a62545c3ebf6091b3f13ccd249396a27dd8aee0531ba8322cb", "http://api.starchain.one");
//        info1.getUtxo().remove(1);
//        info1.getUtxos().remove(1);
//        String txData1 = Transaction.makeTransferWithMulti(info1, "SRFM1wxWVgRmQ75BBhcE7hJ7k6vjrhrTok", "Sazrd3BWvqHeV17vpWrqNSn2NtVqxJavNk", new BigDecimal("2.85"), "1", true);
//        //        Account acc1 = new Account();
//        List<Account> accountList1 = new ArrayList<>();
//        Account account1 = new Account(org.bouncycastle.util.encoders.Hex.decode("1f746efc2e4c5b7f17bfd9a2c38bb2c7c8ace7725a853e6b92ea19bc061c5171"));
//        Account account2 = new Account(org.bouncycastle.util.encoders.Hex.decode("ade4b2218f7744275a04e7d32f608fbe211e9936b2254e95f33ea01eb5112661"));
//        System.out.println(account1.address);
//        System.out.println(account2.address);
//        accountList1.add(account1);
//        accountList1.add(account2);
//        String rawData1 = SendTransfer.signTx(txData1, accountList1);
//        System.out.println(rawData1);
//        String result = SendTransfer.SendTransactionData("http://api.starchain.one", rawData1);
//        System.out.println(result);
    }
}