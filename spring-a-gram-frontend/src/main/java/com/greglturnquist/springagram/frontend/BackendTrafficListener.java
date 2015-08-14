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

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Relay traffic picked up from the RabbitMQ broker to the WebSocket broker.
 *
 * @author Greg Turnquist
 */
// tag::code[]
@Component
public class BackendTrafficListener {

	private static final String BACKEND_CHANNEL = "spring-a-gram";
	private static final Logger log = LoggerFactory.getLogger(BackendTrafficListener.class);

	private final SimpMessagingTemplate template;

	@Autowired
	public BackendTrafficListener(SimpMessagingTemplate template) {
		this.template = template;
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory factory, MessageListener messageListener) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(factory);
		container.addMessageListener(messageListener, new PatternTopic("/topic/*"));
		return container;
	}

	@Bean
	MessageListener messageListener() {

		return (message, pattern) ->
				handle(new String(message.getBody(),Charset.defaultCharset()),
						new String(message.getChannel(),Charset.defaultCharset()));
	}

	public void handle(String message, String destination) {

		log.error("Forwarding <" + message + "> to " + destination);
		template.convertAndSend(destination, message);
	}

}
// end::code[]
