package name.shipilov.bpc;

import com.google.common.primitives.UnsignedBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ilya on 01.12.2016.
 */
public class BencodeDeserializer {

    /**
     * Неверный формат данных
     */
    public static class InvalidFormatException extends Exception {

        public InvalidFormatException(String s) {
            super(s);
        }
    }

    private final InputStream inputStream;

    private final List<? extends Deserializer> deserializers = Arrays.asList(
        new NumberDeserializer(), new StringDeserializer(), new ListDeserializer(), new MapDeserializer());

    public BencodeDeserializer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return значение одного из bencode-типов, или null, если значения закончились - в случае,
     * если достигнут конец потока или коллекции.
     */
    public Object read() throws IOException, InvalidFormatException {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        byte[] buff = new byte[1];
        Deserializer deserializer;
        while(inputStream.read(buff) != -1) {
            header.write(buff);
            if (Arrays.equals(Deserializer.LIST_DICTIONARY_TERMINATOR, header.toByteArray()))
                return null;
            deserializer = getDeserializer(header.toByteArray());
            if (deserializer != null)
                return deserializer.deserialize(header.toByteArray());
        }

        return null;
    }

    interface Deserializer<T> {
        byte[] LIST_DICTIONARY_TERMINATOR = "e".getBytes();

        T deserialize(byte[] header) throws IOException, InvalidFormatException;
        boolean accept(byte[] header);
    }

    class NumberDeserializer implements Deserializer<Number> {
        public Number deserialize(byte[] header) throws IOException, InvalidFormatException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buff = new byte[1];
            while (inputStream.read(buff) != -1) {
                if (Arrays.equals("e".getBytes(), buff))
                    break;
                result.write(buff);
            }
            return Long.valueOf(new String(result.toByteArray()));
        }

        public boolean accept(byte[] header) {
            return Arrays.equals("i".getBytes(), header);
        }
    }

    class StringDeserializer implements Deserializer<byte[]> {
        private final Pattern headerPattern = Pattern.compile("^([0-9]+):$");

        public byte[] deserialize(byte[] header) throws IOException, InvalidFormatException {
            final Matcher matcher = headerPattern.matcher(new String(header));
            matcher.matches();
            final byte[] result = new byte[Integer.valueOf(matcher.group(1))];
            inputStream.read(result);
            return result;
        }

        public boolean accept(byte[] header) {
            return headerPattern.matcher(new String(header)).matches();
        }
    }

    class ListDeserializer implements Deserializer<List> {
        public List deserialize(byte[] header) throws IOException, InvalidFormatException {
            final List<Object> result = new LinkedList<Object>();
            Object element;
            while((element = read()) != null) {
                result.add(element);
            }
            return result;
        }

        public boolean accept(byte[] header) {
            return Arrays.equals("l".getBytes(), header);
        }
    }

    class MapDeserializer implements Deserializer<Map> {
        public Map deserialize(byte[] header) throws IOException, InvalidFormatException {
            final Map<byte[], Object> result = new TreeMap<byte[], Object>(UnsignedBytes.lexicographicalComparator());
            Object key;
            while((key = read()) != null) {
                if (!(key instanceof byte[]))
                    throw new InvalidFormatException("key must be string");
                Object value = read();
                if (value == null)
                    throw new InvalidFormatException("invalid dictionary");
                result.put((byte[])key, value);
            }
            return result;
        }

        public boolean accept(byte[] header) {
            return Arrays.equals("d".getBytes(), header);
        }
    }

    private Deserializer getDeserializer(byte[] header) {
        for (Deserializer deserializer: deserializers)
            if (deserializer.accept(header))
                return deserializer;
        return null;
    }
}
