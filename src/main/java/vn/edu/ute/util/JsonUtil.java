package vn.edu.ute.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.lang.reflect.Type;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import java.time.format.DateTimeFormatter;
import com.google.gson.JsonParser;

public class JsonUtil {
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return true;
        }
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonParseException ex) {
            return false;
        }
    }

    public static boolean isValidJsonObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return true;
        }
        try {
            return JsonParser.parseString(json).isJsonObject();
        } catch (JsonParseException ex) {
            return false;
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}
