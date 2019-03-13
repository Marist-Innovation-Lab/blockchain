package edu.marist.jointstudy.essence.api.client;

public abstract class BlockchainPullObserver {

    private String name;

    public BlockchainPullObserver(String name) {
        this.name = name;
    }

    public abstract void on(BlockchainPullEvent e);

    public abstract void onFailed(Exception e);

    public String getName() {
        return name;
    }
}
