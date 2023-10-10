package dev.arubik.realmcraft.Managers;

public interface Module {
    public void register();

    public default void unregister() {

    };

    public String configId();

    public String displayName();
}
