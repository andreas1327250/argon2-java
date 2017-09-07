package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.model.Block;
import at.gadermaier.argon2.model.Instance;

public class Finalize {

    public static void finalize(Instance instance, Argon2 argon2) {

        if (argon2 != null && instance != null) {

            Block blockhash = new Block();
            blockhash.copyBlock(instance.memory[instance.getLaneLength() - 1]);

            /* XOR the last blocks */
            for (int i=1; i<instance.getLanes(); ++i) {
                int last_block_in_lane = i * instance.getLaneLength() + (instance.getLaneLength() - 1);
                blockhash.xorBlock(instance.memory[last_block_in_lane]);
            }

            /* Hash the result */
            byte[] blockhash_bytes = blockhash.toBytes();
            byte[] finalResult = Functions.blake2bLong(blockhash_bytes,
                    argon2.getOutputLength());

            argon2.setOutput(finalResult);

            if(argon2.isClearMemory()) {
                instance.clear();
                argon2.clear();
            }
        }
    }
}
