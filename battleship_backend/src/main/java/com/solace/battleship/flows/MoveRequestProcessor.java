package com.solace.battleship.flows;

import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;

import com.solace.battleship.events.Move;
import com.solace.battleship.events.MoveResponseEvent;

/**
 * This Spring Cloud Stream processor handles move requests for the Battleship
 * Game
 *
 * @author Andrew Roberts
 */
@SpringBootApplication
public class MoveRequestProcessor extends AbstractRequestProcessor<Move> {

	@Bean
	public Consumer<Move> moveRequest(@Header("reply-to") String replyTo) {
		return moveRequest -> {
			// Pass the request to make a move
			MoveResponseEvent result = gameEngine.requestToMakeMove(moveRequest);
			// Send the result of the MoveRequest to the replyTo destination retrieved from
			// the message header
			resolver.resolveDestination(replyTo).send(message(result));
			// Update the board once the move has been made
			gameEngine.updateBoard(result);
			// If the match should be ended due to a score being 0, publish a match end
			// message
			if (gameEngine.shouldMatchEnd(moveRequest.getSessionId())) {
				resolver.resolveDestination("SOLACE/BATTLESHIP/" + moveRequest.getSessionId() + "/MATCH-END/CONTROLLER")
						.send(message(gameEngine.endMatch(moveRequest.getSessionId())));
			}
		};
	}

}