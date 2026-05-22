package com.carrovivo.api.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] sanitizedBody;

    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.sanitizedBody = sanitize(body).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(sanitizedBody);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady()    { return true; }
            @Override public void setReadListener(ReadListener l) {}
            @Override public int read()           { return bais.read(); }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) sanitized[i] = sanitize(values[i]);
        return sanitized;
    }

    @Override
    public String getParameter(String parameter) {
        return sanitize(super.getParameter(parameter));
    }

    @Override
    public String getHeader(String name) {
        return sanitize(super.getHeader(name));
    }

    private String sanitize(String value) {
        if (value == null) return null;
        return value
                .replaceAll("<",   "&lt;")
                .replaceAll(">",   "&gt;")
                .replaceAll("'",   "&#x27;")
                .replaceAll("\"",  "&quot;")
                .replaceAll("/",   "&#x2F;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("(?i)eval\\(.*\\)", "")
                .replaceAll("(?i)javascript:", "")
                .replaceAll("(?i)<script",      "");
    }
}
