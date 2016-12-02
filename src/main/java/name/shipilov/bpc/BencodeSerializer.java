package name.shipilov.bpc;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.primitives.UnsignedBytes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Сериализатор в bencode.
 *
 * Использует формат специфицированный в статье https://ru.wikipedia.org/wiki/Bencode
 * Работает просто с ассоциированным в конструкторе потоком вывода, контроль за состоянием которого реализуется в клиентах.
 * Содержит единственный открытый метод write(Object), поддерживаются типы: Number, byte[], List, Map.
 *
 * Created by ilya on 01.12.2016.
 */
public class BencodeSerializer {

    /**
     * Неподдерживаемый в Bencode тип данных
     */
    public static class NotSupportedType extends Exception {

        public NotSupportedType(String s) {
            super(s);
        }
    }

    private final OutputStream outputStream;
    private final Serializer numberSerializer = new NumberSerializer();
    private final Serializer stringSerializer = new StringSerializer();
    private final Serializer listSerializer = new ListSerializer();
    private final Serializer mapSerializer = new MapSerializer();

    /**
     * @param outputStream
     */
    public BencodeSerializer(OutputStream outputStream) {
        if (outputStream == null)
            throw new NullPointerException("no outputStream");
        this.outputStream = outputStream;
    }

    /**
     * запись любого bencode-типа в ассоциированный поток
     *
     * @param value любой bencode-тип
     * @throws IOException
     * @throws NotSupportedType
     */
    public void write(Object value) throws IOException, NotSupportedType {
        getSerializer(value).serialize(value);
    }

    private Serializer getSerializer(Object value) throws NotSupportedType {
        if (value == null)
            throw new NotSupportedType("null value not supported");
        if (value instanceof byte[])
            return stringSerializer;
        if (value instanceof Map)
            return mapSerializer;
        if (value instanceof List)
            return listSerializer;
        if (value instanceof Number)
            return numberSerializer;
        throw new NotSupportedType(value.getClass() + " not supported");
    }

    interface Serializer<T> {
        void serialize(T value) throws IOException, NotSupportedType;
    }

    class NumberSerializer implements Serializer<Number> {
        public void serialize(Number value) throws IOException, NotSupportedType {
            outputStream.write(String.format("i%de", value).getBytes());
        }
    }

    class StringSerializer implements Serializer<byte[]> {
        public void serialize(byte[] value) throws IOException, NotSupportedType {
            outputStream.write(String.format("%d:", value.length).getBytes());
            outputStream.write(value);
        }
    }

    class ListSerializer implements Serializer<List<Object>> {
        public void serialize(List<Object> value) throws IOException, NotSupportedType {
            outputStream.write("l".getBytes());
            for (Object item: value) {
                getSerializer(item).serialize(item);
            }
            outputStream.write("e".getBytes());
        }
    }

    class MapSerializer implements Serializer<Map<byte[], Object>> {
        public void serialize(Map<byte[], Object> value) throws IOException, NotSupportedType {
            outputStream.write("d".getBytes());
            for (byte[] item: ImmutableSortedSet.copyOf(UnsignedBytes.lexicographicalComparator(), value.keySet())) {
                getSerializer(item).serialize(item);
                getSerializer(value.get(item)).serialize(value.get(item));
            }
            outputStream.write("e".getBytes());
        }
    }

}
