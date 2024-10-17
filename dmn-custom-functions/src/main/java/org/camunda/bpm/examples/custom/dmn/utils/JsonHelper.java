package org.camunda.bpm.examples.custom.dmn.utils;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@Component
public class JsonHelper {

  public static final ObjectMapper MAPPER = new ObjectMapper()
      // register SPIs
      .findAndRegisterModules()
      .disable(WRITE_DATES_AS_TIMESTAMPS)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .enable(INDENT_OUTPUT)
      .setDateFormat(new StdDateFormat().withColonInTimeZone(true));

  public static String toJSON(Object unit) throws IOException {
    return toJSON(unit, false);
  }

  public static String toJSON(Object unit, boolean format) throws IOException {
    var writer = new StringWriter();
    if (format) {
      MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, unit);
    } else {
      MAPPER.writeValue(writer, unit);
    }
    return writer.toString();
  }

  public static <T> T fromJSON(String json, Class<T> clazz) throws IOException {
    return MAPPER.readValue(json, clazz);
  }

  public static List<String> fromJSONArray(String jsonArray) throws IOException {
    return fromJSONArray(jsonArray, String.class);
  }

  public static <T> List<T> fromJSONArray(String jsonArray, Class<T> clazz) throws IOException {
    var valueType = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
    return MAPPER.readValue(jsonArray, valueType);
  }

}
