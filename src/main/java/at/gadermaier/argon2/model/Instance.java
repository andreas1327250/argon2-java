package at.gadermaier.argon2.model;

import static at.gadermaier.argon2.Constants.*;

import at.gadermaier.argon2.Argon2;

public class Instance {

    private Block[] memory;
    private int version;
    private int iterations;
    private int segmentLength;
    private int laneLength;
    private int lanes;

    private Argon2Type type;

    public Instance(Argon2 argon2) {
        this.version = argon2.getVersion();
        this.iterations = argon2.getIterations();
        this.lanes = argon2.getLanes();
        this.type = argon2.getType();

        /* 2. Align memory size */
        /* Minimum memoryBlocks = 8L blocks, where L is the number of lanes */
        int memoryBlocks = argon2.getMemory();

        if (memoryBlocks < 2 * ARGON2_SYNC_POINTS * argon2.getLanes()) {
            memoryBlocks = 2 * ARGON2_SYNC_POINTS * argon2.getLanes();
        }

        this.segmentLength = memoryBlocks / (argon2.getLanes() * ARGON2_SYNC_POINTS);
        this.laneLength = segmentLength * ARGON2_SYNC_POINTS;
        /* Ensure that all segments have equal length */
        memoryBlocks = segmentLength * (argon2.getLanes() * ARGON2_SYNC_POINTS);

        initMemory(memoryBlocks);
    }

    private void initMemory(int memoryBlocks) {
        this.memory = new Block[memoryBlocks];

        for (int i = 0; i < memory.length; i++) {
            memory[i] = new Block();
        }
    }

    public void clear() {
        for (Block b : memory) {
            b.clear();
        }

        memory = null;
    }

    public final int memoryLength() {
    	return memory.length;
    }
    
    public final Block memory(int index) {
    	return memory[index];
    }
    
    public Block[] getMemory() {
        return memory;
    }

    public int getVersion() {
        return version;
    }

    public int getIterations() {
        return iterations;
    }

    public int getSegmentLength() {
        return segmentLength;
    }

    public int getLaneLength() {
        return laneLength;
    }

    public int getLanes() {
        return lanes;
    }

    public Argon2Type getType() {
        return type;
    }
}
