package net.yura.domination.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

public class FailOnWarningsRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                Logger root = Logger.getLogger("");
                List<LogRecord> warnings = new CopyOnWriteArrayList<>();

                Handler h = new Handler() {
                    public void publish(LogRecord r) {
                        if (r.getLevel().intValue() >= Level.WARNING.intValue())
                            warnings.add(r);
                    }
                    public void flush() {}
                    public void close() {}
                };

                root.addHandler(h);
                try {
                    base.evaluate();
                } finally {
                    root.removeHandler(h);
                }

                if (!warnings.isEmpty())
                    throw new AssertionError("Warnings logged: " + warnings);
            }
        };
    }
}
