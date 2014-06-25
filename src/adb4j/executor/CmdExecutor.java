/*
 * Copyright (C) 2014 Rafael Telles <raftel.ti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package adb4j.executor;

/**
 *
 * @author Rafael Telles <raftel.ti@gmail.com>
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class CmdExecutor {

    protected final CmdResultHandler defaultHandler = new CmdResultHandler();
    private Map environment = new HashMap();
    protected int exitValue;
    protected Optional<Exception> exception = Optional.empty();

    public CmdExecutor() {
    }

    public CmdExecutor(Map environment) {
        this.environment = environment;
    }

    private CmdResultHandler runAsync(String command, CmdResultHandler resultHandler, boolean shouldClone) throws IOException, IOException, IOException, IOException {
        CmdResultHandler handler = shouldClone ? resultHandler.clone() : resultHandler;

        PumpStreamHandler streamHandler = handler.getStreamHandler(this);
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

//        String[] arguments = CommandLine.parse(command).toStrings();
//        CommandLine cmdLine = new CommandLine(arguments[0]);
//        for (int i = 1; i < arguments.length; i++) {
//            cmdLine.addArgument(command, true);
//        }
        CommandLine cmdLine = CommandLine.parse(command);
        executor.execute(cmdLine, getEnvironment(), handler.delegate);

        return handler;
    }

    /**
     * Runs a general command but does not wait for it to exit.<p>
     * After the process' exiting, it calls resultHandler.onProcessComplete<p>
     * When it receives data, it calls resultHandler.onDataReceive
     *
     * @param command The command you want to run.
     * @param resultHandler The object that handles with data and events.
     * @throws IOException
     */
    public void runAsync(String command, CmdResultHandler resultHandler) throws IOException {
        runAsync(command, resultHandler, true);
    }

    /**
     * Runs a general command and wait for it to exit.
     *
     * @param command The command you want to run
     * @return
     * @throws IOException
     * @see CmdResult
     * @see runAsync
     */
    public CmdResult run(String command) throws IOException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        defaultHandler.countDownLatch = Optional.of(countDownLatch);

        runAsync(command, defaultHandler, false);

        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
        }
        return new CmdResult(exitValue, exception, defaultHandler.getOutputStream(), defaultHandler.getErrorStream());
    }

    /**
     * @return the environment
     */
    public Map getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(Map environment) {
        this.environment = environment;
    }
}
