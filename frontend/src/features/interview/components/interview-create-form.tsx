"use client";

import { useRouter } from "next/navigation";
import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type FormEvent,
} from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import {
  analyzeJobPosting,
  analyzeResume,
  createInterview,
  getResumes,
} from "@/features/interview/services/interview-service";
import type {
  JobPostingAnalyzeResponse,
  ResumeAnalyzeResponse,
  ResumeSummaryResponse,
} from "@/features/interview/types/interview";
import { ApiError } from "@/services/api-client";

const MAX_RESUME_FILE_SIZE = 5 * 1024 * 1024;

type LoadState = "loading" | "success" | "error";
type AnalysisState = "idle" | "loading" | "success" | "error";

function getPersonalizationErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return getErrorMessage(error);

  const messages: Record<string, string> = {
    JOB_POSTING_NOT_FOUND:
      "분석한 채용공고를 찾을 수 없습니다. 다시 분석해 주세요.",
    JOB_POSTING_NOT_ANALYZED:
      "채용공고 분석 결과가 없습니다. 다시 분석해 주세요.",
    JOB_POSTING_FETCH_FAILED:
      "채용공고 페이지에 연결하지 못했습니다. URL을 확인해 주세요.",
    JOB_POSTING_CONTENT_NOT_FOUND:
      "채용공고에서 분석할 내용을 찾지 못했습니다.",
    JOB_POSTING_URL_NOT_ALLOWED: "분석할 수 없는 채용공고 URL입니다.",
    JOB_POSTING_ANALYSIS_INSUFFICIENT:
      "채용공고에서 회사명이나 직무명을 확인하지 못했습니다.",
    RESUME_NOT_FOUND: "선택한 이력서를 찾을 수 없습니다.",
    RESUME_ACCESS_DENIED: "선택한 이력서에 접근할 수 없습니다.",
    RESUME_NOT_ANALYZED: "이력서 분석 결과가 없습니다. 다시 업로드해 주세요.",
    RESUME_INVALID_FILE: "PDF 형식의 이력서만 업로드할 수 있습니다.",
    RESUME_FILE_TOO_LARGE: "이력서 파일은 최대 5MB까지 업로드할 수 있습니다.",
    RESUME_TEXT_EXTRACTION_FAILED:
      "이력서 텍스트를 읽지 못했습니다. 텍스트가 포함된 PDF인지 확인해 주세요.",
    RESUME_CONTENT_NOT_FOUND: "이력서에서 분석할 텍스트를 찾지 못했습니다.",
    AI_REQUEST_FAILED: "AI 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.",
  };

  return error.code
    ? (messages[error.code] ?? getErrorMessage(error))
    : getErrorMessage(error);
}

function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(date);
}

function ListPreview({ label, values }: { label: string; values: string[] }) {
  if (values.length === 0) return null;

  return (
    <div className="mt-4">
      <p className="text-xs font-medium text-zinc-500">{label}</p>
      <ul className="mt-2 space-y-1 text-sm leading-6 text-zinc-700">
        {values.map((value, index) => (
          <li key={`${value}-${index}`}>• {value}</li>
        ))}
      </ul>
    </div>
  );
}

function TagList({ values }: { values: string[] }) {
  if (values.length === 0) return null;

  return (
    <div className="mt-2 flex flex-wrap gap-2">
      {values.map((value, index) => (
        <span
          key={`${value}-${index}`}
          className="rounded-full bg-white px-2.5 py-1 text-xs text-zinc-700 ring-1 ring-zinc-200"
        >
          {value}
        </span>
      ))}
    </div>
  );
}

