package io.openk9.datasource.plugindriver;

import io.vertx.core.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class HttpPluginDriverInfo {
	private boolean secure = false;
	private String host;
	private Integer port;
	private String path;
	private Method method;
	private Map<String, Object> body;

	public enum Method {
		GET(HttpMethod.GET), POST(HttpMethod.POST),
		PUT(HttpMethod.PUT), DELETE(HttpMethod.DELETE),
		PATCH(HttpMethod.PATCH), HEAD(HttpMethod.HEAD),
		OPTIONS(HttpMethod.OPTIONS);

		Method(HttpMethod httpMethod) {
			this.httpMethod = httpMethod;
		}

		public HttpMethod getHttpMethod() {
			return httpMethod;
		}

		private HttpMethod httpMethod;

	}

	public enum Schema {
		HTTP, HTTPS
	}

}
