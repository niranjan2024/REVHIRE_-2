import { Injectable } from '@angular/core';

import { JobSummary } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly storageKey = 'revhire.jobseeker.favorites';

  getFavorites(): JobSummary[] {
    const raw = localStorage.getItem(this.storageKey);
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
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }
}
