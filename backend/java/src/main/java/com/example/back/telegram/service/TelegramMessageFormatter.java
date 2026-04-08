package com.example.back.telegram.service;

import com.example.back.runs.dto.RunResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TelegramMessageFormatter {

    public String formatStartMessage() {
        return """
                Trade360Lab Bot
                
                Панель управления для прогонов, тестов на истории и оптимизации.
                
                Команды:
                /runs — recent runs
                /last — latest run
                /run <id> — run details
                /help — help
                /settings — preferences
                """;
    }

    public String formatHelpMessage() {
        return """
                Trade360Lab Bot • Help
                
                /runs — show recent runs
                /last — show latest run
                /run <id> — show run details
                /settings — preferences
                
                Уведомления:
                • run started
                • run completed
                • run failed
                """;
    }

    public String formatRunLookupHint() {
        return "Send /run <id>";
    }

    public String formatOptimizationSoon() {
        return "Optimization UI is coming soon";
    }

    public String formatRunNotFound(Long runId) {
        return "Run not found: #" + runId;
    }

    public String formatRunsModeNotImplemented() {
        return "Notification mode persistence is not implemented yet.";
    }

    public String formatUnknownCommand() {
        return """
                Unknown command.
                
                Use /help to see available actions.
                """;
    }

    public String formatRunStarted(RunResponse run) {
        return """
                TradeLab • Run Started
                
                Run: #%d
                Strategy ID: %s
                Status: %s
                """
                .formatted(
                        run.id(),
                        safe(run.strategyId()),
                        safe(run.status())
                );
    }

    public String formatRunCompleted(RunResponse run) {
        StringBuilder sb = new StringBuilder();
        sb.append("TradeLab • Run Completed\n\n");
        sb.append("Run: #").append(run.id()).append("\n");
        sb.append("Strategy ID: ").append(safe(run.strategyId())).append("\n");
        sb.append("Status: ").append(safe(run.status())).append("\n");

        Map<String, Object> metrics = run.metrics();
        if (metrics != null && !metrics.isEmpty()) {
            sb.append("\n");
            appendMetric(sb, "Net Profit", metrics.get("netProfit"));
            appendMetric(sb, "Win Rate", metrics.get("winRate"));
            appendMetric(sb, "Max Drawdown", metrics.get("maxDrawdown"));
            appendMetric(sb, "Sharpe", metrics.get("sharpe"));
            appendMetric(sb, "Trades", metrics.get("trades"));
        }

        return sb.toString();
    }

    public String formatRunFailed(RunResponse run) {
        StringBuilder sb = new StringBuilder();
        sb.append("TradeLab • Run Failed\n\n");
        sb.append("Run: #").append(run.id()).append("\n");
        sb.append("Strategy ID: ").append(safe(run.strategyId())).append("\n");
        sb.append("Status: ").append(safe(run.status())).append("\n");

        if (run.errorMessage() != null && !run.errorMessage().isBlank()) {
            sb.append("\nError:\n");
            sb.append(run.errorMessage());
        }

        return sb.toString();
    }

    public String formatRecentRuns(List<RunResponse> runs) {
        StringBuilder sb = new StringBuilder();
        sb.append("TradeLab • Recent Runs\n\n");

        if (runs == null || runs.isEmpty()) {
            sb.append("No runs found.");
            return sb.toString();
        }

        for (RunResponse run : runs) {
            sb.append("#").append(run.id())
                    .append(" • ")
                    .append(safe(run.status()));

            if (run.strategyId() != null) {
                sb.append(" • strategy ").append(run.strategyId());
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String formatRunDetails(RunResponse run) {
        StringBuilder sb = new StringBuilder();
        sb.append("TradeLab • Run Details\n\n");
        sb.append("Run: #").append(run.id()).append("\n");
        sb.append("Strategy ID: ").append(safe(run.strategyId())).append("\n");
        sb.append("Status: ").append(safe(run.status())).append("\n");
        sb.append("Created: ").append(safe(run.createdAt())).append("\n");
        sb.append("Started: ").append(safe(run.startedAt())).append("\n");
        sb.append("Finished: ").append(safe(run.finishedAt())).append("\n");

        if (run.parameters() != null && !run.parameters().isEmpty()) {
            sb.append("\nParameters:\n");
            sb.append(formatMap(run.parameters()));
        }

        if (run.metrics() != null && !run.metrics().isEmpty()) {
            sb.append("\nMetrics:\n");
            sb.append(formatMap(run.metrics()));
        }

        if (run.errorMessage() != null && !run.errorMessage().isBlank()) {
            sb.append("\nError:\n");
            sb.append(run.errorMessage()).append("\n");
        }

        return sb.toString();
    }

    public String formatSettingsMessage(boolean notificationsEnabled) {
        return """
                TradeLab • Settings
                
                Notifications: %s
                """
                .formatted(notificationsEnabled ? "enabled" : "disabled");
    }

    private void appendMetric(StringBuilder sb, String label, Object value) {
        if (value != null) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private String formatMap(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " = " + safe(entry.getValue()))
                .collect(Collectors.joining("\n")) + "\n";
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
