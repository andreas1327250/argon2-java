package at.gadermaier.argon2;

import at.gadermaier.argon2.model.Argon2Type;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;

import static at.gadermaier.argon2.Util.bytesToHexString;


public final class Argon2Result {

    private final Argon2Type type;
    private final int version;
    private final int memoryInKb;
    private final int iteration;
    private final int paralellism;
    private final byte[] salt;
    private final byte[] hash;

    Argon2Result(
            Argon2Type type, int version, int memoryInKiB, int iteration, int parallelism, byte[] salt, byte[] hash ) {

        this.type = type;
        this.version = version;
        this.memoryInKb = memoryInKiB;
        this.iteration = iteration;
        this.paralellism = parallelism;
        this.salt = Arrays.copyOf( salt, salt.length );
        this.hash = Arrays.copyOf( hash, hash.length );
    }

    public byte[] asByte() {
        return Arrays.copyOf( hash, hash.length );
    }

    public String asString() {
        return bytesToHexString( hash );
    }

    public String asEncoded() {

        String type = this.type.equals( Argon2Type.Argon2i ) ? "i" :
                      this.type.equals( Argon2Type.Argon2d ) ? "d" :
                      this.type.equals( Argon2Type.Argon2id ) ? "id" : null;
        String salt = BaseEncoding.base64().omitPadding().encode( this.salt );
        String hash = BaseEncoding.base64().omitPadding().encode( this.hash );

        return new StringBuilder()
                .append( "$argon2" )
                .append( type )
                .append( "$v=" )
                .append( version )
                .append( "$m=" )
                .append( memoryInKb )
                .append( ",t=" )
                .append( iteration )
                .append( ",p=" )
                .append( paralellism )
                .append( "$" )
                .append( salt )
                .append( "$" )
                .append( hash )
                .toString();
    }
}
