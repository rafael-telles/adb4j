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
import adb4j.device.Device;
import adb4j.exceptions.AdbException;
import adb4j.exceptions.AdbExceptionType;
import adb4j.executor.CmdResult;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adb extends AdbCore {

    private String version = "unknown";

    protected Adb(String path) {
        super(path);
    }

    public static Adb createInstance() {
        String path = findPath();
        return createInstance(path);
    }

    public static Adb createInstance(String path) {
        return new Adb(path);
    }

    public static String findPath() {
        String fileName = "adb";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            fileName += ".exe";
        }

        String envPaths = System.getenv("PATH");
        String[] paths = envPaths.split(File.pathSeparator);
        for (String path : paths) {
            String[] list = new File(path).list();
            for (String child : list) {
                if (child.equals(fileName)) {
                    return path + File.separatorChar + child;
                }
            }
        }
        return fileName;
    }

    public void startServer() throws IOException, AdbException {
        run("start-server");
    }

    public void killServer() throws IOException, AdbException {
        run("kill-server");
    }

    public Device connect(String host) throws IOException, AdbException {
        CmdResult result = run("connect " + host);
        if (result.getOutputStream().toString().contains("unable to connect")) {
            throw new AdbException(AdbExceptionType.UNABLE_TO_CONNECT);
        }
        return new Device(host, this);
    }

    public Device connect(String host, int port) throws IOException, AdbException {
        return connect(host + ':' + port);
    }

    public void disconnect(String host) throws IOException, AdbException {
        CmdResult result = run("disconnect " + host);
        if (result.getOutputStream().toString().contains("No such device")) {
            throw new AdbException(AdbExceptionType.DEVICE_NOT_FOUND);
        }
    }

    public void disconnect(String host, int port) throws IOException, AdbException {
        disconnect(host + ':' + port);
    }

    public void requestRootPermissions() throws IOException, AdbException {
        CmdResult result = run("root");
        if (result.getOutputStream().toString().contains("disabled by system setting")) {
            throw new AdbException(AdbExceptionType.ROOT_ACCESS_DISABLED);
        }
    }

    public void waitForDevice() throws IOException, AdbException {
        run("wait-for-device");
    }

    public String getVersion() throws IOException, AdbException {
        if ("unknown".equals(version)) {
            String out = run("version").getOutputStream().toString();
            Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+");
            Matcher m = p.matcher(out);
            if (m.find()) {
                version = m.group();
            } else {
                version = out;
            }
        }
        return version;
    }

    public Device getDefaultDevice() throws IOException, AdbException {
        return getDevices()[0];
    }

    public Device[] getDevices() throws IOException, AdbException {
        String output = run("devices").getOutputStream().toString().trim();
        String[] lines = output.split("\n");
        if (lines.length > 1) {
            Device[] devices = new Device[lines.length - 1];
            for (int i = 1; i < lines.length; i++) {
                devices[i - 1] = new Device(lines[i].split("\\t")[0], this);
            }
            return devices;
        }
        return new Device[0];
    }
}
