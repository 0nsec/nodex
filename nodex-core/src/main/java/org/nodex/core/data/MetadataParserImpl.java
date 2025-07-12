package org.nodex.core.data;

import org.nodex.api.data.MetadataParser;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfDictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

/**
 * Implementation of MetadataParser that deserializes metadata from BDF format.
 */
@Immutable
@NotNullByDefault
public class MetadataParserImpl implements MetadataParser {

    private static final Logger LOG = Logger.getLogger(MetadataParserImpl.class.getName());

    @Inject
    public MetadataParserImpl() {
        // Constructor for dependency injection
    }

    @Override
    public BdfDictionary parseDictionary(byte[] data) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            Object parsed = parseValue(in);
            if (!(parsed instanceof BdfDictionary)) {
                throw new IOException("Expected dictionary, got " + parsed.getClass().getName());
            }
            return (BdfDictionary) parsed;
        }
    }

    @Override
    public BdfList parseList(byte[] data) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            Object parsed = parseValue(in);
            if (!(parsed instanceof BdfList)) {
                throw new IOException("Expected list, got " + parsed.getClass().getName());
            }
            return (BdfList) parsed;
        }
    }

    @Override
    public Object parseValue(byte[] data) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            return parseValue(in);
        }
    }

    private Object parseValue(ByteArrayInputStream in) throws IOException {
        int marker = in.read();
        if (marker == -1) {
            throw new IOException("Unexpected end of data");
        }

        switch (marker) {
            case 0x4E: // 'N' for null
                return null;
            case 0x53: // 'S' for string
                return parseString(in);
            case 0x49: // 'I' for integer
                return parseInteger(in);
            case 0x4C: // 'L' for long or list
                // Need to determine if this is a long or list
                // For now, assume it's a list if next byte is a size marker
                return parseList(in);
            case 0x54: // 'T' for true
                return Boolean.TRUE;
            case 0x46: // 'F' for false
                return Boolean.FALSE;
            case 0x42: // 'B' for bytes
                return parseBytes(in);
            case 0x44: // 'D' for dictionary
                return parseDictionary(in);
            default:
                throw new IOException("Unknown marker: 0x" + Integer.toHexString(marker));
        }
    }

    private String parseString(ByteArrayInputStream in) throws IOException {
        int length = readInt(in);
        byte[] bytes = new byte[length];
        int bytesRead = in.read(bytes);
        if (bytesRead != length) {
            throw new IOException("Expected " + length + " bytes, got " + bytesRead);
        }
        return new String(bytes, "UTF-8");
    }

    private Integer parseInteger(ByteArrayInputStream in) throws IOException {
        return readInt(in);
    }

    private Long parseLong(ByteArrayInputStream in) throws IOException {
        return readLong(in);
    }

    private byte[] parseBytes(ByteArrayInputStream in) throws IOException {
        int length = readInt(in);
        byte[] bytes = new byte[length];
        int bytesRead = in.read(bytes);
        if (bytesRead != length) {
            throw new IOException("Expected " + length + " bytes, got " + bytesRead);
        }
        return bytes;
    }

    private BdfDictionary parseDictionary(ByteArrayInputStream in) throws IOException {
        int size = readInt(in);
        Map<String, Object> map = new HashMap<>();
        
        for (int i = 0; i < size; i++) {
            Object keyObj = parseValue(in);
            if (!(keyObj instanceof String)) {
                throw new IOException("Dictionary key must be string, got " + keyObj.getClass().getName());
            }
            String key = (String) keyObj;
            Object value = parseValue(in);
            map.put(key, value);
        }
        
        return new BdfDictionary(map);
    }

    private BdfList parseList(ByteArrayInputStream in) throws IOException {
        int size = readInt(in);
        List<Object> list = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            Object value = parseValue(in);
            list.add(value);
        }
        
        return new BdfList(list);
    }

    private int readInt(ByteArrayInputStream in) throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
            throw new IOException("Unexpected end of data while reading integer");
        }
        
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private long readLong(ByteArrayInputStream in) throws IOException {
        long high = readInt(in);
        long low = readInt(in);
        return (high << 32) | (low & 0xFFFFFFFFL);
    }
}
