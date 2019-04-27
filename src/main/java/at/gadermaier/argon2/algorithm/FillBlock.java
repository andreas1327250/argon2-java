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
        for (int i = 0; i < 128; i+=16) {
            Z.roundFunction(
				i, i + 1, i + 2, i + 3, i + 4, i + 5, i + 6, i + 7, 
				i + 8, i + 9, i + 10, i + 11, i + 12, i + 13, i + 14, i + 15);
        }

        /* Apply Blake2 on rows of 64-bit words: (0,1,16,17,...112,113), then
        (2,3,18,19,...,114,115).. finally (14,15,30,31,...,126,127) */
        for (int i = 0; i < 16; i+=2) {
            Z.roundFunction(
				i, i + 1, i + 16, i + 17, i + 32, i + 33, i + 48, i + 49, 
				i + 64, i + 65, i + 80, i + 81, i + 96, i + 97, i + 112, i + 113);
        }

        if (withXor) {
            currentBlock.xor(R, Z, currentBlock);
        } else {
            currentBlock.xor(R, Z);
        }
    }
}
