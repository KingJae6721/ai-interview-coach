"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState, type FormEvent } from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import {
  createInterview,
  getJobPositions,
} from "@/features/interview/services/interview-service";
import type { JobPositionResponse } from "@/features/interview/types/interview";

type JobPositionLoadState = "loading" | "success" | "error";

export function InterviewCreateForm() {
  const router = useRouter();
  const [jobPositions, setJobPositions] = useState<JobPositionResponse[]>([]);
  const [loadState, setLoadState] = useState<JobPositionLoadState>("loading");
  const [loadErrorMessage, setLoadErrorMessage] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState("");
  const [selectedJobPositionId, setSelectedJobPositionId] = useState("");

  const loadJobPositions = useCallback(async () => {
    setLoadState("loading");
    setLoadErrorMessage("");

    try {
      const response = await getJobPositions();
      setJobPositions(response);
      setLoadState("success");
    } catch (error) {
      setJobPositions([]);
      setLoadErrorMessage(getErrorMessage(error));
      setLoadState("error");
    }
  }, []);

  useEffect(() => {
    let isActive = true;

    getJobPositions()
      .then((response) => {
        if (isActive) {
          setJobPositions(response);
          setLoadState("success");
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setLoadErrorMessage(getErrorMessage(error));
          setLoadState("error");
        }
      });

    return () => {
      isActive = false;
    };
  }, []);

  const selectedJobPosition = jobPositions.find(
    ({ jobPositionId }) => jobPositionId === Number(selectedJobPositionId),
  );
  const hasJobPositions = jobPositions.length > 0;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isCreating) {
      return;
    }

    setCreateErrorMessage("");

    const formData = new FormData(event.currentTarget);
    const title = String(formData.get("title")).trim();
    const jobPositionId = Number(formData.get("jobPositionId"));

    if (!title) {
      setCreateErrorMessage("면접 제목을 입력해 주세요.");
      return;
    }

    if (title.length > 100) {
      setCreateErrorMessage("면접 제목은 100자 이하로 입력해 주세요.");
      return;
    }

    if (!Number.isSafeInteger(jobPositionId) || jobPositionId <= 0) {
      setCreateErrorMessage("직무를 선택해 주세요.");
      return;
    }

    setIsCreating(true);

    try {
      const response = await createInterview({ title, jobPositionId });
      router.push(`/interviews/${response.interviewId}?start=ready`);
    } catch (error) {
      setCreateErrorMessage(getErrorMessage(error));
    } finally {
      setIsCreating(false);
    }
  }

  if (loadState === "loading") {
    return (
      <div role="status" className="mt-8 rounded-xl bg-zinc-50 p-8 text-center">
        <p className="text-sm font-medium text-zinc-700">
          직무 목록을 불러오는 중...
        </p>
        <p className="mt-1 text-xs text-zinc-500">잠시만 기다려 주세요.</p>
      </div>
    );
  }

  if (loadState === "error") {
    return (
      <div className="mt-8 rounded-xl border border-red-200 bg-red-50 p-5 text-sm text-red-700">
        <p role="alert" className="font-medium">
          직무 목록을 불러오지 못했습니다.
        </p>
        <p className="mt-1">{loadErrorMessage}</p>
        <button
          type="button"
          onClick={() => void loadJobPositions()}
          className="mt-4 rounded-lg bg-red-700 px-4 py-2 font-medium text-white hover:bg-red-600"
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (!hasJobPositions) {
    return (
      <div
        role="status"
        className="mt-8 rounded-xl border border-zinc-200 bg-zinc-50 p-8 text-center"
      >
        <p className="font-medium text-zinc-800">
          선택할 수 있는 직무가 없습니다.
        </p>
        <p className="mt-2 text-sm text-zinc-500">
          직무가 등록된 후 면접을 생성할 수 있습니다.
        </p>
        <button
          type="button"
          onClick={() => void loadJobPositions()}
          className="mt-5 rounded-lg border border-zinc-300 bg-white px-4 py-2 text-sm font-medium hover:bg-zinc-100"
        >
          목록 새로고침
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-6" noValidate>
      <div>
        <label
          htmlFor="title"
          className="block text-sm font-medium text-zinc-700"
        >
          면접 제목
        </label>
        <input
          id="title"
          name="title"
          type="text"
          maxLength={100}
          disabled={isCreating}
          placeholder="예: Java 백엔드 면접 연습"
          className="mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
        />
        <p className="mt-1.5 text-xs text-zinc-500">최대 100자</p>
      </div>

      <div>
        <label
          htmlFor="jobPositionId"
          className="block text-sm font-medium text-zinc-700"
        >
          지원 직무
        </label>
        <select
          id="jobPositionId"
          name="jobPositionId"
          value={selectedJobPositionId}
          onChange={(event) => {
            setSelectedJobPositionId(event.target.value);
            setCreateErrorMessage("");
          }}
          disabled={isCreating}
          className="mt-2 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
        >
          <option value="">직무를 선택해 주세요</option>
          {jobPositions.map((position) => (
            <option key={position.jobPositionId} value={position.jobPositionId}>
              {position.companyName} · {position.positionName}
            </option>
          ))}
        </select>
      </div>

      {selectedJobPosition && (
        <section className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 sm:p-5">
          <p className="text-xs font-medium tracking-wide text-zinc-500 uppercase">
            선택한 직무
          </p>
          <div className="mt-3 grid gap-3 sm:grid-cols-2">
            <div>
              <p className="text-xs text-zinc-500">회사명</p>
              <p className="mt-1 font-medium text-zinc-900">
                {selectedJobPosition.companyName}
              </p>
            </div>
            <div>
              <p className="text-xs text-zinc-500">직무명</p>
              <p className="mt-1 font-medium text-zinc-900">
                {selectedJobPosition.positionName}
              </p>
            </div>
          </div>
          <div className="mt-4">
            <p className="text-xs text-zinc-500">기술 스택</p>
            {selectedJobPosition.techStack.length > 0 ? (
              <div className="mt-2 flex flex-wrap gap-2">
                {selectedJobPosition.techStack.map((technology) => (
                  <span
                    key={technology}
                    className="rounded-full bg-white px-2.5 py-1 text-xs text-zinc-700 ring-1 ring-zinc-200"
                  >
                    {technology}
                  </span>
                ))}
              </div>
            ) : (
              <p className="mt-1 text-sm text-zinc-500">
                등록된 기술 스택이 없습니다.
              </p>
            )}
          </div>
        </section>
      )}

      {createErrorMessage && (
        <p role="alert" aria-live="polite" className="text-sm text-red-600">
          {createErrorMessage}
        </p>
      )}

      <button
        type="submit"
        disabled={isCreating}
        className="w-full rounded-lg bg-zinc-900 px-4 py-3 font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isCreating ? "면접 생성 중..." : "면접 시작하기"}
      </button>
    </form>
  );
}
