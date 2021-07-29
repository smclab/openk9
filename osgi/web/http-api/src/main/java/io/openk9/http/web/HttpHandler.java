package io.openk9.http.web;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.function.BiFunction;

public interface HttpHandler
	extends BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

	int GET =  		0b0_0_0_0_0_1;

	int POST = 		0b0_0_0_0_1_0;

	int PUT = 		0b0_0_0_1_0_0;

	int DELETE = 	0b0_0_1_0_0_0;

	int PATCH = 	0b0_1_0_0_0_0;

	int OPTIONS = 	0b1_0_0_0_0_0;

	int ALL = GET | POST | PUT | DELETE | PATCH | OPTIONS;

}
