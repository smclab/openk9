package io.openk9.relationship.graph.api.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Value {
	int size();

	boolean isEmpty();

	Iterable<String> keys();

	Value get(int index);

	Type type();

	boolean hasType(Type type);

	boolean isTrue();

	boolean isFalse();

	boolean isNull();

	Object asObject();

	<T> T computeOrDefault(
		Function<Value, T> mapper, T defaultValue);

	boolean asBoolean();

	boolean asBoolean(boolean defaultValue);

	byte[] asByteArray();

	byte[] asByteArray(byte[] defaultValue);

	String asString();

	String asString(String defaultValue);

	Number asNumber();

	long asLong();

	long asLong(long defaultValue);

	int asInt();

	int asInt(int defaultValue);

	double asDouble();

	double asDouble(double defaultValue);

	float asFloat();

	float asFloat(float defaultValue);

	List<Object> asList();

	List<Object> asList(List<Object> defaultValue);

	<T> List<T> asList(Function<Value, T> mapFunction);

	<T> List<T> asList(
		Function<Value, T> mapFunction, List<T> defaultValue);

	Entity asEntity();

	Node asNode();

	Relationship asRelationship();

	Path asPath();

	LocalDate asLocalDate();

	OffsetTime asOffsetTime();

	LocalTime asLocalTime();

	LocalDateTime asLocalDateTime();

	OffsetDateTime asOffsetDateTime();

	ZonedDateTime asZonedDateTime();

	IsoDuration asIsoDuration();

	Point asPoint();

	LocalDate asLocalDate(LocalDate defaultValue);

	OffsetTime asOffsetTime(OffsetTime defaultValue);

	LocalTime asLocalTime(LocalTime defaultValue);

	LocalDateTime asLocalDateTime(LocalDateTime defaultValue);

	OffsetDateTime asOffsetDateTime(OffsetDateTime defaultValue);

	ZonedDateTime asZonedDateTime(ZonedDateTime defaultValue);

	IsoDuration asIsoDuration(IsoDuration defaultValue);

	Point asPoint(Point defaultValue);

	Map<String, Object> asMap(Map<String, Object> defaultValue);

	<T> Map<String, T> asMap(
		Function<Value, T> mapFunction,
		Map<String, T> defaultValue);

	boolean containsKey(String key);

	Value get(String key);

	Iterable<Value> values();

	<T> Iterable<T> values(
		Function<Value, T> mapFunction);

	Map<String, Object> asMap();

	<T> Map<String, T> asMap(
		Function<Value, T> mapFunction);

	Value get(
		String key, Value defaultValue);

	Object get(String key, Object defaultValue);

	Number get(String key, Number defaultValue);

	Entity get(String key, Entity defaultValue);

	Node get(String key, Node defaultValue);

	Path get(String key, Path defaultValue);

	Relationship get(String key, Relationship defaultValue);

	List<Object> get(String key, List<Object> defaultValue);

	<T> List<T> get(
		String key, List<T> defaultValue,
		Function<Value, T> mapFunc);

	Map<String, Object> get(
		String key, Map<String, Object> defaultValue);

	<T> Map<String, T> get(
		String key, Map<String, T> defaultValue,
		Function<Value, T> mapFunc);

	int get(String key, int defaultValue);

	long get(String key, long defaultValue);

	boolean get(String key, boolean defaultValue);

	String get(String key, String defaultValue);

	float get(String key, float defaultValue);

	double get(String key, double defaultValue);
}
