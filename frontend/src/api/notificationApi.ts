

export interface NotificationResponse {
  notificationId: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export const getNotifications = async (): Promise<NotificationResponse[]> => {
  return [
    {
      notificationId: 1,
      title: '고위험 URL이 탐지되었습니다.',
      message:
        '메일 기반 수집 데이터에서 매우 위험 URL 3건이 추가 탐지되었습니다. URL 관리 페이지에서 상세 링크와 위험도를 확인해 주세요.',
      isRead: false,
      createdAt: '2026-03-25T12:34:00',
    },
    {
      notificationId: 2,
      title: '이메일 연동 상태가 정상입니다.',
      message:
        '연동된 메일 계정의 동기화가 정상적으로 완료되었습니다. 마지막 점검 시간은 10:15이며 현재 오류는 없습니다.',
      isRead: true,
      createdAt: '2026-03-25T10:15:00',
    },
    {
      notificationId: 3,
      title: '신고 접수 건 처리 상태가 변경되었습니다.',
      message:
        '사용자가 신고한 URL 1건이 검토 완료 상태로 변경되었습니다. 신고 내역 페이지에서 조치 결과를 확인할 수 있습니다.',
      isRead: false,
      createdAt: '2026-03-24T17:02:00',
    },
  ];
};

export const getUnreadCount = async (): Promise<number> => {
  return 2;
};

export const readNotification = async (
  notificationId: number
): Promise<void> => {
  console.log(`알림 ${notificationId} 읽음 처리 완료`);
};