package ru.vld.camunda.test;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExampleProcessTest {

    @Rule
    public ProcessEngineRule rule =
            new ProcessEngineRule(new StandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    @Test
    @Deployment(resources = "example.bpmn")
    public void testExampleProcess() throws InterruptedException {
        ProcessEngine processEngine = rule.getProcessEngine();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operation", "sub");
        params.put("variableOne", 2);
        params.put("variableTwo", 1);

        ProcessInstance pi = processEngine
                .getRuntimeService()
                .startProcessInstanceByKey("ExampleProcess", params);

        sleep(3000);

        VariableInstance result = processEngine.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(pi.getId()).variableName("variableResult").singleResult();


        assertNotNull("Ответ определённо не должен быть ноль",result);
        assertEquals("Фактический результат " + result + " отличается от ожидаемого 1L",1L, result);
    }

}
