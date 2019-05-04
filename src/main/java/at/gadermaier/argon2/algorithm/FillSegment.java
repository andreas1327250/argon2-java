package at.gadermaier.argon2.algorithm;

import static at.gadermaier.argon2.Constants.*;

import at.gadermaier.argon2.Constants;
import at.gadermaier.argon2.Util;
import at.gadermaier.argon2.model.Argon2Type;
import at.gadermaier.argon2.model.Block;
import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;


class FillSegment {

    private final FillBlock filler = new FillBlock();
    
    private final Block _addressBlock = new Block();
    private final Block _zeroBlock = new Block();
    private final Block _inputBlock = new Block();


    void fillSegment(Instance instance, Position position) {

        Block addressBlock = null, inputBlock = null, zeroBlock = null;

        boolean dataIndependentAddressing = isDataIndependentAddressing(instance, position);
        int startingIndex = getStartingIndex(position);
        int currentOffset = position.lane * instance.getLaneLength() + position.slice * instance.getSegmentLength() + startingIndex;
        int prevOffset = getPrevOffset(instance, currentOffset);

        if (dataIndependentAddressing) {
            addressBlock = _addressBlock;
            zeroBlock = _zeroBlock;
            inputBlock = _inputBlock;
            
            addressBlock.clear();
            zeroBlock.clear();
            inputBlock.clear();

            initAddressBlocks(instance, position, zeroBlock, inputBlock, addressBlock);
        }

        for (position.index = startingIndex; position.index < instance.getSegmentLength(); position.index++, currentOffset++, prevOffset++) {
            prevOffset = rotatePrevOffset(instance, currentOffset, prevOffset);

            long pseudoRandom = getPseudoRandom(instance, position, addressBlock, inputBlock, zeroBlock, prevOffset, dataIndependentAddressing);
            int refLane = getRefLane(instance, position, pseudoRandom);
            int refColumn = getRefColumn(instance, position, pseudoRandom, refLane == position.lane);

            /* 2 Creating a new block */
            Block prevBlock = instance.memory(prevOffset);
            Block refBlock = instance.memory(((instance.getLaneLength()) * refLane + refColumn));
            Block currentBlock = instance.memory(currentOffset);

            boolean withXor = isWithXor(instance, position);
            filler.fillBlock(prevBlock, refBlock, currentBlock, withXor);
        }
    }

    private static boolean isDataIndependentAddressing(Instance instance, Position position) {
        return (instance.getType() == Argon2Type.Argon2i) ||
                (instance.getType() == Argon2Type.Argon2id
                        && (position.pass == 0)
                        && (position.slice < Constants.ARGON2_SYNC_POINTS / 2)
                );
    }

    private void initAddressBlocks(Instance instance, Position position, Block zeroBlock, Block inputBlock, Block addressBlock) {
        inputBlock.v[0] = Util.intToLong(position.pass);
        inputBlock.v[1] = Util.intToLong(position.lane);
        inputBlock.v[2] = Util.intToLong(position.slice);
        inputBlock.v[3] = Util.intToLong(instance.memoryLength());
        inputBlock.v[4] = Util.intToLong(instance.getIterations());
        inputBlock.v[5] = Util.intToLong(instance.getType().ordinal());

        if ((position.pass == 0) && (position.slice == 0)) {
            /* Don't forget to generate the first block of addresses: */
            nextAddresses(zeroBlock, inputBlock, addressBlock);
        }
    }

    private static boolean isWithXor(Instance instance, Position position) {
        return !(position.pass == 0 || instance.getVersion() == ARGON2_VERSION_10);
    }

    private static int getPrevOffset(Instance instance, int currentOffset) {
        if (currentOffset % instance.getLaneLength() == 0) {
            /* Last block in this lane */
            return currentOffset + instance.getLaneLength() - 1;
        } else {
            /* Previous block */
            return currentOffset - 1;
        }
    }

    private static int rotatePrevOffset(Instance instance, int currentOffset, int prevOffset) {
        if (currentOffset % instance.getLaneLength() == 1) {
            prevOffset = currentOffset - 1;
        }
        return prevOffset;
    }

    private static int getStartingIndex(Position position) {
        if ((position.pass == 0) && (position.slice == 0)) {
            return 2; /* we have already generated the first two blocks */
        } else {
            return 0;
        }
    }

    private void nextAddresses(Block zeroBlock, Block inputBlock, Block addressBlock) {
        inputBlock.v[6]++;
        filler.fillBlock(zeroBlock, inputBlock, addressBlock, false);
        filler.fillBlock(zeroBlock, addressBlock, addressBlock, false);
    }

    /* 1.2 Computing the index of the reference block */
    /* 1.2.1 Taking pseudo-random value from the previous block */
    private long getPseudoRandom(Instance instance, Position position, Block addressBlock, Block inputBlock, Block zeroBlock, int prevOffset, boolean dataIndependentAddressing) {
        if (dataIndependentAddressing) {
            if (position.index % ARGON2_ADDRESSES_IN_BLOCK == 0) {
                nextAddresses(zeroBlock, inputBlock, addressBlock);
            }
            return addressBlock.v[position.index % ARGON2_ADDRESSES_IN_BLOCK];
        } else {
            return instance.memory(prevOffset).v[0];
        }
    }

    private static int getRefLane(Instance instance, Position position, long pseudoRandom) {
        int refLane = (int) (((pseudoRandom >>> 32)) % instance.getLanes());

        if ((position.pass == 0) && (position.slice == 0)) {
            /* Can not reference other lanes yet */
            refLane = position.lane;
        }
        return refLane;
    }

    private static int getRefColumn(Instance instance, Position position, long pseudoRandom,
                                    boolean sameLane) {

        int referenceAreaSize;
        int startPosition;

        if (position.pass == 0) {
            startPosition = 0;

            if (sameLane) {
                /* The same lane => add current segment */
                referenceAreaSize = position.slice * instance.getSegmentLength() + position.index - 1;
            } else {
                /* pass == 0 && !sameLane => position.slice > 0*/
                referenceAreaSize = position.slice * instance.getSegmentLength() + ((position.index == 0) ? (-1) : 0);
            }

        } else {
            startPosition = ((position.slice + 1) * instance.getSegmentLength()) % instance.getLaneLength();

            if (sameLane) {
                referenceAreaSize = instance.getLaneLength() - instance.getSegmentLength() + position.index - 1;
            } else {
                referenceAreaSize = instance.getLaneLength() - instance.getSegmentLength() + ((position.index == 0) ? (-1) : 0);
            }
        }

        long relativePosition = pseudoRandom & 0xFFFFFFFFL;
//        long relativePosition = pseudoRandom << 32 >>> 32;
        relativePosition = (relativePosition * relativePosition) >>> 32;
        relativePosition = referenceAreaSize - 1 - (referenceAreaSize * relativePosition >>> 32);

        return (int) (startPosition + relativePosition) % instance.getLaneLength();
    }

}
