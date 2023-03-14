package io.openk9.datasource.index.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatResponse {
	private String health;
	private String status;
	private String index;
	private String uuid;
	private String pri;
	private String rep;
	private String docsCount;
	private String docsDeleted;
	private long storeSize;
	private long priStoreSize;

	@JsonProperty(value = "docs.count")
	public void setDocsCount(String docsCount) {
		this.docsCount = docsCount;
	}

	@JsonProperty(value = "docs.deleted")
	public void setDocsDeleted(String docsDeleted) {
		this.docsDeleted = docsDeleted;
	}

	@JsonProperty(value = "store.size")
	public void setStoreSize(long storeSize) {
		this.storeSize = storeSize;
	}

	@JsonProperty(value = "pri.store.size")
	public void setPriStoreSize(long priStoreSize) {
		this.priStoreSize = priStoreSize;
	}

}
