package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.Util;
import at.gadermaier.argon2.model.Instance;

import static at.gadermaier.argon2.Constants.*;

public class Initialize {

    public static void initialize(Instance instance, Argon2 argon2){
        byte[] initialHash = Functions.initialHash(argon2);
        fillFirstBlocks(instance, initialHash);
    }

    private static void fillFirstBlocks(Instance instance, byte[] initialHash) {

        byte[] blockhash_bytes;

        for (int i=0;i<instance.getLanes();i++) {

            byte[] iBytes = Util.intToLittleEndianBytes(i);
            byte[] zeroBytes = Util.intToLittleEndianBytes(0);
            byte[] oneBytes = Util.intToLittleEndianBytes(1);

            System.arraycopy(zeroBytes, 0, initialHash, ARGON2_PREHASH_DIGEST_LENGTH, 4);
            System.arraycopy(iBytes, 0, initialHash, ARGON2_PREHASH_DIGEST_LENGTH + 4, 4);
            blockhash_bytes = Functions.blake2bLong(initialHash, ARGON2_BLOCK_SIZE);
            int index = i * instance.getLaneLength() + 0;
            instance.memory[index].fromBytes(blockhash_bytes);

            System.arraycopy(oneBytes, 0, initialHash, ARGON2_PREHASH_DIGEST_LENGTH, 4);
            blockhash_bytes = Functions.blake2bLong(initialHash, ARGON2_BLOCK_SIZE);
            instance.memory[i * instance.getLaneLength() + 1].fromBytes(blockhash_bytes);
        }
    }

}
