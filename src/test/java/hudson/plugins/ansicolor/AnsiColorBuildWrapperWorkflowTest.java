package hudson.plugins.ansicolor;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import hudson.Functions;

public class AnsiColorBuildWrapperWorkflowTest {
	@ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Test
    public void testWorkflowWrap() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue(!Functions.isWindows());
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n"
                        + "  wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm', 'defaultFg': 1, 'defaultBg': 2]) {\n"
                        + "    sh(\"\"\"#!/bin/bash\n"
                        + "      echo -e '\\\\e[31mred\\\\e[0m'\"\"\"\n"
                        + "    )\n"
                        + "  }\n"
                        + "}"
                ));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                p.getLastBuild().getLogText().writeHtmlTo(0L, writer);
                assertTrue(writer.toString().matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"));                        
            }
        });
    }

}
