package edu.marist.jointstudy.essence.api.client;

public class LoggingBlockchainObserver extends BlockchainPullObserver {

    public LoggingBlockchainObserver(String name) {
        super(name);
    }

    public void on(BlockchainPullEvent e) {
        System.out.println(getName() + ": " + e.toString());
    }

    @Override
    public void onFailed(Exception e) {
        System.out.println(getName() + ": Blockchain pull failed. Exception: " + e.getMessage());
    }
}
