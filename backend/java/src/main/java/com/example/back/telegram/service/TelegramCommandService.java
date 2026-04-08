package com.example.back.telegram.service;

import com.example.back.runs.dto.RunResponse;
import com.example.back.runs.service.RunQueryService;
import com.example.back.telegram.config.TelegramBotProperties;
import com.example.back.telegram.ui.TelegramKeyboardFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramCommandService {

    private static final int RECENT_RUNS_LIMIT = 5;
    private static final String WELCOME_IMAGE_RESOURCE = "telegram/welcome.png";

    private final RunQueryService runQueryService;
    private final TelegramBotProperties telegramBotProperties;
    private final TelegramMessageFormatter telegramMessageFormatter;
    private final TelegramKeyboardFactory telegramKeyboardFactory;

    public TelegramCommandResponse handleMessage(String messageText) {
        String normalized = normalize(messageText);

        return switch (normalized) {
            case "/start" -> withMainMenuPhoto(telegramMessageFormatter.formatStartMessage(), WELCOME_IMAGE_RESOURCE);
            case "/help", TelegramKeyboardFactory.BUTTON_HELP -> withMainMenu(telegramMessageFormatter.formatHelpMessage());
            case "/runs", TelegramKeyboardFactory.BUTTON_RUNS -> buildRecentRunsResponse();
            case "/last", TelegramKeyboardFactory.BUTTON_LAST_RUN -> buildLastRunResponse();
            case "/settings", TelegramKeyboardFactory.BUTTON_SETTINGS -> buildSettingsResponse();
            case TelegramKeyboardFactory.BUTTON_RUN_BY_ID -> new TelegramCommandResponse(
                    telegramMessageFormatter.formatRunLookupHint(),
                    null
            );
            case TelegramKeyboardFactory.BUTTON_OPTIMIZATION -> new TelegramCommandResponse(
                    telegramMessageFormatter.formatOptimizationSoon(),
                    null
            );
            default -> handleTextWithArguments(normalized);
        };
    }

    public TelegramCommandResponse handleCallback(String callbackData) {
        if (callbackData == null || callbackData.isBlank()) {
            return new TelegramCommandResponse(telegramMessageFormatter.formatUnknownCommand(), null);
        }

        if ("runs:list".equals(callbackData)) {
            return buildRecentRunsResponse();
        }
        if ("runs:last".equals(callbackData)) {
            return buildLastRunResponse();
        }
        if ("settings:show".equals(callbackData) || "settings:toggle".equals(callbackData)) {
            return buildSettingsResponse();
        }
        if ("settings:errors".equals(callbackData) || "settings:all".equals(callbackData)) {
            return new TelegramCommandResponse(
                    telegramMessageFormatter.formatRunsModeNotImplemented(),
                    telegramKeyboardFactory.createSettingsKeyboard(telegramBotProperties.isNotificationsEnabled())
            );
        }
        if (callbackData.startsWith("run:detail:") || callbackData.startsWith("run:refresh:")) {
            return buildRunDetailsResponse(parseRunId(callbackData.substring(callbackData.lastIndexOf(':') + 1)));
        }

        return new TelegramCommandResponse(telegramMessageFormatter.formatUnknownCommand(), null);
    }

    private TelegramCommandResponse handleTextWithArguments(String normalized) {
        if (normalized.startsWith("/run")) {
            String argument = normalized.substring(4).trim();
            if (argument.isBlank()) {
                return new TelegramCommandResponse(telegramMessageFormatter.formatRunLookupHint(), null);
            }
            return buildRunDetailsResponse(parseRunId(argument));
        }

        return new TelegramCommandResponse(telegramMessageFormatter.formatUnknownCommand(), null);
    }

    private TelegramCommandResponse buildRecentRunsResponse() {
        List<RunResponse> runs = runQueryService.listRecentRuns(RECENT_RUNS_LIMIT);
        return new TelegramCommandResponse(
                telegramMessageFormatter.formatRecentRuns(runs),
                telegramKeyboardFactory.createRecentRunsKeyboard(runs)
        );
    }

    private TelegramCommandResponse buildLastRunResponse() {
        return runQueryService.findLastRun()
                .map(this::toRunDetailsResponse)
                .orElseGet(() -> new TelegramCommandResponse("No runs found.", null));
    }

    private TelegramCommandResponse buildRunDetailsResponse(Long runId) {
        if (runId == null) {
            return new TelegramCommandResponse(telegramMessageFormatter.formatRunLookupHint(), null);
        }

        Optional<RunResponse> run = runQueryService.findRun(runId);
        return run.map(this::toRunDetailsResponse)
                .orElseGet(() -> new TelegramCommandResponse(telegramMessageFormatter.formatRunNotFound(runId), null));
    }

    private TelegramCommandResponse buildSettingsResponse() {
        boolean notificationsEnabled = telegramBotProperties.isNotificationsEnabled();
        return new TelegramCommandResponse(
                telegramMessageFormatter.formatSettingsMessage(notificationsEnabled),
                telegramKeyboardFactory.createSettingsKeyboard(notificationsEnabled)
        );
    }

    private TelegramCommandResponse toRunDetailsResponse(RunResponse run) {
        return new TelegramCommandResponse(
                telegramMessageFormatter.formatRunDetails(run),
                telegramKeyboardFactory.createRunDetailsKeyboard(run.id())
        );
    }

    private TelegramCommandResponse withMainMenu(String text) {
        return new TelegramCommandResponse(text, telegramKeyboardFactory.createMainMenuKeyboard());
    }

    private TelegramCommandResponse withMainMenuPhoto(String text, String photoResourcePath) {
        return new TelegramCommandResponse(text, telegramKeyboardFactory.createMainMenuKeyboard(), photoResourcePath);
    }

    private Long parseRunId(String rawValue) {
        try {
            return Long.parseLong(rawValue.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalize(String messageText) {
        if (messageText == null) {
            return "";
        }
        return messageText.trim();
    }
}
