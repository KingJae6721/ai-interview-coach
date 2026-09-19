"use client";

import Link from "next/link";
import { useEffect, useRef, useState, type ReactNode } from "react";

import { useAuth } from "@/features/auth/context/auth-context";

const processSteps = [
  {
    label: "채용공고",
    title: "채용공고를 분석해 면접 맥락을 준비합니다.",
    detail:
      "공고 URL을 분석하고 회사·직무·기술 스택과 요구사항을 면접 생성에 반영합니다.",
    preview: (
      <div className="space-y-3">
        <div className="rounded-xl border border-zinc-200 bg-zinc-50 p-3 text-sm text-zinc-600">
          https://company.example/careers/backend
        </div>
        <div className="rounded-xl border border-zinc-200 p-4">
          <p className="font-semibold text-zinc-900">Backend Developer</p>
          <p className="mt-1 text-sm text-zinc-600">채용공고 분석 완료</p>
          <div className="mt-3 flex flex-wrap gap-2">
            {["Java", "Spring Boot", "PostgreSQL"].map((item) => (
              <span
                key={item}
                className="rounded-full bg-blue-50 px-2.5 py-1 text-xs text-blue-700"
              >
                {item}
              </span>
            ))}
          </div>
        </div>
      </div>
    ),
  },
  {
    label: "이력서",
    title: "이력서는 필요할 때만 연결합니다.",
    detail:
      "분석된 이력서를 선택하거나 PDF를 업로드해 프로젝트와 경험을 질문에 반영할 수 있습니다.",
    preview: (
      <div className="rounded-xl border border-zinc-200 p-4">
        <div className="flex items-center justify-between gap-3">
          <div>
            <p className="font-semibold text-zinc-900">resume.pdf</p>
            <p className="mt-1 text-sm text-zinc-600">분석된 이력서</p>
          </div>
          <span className="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700">
            선택됨
          </span>
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {["API 설계", "Redis", "팀 프로젝트"].map((item) => (
            <span
              key={item}
              className="rounded-full bg-zinc-100 px-2.5 py-1 text-xs text-zinc-700"
            >
              {item}
            </span>
          ))}
        </div>
      </div>
    ),
  },
  {
    label: "면접 생성",
    title: "준비한 맥락으로 맞춤 면접을 생성합니다.",
    detail:
      "채용공고는 필수, 이력서는 선택으로 조합해 실제 면접 진행 화면으로 이어집니다.",
    preview: (
      <div className="rounded-xl bg-zinc-900 p-5 text-white">
        <p className="text-xs text-zinc-300">맞춤 면접 설정</p>
        <p className="mt-2 font-semibold">Backend Developer 면접</p>
        <ul className="mt-4 space-y-2 text-sm text-zinc-300">
          <li>✓ 채용공고 분석 반영</li>
          <li>✓ 이력서 경험 반영</li>
        </ul>
        <div className="mt-5 rounded-lg bg-white px-3 py-2 text-center text-sm font-medium text-zinc-900">
          면접 시작하기
        </div>
      </div>
    ),
  },
];

function Reveal({
  children,
  className = "",
  id,
}: {
  children: ReactNode;
  className?: string;
  id?: string;
}) {
  const ref = useRef<HTMLElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const element = ref.current;
    if (!element || !window.IntersectionObserver) {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.15 },
    );
    observer.observe(element);

    return () => observer.disconnect();
  }, []);

  return (
    <section
      ref={ref}
      id={id}
      className={`motion-safe:transition-all motion-safe:duration-700 motion-safe:ease-out ${
        isVisible
          ? "motion-safe:animate-[landing-reveal_700ms_ease-out_both]"
          : ""
      } ${className}`}
    >
      {children}
    </section>
  );
}

