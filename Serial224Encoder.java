
// encodes every 32 bytes into just 31 bytes and avoids bad serial characters (< 0x20)
// avoided + and 0x7f for serial command purposes
// note it can be any base such that base^8 > 2^62, which means at least 216 characters must be used (we use 222)
public class SerialEncoder224 {
    public static final String TAG = "SerialEncoder224";
    public static int[] table = new int[256];
    public static int[] reverse = new int[256];
    public static int base = 0;
    static {
        for (int i = 0; i < 256; i++) {
            if (i >= 32 && i != '+' && i != 127) table[base++] = i;
        }
        for (int i = 0; i < base; i++) {
            reverse[table[i]] = i;
        }
    }
    public static byte[] encode(byte[] data) {
        int encodedLength = (data.length * 32 + 30) / 31;
        byte[] encoded = new byte[encodedLength];
        int index = 0;

        int shift = 0;
        for (int i = 0; i < data.length || i == data.length && shift > 0; i += 8) {
            long val = 0;
            for (int j = 7; j >= 0; j--) {
                val <<= 8;
                if (i + j < data.length) {
                    val += (data[i + j] & 0xff);
                }
            }
            if (shift > 0) {
                val <<= shift;
                val += (data[i - 1] & 0xff) >> (8 - shift);
            }
            val &= 0x3fffffffffffffffL;

            for (int j = 0; j < 8; j++) {
                long quotient = val / base;
                if (index < encoded.length) {
                    encoded[index++] = (byte) table[(int) (val - quotient * base)];
                }
                val = quotient;
            }

            if (shift == 6) {
                shift = 0;
                i--;
            } else {
                shift += 2;
            }
        }
        return encoded;
    }

    public static byte[] decode(byte[] data) {
        int decodedLength = (data.length * 31) / 32;
        byte[] decoded = new byte[decodedLength];
        int index = 0;

        int shift = 0;
        for (int i = 0; i < data.length; i += 8) {
            long val = 0;
            for (int j = 7; j >= 0; j--) {
                val *= base;
                if (i + j < data.length) {
                    val += reverse[data[i + j] & 0xff];
                }
            }
            if (shift > 0) {
                decoded[index - 1] |= (byte) ((val << (8 - shift)) & 0xff);
                val >>= shift;
            }
            for (int j = 0; j < (shift == 6 ? 7 : 8); j++) {
                if (index < decoded.length) {
                    decoded[index++] = (byte) (val & 0xff);
                }
                val >>= 8;
            }
            if (shift == 6) {
                shift = 0;
            } else {
                shift += 2;
            }
        }

        return decoded;
    }
}
