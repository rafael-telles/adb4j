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

public class LogcatService {

    private final Device device;

    public LogcatService(Device device) {
        this.device = device;
    }

    public void clear() throws IOException, AdbException {
        device.run("logcat -c");
    }

    public String dump() throws IOException, AdbException {
        return device.run("logcat -d").getOutputStream().toString();
    }
}
