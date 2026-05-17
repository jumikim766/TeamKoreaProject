import apiClient from "./axiosInstance";


// 이메일 제공자 타입
export type EmailProvider = "GMAIL" | "NAVER" | "DAUM" | "OUTLOOK" | "CUSTOM";

// 이메일 연동 계정 응답 타입
export interface EmailAccount {
  accountId: number;
  userId: number;
  provider: EmailProvider | string;
  email: string;
  active: boolean;
  lastSyncStatus: string | null;
  lastSyncedAt: string | null;
  createdAt: string;
}

// 이메일 계정 등록 요청 타입
// GMAIL/NAVER/DAUM/OUTLOOK은 imapHost, imapPort 없어도 됨
// CUSTOM은 imapHost, imapPort까지 같이 보내야 함
export interface CreateEmailAccountRequest {
  provider: EmailProvider;
  email: string;
  loginId: string;
  secret: string;
  imapHost?: string;
  imapPort?: number;
}

// 이메일 목록 조회 요청 파라미터 타입
export interface EmailListParams {
  accountId?: number;
  keyword?: string;
  receivedAtFrom?: string;
  receivedAtTo?: string;
  page?: number;
  size?: number;
}

// 이메일 목록 1개 응답 타입
export interface EmailListItem {
  emailId: number;
  senderName: string | null;
  subject: string | null;
  previewText: string | null;
  receivedAt: string;
}

// 이메일 목록 조회 응답 타입
export interface EmailListResponse {
  emails: EmailListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// 이메일 상세 조회 응답 타입
// 백엔드에서 riskLevel 추가된 버전 기준
export interface EmailDetail {
  emailId: number;
  accountId: number;
  senderEmail: string | null;
  senderName: string | null;
  receiverEmail: string | null;
  subject: string | null;
  bodyText: string | null;
  receivedAt: string;
  createdAt: string;
  urlCount: number;
  riskLevel: string;
}

// 이메일에 포함된 URL 1개 응답 타입
export interface EmailUrlItem {
  urlId: number;
  originalUrl: string;
  normalizedUrl: string;
  domain: string;
  riskLevel: string;
  reasonSummary: string | null;
  score: number | null;
}

// 특정 이메일 URL 목록 조회 응답 타입
export interface EmailUrlsResponse {
  emailId: number;
  urls: EmailUrlItem[];
}

// 이메일 연동 계정 목록 조회
export const getEmailAccounts = async () => {
  const response = await apiClient.get("/api/email-accounts");

  return response.data.data.emailAccounts as EmailAccount[];
};

// 이메일 계정 등록
export const createEmailAccount = async (request: CreateEmailAccountRequest) => {
  const response = await apiClient.post("/api/email-accounts", request);
  return response.data.data as EmailAccount;
};

// 이메일 계정 삭제
export const deleteEmailAccount = async (accountId: number) => {
  const response = await apiClient.delete(`/api/email-accounts/${accountId}`);

  return response.data;
};

// 이메일 즉시 동기화
export const syncEmailAccount = async (accountId: number) => {
  const response = await apiClient.post(`/api/email-accounts/${accountId}/sync`);
  return response.data.data;
};

  return response.data.data;
};

// 이메일 목록 조회
export const getEmails = async (params?: EmailListParams) => {
  const response = await apiClient.get("/api/emails", {
    params: {
      accountId: params?.accountId,
      keyword: params?.keyword,
      receivedAtFrom: params?.receivedAtFrom,
      receivedAtTo: params?.receivedAtTo,
      page: params?.page ?? 0,
      size: params?.size ?? 20,
    },
  });
  const response = await apiClient.get("/api/emails", {
    params: {
      accountId: params?.accountId,
      keyword: params?.keyword,
      receivedAtFrom: params?.receivedAtFrom,
      receivedAtTo: params?.receivedAtTo,
      page: params?.page ?? 0,
      size: params?.size ?? 20,
    },
  });

  return response.data.data as EmailListResponse;
};

// 이메일 상세 조회
export const getEmailDetail = async (emailId: number) => {
  const response = await apiClient.get(`/api/emails/${emailId}`);

  return response.data.data as EmailDetail;
};

// 특정 이메일 URL 목록 조회
export const getEmailUrls = async (emailId: number) => {
  const response = await apiClient.get(`/api/emails/${emailId}/urls`);
  return response.data.data as EmailUrlsResponse;
};

export const getEmailAccount = getEmailAccounts;
export const getEmail = getEmails;
export const getEmailUrl = getEmailUrls;
export type EmailUrlResponse = EmailUrlsResponse;

export const mailApi = {
  getEmailAccounts,
  createEmailAccount,
  deleteEmailAccount,
  syncEmailAccount,
  getEmails,
  getEmailDetail,
  getEmailUrls,
};
