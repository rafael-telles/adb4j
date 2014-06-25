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
package adb4j.device;

/**
 *
 * @author Rafael Telles <raftel.ti@gmail.com>
 */
import adb4j.adb.Adb;
import adb4j.device.services.BatteryService;
import adb4j.device.services.FileService;
import adb4j.device.services.LogcatService;
import adb4j.exceptions.AdbException;
import adb4j.exceptions.AdbExceptionType;
import adb4j.executor.CmdResult;
import adb4j.utils.FileUtils;
import java.io.File;
import java.io.IOException;

public class Device {

    public final BatteryService battery = new BatteryService(this);
    public final LogcatService logcat = new LogcatService(this);
    public final FileService files = new FileService(this);
    private final String serial;
    private final Adb adb;

    public Device(String serial, Adb adb) {
        this.serial = serial;
        this.adb = adb;
    }

    public boolean isRooted() throws IOException, AdbException {
        return !shell("su -v").contains("su: not found");
    }

    public String getState() throws IOException, AdbException {
        return run("get-state").getOutputStream().toString();
    }

    public String getSerialNumber() throws IOException, AdbException {
        return run("get-serialno").getOutputStream().toString().trim();
    }

    public String getDevicePath() throws IOException, AdbException {
        return run("get-devpath").getOutputStream().toString();
    }

    public String getBugReport() throws IOException, AdbException {
        return run("bugreport").getOutputStream().toString();
    }

    public void rebootIntoSystem() throws IOException, AdbException {
        run("reboot");
    }

    public void rebootIntoRecovery() throws IOException, AdbException {
        run("reboot recovery");
    }

    public void rebootIntoBootloader() throws IOException, AdbException {
        run("reboot bootloader");
    }

    public void install(File pack) throws IOException, AdbException {
        CmdResult result = run("install " + FileUtils.getEscapedPath(pack));
        System.out.println("Output = " + result.getOutputStream());
        System.out.println("Error = " + result.getErrorStream());
        System.out.println("Exit value = " + result.getExitValue());
        if (result.getExitValue() != 0) {
            throw new AdbException(AdbExceptionType.FAILED_TO_INSTALL_PACKAGE);
        }
    }

    public void uninstall(String packagePath) throws IOException, AdbException {
        run("uninstall " + packagePath);
    }

    public void uninstall(String packagePath, boolean keepDataAndCache) throws IOException, AdbException {
        String args = "pm uninstall ";
        if (keepDataAndCache) {
            args += "-k ";
        }
        args += packagePath;
        shell(args);
    }

    public DeviceProperties getProperties() throws IOException, AdbException {
        DeviceProperties prop = new DeviceProperties();
        prop.load(this);
        return prop;
    }

    public String shell(String command) throws IOException, AdbException {
        return run("shell " + command).getOutputStream().toString();
    }

    public CmdResult run(String command) throws IOException, AdbException {
        return adb.run("-s " + serial + ' ' + command);
    }
}
