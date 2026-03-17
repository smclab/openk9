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

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A detector that wraps {@link DefaultDetector} with exception handling
 * to work around a bug in Tika 3.2.2 where
 * {@code DefaultZipContainerDetector.detectArchiveFormat()} only catches
 * {@code IOException} but {@code ArchiveStreamFactory.detect()} throws
 * {@code ArchiveException} (which extends {@code Exception}, not
 * {@code IOException}) for non-archive streams like PDFs.
 */
public class SafeDetector implements Detector {

	private static final Logger LOG =
		LoggerFactory.getLogger(SafeDetector.class);

	private final Detector delegate = new DefaultDetector();
	private final Detector fallback = MimeTypes.getDefaultMimeTypes();

	@Override
	public MediaType detect(InputStream input, Metadata metadata)
		throws IOException {

		try {
			return delegate.detect(input, metadata);
		}
		catch (Exception e) {
			if (input != null && input.markSupported()) {
				input.reset();
			}

			LOG.warn(
				"DefaultDetector failed, falling back to MIME magic " +
					"detection: {}",
				e.getMessage());

			return fallback.detect(input, metadata);
		}
	}

}
