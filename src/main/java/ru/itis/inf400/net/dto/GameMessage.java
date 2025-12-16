package ru.itis.inf400.net.dto;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GameMessage implements Serializable {

    private MessageType type;
    private byte[] payload;

    public GameMessage(MessageType type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public GameMessage(MessageType type, String payload) {
        this(type, payload.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        // Заголовок сообщения
        dataStream.writeInt(type.getCode());          // Тип сообщения (4 байта)
        dataStream.writeInt(payload.length);         // Длина данных (4 байта)

        // Полезная нагрузка
        if (payload.length > 0) {
            dataStream.write(payload);
        }

        dataStream.flush();
        return byteStream.toByteArray();
    }

    public static GameMessage fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        DataInputStream dataStream = new DataInputStream(byteStream);

        int typeCode = dataStream.readInt();
        int payloadLength = dataStream.readInt();

        byte[] payload = new byte[payloadLength];
        if (payloadLength > 0) {
            dataStream.readFully(payload);
        }

        return new GameMessage(MessageType.fromCode(typeCode), payload);
    }

    public MessageType getType() { return type; }
    public byte[] getPayload() { return payload; }
    public String getPayloadAsString() {
        return new String(payload, StandardCharsets.UTF_8);
    }
}