package at.gadermaier.argon2.model;


import static at.gadermaier.argon2.Constants.*;

import java.util.Arrays;

import at.gadermaier.argon2.Util;

public final class Block {

    private static final long MASK = 0xFFFFFFFFL;

	private static final int SIZE = ARGON2_QWORDS_IN_BLOCK;
    
	/* 128 * 8 Byte QWords */
    public long[] v;

    public Block() {
        v = new long[SIZE];
    }

    public void fromBytes(byte[] input) {
        assert (input.length == ARGON2_BLOCK_SIZE);

        int offset = 0;
    	long[] v0 = v;
        for (int i = 0; i < SIZE; i++) {
            v0[i] = Util.readLong(input, offset);
            offset += 8;
        }
    }

    public byte[] toBytes() {
        byte[] result = new byte[ARGON2_BLOCK_SIZE];

        int offset = 0;
    	long[] v0 = v;
        for (int i = 0; i < SIZE; i++) {
        	offset = Util.writeLong(result, offset, v0[i]);
        }

        return result;
    }

	public void copyFrom(Block other) {
        System.arraycopy(other.v, 0, v, 0, SIZE);
    }

    public void xor(Block b1, Block b2) {
    	long[] v0 = v;
    	long[] v1 = b1.v;
    	long[] v2 = b2.v;
        for (int i = 0; i < SIZE; i++) {
			v0[i] = v1[i] ^ v2[i];
        }
    }

    public void xor(Block b1, Block b2, Block b3) {
    	long[] v0 = v;
    	long[] v1 = b1.v;
    	long[] v2 = b2.v;
    	long[] v3 = b3.v;
        for (int i = 0; i < SIZE; i++) {
			v0[i] = v1[i] ^ v2[i] ^ v3[i];
        }
    }

    public void xorWith(Block b1) {
    	long[] v0 = v;
    	long[] v1 = b1.v;
        for (int i = 0; i < SIZE; i++) {
			v0[i] ^= v1[i];
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (long value : v) {
            result.append(Util.bytesToHexString(Util.longToLittleEndianBytes(value)));
        }

        return result.toString();
    }

    public void clear() {
        Arrays.fill(v, 0);
    }

	public final void roundFunction(int v0, int v1, int v2, int v3, int v4, int v5,
			int v6, int v7, int v8, int v9, int v10, int v11, int v12, int v13,
			int v14, int v15) {
	    long[] vec = v;
		F(vec, v0, v4, v8, v12);
	    F(vec, v1, v5, v9, v13);
	    F(vec, v2, v6, v10, v14);
	    F(vec, v3, v7, v11, v15);
	
	    F(vec, v0, v5, v10, v15);
	    F(vec, v1, v6, v11, v12);
	    F(vec, v2, v7, v8, v13);
	    F(vec, v3, v4, v9, v14);
	}

	private static void F(long[] v0, int a, int b, int c, int d) {
		fBlaMka(v0, a, b);
	    rotr64(v0, d, a, 32);
	
	    fBlaMka(v0, c, d);
	    rotr64(v0, b, c, 24);
	
	    fBlaMka(v0, a, b);
	    rotr64(v0, d, a, 16);
	
	    fBlaMka(v0, c, d);
	    rotr64(v0, b, c, 63);
	}

    /*designed by the Lyra PHC team */
	/* a <- a + b + 2*aL*bL
	 * + == addition modulo 2^64
	 * aL = least 32 bit */
	private static void fBlaMka(long[] v0, int x, int y) {
		final long xy = (v0[x] & MASK) * (v0[y] & MASK);
	    v0[x] += v0[y] + 2 * xy;
	}

	private static void rotr64(long[] v0, int a, int b, int c) {
		final long xab = v0[a] ^ v0[b];
	    v0[a] = (xab >>> c) | (xab << (64 - c));
	}

}

