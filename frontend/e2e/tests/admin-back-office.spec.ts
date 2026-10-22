import { expect, test, type Page } from "@playwright/test";

import { E2E } from "../fixtures";

const forbiddenValues = [
  "passwordHash",
  "refreshToken",
  "accessToken",
  "resumeText",
  "rawPdf",
  "aiPrompt",
  "DB_PASSWORD",
  "JWT_SECRET",
  E2E.answerSentinel,
  E2E.resumeSentinel,
];

async function login(page: Page, email: string): Promise<void> {
  await page.goto("/login");
  await page.getByLabel("이메일").fill(email);
  await page.getByLabel("비밀번호").fill(E2E.password);
  await page.getByRole("button", { name: "로그인", exact: true }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
}

async function searchUser(page: Page, email: string) {
  await page.getByPlaceholder("이메일 검색").fill(email);
  await page.getByRole("button", { name: "검색" }).click();
  return page.getByRole("row").filter({ hasText: email });
}

async function changeRole(page: Page, email: string): Promise<void> {
  const row = await searchUser(page, email);
  await row.getByRole("button", { name: "Role 변경" }).click();
  const dialog = page.getByRole("dialog", { name: "관리자 권한 변경" });
  await expect(dialog).toContainText(email);
  await dialog.getByRole("button", { name: "확인" }).click();
}

test.describe.serial("Admin Back Office", () => {
  test("USER는 admin UI와 API에 접근할 수 없다", async ({ page }) => {
    await login(page, E2E.userEmail);
    await page.goto("/admin");
    await expect(page).toHaveURL(/\/dashboard$/);

    const status = await page.evaluate(async (backendBaseUrl) => {
      const serialized = sessionStorage.getItem(
        "ai-interview-coach.auth-session",
      );
      if (!serialized) return 0;
      const session = JSON.parse(serialized) as { accessToken: string };
      const response = await fetch(`${backendBaseUrl}/api/v1/admin/dashboard`, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
      });
      return response.status;
    }, E2E.backendBaseUrl);
    expect(status).toBe(403);
  });

  test("ADMIN dashboard는 실제 집계와 최근 면접을 표시한다", async ({
    page,
  }) => {
    await login(page, E2E.adminEmail);
    await page.goto("/admin");
    await expect(
      page.getByRole("heading", { name: "Dashboard" }),
    ).toBeVisible();
    await expect(page.getByText("전체 사용자")).toBeVisible();
    await expect(page.getByText("전체 면접")).toBeVisible();
    await expect(page.getByText("진행 중")).toBeVisible();
    await expect(page.getByText("완료", { exact: true })).toBeVisible();
    await expect(page.getByText("취소", { exact: true })).toBeVisible();
    await expect(page.getByText(E2E.interviewTitle)).toBeVisible();
  });

  test("Users 검색, 필터, pagination과 상세 이동이 동작한다", async ({
    page,
  }) => {
    await login(page, E2E.adminEmail);
    await page.goto("/admin/users");
    await expect(page.getByRole("heading", { name: "Users" })).toBeVisible();
    await expect(page.getByRole("button", { name: "다음" })).toBeEnabled();
    await page.getByRole("button", { name: "다음" }).click();
    await expect(page.getByText("2 / 2")).toBeVisible();

    await page.getByRole("combobox").nth(0).selectOption("USER");
    await page.getByRole("combobox").nth(1).selectOption("ACTIVE");
    const row = await searchUser(page, E2E.userEmail);
    await expect(row).toContainText("USER");
    await row.getByRole("link", { name: "상세" }).click();
    await expect(page.getByText(E2E.userEmail)).toBeVisible();
    await expect(page.getByText("면접 통계")).toBeVisible();
  });

  test("Role 변경은 확인 후 반영되고 audit log로 남는다", async ({ page }) => {
    await login(page, E2E.adminEmail);
    await page.goto("/admin/users");
    await changeRole(page, E2E.userEmail);
    await expect(await searchUser(page, E2E.userEmail)).toContainText("ADMIN");

    await page.getByRole("link", { name: "Audit Logs" }).click();
    await expect(page.getByText("USER_ROLE_CHANGED").first()).toBeVisible();
    const changedRow = page
      .getByRole("row")
      .filter({ hasText: "USER_ROLE_CHANGED" })
      .filter({ hasText: "USER" })
      .filter({ hasText: "ADMIN" });
    await expect(changedRow.first()).toBeVisible();
  });

  test("마지막 ADMIN 강등은 409 메시지를 표시하고 Role을 유지한다", async ({
    page,
  }) => {
    await login(page, E2E.userEmail);
    await page.goto("/admin/users");

    await changeRole(page, E2E.adminEmail);
    await expect(await searchUser(page, E2E.adminEmail)).toContainText("USER");

    const selfRow = await searchUser(page, E2E.userEmail);
    await selfRow.getByRole("button", { name: "Role 변경" }).click();
    const dialog = page.getByRole("dialog", { name: "관리자 권한 변경" });
    await dialog.getByRole("button", { name: "확인" }).click();
    await expect(dialog.getByRole("alert")).toContainText(
      "마지막 관리자의 권한은 해제할 수 없습니다.",
    );
    await dialog.getByRole("button", { name: "취소" }).click();
    await expect(await searchUser(page, E2E.userEmail)).toContainText("ADMIN");

    await changeRole(page, E2E.adminEmail);
    await page.getByRole("button", { name: "로그아웃" }).click();
    await login(page, E2E.adminEmail);
    await page.goto("/admin/users");
    await changeRole(page, E2E.userEmail);
    await expect(await searchUser(page, E2E.userEmail)).toContainText("USER");
  });

  test("Interviews 필터와 상세는 원문 없이 운영 metadata만 표시한다", async ({
    page,
  }) => {
    await login(page, E2E.adminEmail);
    const adminResponses: string[] = [];
    page.on("response", async (response) => {
      if (response.url().includes("/api/v1/admin/") && response.ok()) {
        adminResponses.push(await response.text());
      }
    });

    await page.goto("/admin/interviews");
    await page.getByRole("combobox").selectOption("READY");
    await page.getByPlaceholder("사용자 이메일 검색").fill(E2E.userEmail);
    await page.getByRole("button", { name: "검색" }).click();
    const row = page.getByRole("row").filter({ hasText: E2E.interviewTitle });
    await expect(row).toContainText("1 / 1");
    await row.getByRole("link", { name: /#/ }).click();

    await expect(
      page.getByRole("heading", { name: E2E.interviewTitle }),
    ).toBeVisible();
    await expect(page.getByText("E2E Backend Posting")).toBeVisible();
    await expect(page.getByText("답변 있음")).toBeVisible();
    await expect(page.getByText("평가 있음")).toBeVisible();

    const exposed = `${await page.locator("body").innerText()} ${adminResponses.join(" ")}`;
    for (const forbidden of forbiddenValues) {
      expect(exposed).not.toContain(forbidden);
    }
  });

  test("Audit Logs는 읽기 전용 목록과 pagination을 제공한다", async ({
    page,
  }) => {
    await login(page, E2E.adminEmail);
    await page.goto("/admin/audit-logs");
    await expect(page.getByText("USER_ROLE_CHANGED").first()).toBeVisible();
    await expect(page.getByRole("button", { name: "다음" })).toBeEnabled();
    await page.getByRole("button", { name: "다음" }).click();
    await expect(page.getByText("2 / 2")).toBeVisible();
    await expect(page.getByRole("button", { name: /수정|삭제/ })).toHaveCount(
      0,
    );
  });

  test("좁은 viewport에서도 sidebar navigation과 error state를 사용할 수 있다", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await login(page, E2E.adminEmail);
    await page.route("**/api/v1/admin/dashboard", async (route) => {
      await route.fulfill({
        status: 500,
        contentType: "application/json",
        body: JSON.stringify({
          success: false,
          code: "E2E_FORCED_ERROR",
          message: "E2E forced error",
          data: null,
        }),
      });
    });
    await page.goto("/admin");
    await expect(
      page.getByRole("alert").filter({ hasText: "E2E forced error" }),
    ).toBeVisible();
    await expect(page.getByRole("button", { name: "다시 시도" })).toBeVisible();
    await expect(page.getByRole("link", { name: "Users" })).toBeVisible();
    await page.getByRole("link", { name: "Users" }).click();
    await expect(page.getByRole("heading", { name: "Users" })).toBeVisible();
  });
});
