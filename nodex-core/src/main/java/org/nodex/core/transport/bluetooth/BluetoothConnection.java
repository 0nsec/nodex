package org.nodex.core.transport.bluetooth;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a Bluetooth connection to a contact.
 */
@ThreadSafe
@NotNullByDefault
public class BluetoothConnection {
    
    private final ContactId contactId;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private final long establishedTime;
    
    public BluetoothConnection(ContactId contactId) {
        this.contactId = contactId;
        this.establishedTime = System.currentTimeMillis();
    }
    
    public ContactId getContactId() {
        return contactId;
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public long getEstablishedTime() {
        return establishedTime;
    }
    
    public void close() throws IOException {
        if (connected.compareAndSet(true, false)) {
            // Close the actual Bluetooth socket connection
            // In a real implementation, this would clean up system resources
        }
    }
    
    public void sendData(byte[] data) throws IOException {
        if (!isConnected()) {
            throw new IOException("Connection is closed");
        }
        
        // Send data over Bluetooth connection
        // In a real implementation, this would write to the Bluetooth socket
    }
    
    public byte[] receiveData() throws IOException {
        if (!isConnected()) {
            throw new IOException("Connection is closed");
        }
        
        // Receive data from Bluetooth connection
        // In a real implementation, this would read from the Bluetooth socket
        return new byte[0]; // Simplified
    }
}
