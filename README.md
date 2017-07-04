# Argon2 Java implementation

This is a pure Java implementation of [Argon2](https://github.com/P-H-C/phc-winner-argon2). It targets Java 6.

Many parts are just taken from the C code and converted to Java syntax - there shouldn't be any major performance bottlenecks anymore, but speed can't keep up quite with the C implementation.

Fair warning: this code is not well tested and not reviewed. It is not ready for production, use it on your own risk. Same applies for underlying Blake2b implementation.

## Benchmarks
[Rplots.pdf](benchmarks/Rplots.pdf)

Red is java, green the reference implementation and blue the optimized implementation with SSE-instructions - run on a i7-7700HQ with 16GB RAM.

## Maven

### Installation
You need to install the jar into your local `.m2` repository.

Clone and install it:

    git clone https://github.com/andreas1327250/argon2-java

    cd argon2-java

    mvn install


```xml
<dependency>
    <groupId>at.gadermaier</groupId>
    <artifactId>argon2</artifactId>
    <version>0.1</version>
</dependency>
```

<!---
## Gradle

```groovy
compile 'at.gadermaier:argon2:1.0-SNAPSHOT'
```
-->
## Usage


```
// Read password
char[] password = readPassword();

// Generate salt
String salt = generateRandomSalt();

// Hash password
String hash = Argon2Factory.create()
            .setIterations(2)
            .setMemory(14)
            .setParallelism(1)
            .hash(password, salt);

```
```
% java -jar argon2-0.1.jar
usage: argon2 salt [-d | -i | -id] [-e | -r] [-h]   [-k <N> | -m <N>] [-l
      <N>]  [-p <N>]  [-t <N>]
-d       Use Argon2d instead of Argon2i
-e       Output only encoded hash
-h       Print usage
-i       Use Argon2i (this is the default)
-id      Use Argon2id instead of Argon2i
-k <N>   Sets the memory usage of N KiB (default 2^12)
-l <N>   Sets hash output length to N bytes (default 32)
-m <N>   Sets the memory usage of 2^N KiB (default 12)
-p <N>   Sets parallelism to N (default 1)
-r       Output only the raw bytes of the hash
-t <N>   Sets the number of iterations to N (default = 3)
Password is read from stdin
```

## Blake2b
The [blake2b Java implementation](https://github.com/alphazero/Blake2b) of [@alphazero](https://github.com/alphazero/) is used. Because no artifact is available on maven central, I copied the relevant class file into this project - for the sake of simplicity.

## License
[MIT](https://opensource.org/licenses/MIT)

## TODO
* Verification
* Encoded result
* Performance
  * in Block.xorBlock(), the JIT actually already creates vector operations
* Fix the build
  * Build library without commons-dependency, build executable without
  * maven central
  * add gradle support
* Add Tests
* ..


## Contact
[@andreas1327250](https://github.com/andreas1327250)

e1327250@student.tuwien.ac.at
