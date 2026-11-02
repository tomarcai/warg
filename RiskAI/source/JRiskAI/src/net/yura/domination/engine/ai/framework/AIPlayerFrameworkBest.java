package net.yura.domination.engine.ai.framework;

public class AIPlayerFrameworkBest extends AIPlayerFrameworkAbstract {

    public static final int TYPE = 8;

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public String getCommand() {
        return "framework_best";
    }
}
