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
package adb4j.adb;

/**
 *
 * @author Rafael Telles <raftel.ti@gmail.com>
 */
import adb4j.exceptions.AdbException;
import adb4j.exceptions.AdbExceptionType;
import adb4j.executor.CmdExecutor;
import adb4j.executor.CmdOutputStream;
import adb4j.executor.CmdResult;
import adb4j.executor.CmdResultHandler;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AdbCore {

    private String path = "C:\\adb\\adb.exe";
    private final CmdExecutor executor = new CmdExecutor();

    protected AdbCore(String path) {
        this.path = path;
    }

    public boolean isRunning() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            CmdResult result = getExecutor().run("tasklist");
            return result.getOutputStream().toString().contains("adb.exe");
        }

        return false;
    }

    public CmdResult run(String command) throws IOException, AdbException {
        if (!command.endsWith("-server") && !isRunning()) {
            run("start-server");
        }

        CmdResult result = getExecutor().run(getCommandLine(command));
        throwException(result);
        return result;
    }

    public void runAsync(String command, final CmdResultHandler handler) throws IOException {
        getExecutor().runAsync(getCommandLine(command), new CmdResultHandler() {

            @Override
            public void onProcessComplete(int exitValue, Optional<Exception> exception, CmdOutputStream outputData, CmdOutputStream errorData) {
                CmdResult result = new CmdResult(exitValue, exception, outputData, errorData);
                try {
                    throwException(result);
                    handler.onProcessComplete(exitValue, exception, outputData, errorData);
                } catch (AdbException adbException) {
                    handler.onProcessComplete(exitValue, Optional.of(adbException), outputData, errorData);
                }
            }
        });
    }

    private void throwException(CmdResult result) throws AdbException {
        Optional<Exception> resultException = result.getException();
        if (resultException.isPresent()) {
            String errorMessage = result.getErrorStream().toString();

            if (errorMessage.contains("device not found")) {
                throw new AdbException(AdbExceptionType.DEVICE_NOT_FOUND);
            } else if (errorMessage.contains("does not exist")) {
                throw new AdbException(AdbExceptionType.OBJECT_DOES_NOT_EXIST);
            } else if (errorMessage.contains("cannot connect to daemon")) {
                throw new AdbException(AdbExceptionType.UNABLE_TO_CONNECT);
            } else if (errorMessage.contains("* failed to start daemon *")) {
                throw new AdbException(AdbExceptionType.FAILED_TO_START_DAEMON);
            }
            if (!new File(path).exists()) {
                throw new AdbException(AdbExceptionType.BINARY_NOT_FOUND);
            }
        }
    }

    protected String getCommandLine(String command) {
        return getPath() + ' ' + command;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the executor
     */
    public CmdExecutor getExecutor() {
        return executor;
    }
}
