package name.shipilov.bpc;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilya on 01.12.2016.
 */
public class BencodeSerializerTest {

    @Test
    public void serializeNumberTest() throws IOException, BencodeSerializer.NotSupportedType {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BencodeSerializer serializer = new BencodeSerializer(outputStream);
        serializer.write(10);
        serializer.write(0);
        serializer.write(-10l);
        outputStream.close();
        Assert.assertEquals(outputStream.toByteArray(), "i10ei0ei-10e".getBytes());
    }

    @Test
    public void serializeStringTest() throws IOException, BencodeSerializer.NotSupportedType {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BencodeSerializer serializer = new BencodeSerializer(outputStream);
        serializer.write(new byte[]{1, 2, 3, 4, 5, 6});
        outputStream.close();
        byte[] expected = new byte["6:".getBytes().length + 6];
        System.arraycopy("6:".getBytes(), 0, expected, 0, "6:".getBytes().length);
        System.arraycopy(new byte[] {1,2,3,4,5,6}, 0, expected, "6:".getBytes().length, 6);
        Assert.assertEquals(outputStream.toByteArray(), expected);
    }

    @Test
    public void serializeListTest() throws IOException, BencodeSerializer.NotSupportedType {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BencodeSerializer serializer = new BencodeSerializer(outputStream);
        serializer.write(Arrays.asList(1, 2, "abc".getBytes(), Arrays.asList(3, 4)));
        outputStream.close();
        Assert.assertEquals(outputStream.toByteArray(), "li1ei2e3:abcli3ei4eee".getBytes());
    }

    @Test
    public void serializeMapTest() throws IOException, BencodeSerializer.NotSupportedType {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BencodeSerializer serializer = new BencodeSerializer(outputStream);

        Map<byte[], Object> map = new HashMap<byte[], Object>();
        map.put("def".getBytes(), Arrays.asList(-35, 0));
        map.put("abc".getBytes(), 99);

        serializer.write(map);
        outputStream.close();
        Assert.assertEquals(outputStream.toByteArray(), "d3:abci99e3:defli-35ei0eee".getBytes());
    }

    //TODO: test NotSupportedType case
}
