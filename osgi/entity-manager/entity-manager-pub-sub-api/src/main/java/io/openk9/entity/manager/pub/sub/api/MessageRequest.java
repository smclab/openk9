package io.openk9.entity.manager.pub.sub.api;

import io.openk9.entity.manager.model.payload.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class MessageRequest {
	private Request request;
}
