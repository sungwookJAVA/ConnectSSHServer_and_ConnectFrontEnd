package com.websocket.websocket.websocket;

import com.websocket.websocket.ssh.SSHClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint(value = "/sock")
@Slf4j
@RequiredArgsConstructor
public class SocketHandler implements WebSocketHandler {
    private final SSHClient sshClient;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /***
     * 연결을 성공적으로 마친 후 이벤트
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[+] websocket is connected");
        sshClient.initConnection(session);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                sshClient.connect_to_server();
            }
        });
    }

    /***
     * 메세지수신 이벤트
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if(message instanceof TextMessage){
            String command = message.getPayload().toString();
            sshClient.transToSSH(command);
        }
    }

    /***
     * 에러발생 이벤트
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[-] websocket occur error");
    }

    /***
     * websocket 연결종료 이벤트
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("[+] websocket is disconnected");
        sshClient.disconnect();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
