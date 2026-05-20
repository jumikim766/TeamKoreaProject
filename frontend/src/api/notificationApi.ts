import axiosInstance from './axiosInstance';

export interface NotificationResponse {
  notificationId: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

// 안 읽은 알림 개수 가져오기
export const getUnreadCount = async (): Promise<number> => {
  const response = await axiosInstance.get<number>('/api/notifications/unread-count');
  return response.data;
};

// 알림 읽음 처리하기
export const readNotification = async (notificationId: number): Promise<void> => {
  await axiosInstance.patch(`/api/notifications/${notificationId}/read`);
};