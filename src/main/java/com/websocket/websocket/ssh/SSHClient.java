package com.websocket.websocket.ssh;

import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SSHClient {
    private final String user = "test";
    private final String password = "toor";
    private final String host = "127.0.0.1";
    private int port = 2222;
    private javax.websocket.Session websocket_session;
    private Session session = null;
    private Channel channel = null;
    private ChannelExec channelExec;
    private JSch jSch = new JSch();
    private InputStream inputStream;

    @Builder
    public SSHClient(javax.websocket.Session session) {
        this.websocket_session = session;
    }

    /***
     * SSH서버로 연결
     */
    public void connect_to_server(){
        try {
            // 1. Create a session
            session = jSch.getSession(user, host, port);
            session.setPassword(password);

            // 2. Config a session
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // 3. Connect the ssh server
            session.connect();
            log.info("[*] Session is created");

            // 4. Create a channel
            channel = session.openChannel("shell");
            channel.connect(3000);
            log.info("[*] exec Channel is created");

            // 5. configure sterams
            channel.setInputStream(null);
            inputStream = channel.getInputStream();

            byte[] buffer = new byte[1024];
            while(true){
                while(inputStream.available() > 0){
                    int readsize = inputStream.read(buffer, 0, 1024);
                    if(readsize<0) break;
                    log.info(new String(buffer, 0, readsize));
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
            try {
                OutputStream outputStream = channel.getOutputStream();
                outputStream.write((command+"\r").getBytes());
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
