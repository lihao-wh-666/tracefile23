package com.exam.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMaskingUtil {

    private static final String MASK = "***";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "pwd", "passwd", "pass",
            "secret", "secretkey", "secret_key",
            "token", "accesstoken", "access_token", "refreshtoken", "refresh_token", "jwt",
            "apikey", "api_key", "apisecret", "api_secret",
            "privatekey", "private_key", "privatekey", "privkey",
            "publickey", "public_key",
            "authorization", "auth",
            "credential", "credentials",
            "oldpassword", "old_password", "newpassword", "new_password", "confirmpassword", "confirm_password",
            "rsa_private_key", "rsaprivatekey",
            "signingkey", "signing_key",
            "encryptionkey", "encryption_key",
            "dbpassword", "db_password", "databasepassword", "database_password",
            "redis_password", "redispassword",
            "mysql_password", "mysqlpassword"
    ));

    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)(password|pwd|passwd)\\s*[=:]\\s*[^&\\s,\"'\\}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(secret|token|apikey)\\s*[=:]\\s*[^&\\s,\"'\\}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-_\\.]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)Basic\\s+[A-Za-z0-9+/=]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(sk|pk)_([A-Za-z0-9]{20,})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[A-Za-z0-9+/=]{40,}={0,2}"),
            Pattern.compile("(?i)eyJ[A-Za-z0-9\\-_]+\\.eyJ[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)-----BEGIN (RSA |EC |DSA )?PRIVATE KEY-----[\\s\\S]*?-----END (RSA |EC |DSA )?PRIVATE KEY-----", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(mysql://|postgres://|mongodb://|redis://|jdbc:)[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE)
    );

    private static final Set<String> FALSE_POSITIVE_PATTERNS = new HashSet<>(Arrays.asList(
            "password", "token", "secret", "key"
    ));

    public static String maskLog(String content, String format) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        switch (format.toLowerCase()) {
            case "json":
                return maskJson(content);
            case "csv":
                return maskCsv(content);
            case "text":
            default:
                return maskText(content);
        }
    }

    public static String detectFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "text";
        }

        String trimmed = content.trim();

        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try {
                objectMapper.readTree(trimmed);
                return "json";
            } catch (Exception e) {
                // not valid JSON, continue detection
            }
        }

        String[] lines = trimmed.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0];
            if (firstLine.contains(",") && !firstLine.contains(" ") && firstLine.length() > 5) {
                boolean allHaveCommas = true;
                int commaCount = firstLine.split(",").length - 1;
                for (int i = 1; i < Math.min(lines.length, 5); i++) {
                    int lineCommas = lines[i].split(",").length - 1;
                    if (lineCommas != commaCount) {
                        allHaveCommas = false;
                        break;
                    }
                }
                if (allHaveCommas && commaCount >= 1) {
                    return "csv";
                }
            }
        }

        return "text";
    }

    public static String maskJson(String jsonContent) {
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode maskedRoot = maskJsonNode(root);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(maskedRoot);
        } catch (Exception e) {
            return maskText(jsonContent);
        }
    }

    private static JsonNode maskJsonNode(JsonNode node) {
        if (node == null) {
            return null;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (isSensitiveKey(fieldName)) {
                    if (fieldValue.isTextual()) {
                        objectNode.set(fieldName, new TextNode(MASK));
                    } else if (fieldValue.isNumber() || fieldValue.isBoolean()) {
                        objectNode.set(fieldName, new TextNode(MASK));
                    } else if (fieldValue.isObject() || fieldValue.isArray()) {
                        objectNode.set(fieldName, new TextNode(MASK));
                    }
                } else if (fieldValue.isTextual()) {
                    String textValue = fieldValue.asText();
                    String maskedText = maskSensitivePatterns(textValue);
                    if (!textValue.equals(maskedText)) {
                        objectNode.set(fieldName, new TextNode(maskedText));
                    }
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    objectNode.set(fieldName, maskJsonNode(fieldValue));
                }
            }
            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, maskJsonNode(arrayNode.get(i)));
            }
            return arrayNode;
        } else if (node.isTextual()) {
            String textValue = node.asText();
            String maskedText = maskSensitivePatterns(textValue);
            if (!textValue.equals(maskedText)) {
                return new TextNode(maskedText);
            }
        }

        return node;
    }

    public static String maskCsv(String csvContent) {
        String[] lines = csvContent.split("\n", -1);
        if (lines.length == 0) {
            return csvContent;
        }

        StringBuilder result = new StringBuilder();
        String[] headers = parseCsvLine(lines[0]);
        boolean[] sensitiveColumns = new boolean[headers.length];

        for (int i = 0; i < headers.length; i++) {
            sensitiveColumns[i] = isSensitiveKey(headers[i].trim());
        }

        result.append(lines[0]).append("\n");

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) {
                result.append("\n");
                continue;
            }

            String[] values = parseCsvLine(lines[i]);
            List<String> maskedValues = new ArrayList<>();

            for (int j = 0; j < values.length; j++) {
                if (j < sensitiveColumns.length && sensitiveColumns[j]) {
                    maskedValues.add(MASK);
                } else {
                    String masked = maskSensitivePatterns(values[j]);
                    if (isLikelySensitiveContent(masked)) {
                        maskedValues.add(MASK);
                    } else {
                        maskedValues.add(masked);
                    }
                }
            }

            result.append(formatCsvLine(maskedValues)).append("\n");
        }

        return result.toString();
    }

    private static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());

        return values.toArray(new String[0]);
    }

    private static String formatCsvLine(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                result.append("\"").append(value.replace("\"", "\"\"")).append("\"");
            } else {
                result.append(value);
            }
            if (i < values.size() - 1) {
                result.append(",");
            }
        }
        return result.toString();
    }

    public static String maskText(String textContent) {
        StringBuilder result = new StringBuilder();
        String[] lines = textContent.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String maskedLine = maskLine(line);
            result.append(maskedLine);
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    private static String maskLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }

        String masked = maskKeyValuePatterns(line);
        masked = maskSensitivePatterns(masked);

        if (isLikelySensitiveContent(masked)) {
            return maskHighEntropyContent(masked);
        }

        return masked;
    }

    private static String maskKeyValuePatterns(String line) {
        String[] keyValuePatterns = {
                "(?i)(password|pwd|passwd|secret|token|apikey|authorization|credential)(\\s*[=:]\\s*)([^&\\s,\"'\\}\\]]+)",
                "(?i)(old[_]?password|new[_]?password|confirm[_]?password)(\\s*[=:]\\s*)([^&\\s,\"'\\}\\]]+)"
        };

        String result = line;
        for (String patternStr : keyValuePatterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                String separator = matcher.group(2);
                String value = matcher.group(3);

                if (!isFalsePositive(key, value)) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(key + separator + MASK));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    private static String maskSensitivePatterns(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String match = matcher.group();
                if (!isFalsePositive(match, match)) {
                    if (match.startsWith("Bearer") || match.startsWith("bearer")) {
                        matcher.appendReplacement(sb, "Bearer " + MASK);
                    } else if (match.startsWith("Basic") || match.startsWith("basic")) {
                        matcher.appendReplacement(sb, "Basic " + MASK);
                    } else if (match.startsWith("-----BEGIN")) {
                        matcher.appendReplacement(sb, MASK);
                    } else if (match.contains("://")) {
                        matcher.appendReplacement(sb, maskConnectionString(match));
                    } else {
                        matcher.appendReplacement(sb, MASK);
                    }
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    private static String maskConnectionString(String connectionString) {
        Pattern pattern = Pattern.compile("(?i)([a-z]+://)([^:@]+):([^@]+)@", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(connectionString);
        if (matcher.find()) {
            return matcher.replaceAll("$1$2:" + MASK + "@");
        }
        return connectionString;
    }

    private static String maskHighEntropyContent(String text) {
        Pattern base64Pattern = Pattern.compile("[A-Za-z0-9+/]{30,}={0,2}");
        Matcher matcher = base64Pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            if (isHighEntropy(match)) {
                matcher.appendReplacement(sb, MASK);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalizedKey = key.toLowerCase().replaceAll("[_\\-\\s]", "");
        return SENSITIVE_KEYS.contains(normalizedKey);
    }

    private static boolean isLikelySensitiveContent(String text) {
        if (text == null || text.length() < 8) {
            return false;
        }

        Pattern jwtPattern = Pattern.compile("(?i)eyJ[A-Za-z0-9\\-_]+\\.eyJ");
        if (jwtPattern.matcher(text).find()) {
            return true;
        }

        Pattern longHexPattern = Pattern.compile("[A-Fa-f0-9]{20,}");
        if (longHexPattern.matcher(text).find()) {
            return false;
        }

        return false;
    }

    private static boolean isHighEntropy(String text) {
        if (text == null || text.length() < 20) {
            return false;
        }

        int upper = 0, lower = 0, digit = 0, special = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) upper++;
            else if (Character.isLowerCase(c)) lower++;
            else if (Character.isDigit(c)) digit++;
            else special++;
        }

        int types = 0;
        if (upper > 0) types++;
        if (lower > 0) types++;
        if (digit > 0) types++;
        if (special > 0) types++;

        double ratio = (double) types / 4;
        return types >= 3 && ratio > 0.5 && text.length() >= 32;
    }

    private static boolean isFalsePositive(String key, String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        String lowerValue = value.toLowerCase();
        String lowerKey = key.toLowerCase();

        if (lowerValue.equals("null") || lowerValue.equals("undefined")
                || lowerValue.equals("true") || lowerValue.equals("false")
                || lowerValue.equals("none")) {
            return true;
        }

        if (FALSE_POSITIVE_PATTERNS.contains(lowerValue)) {
            return true;
        }

        if (lowerKey.contains("type") || lowerKey.contains("name")) {
            for (String pattern : FALSE_POSITIVE_PATTERNS) {
                if (lowerValue.equals(pattern) || lowerValue.equals(pattern + "s")) {
                    return true;
                }
            }
        }

        if (lowerValue.matches("^\\d+$") && lowerValue.length() < 10) {
            return true;
        }

        return false;
    }

    public static Map<String, Object> compareLogs(String original, String masked) {
        Map<String, Object> result = new HashMap<>();

        String[] originalLines = original != null ? original.split("\n") : new String[0];
        String[] maskedLines = masked != null ? masked.split("\n") : new String[0];

        List<Map<String, Object>> differences = new ArrayList<>();
        int totalLines = Math.max(originalLines.length, maskedLines.length);
        int changedLines = 0;
        int totalChanges = 0;

        for (int i = 0; i < totalLines; i++) {
            String origLine = i < originalLines.length ? originalLines[i] : "";
            String maskLine = i < maskedLines.length ? maskedLines[i] : "";

            if (!origLine.equals(maskLine)) {
                changedLines++;
                int changesInLine = countChanges(origLine, maskLine);
                totalChanges += changesInLine;

                Map<String, Object> diff = new HashMap<>();
                diff.put("lineNumber", i + 1);
                diff.put("original", origLine);
                diff.put("masked", maskLine);
                diff.put("changeCount", changesInLine);
                differences.add(diff);
            }
        }

        result.put("totalLines", totalLines);
        result.put("changedLines", changedLines);
        result.put("totalChanges", totalChanges);
        result.put("differences", differences);
        result.put("originalSize", original != null ? original.length() : 0);
        result.put("maskedSize", masked != null ? masked.length() : 0);

        return result;
    }

    private static int countChanges(String original, String masked) {
        if (original.equals(masked)) {
            return 0;
        }

        int count = 0;
        Pattern maskPattern = Pattern.compile(Pattern.quote(MASK));
        Matcher matcher = maskPattern.matcher(masked);
        while (matcher.find()) {
            count++;
        }

        return count > 0 ? count : 1;
    }

    public static Set<String> getSensitiveKeys() {
        return Collections.unmodifiableSet(SENSITIVE_KEYS);
    }

    public static List<String> getSupportedFormats() {
        return Arrays.asList("text", "json", "csv", "auto");
    }
}
