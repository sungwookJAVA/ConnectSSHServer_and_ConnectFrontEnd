package com.websocket.websocket.ssh;

import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
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
            channel = session.openChannel("exec");
            channelExec = (ChannelExec) channel;
            log.info("[*] exec Channel is created");

            // 5. configure sterams
            inputStream = channel.getInputStream();
        } catch (JSchException e) { // ssh Exceptions
            log.error("[-] SSHClient Error");
            e.printStackTrace();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        } catch (IOException e) { // stream Exceptions
            log.error("[-] stream Error");
            e.printStackTrace();
        }
    }

    /***
     * 명령어 실행
     * @param 실행할 명령어
     * @return 명령어 실행 결과
     */
    public String ExecCommand(String command){
        byte[] buffer = new byte[1024];
        StringBuffer results = new StringBuffer();

        channelExec.setCommand(command);
        try {
            channel.connect();

            while(true){
                // 입력 스트림 처리
                while(inputStream.available() > 0){
                    int readSize = inputStream.read(buffer, 0, 1024);
                    if (readSize < 0) break;
                    results.append(new String(buffer, 0, readSize));
                }

                if(channel.isClosed()){
                    // 남아 있는 입력, 에러 처리
                    if((inputStream.available()>0)) continue;
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (JSchException e) { // jsch errors
            log.error("[-] exec command error");
            e.printStackTrace();
        } catch (IOException e) { // stream erros
            e.printStackTrace();
        } catch (InterruptedException e) { // Timeunit error
            e.printStackTrace();
        }

        return results.toString();
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
