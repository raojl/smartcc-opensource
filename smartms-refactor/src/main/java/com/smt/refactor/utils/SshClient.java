package com.smt.refactor.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * SSH 客户端（JSch 封装）——用于远程管理 Asterisk：
 *  - exec：执行 Asterisk CLI 命令（module reload 等）
 *  - writeFile：SCP 写文件（pjsip.conf 增量更新）
 */
@Slf4j
public class SshClient implements AutoCloseable {

    private final Session session;

    public SshClient(String host, int port, String user, String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            Properties cfg = new Properties();
            cfg.put("StrictHostKeyChecking", "no");
            session.setConfig(cfg);
            session.setTimeout(15000);
            session.connect();
            log.info("SSH 已连接 {}:{}", host, port);
        } catch (Exception e) {
            throw new IllegalStateException("SSH 连接失败: " + e.getMessage(), e);
        }
    }

    /** 执行远程命令，返回 stdout */
    public String exec(String command) {
        ChannelExec ch = null;
        try {
            ch = (ChannelExec) session.openChannel("exec");
            ch.setCommand(command);
            ch.setInputStream(null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ch.setOutputStream(out);
            ch.setErrStream(err);
            ch.connect(15000);
            while (!ch.isClosed()) {
                Thread.sleep(200);
            }
            String stdout = new String(out.toByteArray(), StandardCharsets.UTF_8);
            String stderr = new String(err.toByteArray(), StandardCharsets.UTF_8);
            if (StringUtils.hasText(stderr)) {
                log.warn("SSH exec stderr: {}", stderr);
            }
            log.info("SSH exec [{}] => stdout=[{}] rc={}", command, stdout.trim(), ch.getExitStatus());
            return stdout + stderr;
        } catch (Exception e) {
            throw new IllegalStateException("SSH 执行失败: " + e.getMessage(), e);
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
        }
    }

    /** 追加内容到远程文件（SCP） */
    public void appendFile(String remotePath, String content) {
        writeFile(remotePath, content, true);
    }

    /** 写入远程文件（SCP），append=false 覆盖 */
    public void writeFile(String remotePath, String content, boolean append) {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(15000);
            InputStream in;
            if (append) {
                // 读取现有内容 + 追加
                String existing = "";
                try {
                    byte[] buf = new byte[8192];
                    try (InputStream is = sftp.get(remotePath)) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int n;
                        while ((n = is.read(buf)) > 0) {
                            bos.write(buf, 0, n);
                        }
                        existing = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    }
                } catch (Exception e) {
                    // 文件不存在则直接新建
                }
                content = existing + content;
            }
            sftp.put(new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), remotePath, ChannelSftp.OVERWRITE);
            log.info("SCP 写入 {} ({} 字节)", remotePath, content.getBytes(StandardCharsets.UTF_8).length);
        } catch (Exception e) {
            throw new IllegalStateException("SCP 写入失败 " + remotePath + ": " + e.getMessage(), e);
        } finally {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
        }
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH 已断开");
        }
    }
}
