import apiClient from "./axiosInstance";

// 이메일 연동 계정 응답 타입
export interface EmailAccount {
  accountId: number;
  userId?: number;
  provider: string;
  email: string;
  active: boolean;
  lastSyncStatus: string | null;
  lastSyncedAt: string | null;
  createdAt: string;
}

// 이메일 계정 등록 요청 타입
export interface CreateEmailAccountRequest {
  provider: string;
  email: string;
  loginId: string;
  secret: string;
  imapHost?: string;
  imapPort?: number;
}

// 이메일 목록 1개 타입
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

// 이메일 URL 1개 타입
export interface EmailUrlItem {
  urlId: number;
  originalUrl: string;
  normalizedUrl: string;
  domain: string;
  riskLevel: string;
}

// 이메일 URL 목록 조회 응답 타입
export interface EmailUrlResponse {
  emailId: number;
  urls: EmailUrlItem[];
}

// 이메일 연동 계정 목록 조회
export const getEmailAccount = async () => {
  const response = await apiClient.get("/api/email-accounts");

  return response.data.data.emailAccounts as EmailAccount[];
};

// 이메일 계정 등록
export const createEmailAccount = async (
  request: CreateEmailAccountRequest,
) => {
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
  const response = await apiClient.post(
    `/api/email-accounts/${accountId}/sync`,
  );

  return response.data.data;
};

// 이메일 목록 조회
export const getEmail = async (params?: {
  accountId?: number;
  keyword?: string;
  receivedAtFrom?: string;
  receivedAtTo?: string;
  page?: number;
  size?: number;
}) => {
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
export const getEmailUrl = async (emailId: number) => {
  const response = await apiClient.get(`/api/emails/${emailId}/urls`);

  return response.data.data as EmailUrlResponse;
};
