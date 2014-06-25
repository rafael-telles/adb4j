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
import java.io.ByteArrayOutputStream;
import java.util.Optional;

public class CmdResult {

    private final ByteArrayOutputStream outputStream;
    private final ByteArrayOutputStream errorStream;
    private final int exitValue;
    private final Optional<Exception> exception;

    public CmdResult(int exitValue, Optional<Exception> exception, ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream) {
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        this.exitValue = exitValue;
        this.exception = exception;
    }

    /**
     * @return the outputStream
     */
    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * @return the errorStream
     */
    public ByteArrayOutputStream getErrorStream() {
        return errorStream;
    }

    /**
     * @return the exitValue
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * @return the exception
     */
    public Optional<Exception> getException() {
        return exception;
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }

}
