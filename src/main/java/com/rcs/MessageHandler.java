package com.rcs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import am.ik.yavi.core.ConstraintViolations;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static java.util.Collections.singletonMap;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class MessageHandler {
	private final List<Message> messages = new CopyOnWriteArrayList<>();

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/messages", this::getMessages) //
				.POST("/messages", this::postMessage) //
				.build();
	}

	Mono<ServerResponse> getMessages(ServerRequest req) {
		return ok().bodyValue(this.messages);
	}

	Mono<ServerResponse> postMessage(ServerRequest req) {
		return req.bodyToMono(Message.class)
				.flatMap(message -> Message.validator.validateToEither(message)
						.doOnRight(this.messages::add)
						.leftMap(ConstraintViolations::details)
						.fold(v -> badRequest().bodyValue(singletonMap("details", v)),
								body -> ok().bodyValue(body)));
	}
}
