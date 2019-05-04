package at.gadermaier.argon2.algorithm;

import static at.gadermaier.argon2.Constants.*;

import at.gadermaier.argon2.blake2.Blake2b;

class Functions {


    /**
     * H0 = H64(p, τ, m, t, v, y, |P|, P, |S|, S, |L|, K, |X|, X)
     * -> 64 byte (ARGON2_PREHASH_DIGEST_LENGTH)
     */
    static byte[] initialHash(int lanes, int outputLength,
                              int memory, int iterations,
                              int version, int type,
                              int passwordLength, byte[] password,
                              int saltLength, byte[] salt,
                              int secretLength, byte[] secret,
                              int additionalLength, byte[] additional) {


        Blake2b.Param params = new Blake2b.Param()
                .setDigestLength(ARGON2_PREHASH_DIGEST_LENGTH);

        final Blake2b blake2b = Blake2b.Digest.newInstance(params);

        blake2b.update(lanes);
        blake2b.update(outputLength);
        blake2b.update(memory);
        blake2b.update(iterations);
        blake2b.update(version);
        blake2b.update(type);

        blake2b.update(passwordLength);
        if (password != null) {
            blake2b.update(password);
        }

        blake2b.update(saltLength);
        if (salt != null) {
            blake2b.update(salt);
        }

        blake2b.update(secretLength);
        if (secret != null) {
            blake2b.update(secret);
        }

        blake2b.update(additionalLength);
        if (additional != null) {
            blake2b.update(additional);
        }

        byte[] blake2hash = blake2b.digest();
        assert (blake2hash.length == 64);

        return blake2hash;
    }


    /**
     * H' - blake2bLong - variable length hash function
     */
    static byte[] blake2bLong(byte[] input, int outputLength) {

        assert (input.length == ARGON2_PREHASH_SEED_LENGTH || input.length == ARGON2_BLOCK_SIZE);

        byte[] result = new byte[outputLength];
        int blake2bLength = 64;

        if (outputLength <= blake2bLength) {
            result = blake2b(input, true, outputLength, outputLength);
        } else {
            byte[] outBuffer;

            /* V1 */
            outBuffer = blake2b(input, true, outputLength, blake2bLength);
            System.arraycopy(outBuffer, 0, result, 0, blake2bLength / 2);

            int r = (outputLength / 32) + (outputLength % 32 == 0 ? 0 : 1) - 2;

            int position = blake2bLength / 2;
            for (int i = 2; i <= r; i++, position += blake2bLength / 2) {
                /* V2 to Vr */
                outBuffer = blake2b(outBuffer, false, 0, blake2bLength);
                System.arraycopy(outBuffer, 0, result, position, blake2bLength / 2);
            }

            int lastLength = outputLength - 32 * r;

            /* Vr+1 */
            outBuffer = blake2b(outBuffer, false, 0, lastLength);
            System.arraycopy(outBuffer, 0, result, position, lastLength);
        }

        assert (result.length == outputLength);
        return result;
    }

    private static byte[] blake2b(byte[] input, boolean withLen, int outlen, int outputLength) {
        Blake2b.Param params = new Blake2b.Param()
                .setDigestLength(outputLength);

        final Blake2b blake2b = Blake2b.Digest.newInstance(params);

        if (withLen) {
        	blake2b.update(outlen);
        }

        blake2b.update(input);

        return blake2b.digest();
    }
}
