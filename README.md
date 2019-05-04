# Argon2 Java implementation

This is a pure Java implementation of [Argon2](https://github.com/P-H-C/phc-winner-argon2). It targets Java 8.

Many parts are just taken from the C code and converted to Java syntax. Runtime overhead compared to the C implementation is within 20 percent.

Fair warning: This code is neither excessively tested nor reviewed. Use it at your own risk. Same applies to the underlying Blake2b cryptographic hash implementation.

## Benchmarks
Old benchmark before memory optimization of the Java implementation: [Rplots.pdf](benchmarks/Rplots.pdf)

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

## Gradle

```groovy
compile 'at.gadermaier:argon2:1.0-SNAPSHOT'
```

## Usage

### Create encoded hash when a user is setting it's password
```java
char[] password = readPassword();
String salt = generateRandomSalt();

// Hash password - Builder pattern
String encodedHash = Argon2Factory.create()
    .setIterations(2)
    .setMemory(14)
    .setParallelism(1)
    .hash(password, salt).
    .asEncoded();
    
storeToDataBase(encodedHash);
```

### Verify that a user entered a correct password
```java
String password = readFromUser();
String encodedHash = readFromDatabase();

boolean match = Argon2().checkHash(encodedHash, password);
if (match) {
	loginUser();
} else {
	printInvalidUsernameOrPassword();
}
```

## Blake2b
The [blake2b Java implementation](https://github.com/alphazero/Blake2b) from [@alphazero](https://github.com/alphazero/) is used. Because no artifact is available on Maven Central the relevant class file is embedded in this project.

## License
[MIT](https://opensource.org/licenses/MIT)

## TODO
* Verification
* Performance
  * Can we get closer to the C implementation?
  * in Block.xorBlock(), the JIT actually already creates vector operations
* Fix the build
  * Build library without commons-dependency, build executable with
  * maven central
  * add gradle support
* Add more Tests

## Contact
[@andreas1327250](https://github.com/andreas1327250)

up.gadermaier@gmail.com
