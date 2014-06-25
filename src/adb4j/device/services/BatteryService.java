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
package adb4j.device.services;

/**
 *
 * @author Rafael Telles <raftel.ti@gmail.com>
 */
import adb4j.device.Device;
import adb4j.exceptions.AdbException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatteryService {

    private final Device device;

    public BatteryService(Device device) {
        this.device = device;
    }

    public String getInfo() throws IOException, AdbException {
        return device.shell("dumpsys battery");
    }

    public boolean isPresent() throws IOException, AdbException {
        String out = getInfo();
        Pattern p = Pattern.compile("present: ?(.+)");
        Matcher m = p.matcher(out);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return true;
    }

    public int getLevel() throws AdbException, IOException {
        String out = getInfo();
        Pattern p = Pattern.compile("level: ?(\\d+)");
        Matcher m = p.matcher(out);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    public int getScale() throws AdbException, IOException {
        String out = getInfo();
        Pattern p = Pattern.compile("scale: ?(\\d+)");
        Matcher m = p.matcher(out);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 100;
    }

    public String getTechnology() throws AdbException, IOException {
        String out = getInfo();
        Pattern p = Pattern.compile("technology: ?(.+)");
        Matcher m = p.matcher(out);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
