package com.carrovivo.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return sanitize(value);
    }

    @Override
    public String getHeader(String name) {
        return sanitize(super.getHeader(name));
    }

    private String sanitize(String value) {
        if (value == null) return null;
        return value
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("'", "&#x27;")
                .replaceAll("\"", "&quot;")
                .replaceAll("/", "&#x2F;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("eval\\((.*)\\)", "")
                .replaceAll("javascript:", "")
                .replaceAll("script", "");
    }
}