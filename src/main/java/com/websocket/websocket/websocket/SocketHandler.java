package com.websocket.websocket.websocket;

import com.jcraft.jsch.JSchException;
import com.websocket.websocket.ssh.SSHClient;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint(value = "/sock")
@Log
public class SocketHandler {
    private Session session;
    private static Set<SocketHandler> sockets = new CopyOnWriteArraySet<>();
    private static int onlineCount = 0;
    private SSHClient sshClient;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /***
     * 소켓이 연결될 때
     * @param session
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        onlineCount++;
        sockets.add(this); // 소켓 추가
        log.info("[*] Socket is connected");

        sshClient = SSHClient.builder()
            .session(session)
            .build();


        executorService.execute(new Runnable() {
            @Override
            public void run() {
                sshClient.connect_to_server();
            }
        });
    }

    /***
     * 클라이언트로부터 전달받은 메세지를 실행
     * @param message
     */
    @OnMessage
    public void onMessage(String message){
        sshClient.transToSSH(message);
    }

    /***
     * 연결 종료
     */
    @OnClose
    public void onClose(){
        sockets.remove(this);
        onlineCount--;
        sshClient.disconnect();

        log.info("[*] Socket is disconnected");
    }
}
