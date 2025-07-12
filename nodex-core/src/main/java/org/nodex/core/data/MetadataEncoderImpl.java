package org.nodex.core.data;

import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfDictionary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

/**
 * Implementation of MetadataEncoder that serializes metadata to BDF format.
 */
@Immutable
@NotNullByDefault
public class MetadataEncoderImpl implements MetadataEncoder {

    private static final Logger LOG = Logger.getLogger(MetadataEncoderImpl.class.getName());

    @Inject
    public MetadataEncoderImpl() {
        // Constructor for dependency injection
    }

    @Override
    public byte[] encode(BdfDictionary metadata) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            encodeDictionary(metadata, out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] encode(BdfList metadata) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            encodeList(metadata, out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] encode(Object value) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            encodeValue(value, out);
            return out.toByteArray();
        }
    }

    private void encodeDictionary(BdfDictionary dict, ByteArrayOutputStream out) throws IOException {
        out.write(0x44); // 'D' marker for dictionary
        writeInt(dict.size(), out);
        
        for (Map.Entry<String, Object> entry : dict.entrySet()) {
            encodeString(entry.getKey(), out);
            encodeValue(entry.getValue(), out);
        }
    }

    private void encodeList(BdfList list, ByteArrayOutputStream out) throws IOException {
        out.write(0x4C); // 'L' marker for list
        writeInt(list.size(), out);
        
        for (Object item : list) {
            encodeValue(item, out);
        }
    }

    private void encodeValue(Object value, ByteArrayOutputStream out) throws IOException {
        if (value == null) {
            out.write(0x4E); // 'N' marker for null
        } else if (value instanceof String) {
            encodeString((String) value, out);
        } else if (value instanceof Integer) {
            encodeInteger((Integer) value, out);
        } else if (value instanceof Long) {
            encodeLong((Long) value, out);
        } else if (value instanceof Boolean) {
            encodeBoolean((Boolean) value, out);
        } else if (value instanceof byte[]) {
            encodeBytes((byte[]) value, out);
        } else if (value instanceof BdfDictionary) {
            encodeDictionary((BdfDictionary) value, out);
        } else if (value instanceof BdfList) {
            encodeList((BdfList) value, out);
        } else {
            throw new IOException("Unsupported type: " + value.getClass().getName());
        }
    }

    private void encodeString(String str, ByteArrayOutputStream out) throws IOException {
        out.write(0x53); // 'S' marker for string
        byte[] bytes = str.getBytes("UTF-8");
        writeInt(bytes.length, out);
        out.write(bytes);
    }

    private void encodeInteger(Integer value, ByteArrayOutputStream out) throws IOException {
        out.write(0x49); // 'I' marker for integer
        writeInt(value, out);
    }

    private void encodeLong(Long value, ByteArrayOutputStream out) throws IOException {
        out.write(0x4C); // 'L' marker for long
        writeLong(value, out);
    }

    private void encodeBoolean(Boolean value, ByteArrayOutputStream out) throws IOException {
        out.write(value ? 0x54 : 0x46); // 'T' for true, 'F' for false
    }

    private void encodeBytes(byte[] bytes, ByteArrayOutputStream out) throws IOException {
        out.write(0x42); // 'B' marker for bytes
        writeInt(bytes.length, out);
        out.write(bytes);
    }

    private void writeInt(int value, ByteArrayOutputStream out) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeLong(long value, ByteArrayOutputStream out) throws IOException {
        writeInt((int) (value >>> 32), out);
        writeInt((int) value, out);
    }
}
