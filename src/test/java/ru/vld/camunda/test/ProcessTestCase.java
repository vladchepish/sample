package ru.vld.camunda.test;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;

public class ProcessTestCase {

    @Rule
    public ProcessEngineRule rule =
            new ProcessEngineRule(new StandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    @Test
    @Deployment(resources = "sample.bpmn")
    public void testSuccessProject(){
        ProcessEngine processEngine = rule.getProcessEngine();

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("sample");

        assertThat(pi).task("DoSomethingUserTask").hasCandidateGroup("management");

        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskCandidateGroup("management").list();
        assertEquals(1, tasks.size());
        assertEquals("DoSomethingUserTask", tasks.get(0).getTaskDefinitionKey());

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("a", 3);
        processEngine.getTaskService().complete(tasks.get(0).getId(), variables);

        Job job = processEngine.getManagementService().createJobQuery().timers().singleResult();
        processEngine
                .getManagementService()
                .executeJob(job.getId());

        assertThat(pi).isStarted().isEnded().hasPassed("FailedEndEvent");
    }

    @Test
    @Deployment(resources = "sample.bpmn")
    public void failedTest(){
        ProcessEngine processEngine = rule.getProcessEngine();

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("sample");

        assertThat(pi).task("DoSomethingUserTask").hasCandidateGroup("management");

        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskCandidateGroup("management").list();
        assertEquals(1, tasks.size());
        assertEquals("DoSomethingUserTask", tasks.get(0).getTaskDefinitionKey());

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("a", 6);
        processEngine.getTaskService().complete(tasks.get(0).getId(), variables);

        Execution execution = processEngine.getRuntimeService().createExecutionQuery().processInstanceId(pi.getId()).messageEventSubscriptionName("MSG_SOMETHING").singleResult();
        processEngine.getRuntimeService().messageEventReceived("MSG_SOMETHING", execution.getId());

        assertThat(pi).isStarted().isEnded().hasPassed("SuccessEndEvent");
    }

}
