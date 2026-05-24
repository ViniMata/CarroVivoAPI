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
// Intercepta e sanitiza toda entrada antes que chegue aos controllers.
// Cobre query params, headers E body JSON (via getInputStream/getReader).
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] sanitizedBody;

    // [SEC-19] SANITIZAÇÃO DO BODY NO CONSTRUTOR
    // O body é lido, sanitizado e armazenado em memória uma única vez.
    // Necessário pois getInputStream() só pode ser lido uma vez por padrão.
    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.sanitizedBody = sanitize(body).getBytes(StandardCharsets.UTF_8);
    }

    // [SEC-20] getInputStream E getReader SOBRESCRITOS
    // O Spring usa estes métodos para ler @RequestBody em APIs REST.
    // Sem sobrescrever ambos, toda sanitização seria ignorada em POSTs e PUTs.
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

    // [SEC-21] SANITIZAÇÃO DE QUERY PARAMS E HEADERS
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

    // [SEC-22] REGRAS DE SANITIZAÇÃO
    // Escapa caracteres HTML especiais e remove padrões XSS conhecidos:
    // - Tags HTML (<, >) convertidas para entidades HTML seguras
    // - Aspas escapadas para evitar injeção em atributos
    // - eval(), javascript: e <script removidos independente de capitalização
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
