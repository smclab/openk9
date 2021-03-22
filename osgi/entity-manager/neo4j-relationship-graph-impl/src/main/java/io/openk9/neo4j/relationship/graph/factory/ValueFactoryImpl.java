package io.openk9.neo4j.relationship.graph.factory;

import io.openk9.neo4j.relationship.graph.client.IsoDurationWrapper;
import io.openk9.neo4j.relationship.graph.client.NodeWrapper;
import io.openk9.neo4j.relationship.graph.client.PathWrapper;
import io.openk9.neo4j.relationship.graph.client.PointWrapper;
import io.openk9.neo4j.relationship.graph.client.RelationshipWrapper;
import io.openk9.neo4j.relationship.graph.client.ValueWrapper;
import io.openk9.relationship.graph.api.client.IsoDuration;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Point;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Value;
import io.openk9.relationship.graph.api.factory.EntityFactory;
import io.openk9.relationship.graph.api.factory.IsoDurationFactory;
import io.openk9.relationship.graph.api.factory.PathFactory;
import io.openk9.relationship.graph.api.factory.PointFactory;
import io.openk9.relationship.graph.api.factory.ValueFactory;
import org.neo4j.driver.internal.value.BooleanValue;
import org.neo4j.driver.internal.value.BytesValue;
import org.neo4j.driver.internal.value.DateTimeValue;
import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.DurationValue;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.LocalDateTimeValue;
import org.neo4j.driver.internal.value.LocalTimeValue;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.internal.value.PointValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.internal.value.TimeValue;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = ValueFactory.class
)
public class ValueFactoryImpl implements ValueFactory {

	@Override
	public Value createBooleanValue(boolean value) {
		return new ValueWrapper(value ? BooleanValue.TRUE : BooleanValue.FALSE);
	}

	@Override
	public Value createBytesValue(byte[] value) {
		return new ValueWrapper(new BytesValue(value));
	}

	@Override
	public Value createDateTimeValue(ZonedDateTime value) {
		return new ValueWrapper(new DateTimeValue(value));
	}

	@Override
	public Value createDateValue(LocalDate value) {
		return new ValueWrapper(new DateValue(value));
	}

	@Override
	public Value createDurationValue(
		Function<IsoDurationFactory, IsoDuration> isoDurationFunction) {
		return new ValueWrapper(
			new DurationValue(
				((IsoDurationWrapper)isoDurationFunction.apply(
					_isoDurationFactory)).getDelegate())
		);
	}

	@Override
	public Value createFloatValue(float value) {
		return new ValueWrapper(new FloatValue(value));
	}

	@Override
	public Value createIntegerValue(int value) {
		return new ValueWrapper(new IntegerValue(value));
	}

	@Override
	public Value createListValue(Value...values) {

		org.neo4j.driver.Value[] deWrap = Arrays
			.stream(values)
			.map(value -> ((ValueWrapper) value).getDelegate())
			.toArray(org.neo4j.driver.Value[]::new);

		return new ValueWrapper(new ListValue(deWrap));
	}

	@Override
	public Value createLocalDateTimeValue(LocalDateTime localDateTime) {
		return new ValueWrapper(new LocalDateTimeValue(localDateTime));
	}

	@Override
	public Value createLocalTimeValue(LocalTime localTime) {
		return new ValueWrapper(new LocalTimeValue(localTime));
	}

	@Override
	public Value createMapValue(Map<String, Value> val) {

		Map<String, org.neo4j.driver.Value> collect = val
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> ((ValueWrapper) entry.getValue()).getDelegate()));

		return new ValueWrapper(new MapValue(collect));
	}

	@Override
	public Value createNodeValue(
		Function<EntityFactory, Node> function) {
		return new ValueWrapper(
			new NodeValue(
				((NodeWrapper)function.apply(_entityFactory)).getDelegate()));
	}

	@Override
	public Value createNullValue() {
		return new ValueWrapper(NullValue.NULL);
	}

	@Override
	public Value createPathValue(
		Function<PathFactory, Path> function) {
		return new ValueWrapper(
			new PathValue(
				((PathWrapper)function.apply(_pathFactory)).getDelegate()
			)
		);
	}

	@Override
	public Value createPointValue(
		Function<PointFactory, Point> function) {
		return new ValueWrapper(
			new PointValue(
				((PointWrapper)function.apply(_pointFactory)).getDelegate()
			)
		);
	}

	@Override
	public Value createRelationshipValue(
		Function<EntityFactory, Relationship> function) {
		return new ValueWrapper(
			new RelationshipValue(
				((RelationshipWrapper)function.apply(_entityFactory)).getDelegate()
			)
		);
	}

	@Override
	public Value createStringValue(String value) {
		return new ValueWrapper(new StringValue(value));
	}

	@Override
	public Value createTimeValue(OffsetTime value) {
		return new ValueWrapper(new TimeValue(value));
	}

	@Reference
	private IsoDurationFactory _isoDurationFactory;

	@Reference
	private EntityFactory _entityFactory;

	@Reference
	private PathFactory _pathFactory;

	@Reference
	private PointFactory _pointFactory;

}
