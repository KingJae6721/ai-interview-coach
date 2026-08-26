"use client";

import { useRouter } from "next/navigation";
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
} from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import {
  cancelInterview,
  completeInterview,
  generateFollowUpQuestion,
  generateInterviewFeedback,
  getInterviewState,
  getInterviewProgress,
  startInterview,
  submitInterviewAnswer,
} from "@/features/interview/services/interview-service";
import type { InterviewProgressResponse } from "@/features/interview/types/interview";
import { ApiError } from "@/services/api-client";

interface InterviewProgressProps {
  interviewId: number;
}

type LoadState = "loading" | "ready" | "success" | "cancelled" | "error";

const MINIMUM_PARTIAL_FEEDBACK_ANSWER_COUNT = 2;

const CATEGORY_LABELS: Record<string, string> = {
  CS: "CS",
  TECH_STACK: "기술 스택",
  EXPERIENCE: "경험",
  SITUATION: "상황",
  COMPANY_FIT: "회사 적합성",
};

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: "쉬움",
  MEDIUM: "보통",
  HARD: "어려움",
};

function getInterviewErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.code === "AI_REQUEST_FAILED") {
      return "AI 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.";
    }

    if (error.code === "INTERVIEW_ANSWER_ALREADY_EXISTS") {
      return "이미 답변한 질문입니다. 진행 상태를 다시 불러와 주세요.";
    }

    if (error.code === "ANSWER_ORDER_INVALID") {
      return "현재 답변할 순서의 질문이 아닙니다. 진행 상태를 다시 확인해 주세요.";
    }

    if (error.code === "INTERVIEW_NOT_STARTED") {
      return "아직 시작하지 않은 면접입니다.";
    }

    if (error.code === "INTERVIEW_ALREADY_STARTED") {
      return "이미 시작된 면접입니다. 진행 상태를 불러옵니다.";
    }

    if (error.code === "PARTIAL_FEEDBACK_GENERATION_NOT_AVAILABLE") {
      return "부분 피드백을 생성하려면 최소 2개의 답변이 필요합니다.";
    }
  }

  return getErrorMessage(error);
}

