import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AppNotification } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class NotificationCenterService {
  private readonly baseUrl = '/api/notifications/user';
  private readonly readUrl = '/api/notifications';

  constructor(private readonly http: HttpClient) {}

  getNotificationsForUser(userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/${userId}`, { withCredentials: true });
  }

  markAsRead(notificationId: number): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.readUrl}/${notificationId}/read`, {}, { withCredentials: true });
  }

  getUnreadCount(_userId: number, notifications: AppNotification[]): number {
    return notifications.filter((item) => !item.read).length;
  }
}
