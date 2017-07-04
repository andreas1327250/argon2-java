package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.Util;
import at.gadermaier.argon2.Constants;
import at.gadermaier.argon2.blake2.Blake2b;
import at.gadermaier.argon2.model.Block;

import static at.gadermaier.argon2.Constants.ARGON2_PREHASH_DIGEST_LENGTH;
import static at.gadermaier.argon2.Constants.ARGON2_PREHASH_SEED_LENGTH;

public class Functions {

    /* H0
        argon2 -> 64 byte (ARGON2_PREHASH_DIGEST_LENGTH)
                -> 72 byte (ARGON2_PREHASH_SEED_LENGTH)
     */
    public static byte[] initialHash(Argon2 argon2) {

        Blake2b.Param params = new Blake2b.Param()
                .setDigestLength(Constants.ARGON2_PREHASH_DIGEST_LENGTH);

        final Blake2b blake2b = Blake2b.Digest.newInstance(params);

        blake2b.update(Util.intToLittleEndianBytes(argon2.getLanes()));
        blake2b.update(Util.intToLittleEndianBytes(argon2.getOutputLength()));
        blake2b.update(Util.intToLittleEndianBytes(argon2.getMemory()));
        blake2b.update(Util.intToLittleEndianBytes(argon2.getIterations()));
        blake2b.update(Util.intToLittleEndianBytes(argon2.getVersion()));
        blake2b.update(Util.intToLittleEndianBytes(argon2.getType().ordinal()));

        byte[] pwLengthBytes = Util.intToLittleEndianBytes(argon2.getPasswordLength());
        blake2b.update(pwLengthBytes);
        if(argon2.getPassword() != null){
            blake2b.update(argon2.getPassword());
        }

        blake2b.update(Util.intToLittleEndianBytes(argon2.getSaltLength()));
        if(argon2.getSalt() != null){
            blake2b.update(argon2.getSalt());
        }

        blake2b.update(Util.intToLittleEndianBytes(argon2.getSecretLength()));
        if(argon2.getSecret() != null){
            blake2b.update(argon2.getSecret());
        }

        blake2b.update(Util.intToLittleEndianBytes(argon2.getAdditionalLength()));
        if(argon2.getAdditional() != null){
            blake2b.update(argon2.getAdditional());
        }

        byte[] blake2hash = blake2b.digest();
        assert(blake2hash.length == 64);

        byte[] result = new byte[ARGON2_PREHASH_SEED_LENGTH];
        System.arraycopy(blake2hash, 0, result, 0, ARGON2_PREHASH_DIGEST_LENGTH);

        assert(result.length == 72);
        assert(result[64] == 0);
        assert(result[71] == 0);

        return result;
    }


    /* blake2_long - variable length hash function
     * H'
      (H0 || 0 || i) 72 byte -> 1024 byte
    */
    public static byte[] blake2bLong(byte[] input, int outlen) {

        byte[] result = new byte[outlen];
        byte[] outlen_bytes = Util.intToLittleEndianBytes(outlen);

        int blake2BOutbytes = 64;

        if (outlen <= blake2BOutbytes) {
            Blake2b.Param params = new Blake2b.Param()
                    .setDigestLength(outlen);

            final Blake2b blake2b = Blake2b.Digest.newInstance(params);
            blake2b.update(outlen_bytes);
            blake2b.update(input);

            result = blake2b.digest();

        } else {
            int toProduce;
            byte[] outBuffer;
            byte[] inBuffer = new byte[blake2BOutbytes];

            Blake2b.Param params = new Blake2b.Param()
                    .setDigestLength(blake2BOutbytes);

            final Blake2b blake2b = Blake2b.Digest.newInstance(params);

            blake2b.update(outlen_bytes);
            blake2b.update(input);

            outBuffer = blake2b.digest();

            System.arraycopy(outBuffer, 0, result, 0, blake2BOutbytes / 2);

            int position = blake2BOutbytes / 2;
            toProduce = outlen - blake2BOutbytes / 2;

            while (toProduce > blake2BOutbytes) {

                System.arraycopy(outBuffer, 0, inBuffer, 0, blake2BOutbytes);

                outBuffer = blake2b(inBuffer, blake2BOutbytes,
                        null);

                System.arraycopy(outBuffer, 0, result, position, blake2BOutbytes / 2);


                position += blake2BOutbytes / 2;
                toProduce -= blake2BOutbytes / 2;
            }

            System.arraycopy(outBuffer, 0, inBuffer, 0, blake2BOutbytes);

            outBuffer = blake2b(inBuffer, toProduce, null);
            System.arraycopy(outBuffer, 0, result, position, toProduce);
        }

        assert(result.length == outlen);

        return result;
    }

    private static byte[] blake2b(byte[] input, int outlen, byte[] key){

        Blake2b.Param params = new Blake2b.Param()
                .setDigestLength(outlen);

        if(key != null)
            params.setKey(key);

        final Blake2b blake2b = Blake2b.Digest.newInstance(params);
        blake2b.update(input);

        return blake2b.digest();
    }

    public static void roundFunction(Block block,
                                     int v0, int v1, int v2, int v3,
                                     int v4, int v5, int v6, int v7,
                                     int v8, int v9, int v10, int v11,
                                     int v12, int v13, int v14, int v15){

        G(block, v0, v4, v8, v12);
        G(block, v1, v5, v9, v13);
        G(block, v2, v6, v10, v14);
        G(block, v3, v7, v11, v15);

        G(block, v0, v5, v10, v15);
        G(block, v1, v6, v11, v12);
        G(block, v2, v7, v8, v13);
        G(block, v3, v4, v9, v14);
    }

    private static void G_old(Block block, int a, int b, int c, int d) {
        block.v[a] = fBlaMka_old(block.v[a], block.v[b]);
        block.v[d] = rotr64_old(block.v[d], block.v[a], 32);

        block.v[c] = fBlaMka_old(block.v[c], block.v[d]);
        block.v[b] = rotr64_old(block.v[b], block.v[c], 24);

        block.v[a] = fBlaMka_old(block.v[a], block.v[b]);
        block.v[d] = rotr64_old(block.v[d], block.v[a], 16);

        block.v[c] = fBlaMka_old(block.v[c], block.v[d]);
        block.v[b] = rotr64_old(block.v[b], block.v[c], 63);
    }

    private static void G(Block block, int a, int b, int c, int d) {
        fBlaMka(block, a, b);
        rotr64(block, d, a, 32);

        fBlaMka(block, c, d);
        rotr64(block, b, c, 24);

        fBlaMka(block, a, b);
        rotr64(block, d, a, 16);

        fBlaMka(block, c, d);
        rotr64(block, b, c, 63);
    }

    /*designed by the Lyra PHC team */
    /* a <- a + b + 2*aL*bL
     * + == addition modulo 2^64
     * aL = least 32 bit */
    private static void fBlaMka(Block block, int x, int y) {
        final long m = 0xFFFFFFFFL;
        long xy = (block.v[x] & m) * (block.v[y] & m);

        block.v[x] =  block.v[x] + block.v[y] + 2 * xy;
    }

    private static void rotr64(Block block, int v, int w, long c) {
        long temp = block.v[v] ^ block.v[w];
        block.v[v] = (temp >>> c) | (temp << (64 - c));
    }

    private static long fBlaMka_old(long x, long y) {
        long m = 0xFFFFFFFFL;
        long xy = (x & m) * (y & m);

        return x + y + 2 * xy;
    }

    private static long rotr64_old(long v, long w, long c) {
        w = v ^ w;
        return (w >>> c) | (w << (64 - c));
    }

}
