import { Component, OnInit } from '@angular/core';

import { JobSummary } from '../../models/auth.models';
import { FavoritesService } from '../../services/favorites.service';

@Component({
  selector: 'app-seeker-favorites',
  templateUrl: './seeker-favorites.component.html'
})
export class SeekerFavoritesComponent implements OnInit {
  favorites: JobSummary[] = [];

  constructor(private readonly favoritesService: FavoritesService) {}

  ngOnInit(): void {
    this.load();
  }

  remove(jobId: number): void {
    this.favoritesService.remove(jobId);
    this.load();
  }

  private load(): void {
    this.favorites = this.favoritesService.getFavorites();
  }
}
