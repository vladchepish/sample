package ru.vld.camunda.test;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
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

public class SmallSampleProject {

    @Rule
    public ProcessEngineRule rule =
            new ProcessEngineRule(new StandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    @Test
    @Deployment(resources = "smallSample.bpmn")
    public void testSuccessProjectSmall(){
        ProcessEngine processEngine = rule.getProcessEngine();

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("sample");

        assertThat(pi).task("DoSomethingUserTask").hasCandidateGroup("management");

        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskCandidateGroup("management").list();
        assertEquals(1, tasks.size());
        assertEquals("DoSomethingUserTask", tasks.get(0).getTaskDefinitionKey());

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("a", 3);
        processEngine.getTaskService().complete(tasks.get(0).getId(), variables);

        assertThat(pi).isStarted().isEnded().hasPassed("FailedEndEvent");
    }

}
