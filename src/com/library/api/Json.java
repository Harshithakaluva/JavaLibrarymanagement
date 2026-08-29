package com.library.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny JSON reader/writer. Written by hand on purpose: it means this whole
 * backend compiles with plain `javac` and needs no Maven/Gradle, no internet
 * access, and no external .jar files. That keeps "open in IntelliJ and run"
 * genuinely simple.
 *
 * Supports flat/nested objects, arrays, strings, numbers, booleans, null -
 * everything this API needs and nothing more.
 */
public final class Json {

    private Json() {}

    // ---------------- Writing ----------------

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String s) {
            writeString(s, sb);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(String.valueOf(e.getKey()), sb);
                sb.append(':');
                writeValue(e.getValue(), sb);
            }
            sb.append('}');
        } else if (value instanceof List<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(',');
                first = false;
                writeValue(item, sb);
            }
            sb.append(']');
        } else {
            writeString(value.toString(), sb);
        }
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---------------- Reading ----------------

    public static Map<String, Object> parseObject(String json) {
        Object result = new Parser(json).parseValue();
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            return map;
        }
        return new LinkedHashMap<>();
    }

    private static class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s == null ? "{}" : s; }

        Object parseValue() {
            skipWhitespace();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> parseObj();
                case '[' -> parseArr();
                case '"' -> parseStr();
                case 't', 'f' -> parseBool();
                case 'n' -> { pos += 4; yield null; }
                default -> parseNum();
            };
        }

        Map<String, Object> parseObj() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            skipWhitespace();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseStr();
                skipWhitespace();
                pos++; // :
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek() == ',') { pos++; continue; }
                if (peek() == '}') { pos++; break; }
                break;
            }
            return map;
        }

        List<Object> parseArr() {
            List<Object> list = new java.util.ArrayList<>();
            pos++; // [
            skipWhitespace();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek() == ',') { pos++; continue; }
                if (peek() == ']') { pos++; break; }
                break;
            }
            return list;
        }

        String parseStr() {
            pos++; // opening quote
            StringBuilder sb = new StringBuilder();
            while (peek() != '"') {
                char c = s.charAt(pos++);
                if (c == '\\') {
                    char next = s.charAt(pos++);
                    switch (next) {
                        case 'n' -> sb.append('\n');
                        case 't' -> sb.append('\t');
                        case 'r' -> sb.append('\r');
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'u' -> {
                            String hex = s.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> sb.append(next);
                    }
                } else {
                    sb.append(c);
                }
            }
            pos++; // closing quote
            return sb.toString();
        }

        Boolean parseBool() {
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            pos += 5;
            return Boolean.FALSE;
        }

        Number parseNum() {
            int start = pos;
            while (pos < s.length() && "-+.eE0123456789".indexOf(s.charAt(pos)) >= 0) pos++;
            String num = s.substring(start, pos);
            return num.contains(".") || num.contains("e") || num.contains("E")
                    ? Double.parseDouble(num)
                    : Long.parseLong(num);
        }

        char peek() { return pos < s.length() ? s.charAt(pos) : '\0'; }

        void skipWhitespace() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }
    }

    // ---------------- Helpers for reading typed values out of a parsed map ----------------

    public static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    public static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
