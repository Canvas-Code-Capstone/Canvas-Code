package com.canvas.service.helperServices;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ProcessExecutorTest {

    @Test
    public void testExecuteProcess() throws IOException {
        File file = new File("fooDirectory");
        file.mkdirs();

        String[] executeCommands = {"ls"};
        ProcessExecutor processExecutor = new ProcessExecutor(executeCommands, "fooDirectory");

        Assertions.assertTrue(processExecutor.executeProcess());

        file.delete();
    }

    @Test
    public void testExecuteProcess_throwsException() throws IOException {
        String[] executeCommands = {"ls"};
        ProcessExecutor processExecutor = new ProcessExecutor(executeCommands, "fooDirectory");

        Assertions.assertFalse(processExecutor.executeProcess());
    }

    @Test
    public void testGetProcessOutput() {
        File file = new File("fooDirectory");
        file.mkdirs();

        String[] executeCommands = {"sh", "echo \"hello\""};
        ProcessExecutor processExecutor = new ProcessExecutor(executeCommands, "fooDirectory");

        processExecutor.executeProcess();
        Assertions.assertEquals(
                "sh: echo \"hello\": No such file or directory\n",
                processExecutor.getProcessOutput()
        );
        file.delete();
        // Probably a better way to do this test
    }
}