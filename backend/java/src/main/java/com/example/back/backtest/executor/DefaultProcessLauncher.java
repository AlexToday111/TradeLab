package com.example.back.backtest.executor;

import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultProcessLauncher implements ProcessLauncher {
    @Override
    public Process start(List<String> command) throws IOException {
        return new ProcessBuilder(command).start();
    }
}
