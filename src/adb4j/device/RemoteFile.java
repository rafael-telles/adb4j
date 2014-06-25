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
import adb4j.exceptions.AdbException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RemoteFile {

    public static final Pattern ESCAPE_PATTERN = Pattern.compile("([\\\\()*+?\"'&#/\\s])");
    public static final Pattern LIST_PATTERN = Pattern.compile("(?<type>\\S)(?<permissions>[rwx-]{9})\\s+(?<owner>\\S+)\\s+(?<group>\\S+)(\\s+(?<size>\\d+))?\\s+(?<date>[0-9-]{10})\\s+(?<time>\\d{2}:\\d{2})(\\s+(?<name>.+))?");

    private final Device device;
    private long size;
    private String name, permissions, date, time, owner, group;
    private boolean exist;
    private FileType type;
    private RemoteFile parent;

    public static RemoteFile root(Device device) throws IOException, AdbException {
        return new RemoteFile("", null, device);
    }

    public static RemoteFile fromPath(String path, Device device) throws IOException, AdbException {
        String[] segments = path.split("/");
        RemoteFile file = root(device);
        for (int i = 1; i < segments.length; i++) {
            file = new RemoteFile(segments[i], file, device);
        }
        return file;
    }

    protected static RemoteFile fromInfo(String info, RemoteFile parent, Device device) throws IOException, AdbException {
        RemoteFile file = new RemoteFile("", parent, device, false);
        file.update(info);
        return file;
    }

    protected RemoteFile(String name, RemoteFile parent, Device device, boolean shouldUpdate) throws IOException, AdbException {
        this.name = name;
        this.parent = parent;
        this.device = device;
        if (shouldUpdate) {
            update();
        }
    }

    protected RemoteFile(String name, RemoteFile parent, Device device) throws IOException, AdbException {
        this(name, parent, device, true);
    }

    public String getPath() {
        return getPath(false);
    }

    public String getPath(boolean escape) {
        if (parent != null) {
            String r = parent.getPath(escape) + '/';
            if (escape) {
                r += ESCAPE_PATTERN.matcher(name).replaceAll("\\\\$1");
            } else {
                r += name;
            }
            return r;
        } else {
            return "";
        }
    }

    protected void update(String info) {
        Matcher m = LIST_PATTERN.matcher(info);
        if (m.find()) {
            exist = true;
            permissions = m.group("permissions");
            owner = m.group("owner");
            group = m.group("group");
            date = m.group("date");
            time = m.group("time");
            name = m.group("name");
            type = FileType.fromIdentifier(m.group("type").charAt(0));
            if (type == FileType.FILE) {
                size = Long.parseLong(m.group("size"));
            }
        } else if (info.contains(getPath(false) + ": No such file or directory")) {
            exist = false;
        }
    }

    public void update() throws IOException, AdbException {
        String command = "ls -ld " + getPath(true);
        String out = getDevice().shell(command);
        update(out);
    }

    public RemoteFile getChild(String name) throws IOException, AdbException {
        return RemoteFile.fromPath(getPath(false) + '/' + name, device);
    }

    public RemoteFile[] getChildren() throws IOException, AdbException {
        if (type != FileType.DIRECTORY && type != FileType.DIRECTORY_LINK) {
            return new RemoteFile[0];
        }
        String out = getDevice().shell("ls -l " + getPath(true));

        RemoteFile[] children = new RemoteFile[out.split("\n").length];
        int n = 0;
        Matcher m = LIST_PATTERN.matcher(out);
        while (m.find()) {
            children[n++] = RemoteFile.fromInfo(m.group(), this, device);
        }
        return children;
    }

    public boolean isFile() {
        return type == FileType.FILE;
    }

    public boolean isDirectory() {
        return type == FileType.DIRECTORY || type == FileType.DIRECTORY_LINK;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the type
     */
    public FileType getType() {
        return type;
    }

    /**
     * @return the parent
     */
    public RemoteFile getParent() {
        return parent;
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }

    public boolean exists() {
        return exist;
    }

    enum FileType {

        FILE('-'), BLOCK('b'), CHARACTER('c'), DIRECTORY('d'), DIRECTORY_LINK('l'), SOCKET('s'), FIFO('p');

        private final char identifier;

        private FileType(char identifier) {
            this.identifier = identifier;
        }

        public static FileType fromIdentifier(char identifier) {
            for (FileType deviceFileType : FileType.values()) {
                if (deviceFileType.getIdentifier() == identifier) {
                    return deviceFileType;
                }
            }
            return null;
        }

        /**
         * @return the identifier
         */
        public char getIdentifier() {
            return identifier;
        }
    }

}
