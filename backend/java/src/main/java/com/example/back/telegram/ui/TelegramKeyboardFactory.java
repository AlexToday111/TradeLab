package com.example.back.telegram.ui;

import com.example.back.runs.dto.RunResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class TelegramKeyboardFactory {

    public static final String BUTTON_RUNS = "\uD83D\uDCCA Runs";
    public static final String BUTTON_LAST_RUN = "\uD83D\uDD04 Last Run";
    public static final String BUTTON_RUN_BY_ID = "\uD83D\uDD0D Run by ID";
    public static final String BUTTON_OPTIMIZATION = "\uD83D\uDCC8 Optimization";
    public static final String BUTTON_SETTINGS = "\u2699\uFE0F Settings";
    public static final String BUTTON_HELP = "\u2753 Help";

    public ReplyKeyboardMarkup createMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(BUTTON_RUNS);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(BUTTON_LAST_RUN);
        row2.add(BUTTON_RUN_BY_ID);
        row2.add(BUTTON_OPTIMIZATION);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(BUTTON_SETTINGS);
        row3.add(BUTTON_HELP);

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    public InlineKeyboardMarkup createRecentRunsKeyboard(List<RunResponse> runs) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        if (runs != null) {
            int limit = Math.min(runs.size(), 4);
            for (int i = 0; i < limit; i++) {
                RunResponse run = runs.get(i);

                InlineKeyboardButton openButton = InlineKeyboardButton.builder()
                        .text("Open #" + run.id())
                        .callbackData("run:detail:" + run.id())
                        .build();

                rows.add(new InlineKeyboardRow(openButton));
            }
        }

        InlineKeyboardButton refreshButton = InlineKeyboardButton.builder()
                .text("Refresh")
                .callbackData("runs:list")
                .build();

        rows.add(new InlineKeyboardRow(refreshButton));

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup createRunDetailsKeyboard(Long runId) {
        InlineKeyboardButton refreshButton = InlineKeyboardButton.builder()
                .text("Refresh")
                .callbackData("run:refresh:" + runId)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("Back to Runs")
                .callbackData("runs:list")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(refreshButton),
                new InlineKeyboardRow(backButton)
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup createSettingsKeyboard(boolean notificationsEnabled) {
        InlineKeyboardButton toggleNotifications = InlineKeyboardButton.builder()
                .text(notificationsEnabled ? "Disable Notifications" : "Enable Notifications")
                .callbackData("settings:toggle")
                .build();

        InlineKeyboardButton onlyErrors = InlineKeyboardButton.builder()
                .text("Only Errors")
                .callbackData("settings:errors")
                .build();

        InlineKeyboardButton allRuns = InlineKeyboardButton.builder()
                .text("All Runs")
                .callbackData("settings:all")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(toggleNotifications),
                new InlineKeyboardRow(onlyErrors, allRuns)
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup createHelpKeyboard() {
        InlineKeyboardButton runsButton = InlineKeyboardButton.builder()
                .text("Recent Runs")
                .callbackData("runs:list")
                .build();

        InlineKeyboardButton lastRunButton = InlineKeyboardButton.builder()
                .text("Last Run")
                .callbackData("runs:last")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(runsButton, lastRunButton)
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
