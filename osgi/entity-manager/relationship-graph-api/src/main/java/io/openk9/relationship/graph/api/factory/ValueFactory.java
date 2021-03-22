package io.openk9.relationship.graph.api.factory;

import io.openk9.relationship.graph.api.client.IsoDuration;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Point;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

public interface ValueFactory {

	Value createBooleanValue(boolean value);

	Value createBytesValue(byte[] value);

	Value createDateTimeValue(ZonedDateTime value);

	Value createDateValue(LocalDate value);

	Value createDurationValue(
		Function<IsoDurationFactory, IsoDuration> isoDurationFunction);

	Value createFloatValue(float value);

	Value createIntegerValue(int value);

	Value createListValue(Value...values);

	Value createLocalDateTimeValue(LocalDateTime localDateTime);

	Value createLocalTimeValue(LocalTime localTime);

	Value createMapValue(Map<String, Value> val);

	Value createNodeValue(Function<EntityFactory, Node> function);

	Value createNullValue();

	Value createPathValue(Function<PathFactory, Path> function);

	Value createPointValue(Function<PointFactory, Point> function);

	Value createRelationshipValue(
		Function<EntityFactory, Relationship> function);

	Value createStringValue(String value);

	Value createTimeValue(OffsetTime value);

}
