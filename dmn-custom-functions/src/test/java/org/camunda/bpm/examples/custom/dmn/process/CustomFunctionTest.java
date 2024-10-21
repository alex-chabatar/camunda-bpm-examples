package org.camunda.bpm.examples.custom.dmn.process;

import static org.camunda.bpm.examples.custom.dmn.ProcessConstants.*;
import static org.camunda.bpm.examples.custom.dmn.utils.DateUtils.dateInPastFrom;
import static org.camunda.bpm.examples.custom.dmn.utils.DateUtils.parseDate;
import static org.camunda.bpm.examples.custom.dmn.utils.JsonHelper.toJSON;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
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

  public static Stream<Arguments> dateTimeProvider() {
    return Stream.of(
        // Weekly email
        arguments("01.10.2024 10:00", Duration.ofDays(7), true, "WEEKLY"),
        arguments("08.10.2024 10:00", Duration.ofDays(8), true, "WEEKLY"),
        arguments("15.10.2024 10:00", Duration.ofDays(9), true, "WEEKLY"),
        arguments("22.10.2024 10:00", Duration.ofDays(10), true, "WEEKLY"),
        arguments("29.10.2024 10:00", Duration.ofDays(11), true, "WEEKLY"),
        // Monthly email
        arguments("05.10.2024 10:00", Duration.ofDays(30), true, "MONTHLY"),
        // Default
        arguments("06.10.2024 10:00", Duration.ofDays(1), false, null)
    );
  }

  @ParameterizedTest
  @MethodSource("dateTimeProvider")
  void dateTime(String dateTime, Duration lastEmailSent, boolean sendReminderEmail, String emailType) {

    var currentTime = parseDate(dateTime);
    var lastEmailSentTime = dateInPastFrom(currentTime, lastEmailSent);

    given()
        .a_decision_engine()
        .a_decision_input()
        .with(PARAM_LAST_EMAIL_SENT, lastEmailSentTime);

    when()
        .set_clock(currentTime)
        .evaluate_decision(TEST_DECISION_DATE_TIME);

    then()
        .decision_result()
        .has_decisions(1)
        .with(PARAM_SEND_REMINDER_EMAIL, sendReminderEmail)
        .with(PARAM_EMAIL_TYPE, emailType);

  }

  public static Stream<Arguments> jsonValueProvider() {
    return Stream.of(
        arguments(Map.of(
                PARAM_PROJECT_ID, "P1",
                PARAM_CUSTOMER_ID, "123"),
            "NORMAL",
            "START_NEW_PROCESS"),
        arguments(Map.of(
                PARAM_PROJECT_ID, "P2",
                PARAM_CUSTOMER_ID, "123",
                PARAM_MMSE, 25),
            "NORMAL",
            "PUBSUB"),
        arguments(Map.of(
                PARAM_PROJECT_ID, "P3",
                PARAM_CUSTOMER_ID, "123",
                PARAM_MMSE, 20),
            "HIGH",
            "EMAIL_AND_PUBSUB"),
        arguments(Map.of(
                PARAM_PROJECT_ID, "P4",
                PARAM_CUSTOMER_ID, "123",
                PARAM_MMSE, 20),
            "NORMAL",
            "DO_NOTHING")
    );
  }

  @ParameterizedTest
  @MethodSource("jsonValueProvider")
  void jsonValue(Map<String, Object> variables, String priority, String action) throws IOException {

    var variablesJson = toJSON(variables);

    given()
        .a_decision_engine()
        .a_decision_input()
        .with(PARAM_VARIABLES, variablesJson)
        .with(PARAM_PRIORITY, priority);

    when()
        .evaluate_decision(TEST_DECISION_JSON);

    then()
        .decision_result()
        .has_decisions(1)
        .with(PARAM_ACTION, action);

  }

}