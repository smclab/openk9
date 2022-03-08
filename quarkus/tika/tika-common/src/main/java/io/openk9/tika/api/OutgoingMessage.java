package io.openk9.tika.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
public class OutgoingMessage {
	private final String exchange;
	private String routingKey = "#";
	private final byte[] body;
}
