import axiosInstance from './axiosInstance';

// 신고 요청 보낼 때의 데이터 타입 규칙
export interface ReportRequestDto {
  url: string;   // 신고 대상 URL
  reason: string; // 신고 사유
}

// 신고 성공 시 백엔드가 뱉어주는 응답 타입
export interface ReportResponseDto {
  reportId: number;
  status: string; // RECEIVED 등
}

/**
 * 사용자가 의심 URL을 직접 신고하는 함수
 */
export const createReport = async (reportData: ReportRequestDto): Promise<ReportResponseDto> => {
  const response = await axiosInstance.post<ReportResponseDto>('/api/reports', reportData);
  return response.data;
};

/**
 * 내가 신고한 내역 리스트를 가져오는 함수
 */
export const getMyReports = async (): Promise<ReportResponseDto[]> => {
  const response = await axiosInstance.get<ReportResponseDto[]>('/api/reports');
  return response.data;
};