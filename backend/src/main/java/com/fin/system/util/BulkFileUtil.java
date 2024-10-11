package com.fin.system.util;

import cn.hutool.core.codec.Base64Encoder;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * 文件相关的处理
 *
 * @author 散装java
 * @version 1.0.0
 * @date 2022-11-20
 */
@Slf4j
public class BulkFileUtil {
    /**
     * 文件下载
     *
     * @param request
     * @param response
     * @param file
     * @throws UnsupportedEncodingException
     */
    public static void downloadFile(HttpServletRequest request, HttpServletResponse response, File file, String filename, Long start, Long end) throws UnsupportedEncodingException, IOException {
        response.setCharacterEncoding(request.getCharacterEncoding());
        response.setContentType("application/octet-stream");

        // 处理文件名编码
        if (filename == null) {
            filename = filenameEncoding(file.getName(), request);
        }

        // 设置响应头，支持断点续传
        long fileLength = file.length();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", String.format("attachment;filename=%s", filename));

        // 设置默认的起始和结束字节
        if (start == null) start = 0L;
        if (end == null) end = fileLength - 1;

        long contentLength = end - start + 1;
        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 如果是断点续传，设置 `Content-Range` 头和 206 状态码
        if (start > 0 || end < fileLength - 1) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            // 移动到指定的开始位置
            raf.seek(start);

            // 读取指定字节范围的数据
            long remaining = contentLength;
            while ((bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                out.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }

            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 适配不同的浏览器，确保文件名字正常
     *
     * @param filename
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String filenameEncoding(String filename, HttpServletRequest request) throws UnsupportedEncodingException {
        // 获得请求头中的User-Agent
        String agent = request.getHeader("User-Agent");
        // 根据不同的客户端进行不同的编码

        if (agent.contains("MSIE")) {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        } else if (agent.contains("Firefox")) {
            // 火狐浏览器
            Base64Encoder base64Encoder = new Base64Encoder();
            filename = "=?utf-8?B?" + base64Encoder.encode(filename.getBytes("utf-8")) + "?=";
        } else {
            // 其它浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }
}
