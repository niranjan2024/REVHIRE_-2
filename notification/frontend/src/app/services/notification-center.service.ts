import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AppNotification } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class NotificationCenterService {
  private readonly baseUrl = '/api/notifications/user';
  private readonly storageKey = 'revhire_read_notifications';

  constructor(private readonly http: HttpClient) {}

  getNotificationsForUser(userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/${userId}`, { withCredentials: true });
  }

  getReadIds(userId: number): number[] {
    const data = this.readMap();
    return Array.isArray(data[userId]) ? data[userId] : [];
  }

  markRead(userId: number, ids: number[]): void {
    const data = this.readMap();
    const existing = new Set<number>(this.getReadIds(userId));
    ids.forEach((id) => existing.add(id));
    data[userId] = Array.from(existing.values());
    localStorage.setItem(this.storageKey, JSON.stringify(data));
  }

  markAllRead(userId: number, notifications: AppNotification[]): void {
    const ids = notifications.map((item) => item.id);
    this.markRead(userId, ids);
  }

  isRead(userId: number, notificationId: number): boolean {
    return this.getReadIds(userId).includes(notificationId);
  }

  getUnreadCount(userId: number, notifications: AppNotification[]): number {
    const readIds = new Set(this.getReadIds(userId));
    return notifications.filter((item) => !readIds.has(item.id)).length;
  }

  private readMap(): Record<number, number[]> {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return {};
    }
    try {
      const parsed = JSON.parse(raw) as Record<number, number[]>;
      return parsed ?? {};
    } catch {
      return {};
    }
  }
}
