import { expect, test, type Page } from "@playwright/test";

async function seedSession(page: Page) {
  await page.addInitScript(() => {
    window.localStorage.setItem(
      "tradelab.auth",
      JSON.stringify({
        token: "smoke-token",
        user: {
          id: 1,
          email: "smoke@example.test",
          createdAt: "2026-05-04T00:00:00Z",
        },
      })
    );
  });
  await page.route("**/api/health", (route) =>
    route.fulfill({ json: { status: "ok", service: "java-api" } })
  );
  await page.route("**/api/python/health", (route) =>
    route.fulfill({ json: { status: "ok", service: "python-parser" } })
  );
  await page.route("**/api/live/risk/status", (route) =>
    route.fulfill({ json: { killSwitchActive: false, circuitBreakers: [] } })
  );
  await page.route("**/api/live/risk/events", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/live/exchange/health?exchange=binance", (route) =>
    route.fulfill({
      json: {
        exchange: "binance",
        connected: true,
        credentialsValid: false,
        realOrderSubmissionEnabled: false,
        message: "smoke",
      },
    })
  );
  await page.route("**/api/live/credentials/status", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/live/sessions", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/live/orders", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/live/positions", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/strategies", (route) => route.fulfill({ json: [] }));
  await page.route("**/api/strategy-templates", (route) => route.fulfill({ json: [] }));
}

test("auth boundary exposes login and register", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByRole("button", { name: /sign in|login|войти/i })).toBeVisible();

  await page.goto("/register");
  await expect(page.getByRole("button", { name: /create account|register|создать|зарегистр/i })).toBeVisible();
});

test("workspace and datasets pages render demo boundaries", async ({ page }) => {
  await seedSession(page);
  await page.goto("/workspace");
  await expect(page.getByText("Demo data")).toBeVisible();

  await page.goto("/data");
  await expect(page.getByText("Demo data visible")).toBeVisible();
});

test("strategy upload and run visibility surfaces render", async ({ page }) => {
  await seedSession(page);
  await page.goto("/strategies");
  await expect(page.getByText(/strategy|стратег/i).first()).toBeVisible();

  await page.goto("/backtests");
  await expect(page.getByText(/backtest|бэктест/i).first()).toBeVisible();
});

test("live trading safety and service health pages render", async ({ page }) => {
  await seedSession(page);
  await page.goto("/live");
  await expect(page.getByText("Safety state")).toBeVisible();
  await expect(page.getByText("Binance testnet certification")).toBeVisible();

  await page.goto("/settings");
  await expect(page.getByText("Service Health")).toBeVisible();
  await expect(page.getByText("v0.9.1-alpha.1").first()).toBeVisible();
});
