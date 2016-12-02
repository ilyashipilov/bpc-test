package name.shipilov.bpc;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by ilya on 01.12.2016.
 */
public class BencodeDeserializerTest {

    @Test
    public void deserializeStringTest() throws IOException, BencodeDeserializer.InvalidFormatException {
        final ByteArrayInputStream stream = new ByteArrayInputStream("5:abcde".getBytes());
        final BencodeDeserializer deserializer = new BencodeDeserializer(stream);
        Assert.assertEquals(deserializer.read(), "abcde".getBytes());
    }

    @Test
    public void deserializeNumberTest() throws IOException, BencodeDeserializer.InvalidFormatException {
        final ByteArrayInputStream stream = new ByteArrayInputStream("i198e".getBytes());
        final BencodeDeserializer deserializer = new BencodeDeserializer(stream);
        Assert.assertEquals(deserializer.read(), 198l);
    }

    @Test
    public void deserializeListTest() throws IOException, BencodeDeserializer.InvalidFormatException {
        final ByteArrayInputStream stream = new ByteArrayInputStream("li1ei2ee".getBytes());
        final BencodeDeserializer deserializer = new BencodeDeserializer(stream);
        final Object list = deserializer.read();
        Assert.assertNotNull(list);
        Assert.assertEquals(((List)list).size(), 2);
        Assert.assertEquals(((List)list).get(0), 1l);
        Assert.assertEquals(((List)list).get(1), 2l);
    }

    @Test
    public void deserializeMapTest() throws IOException, BencodeDeserializer.InvalidFormatException {
        final ByteArrayInputStream stream = new ByteArrayInputStream("d2:abi77e3:cdei88ee".getBytes());
        final BencodeDeserializer deserializer = new BencodeDeserializer(stream);
        final Object map = deserializer.read();
        Assert.assertNotNull(map);
        Assert.assertEquals(((Map)map).size(), 2);
        Assert.assertEquals(((Map)map).get("ab".getBytes()), 77l);
        Assert.assertEquals(((Map)map).get("cde".getBytes()), 88l);
    }

    @Test
    public void deserializeTorrentFile() throws IOException, BencodeDeserializer.InvalidFormatException {
        final InputStream torrentFile = new FileInputStream("C:\\work\\bpc-group-test\\src\\test\\resources\\[rutracker.org].t5241012.torrent");
        final BencodeDeserializer deserializer = new BencodeDeserializer(torrentFile);
        Object element = null;
        while ((element = deserializer.read()) != null) {
            System.out.println(element);
        }
    }

}
