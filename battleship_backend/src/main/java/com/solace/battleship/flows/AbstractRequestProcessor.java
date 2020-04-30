package com.solace.battleship.flows;

import com.solace.battleship.engine.IGameEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;

/**
 * AbstracRequestProcessor
 */
public abstract class AbstractRequestProcessor<Request> {

	@Autowired
	// Since the output destination will be dynamic, we will need a custom resolver
	protected BinderAwareChannelResolver resolver;

	@Autowired
	// An internal construct in order to handle game sessions
	protected IGameEngine gameEngine;

	protected static final <T> Message<T> message(T val) {
		return MessageBuilder.withPayload(val).build();
	}

}