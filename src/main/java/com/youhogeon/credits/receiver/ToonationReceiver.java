package com.youhogeon.credits.receiver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.youhogeon.credits.entity.Donation;
import com.youhogeon.credits.service.ConfigService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.youhogeon.credits.util.Http.*;

@Component
@Qualifier("toonation")
@Slf4j
@RequiredArgsConstructor
public class ToonationReceiver implements Receiver {

    private final JacksonJsonParser jacksonJsonParser;
    private final ConfigService configService;

    private List<MessageHandler<Donation>> donationHandlers = new ArrayList<>();
    private URI uri;

    private final String ALERTBOX_URL = "https://toon.at/widget/alertbox/";
    private final String WS_URL = "wss://toon.at:8071/";
    private final Pattern p = Pattern.compile("\"payload\":\"(.*)\",");

    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    private void retry() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("Thread sleep error.", e);
            }

            log.info("Attempt to reconnect to the Toonation server...");
            init();
        }).start();
    }

    @PostConstruct
    private void init() {
        try {
            ensureURICreated();
        } catch (IOException e) {
            log.error("Can't connect to Toonation.", e);
            retry();

            return;
        } catch (IllegalStateException e) {
            log.error("Information cannot be retrieved due to Toonation version change.", e);
            retry();

            return;
        }

        WebSocketClient client = new StandardWebSocketClient();
        client.execute(new WSHandler(), null, uri);
    }
    
    private void ensureURICreated() throws IOException {
        String accessKey = configService.getToonationKey();

        String script = parseScriptContent(ALERTBOX_URL + accessKey);

        if (script == null) throw new IOException("Can't load script.");

        String payload = parsePayload(script);

        if (payload == null) throw new IllegalStateException("payload is null.");

        try {
            uri = new URI(WS_URL + payload);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URI is invalid.");
        }
    }

    private String parsePayload(String script) {
        Matcher m = p.matcher(script);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    @Override
    public void subscribeDonation(MessageHandler<Donation> handler) {
        donationHandlers.add(handler);
    }

    private class WSHandler implements WebSocketHandler {

        private void ping(WebSocketSession session) {
            (new Thread(() -> {
                try {
                    Thread.sleep(1000 * 25);
                    session.sendMessage(new TextMessage("#ping"));
                } catch (Exception e) {
                    log.error("An error occurred while communicating with the Toonation server.", e);
                }
            })).start();
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            isActive = true;
            log.info("You are connected to the Toonation server.");
            session.setTextMessageSizeLimit(30000);
            ping(session);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            String payload = (String) message.getPayload();

            if (payload.equals("#pong")) {
                ping(session);

                return;
            }

            try {
                Map<String, Object> map = jacksonJsonParser.parseMap(payload);
                Map<String, Object> content = (Map<String, Object>) map.get("content");
    
                if (map == null || content == null || content.size() == 0) return;
    
                Donation donation = new Donation();
                donation.setId((String) content.get("account"));
                donation.setNickname((String) content.get("name"));
                donation.setComment((String) content.get("message"));
                int command = (Integer) map.get("code");
    
                if (command == 101) {
                    donation.setType(Donation.Type.DONATION);
                    donation.setPlatform(Donation.Platform.TOONATION);
                    donation.setAmount((Integer) content.get("amount"));
                } else if (command == 102) {
                    donation.setType(Donation.Type.SUBSCRIPTION);
                    donation.setAmount((Integer) content.get("amount"));
                } else if (command == 103) {
                    donation.setType(Donation.Type.FOLLOW);
                    donation.setAmount(0);
                } else if (command == 104) {
                    donation.setType(Donation.Type.RAID);
                    donation.setAmount((Integer) content.get("count"));
                } else if (command == 107) {
                    donation.setType(Donation.Type.BITS);
                    donation.setAmount((Integer) content.get("count"));
                }
    
                if (donation.getType() == null) return;
    
                donationHandlers.forEach(h -> h.handle(donation));
            } catch (Exception e) {
                log.error("The message received from the Toonation server is incorrect.", e);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("An error occurred while communicating with the Toonation server.", exception);
            
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            log.info("The connection with the Toonation server has been terminated.", new IOException(closeStatus.getReason()));
            isActive = false;
            retry();
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
        
    }

}
