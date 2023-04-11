package com.youhogeon.credits.receiver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParseException;
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
@Qualifier("twip")
@Slf4j
@RequiredArgsConstructor
public class TwipReceiver implements Receiver {

    private final JacksonJsonParser jacksonJsonParser;
    private final ConfigService configService;

    private List<MessageHandler<Donation>> donationHandlers = new ArrayList<>();
    private URI uri;

    private final String ALERTBOX_URL = "https://twip.kr/widgets/alertbox/";
    private final Pattern versionPattern = Pattern.compile("version: '(.*)'");
    private final Pattern tokenPattern = Pattern.compile("window.__TOKEN__ = '(.*)'");
    private final Pattern messagePattern = Pattern.compile("^[0-9]+(.*)$");

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

            log.info("Attempt to reconnect to the TWIP server...");
            init();
        }).start();
    }

    @PostConstruct
    private void init() {
        try {
            ensureURICreated();
        } catch (IOException e) {
            log.error("Can't connect to TWIP.", e);
            retry();

            return;
        } catch (IllegalStateException e) {
            log.error("Information cannot be retrieved due to TWIP version change.", e);
            retry();

            return;
        }

        WebSocketClient client = new StandardWebSocketClient();
        client.execute(new WSHandler(), null, uri);
    }

    private void ensureURICreated() throws IOException {
        String accessKey = configService.getTwipKey();

        String script = parseScriptContent(ALERTBOX_URL + accessKey);

        if (script == null) throw new IOException("Can't load script.");

        String version = parsePattern(versionPattern, script);
        String token = parsePattern(tokenPattern, script);

        if (version == null) throw new IllegalStateException("version is null.");
        if (token == null) throw new IllegalStateException("token is null.");

        try {
            uri = new URI("wss://io.mytwip.net/socket.io/?alertbox_key=" + accessKey + "&version=" + version + "&token=" + encodeURIComponent(token) + "&EIO=3&transport=websocket");
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URI is invalid.");
        }
    }

    private String parsePattern(Pattern p, String str) {
        Matcher m = p.matcher(str);

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
                    session.sendMessage(new TextMessage("2"));
                } catch (Exception e) {
                    log.error("An error occurred while communicating with the TWIP server.", e);
                }
            })).start();
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            isActive = true;
            log.info("You are connected to the TWIP server.");

            ping(session);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            String payload = (String) message.getPayload();

            if (payload.equals("3")) {
                ping(session);

                return;
            }

            String body = parsePattern(messagePattern, payload);

            List<?> list = null;
            String command = null;
            Map<?, ?> map = null;

            try {
                list = jacksonJsonParser.parseList((String) body);
                command = (String) list.get(0);
                map = (Map<?, ?>) list.get(1);
                if (list == null || command == null || map == null) return;

                Donation donation = new Donation();
                donation.setId(UUID.randomUUID().toString());
                donation.setNickname((String) map.get("nickname"));
                
                if (command.equals("new donate")) {
                    donation.setType(Donation.Type.DONATION);
                    donation.setPlatform(Donation.Platform.TWIP);
                    donation.setId((String) map.get("_id"));
                    donation.setComment((String) map.get("comment"));
                    donation.setAmount((Integer) map.get("amount"));
                } else if (command.equals("new follow")) {
                    donation.setType(Donation.Type.FOLLOW);
                } else if (command.equals("new sub")) {
                    donation.setType(Donation.Type.SUBSCRIPTION);
                    donation.setComment((String) map.get("message"));
                    donation.setNickname((String) map.get("username"));
                    donation.setAmount((Integer) map.get("months"));
                } else if (command.equals("new cheer")) {
                    donation.setType(Donation.Type.BITS);
                    donation.setComment((String) map.get("comment"));
                    donation.setAmount((Integer) map.get("amount"));
                } else if (command.equals("new hosting")) {
                    donation.setType(Donation.Type.RAID);
                    donation.setAmount((Integer) map.get("viewers"));
                    donation.setNickname((String) map.get("username"));
                }
    
                if (donation.getType() == null) return;
    
                donationHandlers.forEach(h -> h.handle(donation));
            } catch (JsonParseException | ClassCastException | IndexOutOfBoundsException e1) {
                return;
            } catch (Exception e) {
                log.error("The message received from the TWIP server is incorrect.", e);
            }

        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("An error occurred while communicating with the TWIP server.", exception);
            
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            isActive = false;
            log.info("The connection with the TWIP server has been terminated.", new IOException(closeStatus.getReason()));

            retry();
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

    }
}
