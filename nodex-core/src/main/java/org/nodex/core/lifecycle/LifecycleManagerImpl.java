package org.nodex.core.lifecycle;

import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.system.Clock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

/**
 * Default implementation of LifecycleManager that coordinates the startup
 * and shutdown of all services in the system.
 */
@ThreadSafe
@NotNullByDefault
public class LifecycleManagerImpl implements LifecycleManager {

    private static final Logger LOG = Logger.getLogger(LifecycleManagerImpl.class.getName());
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final Clock clock;
    private final ExecutorService executor;
    private final ConcurrentMap<String, Service> services;
    private final AtomicBoolean started;
    private final AtomicBoolean stopped;

    @Inject
    public LifecycleManagerImpl(Clock clock) {
        this.clock = clock;
        this.executor = Executors.newCachedThreadPool();
        this.services = new ConcurrentHashMap<>();
        this.started = new AtomicBoolean(false);
        this.stopped = new AtomicBoolean(false);
    }

    @Override
    public void registerService(Service service) {
        if (started.get()) {
            throw new IllegalStateException("Cannot register service after startup");
        }
        String name = service.getClass().getSimpleName();
        services.put(name, service);
        LOG.info("Registered service: " + name);
    }

    @Override
    public void startServices() throws ServiceException {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Services already started");
        }
        
        LOG.info("Starting " + services.size() + " services");
        long startTime = clock.currentTimeMillis();
        
        try {
            // Start all services
            for (Service service : services.values()) {
                try {
                    service.startService();
                    LOG.info("Started service: " + service.getClass().getSimpleName());
                } catch (ServiceException e) {
                    LOG.severe("Failed to start service: " + service.getClass().getSimpleName());
                    throw e;
                }
            }
            
            long duration = clock.currentTimeMillis() - startTime;
            LOG.info("All services started in " + duration + " ms");
            
        } catch (ServiceException e) {
            // If any service fails to start, stop all started services
            stopServices();
            throw e;
        }
    }

    @Override
    public void stopServices() throws ServiceException {
        if (!stopped.compareAndSet(false, true)) {
            return; // Already stopped
        }
        
        LOG.info("Stopping " + services.size() + " services");
        long stopTime = clock.currentTimeMillis();
        
        ServiceException lastException = null;
        
        // Stop all services in reverse order
        for (Service service : services.values()) {
            try {
                service.stopService();
                LOG.info("Stopped service: " + service.getClass().getSimpleName());
            } catch (ServiceException e) {
                LOG.severe("Failed to stop service: " + service.getClass().getSimpleName());
                lastException = e;
            }
        }
        
        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                LOG.warning("Executor did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        long duration = clock.currentTimeMillis() - stopTime;
        LOG.info("All services stopped in " + duration + " ms");
        
        if (lastException != null) {
            throw lastException;
        }
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @Override
    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }
}
