package vdr.jonglisto.lib.svdrp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvdrpServer implements Runnable {

    private Logger log = LoggerFactory.getLogger(SvdrpServer.class);

    private int port;
    private int countExecutor;
    private boolean running;
    
    public SvdrpServer(int port, int countExecutor) {
        this.port = port;
        this.countExecutor = countExecutor;
        running = true;
    }

    public void run() {
        ExecutorService executor = null;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            executor = Executors.newFixedThreadPool(countExecutor);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                Runnable worker = new Handler(clientSocket);
                executor.execute(worker);
            }
        } catch (IOException e) {
            log.error("SVDRP server start failed", e);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }
    
    public void initStop() {
        running = false;
    }
}