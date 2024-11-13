package org.camunda.bpm.examples.custom.dmn.engine;

import static java.util.stream.Collectors.toSet;
import static org.camunda.bpm.examples.custom.dmn.utils.JsonHelper.fromJSONArray;
import static org.camunda.bpm.examples.custom.dmn.utils.JsonHelper.jsonToMap;

import java.io.IOException;
import java.util.*;

import org.camunda.bpm.dmn.feel.impl.scala.function.CustomFunction;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

public class DmnCustomFunctionProvider implements FeelCustomFunctionProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DmnCustomFunctionProvider.class);

  private static final Map<String, CustomFunction> FUNCTIONS = new HashMap<>();

  private static final String LIST_START = "[";
  private static final String LIST_END = "]";

  private static final String CONTAINS_ANY_OF = "containsAnyOf"; // containsAnyOfEquals/Like?
  private static final String WORKFLOW_TIME = "workflow time"; // workflow time()
  private static final String JSON_VALUE = "json value"; // json value(?, 'key1')

  public DmnCustomFunctionProvider() {
    FUNCTIONS.put(CONTAINS_ANY_OF, containsAnyOf());
    FUNCTIONS.put(WORKFLOW_TIME, workflowTime());
    FUNCTIONS.put(JSON_VALUE, jsonValue());
  }

  @Override
  public Optional<CustomFunction> resolveFunction(String functionName) {
    return Optional.ofNullable(FUNCTIONS.get(functionName));
  }

  @Override
  public Collection<String> getFunctionNames() {
    return FUNCTIONS.keySet();
  }

  // -- DMN custom functions

  /**
   * Format: containsAnyOf(input, entries...)
   *
   * <p>
   * Supports as Input (see below in XML):
   * <ul>
   * <li>json-array(list) String or Long/Integer as Element</li>
   * <li>String - works as String.contains (substring)</li>
   * </ul>
   * </p>
   *
   * <p>
   * entries... - comma separated
   * </p>
   *
   * Camunda-Modeler:
   *
   * <pre>
   * <inputEntry id="UnaryTests_JsonStringArray"> <!-- List::contains -->
   *   <text>containsAnyOf(?, "A", "B")</text>
   * </inputEntry>
   * <inputEntry id="UnaryTests_JsonIntegerOrLongArray"> <!-- List::contains -->
   *   <text>containsAnyOf(?, 1, 2)</text>
   * </inputEntry>
   * <inputEntry id="UnaryTests_String"> <!-- String::contains (substring) -->
   *   <text>containsAnyOf(?, "abc", "def")</text>
   * </inputEntry>
   * </pre>
   *
   * @return true if expression evaluates to true, otherwise false
   */
  private CustomFunction containsAnyOf() {
    return CustomFunction.create()
        .enableVarargs() // containsAnyOf(?, "A", "B")
        .setFunction(args -> {
          if (!ObjectUtils.isEmpty(args) && args.get(0) != null) {
            var input = args.get(0).toString();
            var entries = args.stream().skip(1).map(Object::toString).collect(toSet());
            if (entries.isEmpty())
              return false; // nothing to check
            if (input.startsWith(LIST_START) && input.endsWith(LIST_END)) { // json list/array
              try {
                return fromJSONArray(input).stream().anyMatch(entries::contains);
              } catch (IOException e) {
                LOG.error("{}: Error while evaluating {} with arguments: {}", CONTAINS_ANY_OF, input, entries, e);
              }
            } else { // default String::contains
              return entries.stream().anyMatch(input::contains);
            }
          }
          return false;
        }).build();
  }

  // Format: workflow time()
  // Return: current workflow time
  private CustomFunction workflowTime() {
    return CustomFunction.create()
        .setFunction(args -> getWorkflowCurrentTime())
        .build();
  }

  // Format: json value(?, 'key1')
  // Return: value object (String, Boolean, Integer, Long, Double, Date)
  private CustomFunction jsonValue() {
    return CustomFunction.create()
        .enableVarargs()
        .setFunction(args -> {
          var json = (String) args.get(0);
          var key = (String) args.get(1);
          var defaultValue = args.size() > 2 ? args.get(2) : "";
          try {
            var map = jsonToMap(json);
            return Optional.ofNullable(map.get(key)).orElse(defaultValue);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .build();
  }

  private Date getWorkflowCurrentTime() {
    return ClockUtil.getCurrentTime();
  }

}
