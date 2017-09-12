package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.Constants;
import at.gadermaier.argon2.Util;
import at.gadermaier.argon2.model.Argon2Type;
import at.gadermaier.argon2.model.Block;
import at.gadermaier.argon2.model.Instance;
import at.gadermaier.argon2.model.Position;

import static at.gadermaier.argon2.Constants.ARGON2_ADDRESSES_IN_BLOCK;
import static at.gadermaier.argon2.Constants.ARGON2_VERSION_10;


class FillSegment {

    static void fillSegment(Instance instance, Position position) {

        Block addressBlock = null, inputBlock = null, zeroBlock = null;

        boolean dataIndependentAddressing = isDataIndependentAddressing(instance, position);
        int startingIndex = getStartingIndex(position);
        int currentOffset = position.lane * instance.getLaneLength() + position.slice * instance.getSegmentLength() + startingIndex;
        int prevOffset = getPrevOffset(instance, currentOffset);

        if (dataIndependentAddressing) {
            addressBlock = new Block();
            zeroBlock = new Block();
            inputBlock = new Block();

            initAddressBlocks(instance, position, zeroBlock, inputBlock, addressBlock);
        }

        for (position.index = startingIndex; position.index < instance.getSegmentLength(); position.index++, currentOffset++, prevOffset++) {
            prevOffset = rotatePrevOffset(instance, currentOffset, prevOffset);

            long pseudoRandom = getPseudoRandom(instance, position, addressBlock, inputBlock, zeroBlock, prevOffset, dataIndependentAddressing);
            int refLane = getRefLane(instance, position, pseudoRandom);
            int refIndex = getRefIndex(instance, position, pseudoRandom, refLane == position.lane);

            /* 2 Creating a new block */
            Block prevBlock = instance.memory[prevOffset];
            Block refBlock = instance.memory[((instance.getLaneLength()) * refLane + refIndex)];
            Block currentBlock = instance.memory[currentOffset];

            boolean withXor = isWithXor(instance, position);
            FillBlock.fillBlock(prevBlock, refBlock, currentBlock, withXor);
        }
    }

    private static boolean isDataIndependentAddressing(Instance instance, Position position) {
        return (instance.getType() == Argon2Type.Argon2i) ||
                (instance.getType() == Argon2Type.Argon2id
                        && (position.pass == 0)
                        && (position.slice < Constants.ARGON2_SYNC_POINTS / 2)
                );
    }

    private static void initAddressBlocks(Instance instance, Position position, Block zeroBlock, Block inputBlock, Block addressBlock) {
        inputBlock.v[0] = Util.intToLong(position.pass);
        inputBlock.v[1] = Util.intToLong(position.lane);
        inputBlock.v[2] = Util.intToLong(position.slice);
        inputBlock.v[3] = Util.intToLong(instance.memory.length);
        inputBlock.v[4] = Util.intToLong(instance.getIterations());
        inputBlock.v[5] = Util.intToLong(instance.getType().ordinal());

        if ((position.pass == 0) && (position.slice == 0)) {
            /* Don't forget to generate the first block of addresses: */
            nextAddresses(zeroBlock, inputBlock, addressBlock);
        }
    }

    private static boolean isWithXor(Instance instance, Position position) {
        if (position.pass == 0 || instance.getVersion() == ARGON2_VERSION_10) {
            return false;
        } else {
            return true;
        }
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

    private static void nextAddresses(Block zeroBlock, Block inputBlock, Block addressBlock) {
        inputBlock.v[6]++;
        FillBlock.fillBlock(zeroBlock, inputBlock, addressBlock, false);
        FillBlock.fillBlock(zeroBlock, addressBlock, addressBlock, false);
    }

    /* 1.2 Computing the index of the reference block */
    /* 1.2.1 Taking pseudo-random value from the previous block */
    private static long getPseudoRandom(Instance instance, Position position, Block addressBlock, Block inputBlock, Block zeroBlock, int prevOffset, boolean dataIndependentAddressing) {
        if (dataIndependentAddressing) {
            if (position.index % ARGON2_ADDRESSES_IN_BLOCK == 0) {
                nextAddresses(zeroBlock, inputBlock, addressBlock);
            }
            return addressBlock.v[position.index % ARGON2_ADDRESSES_IN_BLOCK];
        } else {
            return instance.memory[prevOffset].v[0];
        }
    }

    private static int getRefLane(Instance instance, Position position, long pseudoRandom) {
        /* 1.2.2 Computing the lane of the reference block */
        int refLane = (int) (((pseudoRandom >>> 32)) % instance.getLanes());

        if ((position.pass == 0) && (position.slice == 0)) {
            /* Can not reference other lanes yet */
            refLane = position.lane;
        }
        return refLane;
    }

    /* 1.2.3 Computing the number of possible reference block within the
     * lane.
     */
    private static int getRefIndex(Instance instance, Position position, long pseudoRand,
                                   boolean sameLane) {
        /*
         * Pass 0:
         *      This lane : all already finished segments plus already constructed
         * blocks in this segment
         *      Other lanes : all already finished segments
         * Pass 1+:
         *      This lane : (SYNC_POINTS - 1) last segments plus already constructed
         * blocks in this segment
         *      Other lanes : (SYNC_POINTS - 1) last segments
         */
        int referenceAreaSize;
        long relativePosition;
        int startPosition, absolutePosition;

        if (position.pass == 0) {
            /* First pass */
            /* 1.2.5 Computing starting position */
            startPosition = 0;

            if (sameLane) {
                /* The same lane => add current segment */
                referenceAreaSize = position.slice * instance.getSegmentLength() + position.index - 1;
            } else {
                /* pass == 0 && !sameLane => position.slice > 0*/
                referenceAreaSize = position.slice * instance.getSegmentLength() + ((position.index == 0) ? (-1) : 0);
            }

        } else {
            /* Other passes */
            /* 1.2.5 Computing starting position */
            startPosition = ((position.slice + 1) * instance.getSegmentLength()) % instance.getLaneLength();

            if (sameLane) {
                referenceAreaSize = instance.getLaneLength() - instance.getSegmentLength() + position.index - 1;
            } else {
                referenceAreaSize = instance.getLaneLength() - instance.getSegmentLength() + ((position.index == 0) ? (-1) : 0);
            }
        }


    /* 1.2.4. Mapping pseudoRand to 0..<referenceAreaSize-1> and produce
     * relative position */

    /* long in java is a signed datatype
     * we need to convert it to the unsigned value
     */

//        relativePosition = pseudoRand & 0xFFFFFFFFL;
        relativePosition = pseudoRand << 32 >>> 32;
        relativePosition = relativePosition * relativePosition;
        relativePosition = relativePosition >>> 32;
        relativePosition = referenceAreaSize - 1 - (referenceAreaSize * relativePosition >>> 32);

         /* 1.2.6. Computing absolute position */
        absolutePosition = (int) (startPosition + relativePosition) %
                instance.getLaneLength(); /* absolute position */

        return absolutePosition;
    }

}
