package br.com.trustsystems.gravity.main.runner;


import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.runner.Runner;
import br.com.trustsystems.gravity.runner.impl.DefaultRunner;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Gravity extends DefaultRunner {

    private static Runner defaultRunner;

    public static Runner defaultRunner() {
        if (null == defaultRunner)
            defaultRunner = new Gravity();

        return defaultRunner;
    }

    public static void main(String[] args) throws UnRetriableException {

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d %p [ %c] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        Runner runner = defaultRunner();
        runner.init();
        runner.start();
    }
}
