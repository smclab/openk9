package io.openk9.tika.util;

import io.quarkus.tika.TikaContent;
import io.quarkus.tika.TikaParser;
import io.smallrye.mutiny.Uni;
import org.xml.sax.ContentHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;

@ApplicationScoped
public class TikaParserAsync {

	public Uni<TikaContent> parse(InputStream stream) {
		return Unis.toBlockingToUni(() -> _tikaParser.parse(stream));
	}

	public Uni<TikaContent> parse(InputStream stream, ContentHandler contentHandler) {
		return Unis.toBlockingToUni(() -> _tikaParser.parse(stream, contentHandler));
	}

	public Uni<TikaContent> parse(InputStream stream, String contentType) {
		return Unis.toBlockingToUni(() -> _tikaParser.parse(stream, contentType));
	}

	public Uni<TikaContent> parse(InputStream stream, String contentType, ContentHandler contentHandler) {
		return Unis.toBlockingToUni(() -> _tikaParser.parse(stream, contentType, contentHandler));
	}

	public Uni<String> getText(InputStream stream) {
		return Unis.toBlockingToUni(() -> _tikaParser.getText(stream));
	}

	public Uni<String> getText(InputStream stream, ContentHandler contentHandler) {
		return Unis.toBlockingToUni(() -> _tikaParser.getText(stream, contentHandler));
	}

	public Uni<String> getText(InputStream stream, String contentType) {
		return Unis.toBlockingToUni(() -> _tikaParser.getText(stream, contentType));
	}

	public Uni<String> getText(InputStream stream, String contentType, ContentHandler contentHandler) {
		return Unis.toBlockingToUni(() -> _tikaParser.getText(stream, contentType, contentHandler));
	}

	public Uni<io.quarkus.tika.TikaMetadata> getMetadata(InputStream stream) {
		return Unis.toBlockingToUni(() -> _tikaParser.getMetadata(stream));
	}

	public Uni<io.quarkus.tika.TikaMetadata> getMetadata(InputStream stream, String contentType) {
		return Unis.toBlockingToUni(() -> _tikaParser.getMetadata(stream, contentType));
	}

	@Inject
	TikaParser _tikaParser;

}
