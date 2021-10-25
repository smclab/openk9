package io.openk9.datasource.emitter;

public interface InternalEmitter<T> {

	void send(T t);

}
