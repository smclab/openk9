package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.util.IteratorWrapper;
import io.openk9.relationship.graph.api.client.Entity;
import io.openk9.relationship.graph.api.client.IsoDuration;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Point;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Type;
import io.openk9.relationship.graph.api.client.Value;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class ValueWrapper implements Value {

	@Override
	public int size() {
		return this.delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	@Override
	public Iterable<String> keys() {
		return this.delegate.keys();
	}

	@Override
	public Value get(int index) {
		return new ValueWrapper(this.delegate.get(index));
	}

	@Override
	public Type type() {
		return new TypeWrapper(this.delegate.type());
	}

	@Override
	public boolean hasType(Type type) {
		return this.delegate.hasType(((TypeWrapper)type).delegate);
	}

	@Override
	public boolean isTrue() {
		return this.delegate.isTrue();
	}

	@Override
	public boolean isFalse() {
		return this.delegate.isFalse();
	}

	@Override
	public boolean isNull() {
		return this.delegate.isNull();
	}

	@Override
	public Object asObject() {
		return this.delegate.asObject();
	}

	@Override
	public <T> T computeOrDefault(
		Function<Value, T> mapper, T defaultValue) {
		return this.delegate.computeOrDefault(
			value -> mapper.apply(new ValueWrapper(value)),
			defaultValue);
	}

	@Override
	public boolean asBoolean() {
		return this.delegate.asBoolean();
	}

	@Override
	public boolean asBoolean(boolean defaultValue) {
		return this.delegate.asBoolean(defaultValue);
	}

	@Override
	public byte[] asByteArray() {
		return this.delegate.asByteArray();
	}

	@Override
	public byte[] asByteArray(byte[] defaultValue) {
		return this.delegate.asByteArray(defaultValue);
	}

	@Override
	public String asString() {
		return this.delegate.asString();
	}

	@Override
	public String asString(String defaultValue) {
		return this.delegate.asString(defaultValue);
	}

	@Override
	public Number asNumber() {
		return this.delegate.asNumber();
	}

	@Override
	public long asLong() {
		return this.delegate.asLong();
	}

	@Override
	public long asLong(long defaultValue) {
		return this.delegate.asLong(defaultValue);
	}

	@Override
	public int asInt() {
		return this.delegate.asInt();
	}

	@Override
	public int asInt(int defaultValue) {
		return this.delegate.asInt(defaultValue);
	}

	@Override
	public double asDouble() {
		return this.delegate.asDouble();
	}

	@Override
	public double asDouble(double defaultValue) {
		return this.delegate.asDouble(defaultValue);
	}

	@Override
	public float asFloat() {
		return this.delegate.asFloat();
	}

	@Override
	public float asFloat(float defaultValue) {
		return this.delegate.asFloat(defaultValue);
	}

	@Override
	public List<Object> asList() {
		return this.delegate.asList();
	}

	@Override
	public List<Object> asList(List<Object> defaultValue) {
		return this.delegate.asList(defaultValue);
	}

	@Override
	public <T> List<T> asList(Function<Value, T> mapFunction) {
		return this.delegate.asList(
			value -> mapFunction.apply(new ValueWrapper(value)));
	}

	@Override
	public <T> List<T> asList(
		Function<Value, T> mapFunction, List<T> defaultValue) {
		return this.delegate.asList(
			value -> mapFunction.apply(new ValueWrapper(value)),
			defaultValue);
	}

	@Override
	public Entity asEntity() {
		return new EntityWrapper(this.delegate.asEntity());
	}

	@Override
	public Node asNode() {
		return new NodeWrapper(this.delegate.asNode());
	}

	@Override
	public Relationship asRelationship() {
		return new RelationshipWrapper(this.delegate.asRelationship());
	}

	@Override
	public Path asPath() {
		return new PathWrapper(this.delegate.asPath());
	}

	@Override
	public LocalDate asLocalDate() {
		return this.delegate.asLocalDate();
	}

	@Override
	public OffsetTime asOffsetTime() {
		return this.delegate.asOffsetTime();
	}

	@Override
	public LocalTime asLocalTime() {
		return this.delegate.asLocalTime();
	}

	@Override
	public LocalDateTime asLocalDateTime() {
		return this.delegate.asLocalDateTime();
	}

	@Override
	public OffsetDateTime asOffsetDateTime() {
		return this.delegate.asOffsetDateTime();
	}

	@Override
	public ZonedDateTime asZonedDateTime() {
		return this.delegate.asZonedDateTime();
	}

	@Override
	public IsoDuration asIsoDuration() {
		return new IsoDurationWrapper(this.delegate.asIsoDuration());
	}

	@Override
	public Point asPoint() {
		return new PointWrapper(this.delegate.asPoint());
	}

	@Override
	public LocalDate asLocalDate(LocalDate defaultValue) {
		return this.delegate.asLocalDate(defaultValue);
	}

	@Override
	public OffsetTime asOffsetTime(OffsetTime defaultValue) {
		return this.delegate.asOffsetTime(defaultValue);
	}

	@Override
	public LocalTime asLocalTime(LocalTime defaultValue) {
		return this.delegate.asLocalTime(defaultValue);
	}

	@Override
	public LocalDateTime asLocalDateTime(LocalDateTime defaultValue) {
		return this.delegate.asLocalDateTime(defaultValue);
	}

	@Override
	public OffsetDateTime asOffsetDateTime(OffsetDateTime defaultValue) {
		return this.delegate.asOffsetDateTime(defaultValue);
	}

	@Override
	public ZonedDateTime asZonedDateTime(ZonedDateTime defaultValue) {
		return this.delegate.asZonedDateTime(defaultValue);
	}

	@Override
	public IsoDuration asIsoDuration(IsoDuration defaultValue) {
		return new IsoDurationWrapper(this.delegate.asIsoDuration(
			((IsoDurationWrapper)defaultValue).delegate));
	}

	@Override
	public Point asPoint(Point defaultValue) {
		return new PointWrapper(
			this.delegate.asPoint(((PointWrapper)defaultValue).delegate));
	}

	@Override
	public Map<String, Object> asMap(Map<String, Object> defaultValue) {
		return this.delegate.asMap(defaultValue);
	}

	@Override
	public <T> Map<String, T> asMap(
		Function<Value, T> mapFunction,
		Map<String, T> defaultValue) {
		return this.delegate.asMap(
			value -> mapFunction.apply(new ValueWrapper(value)), defaultValue);
	}

	@Override
	public boolean containsKey(String key) {
		return this.delegate.containsKey(key);
	}

	@Override
	public Value get(String key) {
		return new ValueWrapper(this.delegate.get(key));
	}

	@Override
	public Iterable<Value> values() {
		return () -> IteratorWrapper.of(
			this.delegate.values().iterator(), ValueWrapper::new);
	}

	@Override
	public <T> Iterable<T> values(
		Function<Value, T> mapFunction) {
		return () -> IteratorWrapper.of(
			this.delegate.values(
				value -> mapFunction.apply(new ValueWrapper(value))).iterator(),
			Function.identity()
		);
	}

	@Override
	public Map<String, Object> asMap() {
		return this.delegate.asMap();
	}

	@Override
	public <T> Map<String, T> asMap(
		Function<Value, T> mapFunction) {
		return this.delegate.asMap(
			value -> mapFunction.apply(new ValueWrapper(value)));
	}

	@Override
	public Value get(
		String key, Value defaultValue) {
		return new ValueWrapper(
			this.delegate.get(key, ((ValueWrapper)defaultValue).delegate));
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public Number get(String key, Number defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public Entity get(String key, Entity defaultValue) {
		return new EntityWrapper(
			this.delegate.get(key, ((EntityWrapper)defaultValue).delegate));
	}

	@Override
	public Node get(String key, Node defaultValue) {
		return new NodeWrapper(
			this.delegate.get(key, ((NodeWrapper)defaultValue).getDelegate()));
	}

	@Override
	public Path get(String key, Path defaultValue) {
		return new PathWrapper(
			this.delegate.get(key, ((PathWrapper)defaultValue).delegate));
	}

	@Override
	public Relationship get(String key, Relationship defaultValue) {
		return new RelationshipWrapper(
			this.delegate.get(
				key, ((RelationshipWrapper)defaultValue).getDelegate()));
	}

	@Override
	public List<Object> get(String key, List<Object> defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public <T> List<T> get(
		String key, List<T> defaultValue,
		Function<Value, T> mapFunc) {
		return this.delegate.get(
			key, defaultValue,
			value -> mapFunc.apply(new ValueWrapper(value)));
	}

	@Override
	public Map<String, Object> get(
		String key, Map<String, Object> defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public <T> Map<String, T> get(
		String key, Map<String, T> defaultValue,
		Function<Value, T> mapFunc) {
		return this.delegate.get(
			key, defaultValue,
			value -> mapFunc.apply(new ValueWrapper(value)));
	}

	@Override
	public int get(String key, int defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public long get(String key, long defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public boolean get(String key, boolean defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public String get(String key, String defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public float get(String key, float defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public double get(String key, double defaultValue) {
		return this.delegate.get(key, defaultValue);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.Value getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.Value delegate;

}
