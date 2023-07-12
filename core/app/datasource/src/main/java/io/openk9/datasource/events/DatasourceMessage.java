package io.openk9.datasource.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
	value = {
		@JsonSubTypes.Type(value = DatasourceMessage.Failure.class, name = "FAILURE"),
		@JsonSubTypes.Type(value = DatasourceMessage.Delete.class, name = "DELETE"),
		@JsonSubTypes.Type(value = DatasourceMessage.New.class, name = "NEW"),
		@JsonSubTypes.Type(value = DatasourceMessage.Update.class, name = "UPDATE")
	}
)
@Data
@SuperBuilder(toBuilder = true)
public abstract class DatasourceMessage {
	protected String ingestionId;
	protected Long datasourceId;
	protected String contentId;
	protected Long parsingDate;
	protected String tenantId;
	protected String indexName;

	@Getter
	@Setter
	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Failure extends DatasourceMessage {
		private String error;
	}

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Delete extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class New extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Update extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Unknown extends DatasourceMessage { }

}
