package com.starchain.sdk.info;

import java.math.BigDecimal;

/**
 * @author cloud
 * @data 2019/3/25 16:20
 **/
public class Utxo {
    String txid;
    BigDecimal value;
    int index;

    public Utxo(){}

    public Utxo(String txid,BigDecimal value,int index){
        this.txid = txid;
        this.value = value;
        this.index = index;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
