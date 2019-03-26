package com.starchain.sdk.info;

import com.starchain.sdk.cryptography.Base58;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.math.BigDecimal;

/**
 * @author cloud
 * @data 2019/3/26 8:33
 **/
public class DestAddr {
    private String addr;
    private BigDecimal value;
    private byte[] programHash;

    public DestAddr(String addr,BigDecimal value){
        this.addr = addr;
        this.value = value;
        byte[] program = Base58.decode(addr);
        this.programHash = ByteUtils.subArray(program,1,21);
    }

    public DestAddr(){}

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public byte[] getProgramHash() {
        return programHash;
    }

    public void setProgramHash(byte[] programHash) {
        this.programHash = programHash;
    }
}
