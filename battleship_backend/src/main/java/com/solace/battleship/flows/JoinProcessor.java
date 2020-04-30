package com.solace.battleship.flows;

import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;

import com.solace.battleship.events.JoinResult;
import com.solace.battleship.events.PlayerJoined;

/**
 * This Spring Cloud Stream processor handles join-requests for the Battleship
 * Game
 *
 * @author Thomas Kunnumpurath
 */
@SpringBootApplication
public class JoinProcessor extends AbstractRequestProcessor<PlayerJoined> {

	@Bean
	public Consumer<PlayerJoined> joinRequest(@Header("reply-to") String replyTo) {
		return joinRequest -> {
			// Pass the request to the game engine to join the game
			JoinResult result = gameEngine.requestToJoinGame(joinRequest);
			// Send the result of the JoinRequest to the replyTo destination retrieved from
			// the message header
			resolver.resolveDestination(replyTo).send(message(result));
			// If the result was a successful join and if both player's have joined, then
			// publish a game start message
			if (result.isSuccess() && gameEngine.canGameStart(joinRequest.getSessionId())) {
				resolver.resolveDestination(
						"SOLACE/BATTLESHIP/" + joinRequest.getSessionId() + "/GAME-START/CONTROLLER")
						.send(message(gameEngine.getGameStartAndStartGame(joinRequest.getSessionId())));
			}
		};
	}

}