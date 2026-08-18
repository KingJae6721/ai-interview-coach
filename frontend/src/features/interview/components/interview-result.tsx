"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import {
  generateInterviewFeedback,
  getInterviewResult,
} from "@/features/interview/services/interview-service";
import type { InterviewResultResponse } from "@/features/interview/types/interview";
import { ApiError } from "@/services/api-client";

interface InterviewResultProps {
  interviewId: number;
}

type ResultPhase = "checking" | "generating" | "success" | "error";
type PendingPhase = Exclude<ResultPhase, "success" | "error">;

interface ResultTask {
  phase: PendingPhase;
  promise: Promise<InterviewResultResponse>;
  listeners: Set<(phase: PendingPhase) => void>;
}

const resultTasks = new Map<number, ResultTask>();

function isApiErrorCode(error: unknown, code: string): boolean {
  return error instanceof ApiError && error.code === code;
}

function createResultTask(interviewId: number): ResultTask {
  const task: ResultTask = {
    phase: "checking",
    listeners: new Set(),
    promise: Promise.resolve(null as never),
  };

  task.promise = getInterviewResult(interviewId)
    .catch(async (error: unknown) => {
      if (!isApiErrorCode(error, "FEEDBACK_NOT_FOUND")) {
        throw error;
      }

      task.phase = "generating";
      task.listeners.forEach((listener) => listener("generating"));

      try {
        await generateInterviewFeedback(interviewId);
      } catch (generationError) {
        if (!isApiErrorCode(generationError, "FEEDBACK_ALREADY_EXISTS")) {
          throw generationError;
        }
      }

      return getInterviewResult(interviewId);
    })
    .finally(() => {
      resultTasks.delete(interviewId);
    });

  resultTasks.set(interviewId, task);
  return task;
}

function getResultTask(interviewId: number): ResultTask {
  return resultTasks.get(interviewId) ?? createResultTask(interviewId);
}

function getResultErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return "결과를 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";
  }

  const messages: Record<string, string> = {
    AI_REQUEST_FAILED:
      "AI가 면접 내용을 분석하지 못했습니다. 잠시 후 다시 시도해 주세요.",
    FEEDBACK_NOT_FOUND: "생성된 면접 피드백을 찾을 수 없습니다.",
    INTERVIEW_NOT_FOUND: "존재하지 않는 면접입니다.",
    INTERVIEW_NOT_COMPLETED: "완료된 면접만 결과를 확인할 수 있습니다.",
    FEEDBACK_GENERATION_NOT_AVAILABLE:
      "질문과 답변이 모두 준비된 면접만 분석할 수 있습니다.",
    ACCESS_DENIED: "이 면접 결과를 확인할 권한이 없습니다.",
  };

  return error.code ? (messages[error.code] ?? error.message) : error.message;
}

function formatAnsweredAt(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(date);
}