export function LandingPage() {
  const { user, isInitialized } = useAuth();
  const [activeStep, setActiveStep] = useState(0);
  const primaryHref = user ? "/interviews/new" : "/login";
  const primaryLabel = user ? "맞춤 면접 만들기" : "면접 시작하기";
  const secondaryHref = user ? "/dashboard" : "/login";
  const secondaryLabel = user ? "대시보드 보기" : "로그인";
  const activeProcess = processSteps[activeStep];

  return (
    <main className="min-h-screen bg-zinc-50 text-zinc-900">
      <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-5 py-5 sm:px-6">
        <Link
          href="/"
          className="text-sm font-semibold tracking-tight text-zinc-900"
        >
          AI Interview Coach
        </Link>
        <nav aria-label="주요 메뉴" className="flex items-center gap-3 text-sm">
          <a
            href="#how-it-works"
            className="hidden text-zinc-600 hover:text-zinc-900 sm:inline"
          >
            사용 흐름
          </a>
          <Link
            href={secondaryHref}
            className="rounded-lg border border-zinc-300 px-3 py-2 font-medium hover:bg-white"
          >
            {isInitialized ? secondaryLabel : "로그인"}
          </Link>
        </nav>
      </header>

      <div className="mx-auto w-full max-w-6xl px-5 pb-16 sm:px-6 sm:pb-24">
        <Reveal className="grid items-center gap-10 py-12 sm:py-18 lg:grid-cols-[1fr_0.95fr] lg:py-24">
          <div>
            <p className="text-sm font-semibold text-blue-700">
              AI Interview Coach
            </p>
            <h1 className="mt-4 max-w-2xl text-4xl font-semibold tracking-tight text-zinc-950 sm:text-5xl lg:text-6xl">
              채용공고와 경험을 바탕으로, 더 구체적인 면접 연습을.
            </h1>
            <p className="mt-6 max-w-xl text-base leading-7 text-zinc-600 sm:text-lg">
              채용공고를 분석하고 필요하면 이력서를 연결해 맞춤 면접을 만들세요.
              답변을 이어가며 꼬리질문과 평가 결과까지 한 흐름으로 확인할 수
              있습니다.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Link
                href={primaryHref}
                className="rounded-xl bg-zinc-900 px-5 py-3 text-center font-medium text-white hover:bg-zinc-700"
              >
                {isInitialized ? primaryLabel : "면접 시작하기"}
              </Link>
              <a
                href="#how-it-works"
                className="rounded-xl border border-zinc-300 bg-white px-5 py-3 text-center font-medium text-zinc-800 hover:bg-zinc-100"
              >
                사용 흐름 보기
              </a>
            </div>
          </div>

          <div className="rounded-3xl border border-zinc-200 bg-white p-4 shadow-sm sm:p-6">
            <div className="rounded-2xl bg-zinc-950 p-5 text-white sm:p-6">
              <div className="flex items-center justify-between text-xs text-zinc-400">
                <span>진행 중인 면접</span>
                <span>질문 2 / 6</span>
              </div>
              <div className="mt-4 h-1.5 overflow-hidden rounded-full bg-zinc-700">
                <div className="h-full w-1/3 rounded-full bg-white" />
              </div>
              <div className="mt-6 rounded-2xl rounded-bl-md bg-white p-4 text-zinc-900">
                <p className="text-xs font-medium text-zinc-500">
                  AI 면접관 · 질문 1-1
                </p>
                <p className="mt-2 text-sm leading-6">
                  트래픽이 증가했을 때 캐시 전략을 어떻게 조정했나요?
                </p>
              </div>
              <div className="mt-3 ml-auto max-w-[88%] rounded-2xl rounded-br-md bg-zinc-800 p-4 text-sm leading-6 text-zinc-100">
                조회 패턴을 기준으로 만료 시간과 캐시 갱신 방식을 분리했습니다.
              </div>
              <p className="mt-4 text-xs text-zinc-400">
                답변을 반영한 꼬리질문이 이어집니다.
              </p>
            </div>
          </div>
        </Reveal>

        <Reveal id="how-it-works" className="scroll-mt-6 py-12 sm:py-18">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold text-blue-700">
              면접 생성 과정
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
              준비한 정보는 면접의 맥락이 됩니다.
            </h2>
            <p className="mt-4 leading-7 text-zinc-600">
              아래 단계를 선택해 실제 생성 화면에서 이어지는 흐름을 확인하세요.
            </p>
          </div>
          <div className="mt-8 grid gap-5 lg:grid-cols-[0.78fr_1.22fr]">
            <div className="flex gap-2 overflow-x-auto pb-2 lg:flex-col lg:overflow-visible">
              {processSteps.map((step, index) => (
                <button
                  key={step.label}
                  type="button"
                  onClick={() => setActiveStep(index)}
                  aria-pressed={activeStep === index}
                  className={`min-w-36 rounded-xl border px-4 py-3 text-left text-sm font-medium transition-colors ${
                    activeStep === index
                      ? "border-zinc-900 bg-zinc-900 text-white"
                      : "border-zinc-200 bg-white text-zinc-700 hover:border-zinc-400"
                  }`}
                >
                  <span className="mr-2 text-xs opacity-70">0{index + 1}</span>
                  {step.label}
                </button>
              ))}
            </div>
            <div className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
              <div className="grid gap-6 sm:grid-cols-[1fr_0.9fr] sm:items-center">
                <div>
                  <p className="text-sm font-semibold text-blue-700">
                    {activeProcess.label}
                  </p>
                  <h3 className="mt-3 text-2xl font-semibold tracking-tight">
                    {activeProcess.title}
                  </h3>
                  <p className="mt-3 leading-7 text-zinc-600">
                    {activeProcess.detail}
                  </p>
                </div>
                <div
                  key={activeProcess.label}
                  className="motion-safe:animate-[landing-reveal_300ms_ease-out_both]"
                >
                  {activeProcess.preview}
                </div>
              </div>
            </div>
          </div>
        </Reveal>

        <Reveal className="py-12 sm:py-18">
          <div className="grid gap-8 rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-9 lg:grid-cols-[1.05fr_0.95fr]">
            <div>
              <p className="text-sm font-semibold text-blue-700">
                실제 면접 경험
              </p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
                답변 뒤에는 다음 질문이 이어집니다.
              </h2>
              <p className="mt-4 max-w-xl leading-7 text-zinc-600">
                현재 질문에 답하면 답변을 기반으로 꼬리질문을 만들고, 필요 없을
                때는 다음 기본 질문으로 진행합니다.
              </p>
              <div className="mt-7 grid gap-3 sm:grid-cols-3">
                {["현재 질문 확인", "답변 제출", "다음 질문 진행"].map(
                  (item, index) => (
                    <div
                      key={item}
                      className="rounded-xl bg-zinc-50 p-4 text-sm font-medium text-zinc-800"
                    >
                      <span className="block text-xs text-zinc-500">
                        0{index + 1}
                      </span>
                      <span className="mt-2 block">{item}</span>
                    </div>
                  ),
                )}
              </div>
            </div>
            <div className="rounded-2xl border border-zinc-200 bg-zinc-50 p-4">
              <div className="rounded-xl bg-white p-4 shadow-sm">
                <p className="text-xs font-medium text-blue-700">
                  꼬리질문 · 1-1
                </p>
                <p className="mt-2 text-sm leading-6 font-medium">
                  그 전략을 선택할 때 고려한 데이터 일관성 문제는 무엇이었나요?
                </p>
              </div>
              <div className="mt-3 rounded-xl bg-zinc-900 p-4 text-sm leading-6 text-white">
                변경 빈도가 높은 데이터는 캐시 대상에서 분리하고, 갱신 시점을
                명확히 관리했습니다.
              </div>
              <p className="mt-4 text-xs text-zinc-500">
                원 질문으로 이동하고 현재 질문을 다시 확인하는 흐름도
                제공됩니다.
              </p>
            </div>
          </div>
        </Reveal>

        <Reveal className="py-12 sm:py-18">
          <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
            <div className="rounded-3xl border border-zinc-200 bg-zinc-900 p-6 text-white shadow-sm sm:p-8">
              <p className="text-sm font-semibold text-zinc-300">
                AI 평가와 피드백
              </p>
              <div className="mt-6 rounded-2xl bg-white p-5 text-zinc-900">
                <p className="text-sm text-zinc-500">종합 점수</p>
                <p className="mt-1 text-5xl font-semibold tracking-tight">
                  82<span className="text-2xl">점</span>
                </p>
                <div className="mt-5 grid gap-3 text-sm sm:grid-cols-3">
                  <div className="rounded-xl bg-emerald-50 p-3 text-emerald-800">
                    <p className="font-semibold">강점</p>
                    <p className="mt-1 text-xs">구체적인 경험</p>
                  </div>
                  <div className="rounded-xl bg-amber-50 p-3 text-amber-800">
                    <p className="font-semibold">보완점</p>
                    <p className="mt-1 text-xs">근거 구조화</p>
                  </div>
                  <div className="rounded-xl bg-blue-50 p-3 text-blue-800">
                    <p className="font-semibold">개선 방향</p>
                    <p className="mt-1 text-xs">지표를 덧붙이기</p>
                  </div>
                </div>
              </div>
            </div>
            <div>
              <p className="text-sm font-semibold text-blue-700">결과 확인</p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight sm:text-4xl">
                면접 전체와 질문별 답변을 함께 복기합니다.
              </h2>
              <p className="mt-4 leading-7 text-zinc-600">
                종합 평가뿐 아니라 질문별 점수, 강점, 보완점과 개선 제안을 결과
                화면에서 확인할 수 있습니다. 완료한 면접 기록은 대시보드와
                이력에서 다시 볼 수 있습니다.
              </p>
            </div>
          </div>
        </Reveal>

        <Reveal className="py-12 sm:py-18">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {[
              [
                "채용공고 기반",
                "URL 분석 결과를 면접 질문의 맥락으로 사용합니다.",
              ],
              [
                "선택적 이력서",
                "기존 분석 이력서를 고르거나 PDF를 분석할 수 있습니다.",
              ],
              [
                "대화형 면접",
                "질문·답변 기록, 꼬리질문, 음성 읽기 기능을 제공합니다.",
              ],
              [
                "결과와 이력",
                "완료 또는 중도 종료 면접의 결과와 기록을 확인합니다.",
              ],
            ].map(([title, detail]) => (
              <article
                key={title}
                className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm"
              >
                <h3 className="font-semibold text-zinc-900">{title}</h3>
                <p className="mt-3 text-sm leading-6 text-zinc-600">{detail}</p>
              </article>
            ))}
          </div>
        </Reveal>

        <Reveal className="py-12 sm:py-18">
          <section className="rounded-3xl bg-zinc-900 px-6 py-10 text-center text-white sm:px-10 sm:py-14">
            <h2 className="text-3xl font-semibold tracking-tight sm:text-4xl">
              다음 면접을 한 번 더 구체적으로 준비해 보세요.
            </h2>
            <p className="mx-auto mt-4 max-w-2xl leading-7 text-zinc-300">
              채용공고를 선택하고, 필요하면 이력서를 더해 실제 면접 흐름을
              시작할 수 있습니다.
            </p>
            <Link
              href={primaryHref}
              className="mt-7 inline-flex rounded-xl bg-white px-5 py-3 font-medium text-zinc-900 hover:bg-zinc-200"
            >
              {isInitialized ? primaryLabel : "로그인하고 시작하기"}
            </Link>
          </section>
        </Reveal>
      </div>
    </main>
  );
}
