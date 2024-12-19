package cliente;

import java.util.*;
import java.util.stream.Collectors;

public class JsonObject {

    private final Map<String, Object> data;

    public JsonObject() {
        data = new LinkedHashMap<>();
    }

    public JsonObject(String jsonString){
        data = JsonObject.fromString(jsonString).data;
    }

    private static JsonObject fromString(String jsonString) {
        JsonObject json = new JsonObject();
        int state = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String key = "", value;
        char c;

        for (int i = 0; i < jsonString.length(); i++) {
            c = jsonString.charAt(i);

            switch (state) {
                case 0:
                    if (c == '{') {
                        state = 1;
                    } else {
                        throw new IllegalArgumentException("Error '{' is missing");
                    }
                    break;

                case 1:
                    if (c == '"') {
                        state = 2;
                    } else if (c == '}') {
                        return json;
                    } else {
                        throw new IllegalArgumentException("Error missing valid key.");
                    }
                    break;

                case 2:
                    if (c != '"') {
                        stringBuilder.append(c);
                    } else {
                        key = stringBuilder.toString();
                        stringBuilder.setLength(0);
                        state = 3;
                    }
                    break;

                case 3:
                    if (c == ':') {
                        state = 4;
                    } else {
                        throw new IllegalArgumentException("Error ':' is missing.");
                    }
                    break;

                case 4:
                    if (c == '"') {
                        state = 5;
                    } else if (Character.isDigit(c) || c == '-' || c == '.') {
                        stringBuilder.append(c);
                        state = 6;
                    } else if (c == '[') {
                        state = 9;
                    } else if (c == 't' || c == 'f' || c == 'n') {
                        stringBuilder.append(c);
                        state = 7;
                    } else if (c == '{') {
                        StringBuilder subObjectBuilder = new StringBuilder();
                        subObjectBuilder.append(c);
                        int balance = 1;

                        while (balance != 0 && ++i < jsonString.length()) {
                            c = jsonString.charAt(i);
                            if (c == '{') balance++;
                            if (c == '}') balance--;
                            subObjectBuilder.append(c);
                        }
                        JsonObject subObject = fromString(subObjectBuilder.toString());
                        json.put(key, subObject);
                        key = "";
                        state = 8;
                    } else {
                        throw new IllegalArgumentException("invalid value.");
                    }break;

                case 5:
                    if (c != '"') {
                        stringBuilder.append(c);
                    } else {
                        value = stringBuilder.toString();
                        json.put(key, value);
                        key = "";
                        stringBuilder.setLength(0);
                        state = 8;
                    }
                    break;

                case 6:
                    if (Character.isDigit(c) || c == '.') {
                        stringBuilder.append(c);
                    } else {
                        value = stringBuilder.toString();

                        if (value.contains(".")) {
                            json.put(key, Double.parseDouble(value));
                        } else {
                            json.put(key, Integer.parseInt(value));
                        }
                        key = "";
                        stringBuilder.setLength(0);

                        if (c == ',') {
                            state = 1;
                        } else if (c == '}') {
                            return json;
                        } else {
                            throw new IllegalArgumentException("Error in number format.");
                        }
                    }
                    break;

                case 7:
                    stringBuilder.append(c);
                    if ("true".contentEquals(stringBuilder)) {
                        json.put(key, true);
                        key = "";
                        stringBuilder.setLength(0);
                        state = 8;
                    } else if ("false".contentEquals(stringBuilder)) {
                        json.put(key, false);
                        key = "";
                        stringBuilder.setLength(0);
                        state = 8;
                    } else if ("null".contentEquals(stringBuilder)) {
                        json.put(key);
                        key = "";
                        stringBuilder.setLength(0);
                        state = 8;
                    }
                    break;

                case 8:
                    if (c == ',') {
                        state = 1;
                    } else if (c == '}') {
                        return json;
                    } else {
                        throw new IllegalArgumentException("expected ',' o '}'" );
                    }
                    break;
                case 9:
                    List<Object> list = new ArrayList<>();
                    while (c != ']') {
                        if (Character.isWhitespace(c)) {
                            i++;
                            c = jsonString.charAt(i);
                            continue;
                        }

                        if (c == '"') {
                            stringBuilder.setLength(0);
                            while (++i < jsonString.length() && jsonString.charAt(i) != '"') {
                                stringBuilder.append(jsonString.charAt(i));
                            }
                            list.add(stringBuilder.toString());
                        } else if (Character.isDigit(c) || c == '-' || c == '.') {
                            stringBuilder.setLength(0);
                            while (i < jsonString.length() && (Character.isDigit(c) || c == '.')) {
                                stringBuilder.append(c);
                                if (++i < jsonString.length()) c = jsonString.charAt(i);
                            }
                            i--;
                            if (stringBuilder.toString().contains(".")) {
                                list.add(Double.parseDouble(stringBuilder.toString()));
                            } else {
                                list.add(Integer.parseInt(stringBuilder.toString()));
                            }
                        } else if (c == ',') {

                        } else if (c == ']') {
                            break;
                        } else {
                            throw new IllegalArgumentException("Invalid list element.");
                        }
                        i++;
                        c = jsonString.charAt(i);
                    }
                    json.put(key, list);
                    stringBuilder.setLength(0);
                    key = "";
                    state = 8;
                    break;
                default:
                    throw new IllegalStateException("invalid state: " + state);
            }
        }

        throw new IllegalArgumentException("JSON uncompleted.");
    }

    public JsonObject put(String key, String value) {
        validateKey(key);
        data.put(key, value);
        return this;
    }

    public JsonObject put(String key, JsonObject json) {
        validateKey(key);
        data.put(key, json);
        return this;
    }

    public JsonObject put(String key, Number value) {
        validateKey(key);
        data.put(key, value);
        return this;
    }

    public JsonObject put(String key, boolean value) {
        validateKey(key);
        data.put(key, value);
        return this;
    }

    public JsonObject put(String key, List<?> values) {
        validateKey(key);
        data.put(key, values);
        return this;
    }

    public JsonObject put(String key) {
        validateKey(key);
        data.put(key, null);
        return this;
    }

    public JsonObject remove(String key) {
        validateKey(key);
        data.remove(key);
        return this;
    }

    public boolean containsKey(String key) {
        validateKey(key);
        return data.containsKey(key);
    }

    public Object get(String key) {
        validateKey(key);
        return data.get(key);
    }

    public Map<String, Object> entries() {
        return new HashMap<>(data);
    }

    @Override
    public String toString() {
        return data.entrySet().stream()
                .map(entry -> formatEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private String formatEntry(String key, Object value) {
        String formattedKey = "\"" + key + "\"";
        String formattedValue = formatValue(value);
        return formattedKey + ":" + formattedValue;
    }

    private String formatValue(Object value) {
        return switch (value) {
            case String _ -> "\"" + value + "\"";
            case JsonObject _ -> value.toString();
            case List<?> list -> formatList(list);
            case null, default -> String.valueOf(value);
        };
    }

    private String formatList(List<?> list) {
        return list.stream()
                .map(item -> item instanceof String ? "\"" + item + "\"" : item.toString())
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("null key");
        }
    }

}
