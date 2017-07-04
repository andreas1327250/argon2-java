package at.gadermaier.argon2.algorithm;

import at.gadermaier.argon2.Constants;
import at.gadermaier.argon2.Util;
import at.gadermaier.argon2.model.*;


class FillSegment {

    static void fillSegment(Instance instance, Position position) {
        Block refBlock, currentBlock;
        Block addressBlock = null, inputBlock = null, zeroBlock = null;
        long pseudoRandom;
        int refIndex, refLane;
        int prevOffset, currentOffset;
        int startingIndex;
        boolean data_independent_addressing;

        if (instance == null) {
            return;
        }

        data_independent_addressing =
                (instance.getType() == Argon2Type.Argon2i) ||
                        (instance.getType() == Argon2Type.Argon2id && (position.pass == 0) &&
                                (position.slice < Constants.ARGON2_SYNC_POINTS / 2));

        if (data_independent_addressing) {
            addressBlock = new Block();
            zeroBlock = new Block();
            inputBlock = new Block();

            inputBlock.v[0] = Util.intToLong(position.pass);
            inputBlock.v[1] = Util.intToLong(position.lane);
            inputBlock.v[2] = Util.byteToLong(position.slice);
            inputBlock.v[3] = Util.intToLong(instance.memory.length);
            inputBlock.v[4] = Util.intToLong(instance.getPasses());
            inputBlock.v[5] = Util.intToLong(instance.getType().ordinal());
        }

        startingIndex = 0;

        if ((0 == position.pass) && (0 == position.slice)) {
            startingIndex = 2; /* we have already generated the first two blocks */

            /* Don't forget to generate the first block of addresses: */
            if (data_independent_addressing) {
                nextAddresses(addressBlock, inputBlock, zeroBlock);
            }
        }

        /* Offset of the current block */
        currentOffset = position.lane * instance.getLaneLength() +
                position.slice * instance.getSegmentLength() + startingIndex;

        if (0 == currentOffset % instance.getLaneLength()) {
        /* Last block in this lane */
            prevOffset = currentOffset + instance.getLaneLength() - 1;
        } else {
        /* Previous block */
            prevOffset = currentOffset - 1;
        }

        for (int i = startingIndex; i < instance.getSegmentLength();
             ++i, ++currentOffset, ++prevOffset) {
            /*1.1 Rotating prev_offset if needed */
            if (currentOffset % instance.getLaneLength() == 1) {
                prevOffset = currentOffset - 1;
            }


            /* 1.2 Computing the index of the reference block */
            /* 1.2.1 Taking pseudo-random value from the previous block */
            if (data_independent_addressing) {
                if (i % Constants.ARGON2_ADDRESSES_IN_BLOCK == 0) {
                    nextAddresses(addressBlock, inputBlock, zeroBlock);
                }
                pseudoRandom = addressBlock.v[i % Constants.ARGON2_ADDRESSES_IN_BLOCK];
            } else {
                pseudoRandom = instance.memory[prevOffset].v[0];
            }

            /* 1.2.2 Computing the lane of the reference block */
            refLane = (int) (((pseudoRandom >>> 32)) % instance.getLanes());


            if ((position.pass == 0) && (position.slice == 0)) {
            /* Can not reference other lanes yet */
                refLane = position.lane;
            }

            /* 1.2.3 Computing the number of possible reference block within the
             * lane.
             */

            position.index = i;
            refIndex = indexAlpha(instance, position, pseudoRandom,
                    refLane == position.lane);


            /* 2 Creating a new block */
            refBlock = instance.memory[(int) ((instance.getLaneLength()) * refLane + refIndex)];
            currentBlock = instance.memory[currentOffset];

            if (Constants.ARGON2_VERSION_10 == instance.getVersion()) {
            /* version 1.2.1 and earlier: overwrite, not XOR */
                FillBlock.fillBlock(instance.memory[prevOffset], refBlock, currentBlock, false);
            } else {
                if (0 == position.pass) {
                    FillBlock.fillBlock(instance.memory[prevOffset], refBlock,
                            currentBlock, false);
                } else {
                    FillBlock.fillBlock(instance.memory[prevOffset], refBlock,
                            currentBlock, true);
                }
            }
        }
    }

    private static void nextAddresses(Block address_block, Block input_block, Block zero_block) {
        input_block.v[6]++;
        FillBlock.fillBlock(zero_block, input_block, address_block, false);
        FillBlock.fillBlock(zero_block, address_block, address_block, false);
    }

    private static int indexAlpha(Instance instance, Position position, long pseudoRand,
                                  boolean same_lane) {
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
        int reference_area_size;
        long relative_position;
        int start_position, absolute_position;

        if (0 == position.pass) {
        /* First pass */
            if (0 == position.slice) {
            /* First slice */
                reference_area_size =
                        position.index - 1; /* all but the previous */
            } else {
                if (same_lane) {
                /* The same lane => add current segment */
                    reference_area_size =
                            position.slice * instance.getSegmentLength() +
                                    position.index - 1;
                } else {
                    reference_area_size =
                            position.slice * instance.getSegmentLength() +
                                    ((position.index == 0) ? (-1) : 0);
                }
            }
        } else {
        /* Second pass */
            if (same_lane) {
                reference_area_size = instance.getLaneLength() -
                        instance.getSegmentLength() + position.index -
                        1;
            } else {
                reference_area_size = instance.getLaneLength() -
                        instance.getSegmentLength() +
                        ((position.index == 0) ? (-1) : 0);
            }
        }


    /* 1.2.4. Mapping pseudoRand to 0..<reference_area_size-1> and produce
     * relative position */

    /* long in java is a signed datatype
     * we need to convert it to the unsigned value
     */

//        relative_position = pseudoRand & 0xFFFFFFFFL;
        relative_position = pseudoRand << 32 >>> 32;
        relative_position = relative_position * relative_position;
        relative_position = relative_position >>> 32;
        relative_position = reference_area_size - 1 - (reference_area_size * relative_position >>> 32);

    /* 1.2.5 Computing starting position */
        start_position = 0;

        if (0 != position.pass) {
            start_position = (position.slice == Constants.ARGON2_SYNC_POINTS - 1)
                    ? 0
                    : (position.slice + 1) * instance.getSegmentLength();
        }

    /* 1.2.6. Computing absolute position */
        absolute_position = (int) (start_position + relative_position) %
                instance.getLaneLength(); /* absolute position */

        return absolute_position;
    }

}
