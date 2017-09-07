package at.gadermaier.argon2.model;

public class Position {

    public int pass;
    public int lane;
    public int slice;
    public int index;

    public Position(int pass, int lane, int slice, int index) {
        this.pass = pass;
        this.lane = lane;
        this.slice = slice;
        this.index = index;
    }
}

