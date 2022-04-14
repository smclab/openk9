package io.openk9.ingestion.web;

import com.google.protobuf.Empty;
import io.openk9.ingestion.grpc.Ingestion;
import io.openk9.ingestion.grpc.IngestionRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;

@GrpcService
public class IngestionService implements Ingestion {

	@Override
	public Uni<Empty> ingestion(IngestionRequest request) {

		return Uni
			.createFrom()
			.completionStage(() -> emitter.emit(request))
			.replaceWith(() -> Empty.newBuilder().build());

	}

	@Inject
	IngestionEmitter emitter;

}