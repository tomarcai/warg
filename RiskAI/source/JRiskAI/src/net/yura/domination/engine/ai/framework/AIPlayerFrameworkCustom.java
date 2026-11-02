package net.yura.domination.engine.ai.framework;

public class AIPlayerFrameworkCustom extends AIPlayerFrameworkAbstract {

    public static final int TYPE = 7;

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public String getCommand() {
        return "framework_custom";
    }
}
