package fun.lzyan;

import fun.lzyan.component.compress.Compress;
import fun.lzyan.component.enums.CompressTypeEnum;
import fun.lzyan.component.extension.ExtensionLoader;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author lzyan
 * @description
 */
public class TestSPI {

    @Test
    public void testClassLoader() {
        String fileName = "META-INF/extensions/" + Compress.class.getName();
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(fileName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println(url.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSPI() {

        String name = CompressTypeEnum.getName((byte) 0x01); // gizp
        Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(name);
        byte[] bytes = new byte[]{0x01, 0x02};
        compress.decompress(bytes);

    }

}
