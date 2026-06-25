package com.dongah.dispenser.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitUtilities {

    private static final Logger logger = LoggerFactory.getLogger(BitUtilities.class);

    public static int setBit(int value, int i) {
        return value |= 1 << i;
    }

    public static int clearBit(int value, int i) {
        return value &= ~(1 << i);
    }

    public static long setBit(long value, int i) {
        return value |= 1L << i;
    }

    public static long clearBit(long value, int i) {
        return value &= ~(1L << i);
    }

    public static short setBit(short src, int bitNum, boolean value) {
        if (value) return (short) setBit(src, bitNum);
        else return (short) clearBit(src, bitNum);
    }

    public static boolean getBitBoolean(int value, int i) {
        return (value & (1 << i)) != 0;
    }

    public static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3 & 0xff) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) << 8) |
                ((b0 & 0xff)));
    }

    public static int makeInt(byte b2, byte b1, byte b0) {
        return (((b2 & 0xff) << 16) |
                ((b1 & 0xff) << 8) |
                ((b0 & 0xff)));
    }

    public static int makeInt(byte b1, byte b0) {
        return (((b1 & 0xff) << 8) | ((b0 & 0xff)));
    }

    /**
     * hex array -> String
     */
    public static String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }

    public static byte[] ShortToByteArray(short value) {
        byte[] byteArray = new byte[2];
        try {
            byteArray[0] = (byte) ((value >> 8) & 0xff);
            byteArray[1] = (byte) (value & 0xff);
        } catch (Exception e) {
            logger.error("ShortToByteArray :  {}", e.getMessage());
        }
        return byteArray;
    }

    public static short ByteArrayToShort(byte[] value) {
        short newValue = 0;
        try {
            newValue |= (short) (((value[0]) << 8) & 0xff00);
            newValue |= (short) (((value[1])) & 0xff);
        } catch (Exception e) {
            logger.error("ByteArrayToShort :  {}", e.getMessage());
        }
        return newValue;
    }

    public static short ByteArrayToShort(byte value1, byte value2) {
        short newValue = 0;
        try {
            newValue |= (short) (((value1) << 8) & 0xff00);
            newValue |= (short) (((value2)) & 0xff);
        } catch (Exception e) {
            logger.error("ByteArrayToShort_2 :  {}", e.getMessage());
        }
        return newValue;
    }

    public static int ShortToInt(short value1, short value2) {
        int newValue = 0;
        try {
            newValue |= (value1 & 0xFF) << 8;
            newValue |= (value2 & 0xFF);
        } catch (Exception e) {
            logger.error("ShortToInt : {}" ,e.getMessage());
        }
        return newValue;
    }


    /**
     * src1의 특정 비트 영역 + src2의 특정 비트 영역을 잘라서
     * 하나의 byte[]로 이어 붙여 반환
     *
     * srcPos는 MSB 기준 인덱스(0 = 가장 상위 비트, 7 = 가장 하위 비트)
     * copyLength는 추출할 비트 개수
     */
    public static byte[] SplitArrayToConcatByteArray(byte src1, int srcPos1, int copyLength1,
                                                     byte src2, int srcPos2, int copyLength2) {
        // 범위 체크
        if (srcPos1 < 0 || copyLength1 < 1 || srcPos1 + copyLength1 > 8 ||
                srcPos2 < 0 || copyLength2 < 1 || srcPos2 + copyLength2 > 8) {
            throw new IllegalArgumentException("SplitArrayToConcatByteArray Invalid error");
        }

        byte[] result = new byte[copyLength1 + copyLength2];

        // src1에서 비트 추출
        for (int i = 0; i < copyLength1; i++) {
            int bitIndex = srcPos1 + i; // 0~7
            // MSB 기준이므로 (7 - bitIndex)만큼 쉬프트
            byte bit = (byte) ((src1 >> (7 - bitIndex)) & 0x01);
            result[i] = bit;
        }

        // src2에서 비트 추출
        for (int i = 0; i < copyLength2; i++) {
            int bitIndex = srcPos2 + i; // 0~7
            byte bit = (byte) ((src2 >> (7 - bitIndex)) & 0x01);
            result[copyLength1 + i] = bit;
        }

        return result;
    }

    /**
     * src의 특정 비트 영역만 추출해 반환
     **/
    public static byte toByte_XOR(byte src, int srcPos, int copyLength) {
        // 범위 체크
        if (srcPos < 0 || copyLength < 1 || srcPos + copyLength > 8) {
            throw new IllegalArgumentException("toByte_XOR Invalid error");
        }

        byte result = 0;
        for (int i = 0; i < copyLength; i++) {
            int bitIndex = srcPos + i;
            byte bit = (byte)((src >> (7 - bitIndex)) & 0x01);
            result ^= bit;
        }

        return result;
    }

    /**
     * mac address hex
     **/
    public static String toHexString(long value) {
        return String.format(
                "%04X%04X%04X",
                (value >> 32) & 0xFFFF,
                (value >> 16) & 0xFFFF,
                value        & 0xFFFF);
    }
}
