package com.example.back.backtest.executor;

import java.io.IOException;
import java.util.List;

public interface ProcessLauncher {
    Process start(List<String> command) throws IOException;
}
