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

// [SEC-18] SANITIZAÇÃO XSS — PROTEÇÃO CONTRA CROSS-SITE SCRIPTING
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] sanitizedBody;

    // [SEC-19] SANITIZAÇÃO DO BODY NO CONSTRUTOR
    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.sanitizedBody = sanitizeBody(body).getBytes(StandardCharsets.UTF_8);
    }

    // [SEC-20] getInputStream E getReader SOBRESCRITOS
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

    // [SEC-21] SANITIZAÇÃO DE QUERY PARAMS
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) sanitized[i] = sanitizeParam(values[i]);
        return sanitized;
    }

    @Override
    public String getParameter(String parameter) {
        return sanitizeParam(super.getParameter(parameter));
    }

    // [SEC-21] SANITIZAÇÃO DE HEADERS
    // Headers como Content-Type contêm "/" legítimo (ex: application/json).
    // Não escapamos "/" aqui — apenas removemos padrões XSS reais.
    @Override
    public String getHeader(String name) {
        return sanitizeHeader(super.getHeader(name));
    }

    // [SEC-22] SANITIZAÇÃO DO BODY JSON
    // Remove padrões XSS sem tocar na estrutura JSON.
    private String sanitizeBody(String value) {
        if (value == null) return null;
        return value
                .replaceAll("(?i)<script[^>]*>.*?</script>", "")
                .replaceAll("(?i)<script",      "")
                .replaceAll("(?i)javascript:",  "")
                .replaceAll("(?i)eval\\(.*?\\)", "");
    }

    // [SEC-22] SANITIZAÇÃO DE HEADERS
    // Não escapa "/" pois headers como Content-Type e Authorization
    // contêm barras legítimas que não são vetores XSS.
    private String sanitizeHeader(String value) {
        if (value == null) return null;
        return value
                .replaceAll("<",   "&lt;")
                .replaceAll(">",   "&gt;")
                .replaceAll("'",   "&#x27;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("(?i)eval\\(.*?\\)", "")
                .replaceAll("(?i)javascript:",   "")
                .replaceAll("(?i)<script",       "");
    }

    // [SEC-22] SANITIZAÇÃO DE QUERY PARAMS
    // Query params não têm "/" como parte de estrutura — pode escapar.
    private String sanitizeParam(String value) {
        if (value == null) return null;
        return value
                .replaceAll("<",   "&lt;")
                .replaceAll(">",   "&gt;")
                .replaceAll("'",   "&#x27;")
                .replaceAll("\"",  "&quot;")
                .replaceAll("/",   "&#x2F;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("(?i)eval\\(.*?\\)", "")
                .replaceAll("(?i)javascript:",   "")
                .replaceAll("(?i)<script",       "");
    }
}
