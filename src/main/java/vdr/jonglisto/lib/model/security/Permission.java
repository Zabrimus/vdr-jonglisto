package vdr.jonglisto.lib.model.security;

public class Permission implements Comparable<Permission> {

    private int id;
    private String permission;
    private String messageKey;
    private String part;

    public Permission() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String toString() {
        return "Permission [id=" + id + ", permission=" + permission + ", messageKey=" + messageKey + ", part=" + part
                + "]";
    }

    @Override
    public int compareTo(Permission o) {
        return this.permission.compareTo(o.getPermission());
    }
}
