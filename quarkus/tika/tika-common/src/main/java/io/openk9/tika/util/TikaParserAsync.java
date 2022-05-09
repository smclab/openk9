/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
