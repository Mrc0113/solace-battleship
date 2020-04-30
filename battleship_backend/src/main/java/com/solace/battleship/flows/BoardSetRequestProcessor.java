package com.solace.battleship.flows;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;

import com.solace.battleship.events.BoardSetRequest;
import com.solace.battleship.events.BoardSetResult;

/**
 * This Spring Cloud Stream processor handles board set requests for the
 * Battleship Game
 *
 * @author Andrew Roberts
 */
public class BoardSetRequestProcessor extends AbstractRequestProcessor<BoardSetRequest> {

	@Bean
	public void boardSetRequest(BoardSetRequest boardSetRequest, @Header("reply-to") String replyTo) {
		// Pass the request to the game engine to set the board
		BoardSetResult result = gameEngine.requestToSetBoard(boardSetRequest);
		// Send the result of the BoardSetRequest to the replyTo destination retrieved
		// from the message header
		resolver.resolveDestination(replyTo).send(message(result));
		// If the result was a succesful board set and if both player's have joined,
		// then publish a Match Start Message
		if (result.isSuccess() && gameEngine.canMatchStart(boardSetRequest.getSessionId())) {
			resolver.resolveDestination(
					"SOLACE/BATTLESHIP/" + boardSetRequest.getSessionId() + "/MATCH-START/CONTROLLER")
					.send(message(gameEngine.getMatchStartAndStartMatch(boardSetRequest.getSessionId())));
		}

	}

}