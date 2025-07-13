package org.nodex.core.transport.lan;

import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * LAN server socket for accepting incoming connections - matches Briar's implementation.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class LanServerSocket implements Service {

    private static final Logger LOG = Logger.getLogger(LanServerSocket.class.getName());
    private static final int DEFAULT_PORT = 7916;
    
    private ServerSocket serverSocket;
    private ExecutorService acceptorExecutor;
    private volatile boolean started = false;

    @Inject
    public LanServerSocket() {
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        try {
            LOG.info("Starting LAN server socket on port " + DEFAULT_PORT);
            
            serverSocket = new ServerSocket(DEFAULT_PORT);
            
            acceptorExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "LAN-Acceptor");
                t.setDaemon(true);
                return t;
            });
            
            // Start accepting connections
            acceptorExecutor.submit(this::acceptConnections);
            
            started = true;
            LOG.info("LAN server socket started successfully");
            
        } catch (IOException e) {
            throw new ServiceException("Failed to start LAN server socket", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping LAN server socket");
        started = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOG.warning("Error closing server socket: " + e.getMessage());
        }
        
        if (acceptorExecutor != null) {
            acceptorExecutor.shutdown();
        }
    }

    private void acceptConnections() {
        while (started && !Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                handleIncomingConnection(socket);
            } catch (IOException e) {
                if (started) {
                    LOG.warning("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleIncomingConnection(Socket socket) {
        // Implementation would handle the incoming connection
        LOG.info("Received incoming LAN connection from: " + socket.getRemoteSocketAddress());
        
        try {
            // For now, just close the connection
            socket.close();
        } catch (IOException e) {
            LOG.warning("Error closing connection: " + e.getMessage());
        }
    }
}
