package client.inventory;

public enum SocketFlag {

    DEFAULT(1),
    SOCKET_BOX_1(2),
    SOCKET_BOX_2(4),
    SOCKET_BOX_3(8),
    USED_SOCKET_1(16),
    USED_SOCKET_2(32),
    USED_SOCKET_3(64);
    private final int i;

    private SocketFlag(int i) {
        this.i = i;
    }

    public final int getValue() {
        return this.i;
    }

    public final boolean check(int flag) {
        return (flag & this.i) == this.i;
    }
}