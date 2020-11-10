package com.radixdlt.serialization.experiment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.radixdlt.serialization.experiment.TestAtom.CMMicroInstruction.CMMicroOp.PARTICLE_GROUP;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TestAtom {
	private final String serializer = "consensus.client_atom";
	private final Map<String, String> metaData;
	private final List<Map<String, String>> perGroupMetadata;
	private final Map<EUID, ECDSASignature> signatures;
	private final List<CMMicroInstruction> instructions;
	private final AID aid;
	private final HashCode witness;

	private TestAtom(
			Map<String, String> metaData,
			List<Map<String, String>> perGroupMetadata,
			Map<EUID, ECDSASignature> signatures,
			List<CMMicroInstruction> instructions,
			AID aid,
			HashCode witness
	) {
		this.metaData = metaData;
		this.perGroupMetadata = perGroupMetadata;
		this.signatures = signatures;
		this.instructions = instructions;
		this.aid = aid;
		this.witness = witness;
	}

	@Override
	public int hashCode() {
		return Objects.hash(serializer, metaData, perGroupMetadata, signatures, instructions, aid, witness);
	}

	@JsonCreator
	public TestAtom(
			@JsonProperty("serializer") String serializer,
			@JsonProperty("metadata") List<Tuple<String, String>> metaData,
			@JsonProperty("perGroupMetadata") List<Wrapper<String, String>> perGroupMetadata,
			@JsonProperty("signatures") List<Tuple<EUID, ECDSASignature>> signatures,
			@JsonProperty("instructions") List<CMMicroInstruction> instructions,
			@JsonProperty("aid") AID aid,
			@JsonProperty("witness") HashCode witness
	) {
		this(Tuple.asMap(metaData),
				perGroupMetadata.stream().map(Wrapper::asMap).collect(toList()),
				Tuple.asMap(signatures),
				instructions,
				aid,
				witness);
	}

	public static TestAtom min() {
		return new TestAtom(Map.of(), List.of(), Map.of(), List.of(), AID.random(), HashCode.random());
	}

	public static TestAtom sized(final int metaSize, final int perGroupMetaSize, final int numSignatures, final int numInstructions) {
		var instructions = randomInstructions(numInstructions, Math.max(2, numInstructions / 4));
		var perGroupLen = instructions
				.stream()
				.filter(instruction -> instruction.operation == PARTICLE_GROUP)
				.count();

		return new TestAtom(
				randomMeta(metaSize),
				randomPerGroupMeta((int) perGroupLen, perGroupMetaSize),
				randomSignatures(numSignatures),
				instructions,
				AID.random(),
				HashCode.random());
	}

	@JsonProperty("serializer")
	public String serializer() {
		return serializer;
	}

	@JsonProperty("metadata")
	public List<Tuple<String, String>> metaData() {
		return Tuple.asList(metaData);
	}

	@JsonProperty("perGroupMetadata")
	public List<Wrapper<String, String>> perGroupMetadata() {
		return perGroupMetadata
				.stream()
				.map(oneMeta -> Wrapper.wrap(Tuple.asList(oneMeta)))
				.collect(toList());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TestAtom)) {
			return false;
		}
		TestAtom testAtom = (TestAtom) o;
		return metaData.equals(testAtom.metaData)
				&& perGroupMetadata.equals(testAtom.perGroupMetadata)
				&& signatures.equals(testAtom.signatures)
				&& instructions.equals(testAtom.instructions)
				&& aid.equals(testAtom.aid)
				&& witness.equals(testAtom.witness);
	}

	public static class Wrapper<A, B> {
		private final List<Tuple<A, B>> tuples;

		@JsonCreator
		Wrapper(@JsonProperty("tuples") List<Tuple<A, B>> tuples) {
			this.tuples = tuples;
		}

		public static <A, B> Wrapper<A, B> wrap(List<Tuple<A, B>> tuples) {
			return new Wrapper<>(tuples);
		}

		@JsonProperty("tuples")
		public List<Tuple<A, B>> tuples() {
			return tuples;
		}

		public Map<A, B> asMap() {
			return Tuple.asMap(tuples);
		}
	}

	@JsonProperty("signatures")
	public List<Tuple<EUID, ECDSASignature>> signatures() {
		return Tuple.asList(signatures);
	}

	@JsonProperty("instructions")
	public List<CMMicroInstruction> instructions() {
		return instructions;
	}

	@JsonProperty("aid")
	public AID aid() {
		return aid;
	}

	@JsonProperty("witness")
	public HashCode witness() {
		return witness;
	}

	public static class ECDSASignature {
		private final String serializer = "crypto.ecdsa_signature";
		private final short version = 100;
		private final BigInteger r;
		private final BigInteger s;

		@JsonCreator
		ECDSASignature(final String data) {
			r = null;
			s = null;
		}

		@JsonCreator
		ECDSASignature(
				@JsonProperty("serializer") String serializer,
				@JsonProperty("version") short version,
				@JsonProperty("r") byte[] r,
				@JsonProperty("s") byte[] s
		) {
			this(new BigInteger(r), new BigInteger(s));
		}

		private ECDSASignature(BigInteger r, BigInteger s) {
			this.r = r;
			this.s = s;
		}

		public static ECDSASignature min() {
			return new ECDSASignature(BigInteger.ZERO, BigInteger.ZERO);
		}

		public static ECDSASignature avg() {
			return new ECDSASignature(BigInteger.valueOf(nanoTime()), BigInteger.valueOf(nanoTime()));
		}

		public static ECDSASignature big() {
			return new ECDSASignature(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE));
		}

		public static ECDSASignature random() {
			return new ECDSASignature(BigInteger.valueOf(random.nextLong()), BigInteger.valueOf(random.nextLong()));
		}

		@JsonProperty("serializer")
		public String serializer() {
			return serializer;
		}

		@JsonProperty("version")
		public short version() {
			return version;
		}

		@JsonProperty("r")
		public byte[] r() {
			return r.toByteArray();
		}

		@JsonProperty("s")
		public byte[] s() {
			return s.toByteArray();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ECDSASignature)) {
				return false;
			}
			ECDSASignature that = (ECDSASignature) o;
			return r.equals(that.r) && s.equals(that.s);
		}

		@Override
		public int hashCode() {
			return Objects.hash(serializer, version, r, s);
		}
	}

	public static class EUID {
		private final int high;
		private final int midHigh;
		private final int midLow;
		private final int low;

		public EUID(String value) {
			high = 0;
			midHigh = 0;
			midLow = 0;
			low = 0;
		}

		@JsonCreator
		EUID(
				@JsonProperty("high") int high,
				@JsonProperty("midHigh") int midHigh,
				@JsonProperty("midLow") int midLow,
				@JsonProperty("low") int low
		) {
			this.high = high;
			this.midHigh = midHigh;
			this.midLow = midLow;
			this.low = low;
		}

		public static EUID min() {
			return new EUID(0, 0, 0, 0);
		}

		public static EUID max() {
			return new EUID(0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF);
		}

		public static EUID random() {
			return new EUID(randomInt(), randomInt(), randomInt(), randomInt());
		}

		@JsonProperty("high")
		public int high() {
			return high;
		}

		@JsonProperty("midHigh")
		public int midHigh() {
			return midHigh;
		}

		@JsonProperty("midLow")
		public int midLow() {
			return midLow;
		}

		@JsonProperty("low")
		public int low() {
			return low;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (!(o instanceof EUID)) {
				return false;
			}

			EUID euid = (EUID) o;
			return high == euid.high
					&& midHigh == euid.midHigh
					&& midLow == euid.midLow
					&& low == euid.low;
		}

		@Override
		public int hashCode() {
			return Objects.hash(high, midHigh, midLow, low);
		}
	}

	public static class Particle {
		private final String serializer = "radix.particle";
		private final Set<EUID> destinations;
		private final short version = 100;

		@JsonCreator
		Particle(
				@JsonProperty("serializer") String serializer,
				@JsonProperty("version") short version,
				@JsonProperty("destinations") Set<EUID> destinations
		) {
			this(destinations);
		}

		private Particle(Set<EUID> destinations) {
			this.destinations = destinations;
		}

		public static Particle min() {
			return new Particle(Set.of());
		}

		public static Particle withDestinations(int numDestinations) {
			final var set = new HashSet<EUID>();

			while (set.size() < numDestinations) {
				set.add(EUID.random());
			}

			return new Particle(Set.copyOf(set));
		}

		@JsonProperty("serializer")
		public String serializer() {
			return serializer;
		}

		@JsonProperty("destinations")
		public Set<EUID> destinations() {
			return destinations;
		}

		@JsonProperty("version")
		public short version() {
			return version;
		}
	}

	public static class CMMicroInstruction {
		public enum CMMicroOp {
			CHECK_NEUTRAL_THEN_UP,
			CHECK_UP_THEN_DOWN,
			PARTICLE_GROUP
		}

		private final CMMicroOp operation;
		private final Particle particle;

		@JsonCreator
		private CMMicroInstruction(@JsonProperty("operation") CMMicroOp operation, @JsonProperty("particle") Particle particle) {
			this.operation = operation;
			this.particle = particle;
		}

		public static CMMicroInstruction min() {
			var op = CMMicroOp.values()[randomInt() % CMMicroOp.values().length];
			var particle = Particle.min();
			return new CMMicroInstruction(op, particle);
		}

		public static CMMicroInstruction random(final int size) {
			var op = CMMicroOp.values()[randomInt() % CMMicroOp.values().length];
			var particle = Particle.withDestinations(size);
			return new CMMicroInstruction(op, particle);
		}

		@JsonProperty("operation")
		public CMMicroOp operation() {
			return operation;
		}

		@JsonProperty("particle")
		public Particle particle() {
			return particle;
		}
	}

	public static class AID {
		public static final int HASH_BYTES = 32;
		private final byte[] value;

		@JsonCreator
		private AID(@JsonProperty("hash") byte[] value) {
			this.value = value;
		}

		public static AID random() {
			final var value = new byte[HASH_BYTES];
			random.nextBytes(value);

			return new AID(value);
		}

		@JsonProperty("hash")
		public byte[] value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof AID)) {
				return false;
			}
			AID hashCode = (AID) o;
			return Arrays.equals(value, hashCode.value);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(value);
		}
	}

	public static class HashCode {
		private static final int HASH_BYTES = 32;
		private final byte[] value;

		@JsonCreator
		private HashCode(@JsonProperty("hash") byte[] value) {
			this.value = value;
		}

		public static HashCode random() {
			final var value = new byte[HASH_BYTES];
			random.nextBytes(value);

			return new HashCode(value);
		}

		@JsonProperty("hash")
		public byte[] value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof HashCode)) {
				return false;
			}
			HashCode hashCode = (HashCode) o;
			return Arrays.equals(value, hashCode.value);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(value);
		}
	}

	private static long nanoTime() {
		return System.currentTimeMillis() * 1_000_000 + System.nanoTime();
	}

	private static int randomInt() {
		return Math.abs(random.nextInt());
	}

	private static List<CMMicroInstruction> randomInstructions(final int size, final int numDestinations) {
		var list = new ArrayList<CMMicroInstruction>(size);

		while (list.size() < size) {
			list.add(CMMicroInstruction.random(numDestinations));
		}

		return List.copyOf(list);
	}

	public static Map<EUID, ECDSASignature> randomSignatures(final int size) {
		var map = new HashMap<EUID, ECDSASignature>();

		while (map.size() < size) {
			map.put(EUID.random(), ECDSASignature.random());
		}

		return Map.copyOf(map);
	}

	private static String randomString(int length) {
		var builder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			builder.append(ALPHABET[randomInt() % ALPHABET.length]);
		}

		return builder.toString();
	}

	private static Map<String, String> randomMeta(final int size) {
		var map = new HashMap<String, String>();

		while (map.size() < size) {
			map.put(randomString(8), randomString(16));
		}

		return Map.copyOf(map);
	}

	private static List<Map<String, String>> randomPerGroupMeta(final int length, final int metaSize) {
		var list = new ArrayList<Map<String, String>>(length);

		while (list.size() < length) {
			list.add(randomMeta(metaSize));
		}

		return List.copyOf(list);
	}

	private static final Random random = new Random(System.nanoTime());
	private static final char[] ALPHABET;

	static {
		var builder = new StringBuilder();

		for (char i = 'a'; i <= 'z'; i++) {
			builder.append(i);
		}
		for (char i = 'A'; i <= 'Z'; i++) {
			builder.append(i);
		}
		for (char i = '0'; i <= '9'; i++) {
			builder.append(i);
		}
		builder.append("_.");

		ALPHABET = builder.toString().toCharArray();
	}

	public static class Tuple<A, B> {
		private final A first;
		private final B second;

		@JsonCreator
		Tuple(@JsonProperty("key") A first, @JsonProperty("val") B second) {
			this.first = first;
			this.second = second;
		}

		public static <A, B> Tuple<A, B> of(final A first, final B second) {
			return new Tuple<>(first, second);
		}

		public static <A, B> Tuple<A, B> of(final Map.Entry<A, B> e) {
			return new Tuple<>(e.getKey(), e.getValue());
		}

		public static <A, B> List<Tuple<A, B>> asList(Map<A, B> input) {
			return  input.entrySet().stream().map(Tuple::of).collect(toList());
		}

		@JsonProperty("key")
		public A first() {
			return first;
		}

		@JsonProperty("val")
		public B second() {
			return second;
		}

		public static <K, V> Map<K, V> asMap(List<Tuple<K, V>> input) {
			return input.stream().collect(toMap(Tuple::first, Tuple::second));
		}
	}
}
