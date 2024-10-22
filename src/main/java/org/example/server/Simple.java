package org.example.server;

import org.example.model.Post;
import org.example.repo.PsqlPostStore;
import org.example.repo.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class Simple {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlPostStore.class.getName());
    private int port;

    public Simple(int port) {
        this.port = port;
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        LOG.error("Something wrong with message ", io);
                    }
                }
            } catch (Exception e) {
                LOG.error("Something wrong with server ", e);
            }
        }).start();
    }
}