export function InterviewResult({ interviewId }: InterviewResultProps) {
  const [phase, setPhase] = useState<ResultPhase>("checking");
  const [result, setResult] = useState<InterviewResultResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let isActive = true;
    const task = getResultTask(interviewId);
    const handlePhase = (nextPhase: PendingPhase) => {
      if (isActive) {
        setPhase(nextPhase);
      }
    };

    task.listeners.add(handlePhase);
    task.promise
      .then((response) => {
        if (isActive) {
          setResult(response);
          setPhase("success");
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setErrorMessage(getResultErrorMessage(error));
          setPhase("error");
        }
      });

    return () => {
      isActive = false;
      task.listeners.delete(handlePhase);
    };
  }, [interviewId, retryCount]);

  if (phase === "checking" || phase === "generating") {
    return (
      <section
        role="status"
        className="rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm sm:p-12"
      >
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-zinc-900 text-xl text-white">
          ✦
        </div>
        <h1 className="mt-5 text-xl font-semibold text-zinc-900">
          {phase === "checking"
            ? "면접 결과를 확인하고 있습니다"
            : "면접 내용을 분석하고 있습니다"}
        </h1>
        <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-zinc-600">
          {phase === "checking"
            ? "기존에 생성된 피드백이 있는지 확인합니다."
            : "질문과 답변을 바탕으로 강점과 개선 방향을 정리하고 있습니다. 잠시만 기다려 주세요."}
        </p>
      </section>
    );
  }

  if (phase === "error" || !result) {
    return (
      <section className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-xl font-semibold text-red-800">
          면접 결과를 준비하지 못했습니다.
        </h1>
        <p role="alert" className="mt-3 text-sm text-red-600">
          {errorMessage}
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <button
            type="button"
            onClick={() => {
              setErrorMessage("");
              setPhase("checking");
              setRetryCount((count) => count + 1);
            }}
            className="rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
          >
            다시 시도
          </button>
          <Link
            href="/dashboard"
            className="rounded-lg border border-zinc-300 px-5 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-50"
          >
            대시보드로 이동
          </Link>
        </div>
      </section>
    );
  }

  const { feedback } = result;

  return (
    <div className="space-y-6">
      <header className="overflow-hidden rounded-2xl bg-zinc-900 p-6 text-white shadow-sm sm:p-9">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm text-zinc-400">
              Interview #{result.interviewId} · {result.status}
            </p>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight sm:text-3xl">
              {result.title}
            </h1>
            <p className="mt-4 max-w-2xl text-sm leading-6 text-zinc-300">
              {feedback.summary}
            </p>
          </div>
          <div className="flex size-32 shrink-0 flex-col items-center justify-center rounded-full border-4 border-white/20 bg-white/10">
            <strong className="text-4xl font-semibold">
              {feedback.overallScore}
            </strong>
            <span className="mt-1 text-xs text-zinc-300">100점 만점</span>
          </div>
        </div>
      </header>

      <section className="grid gap-4 md:grid-cols-3">
        <article className="rounded-2xl border border-emerald-200 bg-emerald-50 p-5">
          <h2 className="font-semibold text-emerald-900">강점</h2>
          <p className="mt-3 text-sm leading-6 whitespace-pre-wrap text-emerald-800">
            {feedback.strengths}
          </p>
        </article>
        <article className="rounded-2xl border border-rose-200 bg-rose-50 p-5">
          <h2 className="font-semibold text-rose-900">약점</h2>
          <p className="mt-3 text-sm leading-6 whitespace-pre-wrap text-rose-800">
            {feedback.weaknesses}
          </p>
        </article>
        <article className="rounded-2xl border border-blue-200 bg-blue-50 p-5">
          <h2 className="font-semibold text-blue-900">개선 제안</h2>
          <p className="mt-3 text-sm leading-6 whitespace-pre-wrap text-blue-800">
            {feedback.improvementSuggestions}
          </p>
        </article>
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
        <div>
          <h2 className="text-xl font-semibold text-zinc-900">질문별 복기</h2>
          <p className="mt-1 text-sm text-zinc-500">
            총 {result.questionAnswers.length}개의 질문과 답변입니다.
          </p>
        </div>
        <div className="mt-5 space-y-3">
          {result.questionAnswers.map((item) => (
            <details
              key={item.questionOrder}
              className="group rounded-xl border border-zinc-200 bg-zinc-50 open:bg-white"
            >
              <summary className="flex cursor-pointer list-none items-start gap-3 px-4 py-4 sm:px-5">
                <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-zinc-900 text-xs font-medium text-white">
                  {item.questionOrder}
                </span>
                <span className="flex-1 text-sm leading-6 font-medium text-zinc-900 sm:text-base">
                  {item.questionContent}
                </span>
                <span className="text-zinc-400 transition-transform group-open:rotate-180">
                  ⌄
                </span>
              </summary>
              <div className="border-t border-zinc-200 px-4 py-5 sm:px-5">
                <p className="text-xs font-medium text-zinc-500">내 답변</p>
                <p className="mt-2 rounded-2xl rounded-tr-md bg-zinc-900 px-4 py-3 text-sm leading-6 whitespace-pre-wrap text-white">
                  {item.answerContent}
                </p>
                <time className="mt-2 block text-right text-xs text-zinc-500">
                  {formatAnsweredAt(item.answeredAt)}
                </time>
              </div>
            </details>
          ))}
        </div>
      </section>

      <div className="flex flex-wrap justify-end gap-3">
        <Link
          href="/interviews/new"
          className="rounded-lg border border-zinc-300 bg-white px-5 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-50"
        >
          새 면접 시작
        </Link>
        <Link
          href="/dashboard"
          className="rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          대시보드로 이동
        </Link>
      </div>
    </div>
  );
}
