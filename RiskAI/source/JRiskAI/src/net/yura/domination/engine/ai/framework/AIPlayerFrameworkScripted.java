package net.yura.domination.engine.ai.framework;

public class AIPlayerFrameworkScripted extends AIPlayerFrameworkAbstract {

    public static final int TYPE = 9;

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public String getCommand() {
        return "framework";
    }
}