export function InterviewProgress({ interviewId }: InterviewProgressProps) {
  const router = useRouter();
  const conversationRef = useRef<HTMLDivElement>(null);
  const conversationEndRef = useRef<HTMLDivElement>(null);
  const isNearBottomRef = useRef(true);
  const isStartingRef = useRef(false);
  const isCancellingRef = useRef(false);
  const isGeneratingPartialFeedbackRef = useRef(false);
  const hasRenderedConversationRef = useRef(false);
  const highlightTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [progress, setProgress] = useState<InterviewProgressResponse | null>(
    null,
  );
  const [loadErrorMessage, setLoadErrorMessage] = useState("");
  const [answer, setAnswer] = useState("");
  const [submitErrorMessage, setSubmitErrorMessage] = useState("");
  const [followUpMessage, setFollowUpMessage] = useState("");
  const [followUpErrorMessage, setFollowUpErrorMessage] = useState("");
  const [pendingFollowUpQuestionId, setPendingFollowUpQuestionId] = useState<
    number | null
  >(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isGeneratingFollowUp, setIsGeneratingFollowUp] = useState(false);
  const [isCompleting, setIsCompleting] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [cancelErrorMessage, setCancelErrorMessage] = useState("");
  const [hasCancelled, setHasCancelled] = useState(false);
  const [isGeneratingPartialFeedback, setIsGeneratingPartialFeedback] =
    useState(false);

  const loadInterview = useCallback(async () => {
    setLoadErrorMessage("");

    try {
      const state = await getInterviewState(interviewId);

      if (state.status === "READY") {
        setProgress(null);
        setLoadState("ready");
        return;
      }

      if (state.status === "COMPLETED" || state.status === "CANCELLED") {
        router.replace(`/interviews/${interviewId}/result`);
        return;
      }

      setProgress(await getInterviewProgress(interviewId));
      setLoadState("success");
    } catch (error) {
      setLoadErrorMessage(getInterviewErrorMessage(error));
      setLoadState("error");
    }
  }, [interviewId, router]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadInterview();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [loadInterview]);

  const currentQuestion = useMemo(
    () =>
      progress?.questions.find(
        ({ questionId }) => questionId === progress.nextQuestionId,
      ) ?? null,
    [progress],
  );
  const visibleQuestions = useMemo(
    () =>
      progress?.questions.filter(
        ({ questionId, answeredAt }) =>
          answeredAt !== null || questionId === progress.nextQuestionId,
      ) ?? [],
    [progress],
  );
  const conversationVersion = visibleQuestions
    .map(({ questionId, answeredAt }) => `${questionId}:${answeredAt ?? ""}`)
    .join("|");
  const answeredCount =
    progress?.questions.filter(({ answeredAt }) => answeredAt !== null)
      .length ?? 0;
  const totalCount = progress?.questions.length ?? 0;
  const progressPercent =
    totalCount === 0 ? 0 : Math.round((answeredCount / totalCount) * 100);

  useEffect(() => {
    if (!conversationVersion) {
      return;
    }

    const shouldScroll =
      !hasRenderedConversationRef.current || isNearBottomRef.current;
    const behavior = hasRenderedConversationRef.current ? "smooth" : "auto";
    hasRenderedConversationRef.current = true;

    if (shouldScroll) {
      conversationEndRef.current?.scrollIntoView({ behavior, block: "end" });
    }
  }, [conversationVersion]);

  useEffect(
    () => () => {
      if (highlightTimerRef.current) {
        clearTimeout(highlightTimerRef.current);
      }
    },
    [],
  );

  const [highlightedQuestionId, setHighlightedQuestionId] = useState<
    number | null
  >(null);

  function handleConversationScroll() {
    const conversation = conversationRef.current;
    if (!conversation) {
      return;
    }

    const distanceFromBottom =
      conversation.scrollHeight -
      conversation.scrollTop -
      conversation.clientHeight;
    isNearBottomRef.current = distanceFromBottom < 96;
  }

  function scrollToQuestion(questionId: number, highlight = false) {
    document.getElementById(`question-${questionId}`)?.scrollIntoView({
      behavior: "smooth",
      block: "center",
    });

    if (!highlight) {
      return;
    }

    setHighlightedQuestionId(questionId);
    if (highlightTimerRef.current) {
      clearTimeout(highlightTimerRef.current);
    }
    highlightTimerRef.current = setTimeout(() => {
      setHighlightedQuestionId(null);
      highlightTimerRef.current = null;
    }, 1800);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (
      !currentQuestion ||
      isSubmitting ||
      isGeneratingFollowUp ||
      pendingFollowUpQuestionId !== null
    ) {
      return;
    }

    const normalizedAnswer = answer.trim();
    if (!normalizedAnswer) {
      setSubmitErrorMessage("답변을 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);
    setSubmitErrorMessage("");
    setFollowUpMessage("");
    setFollowUpErrorMessage("");

    try {
      await submitInterviewAnswer(currentQuestion.questionId, {
        answerContent: normalizedAnswer,
      });
      setAnswer("");
      setProgress(await getInterviewProgress(interviewId));

      if (currentQuestion.parentQuestionId === null) {
        setPendingFollowUpQuestionId(currentQuestion.questionId);
        await requestFollowUp(currentQuestion.questionId);
      }
    } catch (error) {
      setSubmitErrorMessage(getInterviewErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleStart() {
    if (isStartingRef.current) {
      return;
    }

    isStartingRef.current = true;
    setIsStarting(true);
    setLoadErrorMessage("");

    try {
      await startInterview(interviewId);
      setProgress(await getInterviewProgress(interviewId));
      setLoadState("success");
      router.replace(`/interviews/${interviewId}`);
    } catch (error) {
      if (
        error instanceof ApiError &&
        error.code === "INTERVIEW_ALREADY_STARTED"
      ) {
        try {
          setProgress(await getInterviewProgress(interviewId));
          setLoadState("success");
          router.replace(`/interviews/${interviewId}`);
          return;
        } catch (progressError) {
          setLoadErrorMessage(getInterviewErrorMessage(progressError));
          setLoadState("error");
          return;
        }
      }

      if (
        error instanceof ApiError &&
        error.code === "INTERVIEW_ALREADY_COMPLETED"
      ) {
        router.replace(`/interviews/${interviewId}/result`);
        return;
      }

      setLoadErrorMessage(getInterviewErrorMessage(error));
      setLoadState("ready");
    } finally {
      isStartingRef.current = false;
      setIsStarting(false);
    }
  }

  async function requestFollowUp(questionId: number) {
    setIsGeneratingFollowUp(true);
    setFollowUpErrorMessage("");

    try {
      const followUp = await generateFollowUpQuestion(questionId);
      setPendingFollowUpQuestionId(null);
      setFollowUpMessage(
        followUp.followUpQuestionId !== null
          ? `꼬리질문이 등록되었습니다: ${followUp.content}`
          : "추가 꼬리질문 없이 다음 질문으로 진행합니다.",
      );
    } catch (error) {
      setFollowUpErrorMessage(
        `답변은 저장됐지만 꼬리질문 생성에 실패했습니다. ${getInterviewErrorMessage(error)}`,
      );
    } finally {
      try {
        const refreshedProgress = await getInterviewProgress(interviewId);
        setProgress(refreshedProgress);
      } catch (error) {
        setSubmitErrorMessage(
          `진행 상태를 갱신하지 못했습니다. ${getInterviewErrorMessage(error)}`,
        );
      }
      setIsGeneratingFollowUp(false);
    }
  }

  async function handleComplete() {
    if (
      !progress?.allAnswered ||
      isCompleting ||
      pendingFollowUpQuestionId !== null
    ) {
      return;
    }

    setIsCompleting(true);
    setSubmitErrorMessage("");

    try {
      const response = await completeInterview(interviewId);
      router.push(`/interviews/${response.interviewId}/result`);
    } catch (error) {
      setSubmitErrorMessage(getInterviewErrorMessage(error));
      setIsCompleting(false);
    }
  }

  async function handleCancel() {
    if (isCancellingRef.current || !progress) {
      return;
    }

    isCancellingRef.current = true;
    setIsCancelling(true);
    setCancelErrorMessage("");

    try {
      const response = await cancelInterview(interviewId);
      if (response.status !== "CANCELLED") {
        throw new Error("면접 중도 종료 상태를 확인하지 못했습니다.");
      }

      setIsCancelModalOpen(false);
      if (answeredCount < MINIMUM_PARTIAL_FEEDBACK_ANSWER_COUNT) {
        setProgress(null);
        setLoadState("cancelled");
      } else {
        setHasCancelled(true);
        setIsCancelModalOpen(true);
        await generatePartialFeedback(response.interviewId);
      }
    } catch (error) {
      if (
        error instanceof ApiError &&
        [
          "INTERVIEW_NOT_CANCELLABLE",
          "INTERVIEW_ALREADY_CANCELLED",
          "INTERVIEW_ALREADY_COMPLETED",
        ].includes(error.code ?? "")
      ) {
        setIsCancelModalOpen(false);
        setLoadState("loading");
        await loadInterview();
        return;
      }

      setCancelErrorMessage(getInterviewErrorMessage(error));
    } finally {
      isCancellingRef.current = false;
      setIsCancelling(false);
    }
  }

  async function generatePartialFeedback(cancelledInterviewId = interviewId) {
    if (isGeneratingPartialFeedbackRef.current) {
      return;
    }

    isGeneratingPartialFeedbackRef.current = true;
    setIsGeneratingPartialFeedback(true);
    setCancelErrorMessage("");

    try {
      await generateInterviewFeedback(cancelledInterviewId);
      router.replace(`/interviews/${cancelledInterviewId}/result`);
    } catch (error) {
      if (
        error instanceof ApiError &&
        error.code === "FEEDBACK_ALREADY_EXISTS"
      ) {
        router.replace(`/interviews/${cancelledInterviewId}/result`);
        return;
      }

      setCancelErrorMessage(getInterviewErrorMessage(error));
    } finally {
      isGeneratingPartialFeedbackRef.current = false;
      setIsGeneratingPartialFeedback(false);
    }
  }

  if (loadState === "loading") {
    return (
      <div
        role="status"
        className="rounded-2xl border border-zinc-200 bg-white p-10 text-center shadow-sm"
      >
        <p className="font-medium text-zinc-800">
          면접 진행 상태를 불러오는 중...
        </p>
      </div>
    );
  }

  if (loadState === "error") {
    return (
      <div className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <p role="alert" className="font-medium text-red-700">
          진행 상태를 불러오지 못했습니다.
        </p>
        <p className="mt-2 text-sm text-red-600">{loadErrorMessage}</p>
        <button
          type="button"
          onClick={() => {
            setLoadState("loading");
            void loadInterview();
          }}
          className="mt-5 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (loadState === "ready") {
    return (
      <section className="rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm sm:p-12">
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-zinc-900 text-xl text-white">
          ✓
        </div>
        <p className="mt-5 text-sm font-medium text-emerald-700">준비 완료</p>
        <h1 className="mt-2 text-2xl font-semibold text-zinc-900">
          면접을 시작할 준비가 되었습니다
        </h1>
        <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-zinc-600">
          시작 버튼을 누르면 면접이 진행 상태로 전환되고 첫 질문이 표시됩니다.
          답변할 준비가 되었을 때 시작해 주세요.
        </p>
        {loadErrorMessage && (
          <p role="alert" className="mt-4 text-sm text-red-600">
            {loadErrorMessage}
          </p>
        )}
        <button
          type="button"
          onClick={() => void handleStart()}
          disabled={isStarting}
          className="mt-7 w-full rounded-xl bg-zinc-900 px-5 py-3 font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto sm:min-w-48"
        >
          {isStarting ? "면접 시작 중..." : "면접 시작"}
        </button>
      </section>
    );
  }

  if (loadState === "cancelled") {
    return (
      <section className="rounded-2xl border border-amber-200 bg-white p-8 text-center shadow-sm sm:p-12">
        <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-amber-100 text-xl text-amber-800">
          !
        </div>
        <p className="mt-5 text-sm font-medium text-amber-700">중도 종료</p>
        <h1 className="mt-2 text-2xl font-semibold text-zinc-900">
          면접이 중도 종료되었습니다
        </h1>
        <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-zinc-600">
          부분 피드백을 생성하기에는 답변이 부족합니다. 부분 피드백은 최소{" "}
          {MINIMUM_PARTIAL_FEEDBACK_ANSWER_COUNT}개의 답변이 필요합니다.
        </p>
        <button
          type="button"
          onClick={() => router.replace("/dashboard")}
          className="mt-7 rounded-xl bg-zinc-900 px-5 py-3 font-medium text-white hover:bg-zinc-700"
        >
          대시보드로 이동
        </button>
      </section>
    );
  }

  if (!progress) {
    return null;
  }

  return (
    <div className="space-y-6">
      <header className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
        <p className="text-sm text-zinc-500">
          Interview #{progress.interviewId}
        </p>
        <div className="mt-2 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold tracking-tight">면접 진행</h1>
            <p className="mt-1 text-sm text-zinc-500">
              상태: {progress.status}
            </p>
          </div>
          <p className="text-sm font-medium text-zinc-700">
            {answeredCount} / {totalCount} 답변 완료
          </p>
        </div>
        <div className="mt-5 h-2 overflow-hidden rounded-full bg-zinc-200">
          <div
            className="h-full rounded-full bg-zinc-900"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
        <p className="mt-2 text-right text-xs text-zinc-500">
          진행률 {progressPercent}%
        </p>
      </header>

      {totalCount === 0 ? (
        <section className="rounded-2xl border border-amber-200 bg-amber-50 p-8 text-center">
          <p className="font-medium text-amber-800">진행할 질문이 없습니다.</p>
          <p className="mt-2 text-sm text-amber-700">
            면접 질문 생성 상태를 확인한 후 다시 시도해 주세요.
          </p>
          <button
            type="button"
            onClick={() => {
              setLoadState("loading");
              void loadInterview();
            }}
            className="mt-5 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
          >
            다시 불러오기
          </button>
        </section>
      ) : (
        <>
          <section className="overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-zinc-200 px-4 py-3 sm:px-6">
              <div>
                <h2 className="text-sm font-semibold text-zinc-900">
                  면접 대화
                </h2>
                <p className="mt-0.5 text-xs text-zinc-500">
                  질문과 답변 기록은 진행 순서대로 표시됩니다.
                </p>
              </div>
              {currentQuestion && (
                <button
                  type="button"
                  onClick={() => scrollToQuestion(currentQuestion.questionId)}
                  className="shrink-0 rounded-lg border border-zinc-300 px-3 py-2 text-xs font-medium text-zinc-700 hover:bg-zinc-50"
                >
                  현재 질문으로 이동
                </button>
              )}
            </div>

            <div
              ref={conversationRef}
              onScroll={handleConversationScroll}
              className="max-h-[58vh] min-h-80 space-y-7 overflow-y-auto bg-zinc-50/70 px-4 py-6 sm:px-6 sm:py-8"
            >
              {visibleQuestions.map((question) => {
                const isCurrent =
                  question.questionId === progress.nextQuestionId;
                const isHighlighted =
                  question.questionId === highlightedQuestionId;

                return (
                  <article
                    id={`question-${question.questionId}`}
                    key={question.questionId}
                    aria-current={isCurrent ? "step" : undefined}
                    className={`scroll-m-6 rounded-2xl transition-colors duration-500 ${
                      isHighlighted
                        ? "bg-violet-100/80 ring-4 ring-violet-200"
                        : ""
                    }`}
                  >
                    <div className="flex justify-start">
                      <div
                        className={`max-w-[88%] rounded-2xl rounded-bl-md border px-4 py-3 shadow-sm sm:max-w-[78%] sm:px-5 ${
                          isCurrent
                            ? "border-zinc-900 bg-white ring-2 ring-zinc-900/10"
                            : "border-zinc-200 bg-white"
                        }`}
                      >
                        <div className="flex flex-wrap items-center gap-1.5 text-[11px] font-medium">
                          <span className="text-zinc-500">
                            질문 {question.questionOrder}
                          </span>
                          {isCurrent && (
                            <span className="rounded-full bg-zinc-900 px-2 py-0.5 text-white">
                              현재 질문
                            </span>
                          )}
                          {question.parentQuestionId !== null && (
                            <span className="rounded-full bg-violet-100 px-2 py-0.5 text-violet-700">
                              꼬리질문
                            </span>
                          )}
                          {question.category && (
                            <span className="rounded-full bg-blue-50 px-2 py-0.5 text-blue-700">
                              {CATEGORY_LABELS[question.category] ??
                                question.category}
                            </span>
                          )}
                          {question.difficulty && (
                            <span className="rounded-full bg-amber-50 px-2 py-0.5 text-amber-700">
                              {DIFFICULTY_LABELS[question.difficulty] ??
                                question.difficulty}
                            </span>
                          )}
                        </div>
                        <p className="mt-2 text-sm leading-6 whitespace-pre-wrap text-zinc-900 sm:text-base">
                          {question.content}
                        </p>
                        {question.parentQuestionId !== null && (
                          <button
                            type="button"
                            onClick={() =>
                              scrollToQuestion(question.parentQuestionId!, true)
                            }
                            className="mt-3 text-xs font-medium text-violet-700 underline decoration-violet-300 underline-offset-4 hover:text-violet-900"
                          >
                            원 질문 보기
                          </button>
                        )}
                      </div>
                    </div>

                    {question.answerContent !== null && (
                      <div className="mt-3 flex justify-end">
                        <div className="max-w-[88%] rounded-2xl rounded-br-md bg-zinc-900 px-4 py-3 text-white shadow-sm sm:max-w-[78%] sm:px-5">
                          <p className="text-[11px] font-medium text-zinc-300">
                            내 답변
                          </p>
                          <p className="mt-2 text-sm leading-6 whitespace-pre-wrap sm:text-base">
                            {question.answerContent}
                          </p>
                          {question.answeredAt && (
                            <time className="mt-2 block text-right text-[10px] text-zinc-400">
                              답변 완료
                            </time>
                          )}
                        </div>
                      </div>
                    )}
                  </article>
                );
              })}
              <div ref={conversationEndRef} aria-hidden="true" />
            </div>
          </section>

          {!currentQuestion && !progress.allAnswered && (
            <section className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center">
              <p className="font-medium text-red-800">
                다음 질문을 확인할 수 없습니다.
              </p>
              <button
                type="button"
                onClick={() => {
                  setLoadState("loading");
                  void loadInterview();
                }}
                className="mt-4 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
              >
                다시 불러오기
              </button>
            </section>
          )}

          <section className="sticky bottom-3 z-10 rounded-2xl border border-zinc-200 bg-white/95 p-4 shadow-lg backdrop-blur sm:p-5">
            {progress.allAnswered ? (
              <div className="rounded-xl bg-emerald-50 p-4 text-center">
                <p className="font-medium text-emerald-800">
                  모든 질문에 답변했습니다.
                </p>
                <p className="mt-1 text-sm text-emerald-700">
                  면접을 종료하고 피드백 단계로 이동할 수 있습니다.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                <label
                  htmlFor="answer"
                  className="block text-sm font-medium text-zinc-700"
                >
                  {currentQuestion
                    ? `질문 ${currentQuestion.questionOrder} 답변`
                    : "현재 질문 답변"}
                </label>
                <div className="mt-2 flex flex-col gap-3 sm:flex-row sm:items-end">
                  <textarea
                    id="answer"
                    value={answer}
                    onChange={(event) => {
                      setAnswer(event.target.value);
                      setSubmitErrorMessage("");
                    }}
                    disabled={
                      !currentQuestion ||
                      isSubmitting ||
                      isGeneratingFollowUp ||
                      pendingFollowUpQuestionId !== null
                    }
                    rows={3}
                    placeholder={
                      currentQuestion
                        ? "답변을 구체적으로 작성해 주세요."
                        : "답변할 수 있는 질문이 없습니다."
                    }
                    className="min-h-24 flex-1 resize-y rounded-xl border border-zinc-300 px-3 py-3 text-sm leading-6 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
                  />
                  <button
                    type="submit"
                    disabled={
                      !currentQuestion ||
                      isSubmitting ||
                      isGeneratingFollowUp ||
                      pendingFollowUpQuestionId !== null
                    }
                    className="h-12 shrink-0 rounded-xl bg-zinc-900 px-5 font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-50 sm:h-24"
                  >
                    {isSubmitting
                      ? isGeneratingFollowUp
                        ? "꼬리질문 생성 중..."
                        : "답변 제출 중..."
                      : "답변 제출"}
                  </button>
                </div>
              </form>
            )}

            {isGeneratingFollowUp && (
              <p role="status" className="mt-3 text-sm text-blue-700">
                답변을 바탕으로 다음 질문을 준비하고 있습니다...
              </p>
            )}
            {submitErrorMessage && (
              <p role="alert" className="mt-3 text-sm text-red-600">
                {submitErrorMessage}
              </p>
            )}
            {followUpMessage && (
              <p role="status" className="mt-3 text-sm text-blue-700">
                {followUpMessage}
              </p>
            )}
            {followUpErrorMessage && (
              <div className="mt-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                <p role="alert">{followUpErrorMessage}</p>
                {pendingFollowUpQuestionId !== null && (
                  <button
                    type="button"
                    onClick={() =>
                      void requestFollowUp(pendingFollowUpQuestionId)
                    }
                    disabled={isGeneratingFollowUp}
                    className="mt-3 rounded-lg bg-red-700 px-4 py-2 font-medium text-white hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    꼬리질문 다시 시도
                  </button>
                )}
              </div>
            )}

            <button
              type="button"
              onClick={handleComplete}
              disabled={
                !progress.allAnswered ||
                isCompleting ||
                isSubmitting ||
                isGeneratingFollowUp ||
                pendingFollowUpQuestionId !== null
              }
              className="mt-3 w-full rounded-xl border border-zinc-900 bg-white px-4 py-3 font-medium text-zinc-900 hover:bg-zinc-100 disabled:cursor-not-allowed disabled:border-zinc-300 disabled:text-zinc-400"
            >
              {isCompleting ? "면접 종료 중..." : "면접 종료"}
            </button>
            <button
              type="button"
              onClick={() => {
                setCancelErrorMessage("");
                setIsCancelModalOpen(true);
              }}
              disabled={
                isCancelling ||
                isGeneratingPartialFeedback ||
                isCompleting ||
                isSubmitting ||
                isGeneratingFollowUp
              }
              className="mt-3 w-full rounded-xl border border-red-300 bg-white px-4 py-3 font-medium text-red-700 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              면접 그만하기
            </button>
          </section>
        </>
      )}

      {isCancelModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 p-4 sm:items-center"
          role="presentation"
          onMouseDown={(event) => {
            if (
              event.target === event.currentTarget &&
              !isCancelling &&
              !isGeneratingPartialFeedback &&
              !hasCancelled
            ) {
              setIsCancelModalOpen(false);
            }
          }}
        >
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="cancel-interview-title"
            className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
          >
            <h2
              id="cancel-interview-title"
              className="text-lg font-semibold text-zinc-900"
            >
              {hasCancelled
                ? "부분 피드백을 준비하고 있습니다"
                : "면접을 중도 종료하시겠습니까?"}
            </h2>
            {hasCancelled ? (
              <div className="mt-4 rounded-xl bg-blue-50 p-4 text-sm leading-6 text-blue-800">
                {isGeneratingPartialFeedback
                  ? "현재까지의 답변을 분석해 부분 피드백을 생성하고 있습니다. 잠시만 기다려 주세요."
                  : "면접은 중도 종료되었습니다. 부분 피드백 생성을 다시 시도할 수 있습니다."}
              </div>
            ) : (
              <>
                <p className="mt-3 text-sm leading-6 text-zinc-600">
                  완료하지 않은 질문은 평가되지 않으며, 현재까지 답변한 내용을
                  기준으로 부분 피드백이 제공될 수 있습니다.
                </p>
                <div className="mt-4 rounded-xl bg-zinc-50 p-4 text-sm leading-6 text-zinc-700">
                  현재 {answeredCount}개 질문에 답변했습니다. 부분 피드백은 최소{" "}
                  {MINIMUM_PARTIAL_FEEDBACK_ANSWER_COUNT}개의 답변이 필요합니다.
                  {answeredCount < MINIMUM_PARTIAL_FEEDBACK_ANSWER_COUNT && (
                    <p className="mt-1 font-medium text-amber-700">
                      지금 종료하면 부분 피드백을 생성할 수 없습니다.
                    </p>
                  )}
                </div>
              </>
            )}
            {cancelErrorMessage && (
              <p role="alert" className="mt-4 text-sm text-red-600">
                {cancelErrorMessage}
              </p>
            )}
            <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() =>
                  hasCancelled
                    ? router.replace("/dashboard")
                    : setIsCancelModalOpen(false)
                }
                disabled={isCancelling || isGeneratingPartialFeedback}
                className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-50"
              >
                {hasCancelled ? "대시보드로 이동" : "계속 면접하기"}
              </button>
              <button
                type="button"
                onClick={() =>
                  void (hasCancelled
                    ? generatePartialFeedback()
                    : handleCancel())
                }
                disabled={isCancelling || isGeneratingPartialFeedback}
                className="rounded-xl bg-red-700 px-4 py-3 text-sm font-medium text-white hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isCancelling
                  ? "종료 처리 중..."
                  : isGeneratingPartialFeedback
                    ? "부분 피드백 생성 중..."
                    : hasCancelled
                      ? "부분 피드백 다시 생성"
                      : "면접 종료하기"}
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  );
}
