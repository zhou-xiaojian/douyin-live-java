package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GzipUtil {

    /**
     * 将 gzip 压缩的数据解压缩
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] degzip(byte[] data) throws IOException {
        // 将字节数组包装成一个 ByteArrayInputStream 对象
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        // 创建一个 GZIPInputStream 对象
        GZIPInputStream gis = new GZIPInputStream(bis);
        // 创建一个 ByteArrayOutputStream 对象，用于存储解压后的数据
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // 创建一个缓冲区，用于存储读取的数据
        byte[] buffer = new byte[1024];
        int len;
        // 从 GZIPInputStream 对象中读取数据，并写入 ByteArrayOutputStream 中
        while ((len = gis.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }
        // 关闭流
        bos.close();
        gis.close();
        bis.close();
        // 返回解压后的字节数组
        return bos.toByteArray();
    }
}