export function InterviewCreateForm() {
  const router = useRouter();
  const isCreatingRef = useRef(false);
  const isPostingAnalysisRef = useRef(false);
  const isResumeAnalysisRef = useRef(false);
  const [resumes, setResumes] = useState<ResumeSummaryResponse[]>([]);
  const [resumeLoadState, setResumeLoadState] = useState<LoadState>("loading");
  const [resumeLoadError, setResumeLoadError] = useState("");
  const [title, setTitle] = useState("");
  const [postingUrl, setPostingUrl] = useState("");
  const [jobPosting, setJobPosting] =
    useState<JobPostingAnalyzeResponse | null>(null);
  const [postingState, setPostingState] = useState<AnalysisState>("idle");
  const [postingError, setPostingError] = useState("");
  const [postingNotice, setPostingNotice] = useState("");
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [uploadedResume, setUploadedResume] =
    useState<ResumeAnalyzeResponse | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [resumeState, setResumeState] = useState<AnalysisState>("idle");
  const [resumeError, setResumeError] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const loadResumes = useCallback(async () => {
    setResumeLoadState("loading");
    setResumeLoadError("");
    try {
      setResumes(await getResumes());
      setResumeLoadState("success");
    } catch (error) {
      setResumes([]);
      setResumeLoadError(getPersonalizationErrorMessage(error));
      setResumeLoadState("error");
    }
  }, []);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadResumes();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [loadResumes]);
  const selectedResume =
    uploadedResume?.resumeId === selectedResumeId
      ? uploadedResume
      : (resumes.find((resume) => resume.resumeId === selectedResumeId) ??
        null);
  const selectedUploadedResume =
    uploadedResume?.resumeId === selectedResumeId ? uploadedResume : null;
  const personalizationMessage =
    jobPosting && selectedResume
      ? "채용공고 요구사항과 이력서 경험을 함께 분석해 맞춤 질문을 생성합니다."
      : jobPosting
        ? "채용공고의 업무와 요구사항을 반영해 질문을 생성합니다."
        : "채용공고를 분석하면 맞춤 면접을 생성할 수 있습니다.";

  function clearJobPosting(message = "") {
    setJobPosting(null);
    setPostingState("idle");
    setPostingError("");
    setPostingNotice(message);
  }

  async function handleJobPostingAnalysis() {
    const normalizedUrl = postingUrl.trim();
    if (isPostingAnalysisRef.current) return;
    if (!normalizedUrl) {
      setPostingError("채용공고 URL을 입력해 주세요.");
      return;
    }

    isPostingAnalysisRef.current = true;
    setPostingState("loading");
    setPostingError("");
    setPostingNotice("");
    try {
      setJobPosting(await analyzeJobPosting({ postingUrl: normalizedUrl }));
      setPostingState("success");
    } catch (error) {
      setJobPosting(null);
      setPostingError(getPersonalizationErrorMessage(error));
      setPostingState("error");
    } finally {
      isPostingAnalysisRef.current = false;
    }
  }

  function handleFileChange(file: File | null) {
    setResumeError("");
    setResumeState("idle");
    if (!file) {
      setSelectedFile(null);
      return;
    }
    const isPdf =
      file.type === "application/pdf" ||
      file.name.toLowerCase().endsWith(".pdf");
    if (!isPdf) {
      setSelectedFile(null);
      setResumeError("PDF 형식의 이력서만 선택할 수 있습니다.");
      return;
    }
    if (file.size === 0) {
      setSelectedFile(null);
      setResumeError("빈 파일은 업로드할 수 없습니다.");
      return;
    }
    if (file.size > MAX_RESUME_FILE_SIZE) {
      setSelectedFile(null);
      setResumeError("이력서 파일은 최대 5MB까지 업로드할 수 있습니다.");
      return;
    }
    setSelectedFile(file);
  }

  async function handleResumeAnalysis() {
    if (!selectedFile || isResumeAnalysisRef.current) return;
    isResumeAnalysisRef.current = true;
    setResumeState("loading");
    setResumeError("");
    try {
      const response = await analyzeResume(selectedFile);
      setUploadedResume(response);
      setSelectedResumeId(response.resumeId);
      setResumes((current) => [
        {
          resumeId: response.resumeId,
          originalFileName: response.originalFileName,
          createdAt: response.analyzedAt,
          summary: response.summary,
          skills: response.skills,
        },
        ...current.filter((resume) => resume.resumeId !== response.resumeId),
      ]);
      setSelectedFile(null);
      setResumeState("success");
    } catch (error) {
      setResumeError(getPersonalizationErrorMessage(error));
      setResumeState("error");
    } finally {
      isResumeAnalysisRef.current = false;
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (isCreatingRef.current) return;
    if (!title.trim()) return setCreateError("면접 제목을 입력해 주세요.");
    if (title.trim().length > 100)
      return setCreateError("면접 제목은 100자 이하로 입력해 주세요.");
    if (!jobPosting)
      return setCreateError("채용공고 URL 분석을 먼저 완료해 주세요.");

    isCreatingRef.current = true;
    setIsCreating(true);
    setCreateError("");
    try {
      const response = await createInterview({
        title: title.trim(),
        jobPostingId: jobPosting.jobPostingId,
        ...(selectedResumeId ? { resumeId: selectedResumeId } : {}),
      });
      router.push(`/interviews/${response.interviewId}`);
    } catch (error) {
      setCreateError(getPersonalizationErrorMessage(error));
    } finally {
      isCreatingRef.current = false;
      setIsCreating(false);
    }
  }

  const isBusy =
    isCreating || postingState === "loading" || resumeState === "loading";
  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-8" noValidate>
      <section className="space-y-5">
        <div>
          <p className="text-sm font-semibold text-zinc-900">
            1. 면접 기본 정보
          </p>
          <p className="mt-1 text-sm text-zinc-500">
            면접 제목을 입력하고 채용공고를 분석해 주세요.
          </p>
        </div>
        <div>
          <label
            htmlFor="title"
            className="block text-sm font-medium text-zinc-700"
          >
            면접 제목
          </label>
          <input
            id="title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            maxLength={100}
            disabled={isBusy}
            placeholder="예: Java 백엔드 면접 연습"
            className="mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
          />
          <p className="mt-1.5 text-xs text-zinc-500">최대 100자</p>
        </div>
      </section>

      <section className="border-t border-zinc-200 pt-8">
        <p className="text-sm font-semibold text-zinc-900">2. 채용공고</p>
        <p className="mt-2 text-sm text-zinc-500">
          URL을 분석하면 회사와 직무가 자동으로 결정됩니다.
        </p>
        <div className="mt-4 rounded-xl border border-zinc-200 p-4 sm:p-5">
          <label
            htmlFor="postingUrl"
            className="block text-sm font-medium text-zinc-700"
          >
            채용공고 URL
          </label>
          <div className="mt-2 flex flex-col gap-3 sm:flex-row">
            <input
              id="postingUrl"
              type="url"
              value={postingUrl}
              disabled={isBusy}
              onChange={(event) => {
                setPostingUrl(event.target.value);
                if (jobPosting)
                  clearJobPosting(
                    "URL이 변경되어 이전 분석 결과를 해제했습니다.",
                  );
              }}
              placeholder="https://example.com/jobs/backend"
              className="min-w-0 flex-1 rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
            />
            <button
              type="button"
              onClick={() => void handleJobPostingAnalysis()}
              disabled={isBusy}
              className="rounded-lg bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white disabled:opacity-50"
            >
              {postingState === "loading" ? "분석 중..." : "채용공고 분석"}
            </button>
          </div>
          {postingState === "loading" && (
            <p role="status" className="mt-3 text-sm text-blue-700">
              채용공고를 분석하고 있습니다.
            </p>
          )}
          {postingNotice && (
            <p className="mt-3 text-sm text-amber-700">{postingNotice}</p>
          )}
          {postingError && (
            <p role="alert" className="mt-3 text-sm text-red-600">
              {postingError}
            </p>
          )}
          {jobPosting && (
            <section className="mt-5 rounded-xl bg-zinc-50 p-4">
              <p className="text-xs font-medium text-zinc-500">
                분석된 채용공고
              </p>
              <h3 className="mt-1 font-semibold text-zinc-900">
                {jobPosting.title}
              </h3>
              <p className="mt-1 text-sm text-zinc-600">
                {jobPosting.companyName} · {jobPosting.positionName}
              </p>
              <p className="mt-3 text-sm leading-6 text-zinc-700">
                {jobPosting.summary}
              </p>
              <ListPreview
                label="주요 업무"
                values={jobPosting.responsibilities}
              />
              <ListPreview
                label="필수 자격"
                values={jobPosting.requiredQualifications}
              />
              <ListPreview
                label="우대 사항"
                values={jobPosting.preferredQualifications}
              />
              <ListPreview
                label="경력 요구사항"
                values={jobPosting.experienceRequirements}
              />
              <div className="mt-4">
                <p className="text-xs font-medium text-zinc-500">
                  기술 및 키워드
                </p>
                <TagList
                  values={[...jobPosting.techStack, ...jobPosting.keywords]}
                />
              </div>
            </section>
          )}
        </div>
      </section>

      <section className="border-t border-zinc-200 pt-8">
        <p className="text-sm font-semibold text-zinc-900">
          3. 이력서 <span className="font-normal text-zinc-500">(선택)</span>
        </p>
        <p className="mt-2 text-sm text-zinc-500">
          이력서 분석 결과는 맞춤 면접 질문을 생성하는 데 사용됩니다.
        </p>
        <fieldset disabled={isBusy} className="mt-4 space-y-3">
          <label className="flex items-center gap-2 text-sm">
            <input
              type="radio"
              checked={selectedResumeId === null}
              onChange={() => {
                setSelectedResumeId(null);
                setUploadedResume(null);
              }}
            />{" "}
            사용하지 않음
          </label>
          {resumeLoadState === "loading" && (
            <p className="text-sm text-zinc-500">
              기존 이력서를 불러오는 중...
            </p>
          )}
          {resumeLoadState === "error" && (
            <div className="text-sm text-red-600">
              <p>{resumeLoadError}</p>
              <button
                type="button"
                onClick={() => void loadResumes()}
                className="mt-2 underline"
              >
                다시 시도
              </button>
            </div>
          )}
          {resumes.map((resume) => (
            <label
              key={resume.resumeId}
              className="block rounded-xl border border-zinc-200 p-4"
            >
              <span className="flex items-start gap-2">
                <input
                  type="radio"
                  checked={selectedResumeId === resume.resumeId}
                  onChange={() => {
                    setSelectedResumeId(resume.resumeId);
                    if (uploadedResume?.resumeId !== resume.resumeId)
                      setUploadedResume(null);
                  }}
                />
                <span>
                  <span className="block font-medium text-zinc-900">
                    {resume.originalFileName}
                  </span>
                  <span className="mt-1 block text-xs text-zinc-500">
                    {formatDate(resume.createdAt)}
                  </span>
                  <span className="mt-2 block text-sm text-zinc-600">
                    {resume.summary}
                  </span>
                  <TagList values={resume.skills} />
                </span>
              </span>
            </label>
          ))}
        </fieldset>
        <div className="mt-5 rounded-xl border border-dashed border-zinc-300 bg-zinc-50 p-4 sm:p-5">
          <label
            htmlFor="resumeFile"
            className="block text-sm font-medium text-zinc-700"
          >
            새 PDF 이력서 업로드
          </label>
          <input
            id="resumeFile"
            type="file"
            accept="application/pdf,.pdf"
            disabled={isBusy}
            onChange={(event) =>
              handleFileChange(event.target.files?.[0] ?? null)
            }
            className="mt-3 block w-full text-sm text-zinc-600 file:mr-4 file:rounded-lg file:border-0 file:bg-zinc-900 file:px-3 file:py-2 file:text-sm file:font-medium file:text-white"
          />
          {selectedFile && (
            <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-zinc-700">
                선택됨: {selectedFile.name} (
                {Math.ceil(selectedFile.size / 1024)}KB)
              </p>
              <button
                type="button"
                onClick={() => void handleResumeAnalysis()}
                disabled={isBusy}
                className="rounded-lg bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white disabled:opacity-50"
              >
                {resumeState === "loading" ? "분석 중..." : "이력서 분석"}
              </button>
            </div>
          )}
          {resumeState === "loading" && (
            <p role="status" className="mt-3 text-sm text-blue-700">
              이력서를 분석하고 있습니다.
            </p>
          )}
          {resumeError && (
            <p role="alert" className="mt-3 text-sm text-red-600">
              {resumeError}
            </p>
          )}
        </div>
        {selectedResume && (
          <section className="mt-5 rounded-xl border border-violet-200 bg-violet-50 p-4">
            <p className="text-xs font-medium text-violet-700">
              현재 사용할 이력서
            </p>
            <p className="mt-1 font-semibold text-zinc-900">
              {selectedResume.originalFileName}
            </p>
            <p className="mt-2 text-sm text-zinc-700">
              {selectedResume.summary}
            </p>
            <TagList values={selectedResume.skills} />
            {selectedUploadedResume && (
              <>
                <ListPreview
                  label="주요 경력"
                  values={selectedUploadedResume.workExperiences}
                />
                <ListPreview
                  label="프로젝트"
                  values={selectedUploadedResume.projects}
                />
                <ListPreview
                  label="성과"
                  values={selectedUploadedResume.achievements}
                />
                <ListPreview
                  label="키워드"
                  values={selectedUploadedResume.keywords}
                />
              </>
            )}
          </section>
        )}
      </section>

      <section className="border-t border-zinc-200 pt-8">
        <p className="text-sm font-semibold text-zinc-900">4. 맞춤 설정 요약</p>
        <div className="mt-4 rounded-xl bg-zinc-900 p-5 text-white">
          <dl className="grid gap-3 text-sm sm:grid-cols-3">
            <div>
              <dt className="text-zinc-400">직무</dt>
              <dd className="mt-1 font-medium">
                {jobPosting
                  ? `${jobPosting.companyName} · ${jobPosting.positionName}`
                  : "채용공고 분석 필요"}
              </dd>
            </div>
            <div>
              <dt className="text-zinc-400">채용공고</dt>
              <dd className="mt-1 font-medium">
                {jobPosting ? "✓ 분석 완료" : "분석 필요"}
              </dd>
            </div>
            <div>
              <dt className="text-zinc-400">이력서</dt>
              <dd className="mt-1 font-medium">
                {selectedResume
                  ? `✓ ${selectedResume.originalFileName}`
                  : "사용하지 않음"}
              </dd>
            </div>
          </dl>
          <p className="mt-5 border-t border-white/20 pt-4 text-sm leading-6 text-zinc-200">
            {personalizationMessage}
          </p>
        </div>
      </section>
      {createError && (
        <p role="alert" aria-live="polite" className="text-sm text-red-600">
          {createError}
        </p>
      )}
      <button
        type="submit"
        disabled={isBusy || !jobPosting}
        className="w-full rounded-lg bg-zinc-900 px-4 py-3 font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isCreating
          ? selectedResume
            ? "채용공고와 이력서를 바탕으로 면접 질문을 준비하고 있습니다."
            : "채용공고를 바탕으로 면접 질문을 준비하고 있습니다."
          : "면접 시작하기"}
      </button>
    </form>
  );
}
