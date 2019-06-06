package com.starchain.sdk;

import org.bouncycastle.util.encoders.Hex;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author cloud
 * @data 2019/6/6 9:21
 **/
public class ByteBufTest {



    public static void main(String[] args) {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte)254);
        buf.putInt(1023);
        //fe000003ff
        System.out.println(Hex.toHexString(buf.array()));
    }
}
