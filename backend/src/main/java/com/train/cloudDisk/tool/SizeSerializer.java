package com.train.cloudDisk.tool;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SizeSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long aLong, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(formatFileSize(aLong));
    }

    public String formatFileSize(long size_l) {
        double size = size_l;
        if (size <= 0) {
            return "0";
        }
        if (size < 1024) {
            return size + " B"; // Bytes
        }
        size /= 1024.0;
        if (size < 1024) {
            return String.format("%.2f KB", size); // Kilobytes
        }
        size /= 1024.0;
        if (size < 1024) {
            return String.format("%.2f MB", size); // Megabytes
        }
        size /= 1024.0;
        if (size < 1024) {
            return String.format("%.2f GB", size); // Gigabytes
        }
        size /= 1024.0;
        return String.format("%.2f TB", size); // Terabytes
    }
}
