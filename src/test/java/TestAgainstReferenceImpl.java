import static at.gadermaier.argon2.Constants.Defaults.OUTLEN_DEF;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import at.gadermaier.argon2.Argon2;
import at.gadermaier.argon2.model.Argon2Type;

/**
 * Tests this Argon2 implementation (pure in java) against the implementation "argon2-jvm" in version 2.5.
 */
public class TestAgainstReferenceImpl {

	private static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	@Test
	public void testSameHashes() {
		for (int i = 1; i < 35; i++) {
			System.out.println("Iteration " + i);
			Random random = new Random(i);
			int iterations = 5 + random.nextInt(11);
			int memory = 65536 + random.nextInt(65537);
			int parallelism = 1 + random.nextInt(6);
			char[] password = new char[1 + random.nextInt(32)];
			for (int j = 0; j < password.length; j++) {
				password[j] = (char) (32 + random.nextInt(96));
			}
			byte[] salt = new byte[16 + random.nextInt(32)];
			random.nextBytes(salt);
			assertEquals(iterations, memory, parallelism, password, salt);
		}
	}

	private void assertEquals(int iterations, int memory, int parallelism, char[] password, byte[] salt) {
		assertEquals(Argon2Type.Argon2id, de.mkammerer.argon2.Argon2Factory.Argon2Types.ARGON2id, iterations, memory, parallelism, password, salt);
		assertEquals(Argon2Type.Argon2i, de.mkammerer.argon2.Argon2Factory.Argon2Types.ARGON2i, iterations, memory, parallelism, password, salt);
		assertEquals(Argon2Type.Argon2d, de.mkammerer.argon2.Argon2Factory.Argon2Types.ARGON2d, iterations, memory, parallelism, password, salt);
	}

	private void assertEquals(Argon2Type type, de.mkammerer.argon2.Argon2Factory.Argon2Types referenceType, int iterations, int memory, int parallelism,
			char[] password, byte[] salt) {
		int version = 19;
		Argon2 argon2 = Argon2.create().setVersion(version).setType(type).setOutputLength(OUTLEN_DEF);
		de.mkammerer.argon2.Argon2Advanced referenceArgon2 = de.mkammerer.argon2.Argon2Factory.createAdvanced(referenceType);

		long base = System.currentTimeMillis();
		String referenceHash = referenceHash(referenceArgon2, iterations, memory, parallelism, password, salt);
		long timeReferenceHashComputation = System.currentTimeMillis();
		String hash = hash(argon2, iterations, memory, parallelism, password, Arrays.copyOf(salt, salt.length));
		long timeHashComputation = System.currentTimeMillis();
		logTime(timeReferenceHashComputation - base, timeHashComputation - timeReferenceHashComputation);

		Assert.assertEquals("Hash differs.", referenceHash, hash);
	}

	private void logTime(long timeReferenceHashComputation, long timeHashComputation) {
		StringBuilder msg = new StringBuilder();
		msg.append("Reference implementation (");
		msg.append(toTime(timeReferenceHashComputation));
		msg.append(") - Argon2 implementation (");
		msg.append(toTime(timeHashComputation));
		msg.append("): Overhead ");
		msg.append(((((float)timeHashComputation) / timeReferenceHashComputation) -1) * 100);
		msg.append("%");
		System.out.println(msg.toString());
	}

	private String hash(Argon2 argon2, int iterations, int memory, int parallelism, char[] password, byte[] salt) {
		return argon2.setIterations(iterations)
				.setMemoryInKiB(memory)
				.setParallelism(parallelism)
				.hash(toByteArray(password), salt)
				.asEncoded();
	}

	private String referenceHash(de.mkammerer.argon2.Argon2Advanced argon2, int iterations, int memory, int parallelism, char[] password,			byte[] salt) {
		return argon2.hash(iterations, memory, parallelism, password, DEFAULT_CHARSET, salt);
	}

	private byte[] toByteArray(char[] passwd) {
		return new String(passwd).getBytes(DEFAULT_CHARSET);
	}

	private String toTime(long duration) {
		StringBuilder res = new StringBuilder();
		if (duration < 0) {
			duration = -duration;
			res.append("-");
		}
		long s = duration / 1000;
		res.append(s);
		res.append("s ");
		long ms = duration - (s * 1000);
		res.append(ms);
		res.append("ms");
		return res.toString();
	}
	
}
