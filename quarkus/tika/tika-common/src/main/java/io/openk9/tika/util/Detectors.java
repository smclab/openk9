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
