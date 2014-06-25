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
import adb4j.device.RemoteFile;
import adb4j.exceptions.AdbException;
import adb4j.executor.CmdResult;
import java.io.File;
import java.io.IOException;

public class FileService {

    private final Device device;

    public FileService(Device device) {
        this.device = device;
    }

    public RemoteFile getRoot() throws IOException, AdbException {
        return RemoteFile.root(device);
    }

    public RemoteFile getFile(String path) throws IOException, AdbException {
        return RemoteFile.fromPath(path, device);
    }

    public File pull(RemoteFile remote) throws IOException, AdbException {
        File local = File.createTempFile("Adb4j_", '_' + remote.getName());
        pull(remote, local);
        return local;
    }

    public boolean pull(RemoteFile remote, File local) throws IOException, AdbException {
        if (!remote.exists()) {
            return false;
        }

        if (remote.isDirectory()) {
            if (local.exists()) {
                if (local.isFile()) {
                    return false;
                }
            } else {
                if (!local.mkdirs()) {
                    return false;
                }
            }
            RemoteFile[] children = remote.getChildren();
            boolean r = true;
            for (RemoteFile child : children) {
                File newLocal = new File(local, child.getName());
                if (!pull(child, newLocal)) {
                    r = false;
                }
            }
            return r;
        }

        if (remote.isFile()) {
            if (local.canWrite() || !local.exists()) {

                if (local.isFile() && local.exists()) {
                    if (!local.delete()) {
                        return false;
                    }
                }
                String command = String.format("pull \"%s\" \"%s\"", remote.getPath(), local.getAbsolutePath());
                CmdResult result = device.run(command);

                if (local.isDirectory()) {
                    for (String child : local.list()) {
                        if (child.equals(remote.getName())) {
                            return true;
                        }
                    }
                } else {
                    return local.exists();
                }
            }
        }
        return false;
    }
}
