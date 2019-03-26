package com.starchain.sdk;

import com.starchain.sdk.cryptography.Base58;
import com.starchain.sdk.service.ClientService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RpcTest {

	public static void main(String[] args) throws IOException, InterruptedException {

		List<Account> list = new ArrayList<>();
		list.add(new Account(Base58.decode("9nHiGWjNtZkkTWzNgTn4oXvQ2Kpqrvd7FEbxX1BNmqFy")));
		String txid = ClientService.sendStc(list,"SSCvAMDLEZqyYXEfN8KchVsZtxbFEDEeZS","SgqwmJWVusmudpYGAtSzNvkUewV9dDi3HB",BigDecimal.valueOf(5),"dest");
		System.out.println(txid);
	}
}
