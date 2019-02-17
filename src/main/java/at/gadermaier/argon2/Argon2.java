package at.gadermaier.argon2;

import at.gadermaier.argon2.algorithm.FillMemory;
import at.gadermaier.argon2.algorithm.Finalize;
import at.gadermaier.argon2.algorithm.Initialize;
import at.gadermaier.argon2.model.Argon2Type;
import at.gadermaier.argon2.model.Instance;
import com.google.common.io.BaseEncoding;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static at.gadermaier.argon2.Constants.Defaults.*;
import static at.gadermaier.argon2.Util.clearArray;
import static at.gadermaier.argon2.Util.toByteArray;
import static at.gadermaier.argon2.model.Argon2Type.*;


public class Argon2 {

    private byte[] output;
    private int outputLength; // -l N

    private byte[] password;
    private byte[] salt;
    private byte[] secret;
    private byte[] additional;

    private int iterations; // -t N
    private int memory; // -m N
    private int lanes; // -p N

    private int version; // -v (10/13)
    private Argon2Type type;

    private boolean clearMemory = true;
    private static Charset charset = Charset.forName("UTF-8");

    private ExecutorService executor;

    private static final class ExecutorHolder {
        private static final ExecutorService executor = Executors.newCachedThreadPool();
    }

    private Argon2() {
        this.lanes = LANES_DEF;
        this.outputLength = OUTLEN_DEF;
        this.memory = 1 << LOG_M_COST_DEF;
        this.iterations = T_COST_DEF;
        this.version = VERSION_DEF;
        this.type = TYPE_DEF;
    }

    public static Argon2 create() {
        return new Argon2();
    }

    private static Argon2 createFromEncoded( String encoded ) {

        String[] parts = encoded.split( "\\$" );
        Argon2Type type = parts[1].endsWith( "id" ) ? Argon2id :
                          parts[1].endsWith( "i" ) ? Argon2i :
                          parts[1].endsWith( "d" ) ? Argon2d : null;
        boolean hasVersion = !parts[2].startsWith( "m=" );
        String version = hasVersion ? parts[2].split( "=" )[1] : "16";
        String[] paramParts = (hasVersion ? parts[3] : parts[2]).split( "," );
        String m = paramParts[0].split( "=" )[1];
        String t = paramParts[1].split( "=" )[1];
        String p = paramParts[2].split( "=" )[1];

        String saltEncoded = hasVersion ? parts[4] : parts[3];
        byte[] salt = BaseEncoding.base64().decode( saltEncoded );
        String hashEncoded = hasVersion ? parts[5] : parts[4];
        byte[] hash = BaseEncoding.base64().decode( hashEncoded );

        Argon2 instance = new Argon2();
        instance.setType( type );
        instance.setVersion( Integer.parseInt( version ) );
        instance.setMemoryInKiB( Integer.parseInt( m ) );
        instance.setIterations( Integer.parseInt( t ) );
        instance.setParallelism( Integer.parseInt( p ) );
        instance.setSalt( salt );
        instance.setOutput( hash );
        return instance;
    }

    public static boolean checkHash( String encoded, byte[] password ) {

        Argon2 argon2 = Argon2.createFromEncoded( encoded );
        byte[] expectedHash = Arrays.copyOf( argon2.getOutput(), argon2.getOutput().length );
        argon2.setPassword( password );
        Argon2Result result = argon2.hash();

        return Arrays.equals( result.asByte(), expectedHash );
    }

    public static boolean checkHash( String encoded, String password ) {
        return checkHash( encoded, password.getBytes( charset ) );
    }

    public static boolean checkHash( String encoded, String password, Charset charset ) {
        return checkHash( encoded, password.getBytes( charset ) );
    }

    public Argon2Result hash(byte[] password, byte[] salt){
        setPassword(password);
        setSalt(salt);

        return hash();
    }

    public Argon2Result hash(char[] password, String salt, Charset charset){
        setPassword(toByteArray( password, charset ));
        setSalt(salt.getBytes( charset ));

        return hash();
    }

    public Argon2Result hash(String password, String salt) {
        return hash( password, salt, charset );
    }

    public Argon2Result hash(String password, String salt, Charset charset) {
        setPassword( password.getBytes( charset ) );
        setSalt( salt.getBytes( charset ) );

        return hash();
    }

    public Argon2Result hash() {
        try {
            byte[] keepSalt = Arrays.copyOf( salt, salt.length );
            argon2_hash();
            return new Argon2Result( type, version, memory, iterations, lanes, keepSalt, output );
        } finally {
            clear();
        }
    }

    private void argon2_hash() {

        if (executor == null) {
            executor = ExecutorHolder.executor;
        }

        Validation.validateInput(this);

        Instance instance = new Instance(this);

        Initialize.initialize(instance, this);
        FillMemory.fillMemoryBlocks(instance, executor);
        Finalize.finalize(instance, this);
    }

    public void clear() {
        clearArray( password );
        clearArray( salt );
        clearArray( secret );
        clearArray( additional );
    }

    public Argon2 setMemoryInKiB(int memory) {
        this.memory = memory;
        return this;
    }

    public Argon2 setParallelism(int parallelism){
        this.lanes = parallelism;
        return this;
    }

    public byte[] getOutput() {
        return output;
    }

    public void setOutput(byte[] finalResult) {
        this.output = finalResult;
    }

    public String getOutputString() {
        return Util.bytesToHexString(output);
    }

    public int getOutputLength() {
        return outputLength;
    }

    public Argon2 setOutputLength(int outputLength) {
        this.outputLength = outputLength;
        return this;
    }

    public byte[] getPassword() {
        return password;
    }

    public Argon2 setPassword(byte[] password) {
        this.password = password;
        return this;
    }

    public int getPasswordLength() {
        return password.length;
    }

    public byte[] getSalt() {
        return salt;
    }

    public Argon2 setSalt(byte[] salt) {
        this.salt = salt;
        return this;
    }

    public int getSaltLength() {
        return salt.length;
    }

    public byte[] getSecret() {
        return secret;
    }

    public Argon2 setSecret(byte[] secret) {
        this.secret = secret;
        return this;
    }

    public int getSecretLength() {
        return secret != null ? secret.length : 0;
    }

    public byte[] getAdditional() {
        return additional;
    }

    public Argon2 setAdditional(byte[] additional) {
        this.additional = additional;
        return this;
    }

    public int getAdditionalLength() {
        return additional  != null ? additional.length : 0;
    }

    public int getIterations() {
        return iterations;
    }

    public Argon2 setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public int getMemory() {
        return memory;
    }

    public Argon2 setMemory(int memory) {
        this.memory = 1 << memory;
        return this;
    }

    public int getLanes() {
        return lanes;
    }

    public int getVersion() {
        return version;
    }

    public Argon2 setVersion(int version) {
        this.version = version;
        return this;
    }

    public Argon2Type getType() {
        return type;
    }

    public Argon2 setType(Argon2Type type) {
        this.type = type;
        return this;
    }

    public boolean isClearMemory() {
        return clearMemory;
    }

    public void setClearMemory(boolean clearMemory) {
        this.clearMemory = clearMemory;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Argon2 setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }
}
