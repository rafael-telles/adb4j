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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;

public class CmdResultHandler {

    private CmdOutputStream outputStream;
    private CmdOutputStream errorStream;
    protected Optional<CountDownLatch> countDownLatch;
    protected CmdExecutor executor;

    public PumpStreamHandler getStreamHandler(CmdExecutor executor) {
        this.executor = executor;
        outputStream = new CmdOutputStream(this);
        errorStream = new CmdOutputStream(this);
        return new PumpStreamHandler(getOutputStream(), getErrorStream());
    }

    public CmdResultHandler clone() {
        CmdResultHandler original = this;
        CmdResultHandler clone = new CmdResultHandler() {

            @Override
            public void onProcessComplete(int exitValue, Optional<Exception> exception, CmdOutputStream outputData, CmdOutputStream errorData) {
                original.onProcessComplete(exitValue, exception, outputData, errorData);
            }

            @Override
            public void onDataReceive(CmdOutputStream stream, byte[] b, int off, int len) {
                original.onDataReceive(stream, b, off, len);
            }

        };
        return clone;
    }

    protected ExecuteResultHandler delegate = new ExecuteResultHandler() {

        @Override
        public void onProcessComplete(int exitValue) {
            executor.exception = Optional.empty();
            executor.exitValue = exitValue;
            CmdResultHandler.this.onProcessComplete(exitValue, Optional.empty(), getOutputStream(), getErrorStream());
            countDown();
        }

        @Override
        public void onProcessFailed(ExecuteException executeException) {
            executor.exception = Optional.of(executeException);
            executor.exitValue = executeException.getExitValue();
            CmdResultHandler.this.onProcessComplete(executeException.getExitValue(), executor.exception, getOutputStream(), getErrorStream());
            countDown();
        }
    };

    protected void countDown() {
        countDownLatch.ifPresent(CountDownLatch::countDown);
    }

    public void onProcessComplete(int exitValue, Optional<Exception> exception, CmdOutputStream outputData, CmdOutputStream errorData) {
    }

    public void onDataReceive(CmdOutputStream stream, byte[] b, int off, int len) {

    }

    /**
     * @return the outputStream
     */
    public CmdOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * @return the errorStream
     */
    public CmdOutputStream getErrorStream() {
        return errorStream;
    }

}
