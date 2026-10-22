import { defineConfig, devices } from "@playwright/test";

const frontendBaseUrl = "http://127.0.0.1:13000";
const backendBaseUrl = "http://127.0.0.1:18081";

export default defineConfig({
  testDir: "./e2e/tests",
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [["list"], ["html", { open: "never" }]],
  globalSetup: "./e2e/global-setup.ts",
  globalTeardown: "./e2e/global-teardown.ts",
  use: {
    baseURL: frontendBaseUrl,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: [
    {
      command:
        "docker compose -p ai-interview-e2e -f e2e/docker-compose.yml up --build --force-recreate",
      url: `${backendBaseUrl}/actuator/health`,
      timeout: 300_000,
      reuseExistingServer: false,
      stdout: "pipe",
      stderr: "pipe",
    },
    {
      command:
        "npm run build && npm run start -- --hostname 127.0.0.1 --port 13000",
      url: frontendBaseUrl,
      timeout: 180_000,
      reuseExistingServer: false,
      env: {
        NEXT_PUBLIC_API_BASE_URL: backendBaseUrl,
      },
      stdout: "pipe",
      stderr: "pipe",
    },
  ],
});
