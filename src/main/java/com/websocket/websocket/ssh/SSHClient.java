package com.websocket.websocket.ssh;

import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        } catch (JSchException e) {
            log.error("[-] SSHClient Error");
            e.printStackTrace();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
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
