package io.openk9.datasource.util;

import io.smallrye.mutiny.subscription.UniEmitter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.ActionListener;

@RequiredArgsConstructor
public class UniActionListener<Response> implements ActionListener<Response> {

	@Override
	public void onResponse(Response response) {
		sink.complete(response);
	}

	@Override
	public void onFailure(Exception e) {
		sink.fail(e);
	}

	public static <T> ActionListener<T> of(UniEmitter<? super T> sink) {
		return new UniActionListener<>((UniEmitter<T>)sink);
	}

	private final UniEmitter<Response> sink;

}
