package org.camunda.bpm.examples.custom.mappings.process;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_UPDATE;
import static org.camunda.bpm.examples.custom.mappings.engine.cmd.EntityTypesExtended.ENTITY_TYPE_COMMENT;

import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessGiven;
import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessThen;
import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessWhen;
import org.camunda.bpm.examples.custom.mappings.spring.AbstractSpringTest;
import org.camunda.bpm.examples.custom.mappings.utils.DateUtils;
import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S2699") // Tests should include assertions
class ProcessTest extends AbstractSpringTest<ProcessGiven<?>, ProcessWhen<?>, ProcessThen<?>> {

  @Test
  void ensureComments() {

    var processComment = "Test process comment";
    var processCommentDate = DateUtils.dateInFuture(5, MINUTES);
    var updatedProcessComment = "Test process comment (updated)";
    var updatedProcessCommentDate = DateUtils.dateInFuture(10, MINUTES);

    var taskComment = "Test task comment";
    var updatedTaskComment = "Test task comment (updated)";
    var taskCommentDate = DateUtils.dateInFuture(15, MINUTES);
    var updatedTaskCommentDate = DateUtils.dateInFuture(20, MINUTES);

    given()
        .a_process_engine();

    when()
        .create_a_process(TEST_PROCESS);

    then()
        .active_processes(1, TEST_PROCESS)
        .active_task(USER_TASK_PROCESS_DATA);

    when()
        .comment_on_process(processComment, processCommentDate)
        .comment_on_tasks(USER_TASK_PROCESS_DATA, taskComment, taskCommentDate);
    then()
        .processes_of_type_$_have_comment(TEST_PROCESS, processComment, processCommentDate)
        .tasks_of_type_$_have_comment(USER_TASK_PROCESS_DATA, taskComment, taskCommentDate);

    when()
        .update_process_comment(updatedProcessComment)
        .update_process_comment(updatedProcessCommentDate)
        .update_task_comment(USER_TASK_PROCESS_DATA, updatedTaskComment)
        .update_task_comment(USER_TASK_PROCESS_DATA, updatedTaskCommentDate);
    then()
        .processes_of_type_$_have_comment(TEST_PROCESS, updatedProcessComment, updatedProcessCommentDate)
        .user_operation_log_for_process_contains(ENTITY_TYPE_COMMENT, OPERATION_TYPE_UPDATE)
        .tasks_of_type_$_have_comment(USER_TASK_PROCESS_DATA, updatedTaskComment, updatedTaskCommentDate)
        .user_operation_log_for_task_contains(ENTITY_TYPE_COMMENT, OPERATION_TYPE_UPDATE);

    when()
        .delete_process_comments()
        .delete_task_comments(USER_TASK_PROCESS_DATA);
    then()
        .processes_of_type_$_have_no_comment(TEST_PROCESS, processComment)
        .processes_of_type_$_have_no_comment(TEST_PROCESS, updatedProcessComment)
        .user_operation_log_for_process_contains(ENTITY_TYPE_COMMENT, OPERATION_TYPE_DELETE)
        .tasks_of_type_$_have_no_comment(USER_TASK_PROCESS_DATA, taskComment)
        .tasks_of_type_$_have_no_comment(USER_TASK_PROCESS_DATA, updatedTaskComment)
        .user_operation_log_for_task_contains(ENTITY_TYPE_COMMENT, OPERATION_TYPE_DELETE);

    when()
        .complete_task(USER_TASK_PROCESS_DATA);

    then()
        .completed_processes(1, TEST_PROCESS)
        .completed_tasks(1)
        .completed_tasks(1, USER_TASK_PROCESS_DATA);

  }

}
