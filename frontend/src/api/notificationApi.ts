import { getAccessToken } from "../utils/token";

const API_BASE_URL = "http://localhost:8080";

export interface NotificationResponse {
  notificationId: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

interface NotificationPageResponse {
  content?: NotificationResponse[];
}

const getAuthHeaders = () => {
  const token = getAccessToken();

  return {
    "Content-Type": "application/json",
    Authorization: token ? `Bearer ${token}` : "",
  };
};

export const getNotifications = async (): Promise<NotificationResponse[]> => {
  const response = await fetch(`${API_BASE_URL}/api/notifications`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("알림 목록을 불러오지 못했습니다.");
  }

  const data: NotificationResponse[] | NotificationPageResponse =
    await response.json();

  if (Array.isArray(data)) {
    return data;
  }

  return data.content ?? [];
};

export const getUnreadCount = async (): Promise<number> => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/unread-count`,
    {
      method: "GET",
      headers: getAuthHeaders(),
    },
  );

  if (!response.ok) {
    throw new Error("읽지 않은 알림 개수를 불러오지 못했습니다.");
  }

  const data = await response.json();

  if (typeof data === "number") {
    return data;
  }

  return data.count ?? data.unreadCount ?? 0;
};

export const readNotification = async (
  notificationId: number,
): Promise<void> => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/${notificationId}/read`,
    {
      method: "PATCH",
      headers: getAuthHeaders(),
    },
  );

  if (!response.ok) {
    throw new Error("알림 읽음 처리에 실패했습니다.");
  }
};

export const deleteNotification = async (
  notificationId: number,
): Promise<void> => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/${notificationId}`,
    {
      method: "DELETE",
      headers: getAuthHeaders(),
    },
  );

  if (!response.ok) {
    throw new Error("알림 삭제에 실패했습니다.");
  }
};
