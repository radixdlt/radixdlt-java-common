package com.radixdlt.serialization.experiment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class TestPojo {
	private final int intProperty;
	private final long longProperty;
	private final byte[] byteArrayProperty;
	private final String stringProperty;
	private final List<byte[]> signatures;

	@JsonCreator
	public TestPojo(@JsonProperty("integer") int intProperty,
					@JsonProperty("long") long longProperty,
					@JsonProperty("byteArray") byte[] byteArrayProperty,
					@JsonProperty("string") String stringProperty,
					@JsonProperty("signatures") List<byte[]> signatures) {
		this.intProperty = intProperty;
		this.longProperty = longProperty;
		this.byteArrayProperty = byteArrayProperty;
		this.stringProperty = stringProperty;
		this.signatures = signatures;
	}

	@JsonProperty("integer")
	public int intProperty() {
		return intProperty;
	}

	@JsonProperty("long")
	public long longProperty() {
		return longProperty;
	}

	@JsonProperty("byteArray")
	public byte[] byteArrayProperty() {
		return byteArrayProperty;
	}

	@JsonProperty("string")
	public String stringProperty() {
		return stringProperty;
	}

	@JsonProperty("signatures")
	public List<byte[]> signatures() {
		return signatures;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof TestPojo)) {
			return false;
		}

		TestPojo testPojo = (TestPojo) o;
		return intProperty == testPojo.intProperty
				&& longProperty == testPojo.longProperty
				&& Arrays.equals(byteArrayProperty, testPojo.byteArrayProperty)
				&& stringProperty.equals(testPojo.stringProperty);
	}

	@Override
	public int hashCode() {
		return 31 * Objects.hash(intProperty, longProperty, stringProperty)
				+ Arrays.hashCode(byteArrayProperty);
	}
}
