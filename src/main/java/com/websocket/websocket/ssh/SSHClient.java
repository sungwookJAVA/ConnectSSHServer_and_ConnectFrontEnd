package com.websocket.websocket.ssh;

import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.*;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Component
public class SSHClient {
    private final String user = "test";
    private final String password = "toor";
    private final String host = "127.0.0.1";
    private int port = 2222;
    private WebSocketSession webSocketSession;
    private Session session = null;
    private ChannelShell channel = null;
    private JSch jSch = new JSch();
    private InputStream inputStream;
    PrintStream ps;

    /***
     * 웹소켓 세션 설정
     * @param session
     */
    public void initConnection(WebSocketSession session){
        this.webSocketSession = session;
    }

    /***
     * SSH서버로 연결
     */
    public void connect_to_server(){
        try {
            // 1. 세션 생성
            session = jSch.getSession(user, host, port);
            session.setPassword(password);

            // 2. 세션 설정
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // 3. ssh 서버 연결
            session.connect();
            log.info("[*] Session is created");

            // 4. 채널(shell) 생성과 출력 스트림(서버에게 전달하는 스트림) 설정
            channel = (ChannelShell) session.openChannel("shell");
            OutputStream ops = channel.getOutputStream();
            ps = new PrintStream(ops);
            channel.connect(3000);
            log.info("[*] exec Channel is created");

            // 5. 채널 입력 스트림 설정(명령어 결과를 전달 받는 스트림)
            inputStream = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String output;
            while(true){
                // 결과가 없을 때까지 스트림 복사
                while((output = reader.readLine()) != null){
                    webSocketSession.sendMessage(new TextMessage(output));
                }
                if(channel.isClosed()){
                    log.info("[*] 4. Channel is disconnected");
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (JSchException e) { // ssh Exceptions
            log.error("[-] SSHClient Error");
            e.printStackTrace();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        } catch (IOException e) { // stream Exceptions
            log.error("[-] stream Error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /***
     * ssh서버에게 실행할 명령어 전달
     * @param command
     */
    public void transToSSH(String command){
        if (channel != null) {
            ps.println(command);
            ps.flush();
        }
    }

    /***
     * SSH연결 종료
     */
    public void disconnect(){
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
        log.info("[*] Session and Channel are disconnected");
    }
}
