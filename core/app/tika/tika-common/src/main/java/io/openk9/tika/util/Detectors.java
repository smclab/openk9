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

import io.smallrye.mutiny.Uni;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class Detectors {

	public MediaType detect(InputStream stream) throws IOException {

		stream = stream instanceof BufferedInputStream
			? stream
			: new BufferedInputStream(stream);

		return _detector.detect(stream, new Metadata());

	}

	public Uni<MediaType> detectAsync(InputStream stream) {

		return Unis.toBlockingToUni(() -> detect(stream));

	}

	private final Detector _detector = new DefaultDetector();

}
