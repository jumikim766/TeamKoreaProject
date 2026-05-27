import apiClient from "./axiosInstance";

// URL 위험도 타입
// 백엔드 RiskLevel enum 및 현재 응답값 기준
export type RiskLevel =
  | "SAFE"
  | "SUSPICIOUS"
  | "WARNING"
  | "DANGER"
  | "CRITICAL";

// 전체 URL 목록 조회 요청 파라미터 타입
export interface UrlListParams {
  domain?: string;
  riskLevel?: RiskLevel | string;
  isAnalyzed?: boolean;
  page?: number;
  size?: number;
}

// 전체 URL 목록 1개 응답 타입
export interface UrlListItem {
  urlId: number;
  normalizedUrl: string;
  domain: string;

  riskLevel: string;

  score?: number | null;
  detectedRules?: string[] | null;
  reasonSummary?: string | null;

  isAnalyzed: boolean;
  createdAt: string;
}

// 전체 URL 목록 조회 응답 타입
export interface UrlListResponse {
  urls: UrlListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// URL 상세 조회 응답 타입
export interface UrlDetail {
  urlId: number;
  senderName: string | null;
  senderEmail: string | null;
  originalUrl: string | null;
  normalizedUrl: string;
  domain: string;
  riskLevel: string;
  reasonSummary: string | null;
  createdAt: string;
  updatedAt: string | null;
}

// 나의 URL 목록 조회 요청 파라미터 타입
export interface MyUrlListParams {
  accountId?: number;
  domain?: string;
  riskLevel?: RiskLevel | string;
  isAnalyzed?: boolean;
  page?: number;
  size?: number;
}

// 나의 URL 목록 1개 응답 타입
export interface MyUrlItem {
  urlId: number;
  emailId: number;
  accountId: number;
  senderName: string | null;
  senderEmail: string | null;
  emailSubject: string | null;
  originalUrl: string | null;
  normalizedUrl: string;
  domain: string;
  riskLevel: string;
  reasonSummary: string | null;
  isAnalyzed: boolean;
  receivedAt: string | null;
  createdAt: string;
  score?: number | null;
detectedRules?: string[] | null;
}

// 나의 URL 목록 조회 응답 타입
export interface MyUrlListResponse {
  urls: MyUrlItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// URL 위험도 통계 조회 요청 파라미터 타입
export interface UrlStatisticsParams {
  scope?: "ALL" | "MY";
  accountId?: number;
  domain?: string;
  isAnalyzed?: boolean;
  period?: "TODAY" | "ALL";
}

// URL 위험도 통계 조회 응답 타입
export interface UrlStatistics {
  totalCount: number;
  safeCount: number;
  suspiciousCount: number;
  warningCount: number;
  dangerCount: number;
  criticalCount: number;
  unanalyzedCount: number;
}

// 전체 URL 목록 조회
export const getUrls = async (params?: UrlListParams) => {
  const response = await apiClient.get("/api/urls", {
    params: {
      domain: params?.domain,
      riskLevel: params?.riskLevel,
      isAnalyzed: params?.isAnalyzed,
      page: params?.page ?? 0,
      size: params?.size ?? 20,
    },
  });

  return response.data.data as UrlListResponse;
};

// URL 상세 조회
export const getUrlDetail = async (urlId: number) => {
  const response = await apiClient.get(`/api/urls/${urlId}`);

  return response.data.data as UrlDetail;
};

// 나의 URL 목록 조회
export const getMyUrls = async (params?: MyUrlListParams) => {
  const response = await apiClient.get("/api/my-urls", {
    params: {
      accountId: params?.accountId,
      domain: params?.domain,
      riskLevel: params?.riskLevel,
      isAnalyzed: params?.isAnalyzed,
      page: params?.page ?? 0,
      size: params?.size ?? 20,
    },
  });

  return response.data.data as MyUrlListResponse;
};

// URL 위험도 통계 조회
export const getUrlStatistics = async (params?: UrlStatisticsParams) => {
  const response = await apiClient.get("/api/urls/statistics", {
    params: {
      scope: params?.scope ?? "ALL",
      accountId: params?.accountId,
      domain: params?.domain,
      isAnalyzed: params?.isAnalyzed,
      period: params?.period,
    },
  });

  return response.data.data as UrlStatistics;
};
export interface LlmAnalysisResponse {
  risk: string;
  reasonSummary: string;
  score: number;
  detectedRules: string[];
}

export const analyzeUrlWithLlm = async (urlId: number) => {
  const response = await apiClient.get(`/api/url-analysis/llm/url/${urlId}`);
  return response.data.data as LlmAnalysisResponse;
};

export const urlApi = {
  getUrls,
  getUrlDetail,
  getMyUrls,
  getUrlStatistics,
  analyzeUrlWithLlm,
};


