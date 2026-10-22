import { execFileSync } from "node:child_process";

export default function globalTeardown(): void {
  execFileSync(
    "docker",
    [
      "compose",
      "-p",
      "ai-interview-e2e",
      "-f",
      "e2e/docker-compose.yml",
      "down",
      "--volumes",
      "--remove-orphans",
    ],
    { cwd: process.cwd(), stdio: "inherit" },
  );
}
