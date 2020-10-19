package com.websocket.websocket.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
    private Session session;
    private Channel channel;
    private JSch jSch = new JSch();

    @Builder
    public SSHClient(javax.websocket.Session session) {
        this.websocket_session = session;
    }

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

            session.disconnect();
        } catch (JSchException e) {
            log.error("[-] SSHClient Error");
            e.printStackTrace();

            session.disconnect();
        }
    }
}
