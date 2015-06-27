/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.springagram.frontend;

import static com.greglturnquist.springagram.frontend.WebSocketConfiguration.*;
import static org.springframework.amqp.support.AmqpHeaders.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Relay traffic picked up from the RabbitMQ broker to the WebSocket broker.
 *
 * @author Greg Turnquist
 */
// tag::code[]
@Component
@EnableRabbit
public class BackendTrafficListener {

	private static final String BACKEND_CHANNEL = "spring-a-gram";
	private static final Logger log = LoggerFactory.getLogger(BackendTrafficListener.class);

	private final SimpMessagingTemplate template;

	@Autowired
	public BackendTrafficListener(SimpMessagingTemplate template) {
		this.template = template;
	}

	@RabbitListener(queues = BACKEND_CHANNEL)
	public void handle(@Header(RECEIVED_ROUTING_KEY) String routingKey, String message) {

		log.info("Forwarding <" + message + "> to " + MESSAGE_PREFIX + "/" + routingKey);
		template.convertAndSend(MESSAGE_PREFIX + "/" + routingKey, message);
	}

	@Bean
	public Queue queue() {
		return new Queue(BACKEND_CHANNEL, false);
	}

}
// end::code[]
