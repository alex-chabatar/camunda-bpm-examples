package org.camunda.bpm.examples.custom.dmn.process;

import static org.camunda.bpm.examples.custom.dmn.ProcessConstants.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.camunda.bpm.examples.custom.dmn.process.scenario.DecisionGiven;
import org.camunda.bpm.examples.custom.dmn.process.scenario.DecisionThen;
import org.camunda.bpm.examples.custom.dmn.process.scenario.DecisionWhen;
import org.camunda.bpm.examples.custom.dmn.spring.AbstractSpringTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CustomFunctionTest extends AbstractSpringTest<DecisionGiven<?>, DecisionWhen<?>, DecisionThen<?>> {

  public static Stream<Arguments> containsAnyOfProvider() {
    return Stream.of(
        // json list/array
        arguments("[\"A\",\"B\"]", "1", RULE_1), // List<String>
        arguments("[\"A\",\"B\"]", "1", RULE_1), // List<String>
        arguments("[\"B\",\"C\"]", "1", RULE_1),
        arguments("[\"C\",\"D\"]", "2", RULE_2),
        arguments("[\"D\",\"E\"]", "2", RULE_2),
        arguments("[1,2,3,4,5]", "3", RULE_3), // List<Integer>
        // string
        arguments("ABC", "1", RULE_1),
        arguments("CDE", "2", RULE_2),
        // clearing
        arguments("[\"AB\",\"BC\"]", null, RULE_CLEARING),
        arguments("[\"ab\",\"bc\"]", null, RULE_CLEARING),
        arguments("[\"Ab\",\"bC\"]", null, RULE_CLEARING),
        arguments("[\"E\",\"F\"]", null, RULE_CLEARING),
        arguments("[3,4,5]", null, RULE_CLEARING), // List<Integer>
        arguments("EF", null, RULE_CLEARING));
  }

  @ParameterizedTest
  @MethodSource("containsAnyOfProvider")
  void containsAnyOf(String input, String output, String ruleId) {

    given()
        .a_decision_engine()
        .a_decision_input()
        .with(PARAM_INPUT, input);

    when()
        .evaluate_decision(TEST_DECISION_CONTAINS_ANY_OF);

    then()
        .decision_result()
        .has_decisions(1)
        .with_rule(ruleId)
        .with(PARAM_RESULT, output);

  }

}