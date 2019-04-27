package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.model.Block;

class FillBlock {
	
	private Block R = new Block();
	private Block Z = new Block();

    void fillBlock(Block X, Block Y, Block currentBlock, boolean withXor) {
        R.xor(X, Y);
        Z.copyFrom(R);

        /* Apply Blake2 on columns of 64-bit words: (0,1,...,15) , then
        (16,17,..31)... finally (112,113,...127) */
        for (int i = 0; i < 8; i++) {
            int i16 = 16 * i;
			Z.roundFunction(
				i16, i16 + 1, i16 + 2, i16 + 3, i16 + 4, i16 + 5, i16 + 6, i16 + 7, 
				i16 + 8, i16 + 9, i16 + 10, i16 + 11, i16 + 12, i16 + 13, i16 + 14, i16 + 15);
        }

        /* Apply Blake2 on rows of 64-bit words: (0,1,16,17,...112,113), then
        (2,3,18,19,...,114,115).. finally (14,15,30,31,...,126,127) */
        for (int i = 0; i < 8; i++) {
            int i2 = 2 * i;
			Z.roundFunction(
				i2, i2 + 1, i2 + 16, i2 + 17, i2 + 32, i2 + 33, i2 + 48, i2 + 49, 
				i2 + 64, i2 + 65, i2 + 80, i2 + 81, i2 + 96, i2 + 97, i2 + 112, i2 + 113);
        }

        if (withXor) {
            currentBlock.xor(R, Z, currentBlock);
        } else {
            currentBlock.xor(R, Z);
        }
    }
}
