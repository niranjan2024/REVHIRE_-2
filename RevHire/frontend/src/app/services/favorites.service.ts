import { Injectable } from '@angular/core';

import { JobSummary } from '../models/auth.models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class FavoritesService {
  constructor(private readonly authService: AuthService) {}

  getFavorites(): JobSummary[] {
    const raw = localStorage.getItem(this.storageKey());
    if (!raw) {
      return [];
    }
    try {
      return JSON.parse(raw) as JobSummary[];
    } catch {
      return [];
    }
  }

  isFavorite(jobId: number): boolean {
    return this.getFavorites().some((item) => item.id === jobId);
  }

  toggle(job: JobSummary): void {
    const items = this.getFavorites();
    const existing = items.find((item) => item.id === job.id);
    if (existing) {
      this.save(items.filter((item) => item.id !== job.id));
      return;
    }
    this.save([...items, job]);
  }

  remove(jobId: number): void {
    const items = this.getFavorites().filter((item) => item.id !== jobId);
    this.save(items);
  }

  private save(items: JobSummary[]): void {
    localStorage.setItem(this.storageKey(), JSON.stringify(items));
  }

  private storageKey(): string {
    const seekerId = this.authService.getCurrentUser()?.userId;
    return `revhire.favorite.jobs.${seekerId ?? 'unknown'}`;
  }
}
