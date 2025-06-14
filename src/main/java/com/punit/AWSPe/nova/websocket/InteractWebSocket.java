package com.punit.AWSPe.nova.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import com.punit.AWSPe.nova.utility.InteractObserver;
import com.punit.AWSPe.nova.utility.NovaSonicBedrockInteractClient;
import com.punit.AWSPe.nova.utility.OutputEventsInteractObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class InteractWebSocket extends BinaryWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(InteractWebSocket.class);

    private final NovaSonicBedrockInteractClient interactClient;
    private AtomicBoolean expectedInitialRequest = new AtomicBoolean(true);
    private WebSocketSession session;
    private InteractObserver<String> inputObserver;

    public InteractWebSocket(NovaSonicBedrockInteractClient interactClient) {
        this.interactClient = interactClient;
    }

    public void onWebSocketConnect(WebSocketSession session) {
        log.info("Web socket connected session={}", session);
        this.session = session;
    }


    public void onWebSocketText(String jsonText) {
        if (expectedInitialRequest.compareAndSet(true, false)) {
            handleInitialRequest(jsonText);
        } else {
            handleRemainingRequests(jsonText);
        }
    }

    private void handleRemainingRequests(String jsonMsg) {
        try {
            log.info("Parsing msg jsonText={}", jsonMsg);
            inputObserver.onNext(jsonMsg);
        } catch (Exception e) {
            log.error("Error handling remaining requests", e);
            inputObserver.onError(e);
        }
    }

    private void handleInitialRequest(String jsonInitialRequestText) {
        try {
            log.info("Parsing initial request jsonText={}", jsonInitialRequestText);
            OutputEventsInteractObserver outputObserver = new OutputEventsInteractObserver(session);
            inputObserver = interactClient.interactMultimodal(jsonInitialRequestText, outputObserver);
            outputObserver.setInputObserver(inputObserver);
        } catch (Exception e) {
            log.error("Error handling initial request", e);
            inputObserver.onError(e);
        }
    }

  //  @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        throw new UnsupportedOperationException("Binary websocket not yet implemented");
    }

  //  @Override
    public void onWebSocketError(Throwable t) {
        log.error("WebSocket error", t);
        throw new RuntimeException("WebSocket error", t);
    }

  //  @Override
    public void onWebSocketClose(int statusCode, String reason) {
        log.info("onWebSocketClose: code={} reason={}", statusCode, reason);
        if (inputObserver != null) {
            inputObserver.onComplete();
        }
    }
}
