package com.radixdlt.serialization.experiment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.radixdlt.utils.Bytes;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.radixdlt.serialization.experiment.TestAtom.AID.HASH_BYTES;
import static org.junit.Assert.fail;

public class SerializeTest {
	private static final List<?> TEST_POJOS = List.of(
			TestAtom.min(),
			TestAtom.sized(0, 0, 1, 1),
			TestAtom.sized(5, 5, 3, 5),
			TestAtom.sized(5, 5, 5, 10),
			TestAtom.sized(10, 10, 10, 20),
			TestAtom.sized(10, 10, 30, 50),
			TestAtom.sized(50, 100, 50, 100)
	);

	@Test
	@Ignore
	public void testSerDeserAvro() throws JsonMappingException {
		var pojo = TestAtom.EUID.min();
		serialize(pojo, new AvroMapper(), "avro");
		serialize(pojo, new ProtobufMapper(), "proto");
	}

	@Test
	public void testSerialize() throws JsonMappingException {
//		printProtobufSchema(TestAtom.class);
//		printAvroSchema(TestAtom.class);

		final var objectMapper = new ObjectMapper();
		final var cborMapper = new CBORMapper();
		final var protobufMapper = new ProtobufMapper();
		final var avroMapper = new AvroMapper();

		TEST_POJOS.forEach(pojo -> {
			serialize(pojo, objectMapper, "JSON    ");
			serialize(pojo, cborMapper, "CBOR    ");
			serialize(pojo, avroMapper, "avro    ");
			serialize(pojo, protobufMapper, "protobuf");
			System.out.println();
		});
	}

	private void printAvroSchema(Class<?> clazz) throws JsonMappingException {
		final var avroMapper = new AvroMapper();
		System.out.println("Avro schema:\n" + avroMapper.schemaFor(clazz).getAvroSchema().toString() + "\n");
	}

	private void printProtobufSchema(Class<?> clazz) throws JsonMappingException {
		final var protobufMapper = new ProtobufMapper();
		System.out.println("Protobuf schema:\n" + protobufMapper.generateSchemaFor(clazz).getSource().toString() + "\n");
	}

	private static void serialize(final Object pojo, final ObjectMapper mapper, final String name) {
		try {
			var serialized = pojoToBytes(mapper, pojo);
			var arrayContent = printByteArray(serialized);
			System.out.println(name + ": " + serialized.length + "\nContent : " + arrayContent);

			//Disabled for now
//			if (mapper instanceof AvroMapper || mapper instanceof ProtobufMapper) {
//				try {
//					var result = bytesToPojo(mapper, serialized, pojo.getClass(), elementClass(pojo));
//					assertEquals(pojo, result);
//				} catch (Exception e) {
//					System.err.println("Unable to deserialize content: " + e.getMessage());
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail(e.getMessage());
		}
	}

	private static String printByteArray(byte[] serialized) {
		var toPrint = Arrays.copyOfRange(serialized, 0, Math.min(serialized.length, HASH_BYTES));
		var shortened = serialized.length > toPrint.length;
		var builder = new StringBuilder(HASH_BYTES * 2).append('[');

		for (var oneByte : toPrint) {
			builder.append(Bytes.toHexString(oneByte)).append(' ');
		}

		if (shortened) {
			builder.append("...");
		}

		return builder.append(']').toString();
	}

	private static byte[] pojoToBytes(ObjectMapper mapper, Object pojo) throws JsonProcessingException {
		final var pojoClass = pojo.getClass();
		final var elementClass = elementClass(pojo);

		if (mapper instanceof ProtobufMapper) {
			final var schema = protobufSchema(mapper, pojoClass, elementClass);
			return mapper.writer(schema).writeValueAsBytes(pojo);
		} else if (mapper instanceof AvroMapper) {
			final var schema = avroSchema(mapper, pojoClass, elementClass);
			return mapper.writer(schema).writeValueAsBytes(pojo);
		}

		return mapper.writeValueAsBytes(pojo);
	}

	private static <T> T bytesToPojo(ObjectMapper mapper, byte[] data, Class<T> pojoClass, Class<?> elementClass) throws IOException {
		if (mapper instanceof ProtobufMapper) {
			final var schema = protobufSchema(mapper, pojoClass, elementClass);
			return mapper.readerFor(pojoClass).with(schema).readValue(data);
		} else if (mapper instanceof AvroMapper) {
			final var schema = avroSchema(mapper, pojoClass, elementClass);
			return mapper.readerFor(pojoClass).with(schema).readValue(data);
		}

		return mapper.readerFor(pojoClass).readValue(data);
	}

	private static Class<?> elementClass(Object pojo) {
		return pojo instanceof Collection ? ((Collection) pojo).iterator().next().getClass() : null;
	}

	private static <T> AvroSchema avroSchema(ObjectMapper mapper, Class<T> pojoClass, Class<?> elementClass) throws JsonMappingException {
		var avroMapper = (AvroMapper) mapper;
		return avroMapper.schemaFor(constructType(pojoClass, elementClass));
	}

	private static <T> ProtobufSchema protobufSchema(ObjectMapper mapper, Class<T> pojoClass, Class<?> elementClass) throws JsonMappingException {
		var protoMapper = (ProtobufMapper) mapper;
		return protoMapper.generateSchemaFor(constructType(pojoClass, elementClass));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T> JavaType constructType(Class<T> pojoClass, Class<?> elementClass) {
		final TypeFactory typeFactory = TypeFactory.defaultInstance();
		var type = elementClass == null
				? typeFactory.constructSimpleType(pojoClass, null)
				: typeFactory.constructCollectionType((Class<? extends Collection>) pojoClass, elementClass);
		return type;
	}
}
