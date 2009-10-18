// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.ips;

import java.io.ByteArrayOutputStream;

/**
 * @author Matvey Kazakov
 */
public class Base64 {
    private static final int strlen = 51;

    public static String encode(byte[] src) {
        return encode(src, true);
    }

    public static String encode(byte[] src, boolean insertCR) {

        final char[] alphabet =
                {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
                 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
                 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

        int i, t, carry = 0, j;
        i = ((src.length + 2) / 3) * 4 + (insertCR ? src.length / strlen : 0);
        byte[] result = new byte[i];

        for (i = 0, j = 0; i < src.length; i++) {
            t = src[i];
            switch (i % 3) {
                case 0:
                    {
                        result[j++] = (byte) alphabet[(t & 0xff) >> 2];
                        carry = (t & 0x3) << 4;
                        break;
                    }
                case 1:
                    {
                        result[j++] = (byte) alphabet[carry | ((t >> 4) & 0xf)];
                        carry = (src[i] & 0xf) << 2;
                        break;
                    }
                case 2:
                    {
                        result[j++] = (byte) alphabet[carry | ((t & 0xff) >> 6)];
                        result[j++] = (byte) alphabet[t & 0x3f];
                        carry = 0;
                        break;
                    }
            }
            if (insertCR && (((i + 1) % strlen) == 0)) result[j++] = (byte) '\n';
        }
        t = src.length % 3;
        if (t != 0) {
            result[j++] = (byte) alphabet[carry];
        }
        switch (t) {
            case 1:
                {
                    result[j++] = (byte) '=';
                    result[j] = (byte) '=';
                    break;
                }
            case 2:
                {
                    result[j] = (byte) '=';
                }
        }
        return (new String(result));
    }

    public static byte[] decode(StringBuffer s) {
        final int rvs_alphabet[] =
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, // '+','/'
                 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, // '0'-'9','='
                 -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 'A'-'Z'
                 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
                 -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, //'a'-'z'
                 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        ByteArrayOutputStream v = new ByteArrayOutputStream((s.length() * 3) / 4);
        int i, j, carry = 0, t = 0;
        try {
            for (i = 0, j = 0; (i < s.length()) & (t >= -1); i++) {
                t = rvs_alphabet[s.charAt(i)];
                switch (t) {
                    case -1:
                        continue;
                    case -2:
                        break;
                    default:
                        {
                            switch (j) {
                                case 0:
                                    {
                                        carry = (t & 0x3f) << 2;
                                        break;
                                    }
                                case 1:
                                    {
                                        v.write(carry | (t >> 4));
                                        carry = (t & 0xf) << 4;
                                        break;
                                    }
                                case 2:
                                    {
                                        v.write(carry | (t >> 2));
                                        carry = (t & 3) << 6;
                                        break;
                                    }
                                case 3:
                                    {
                                        v.write(carry | t);
                                        break;
                                    }
                            }
                            j = (j + 1) & 3;
                        }
                }
            }
        } catch (Exception e) {
        }

        return v.toByteArray();
    }
}

