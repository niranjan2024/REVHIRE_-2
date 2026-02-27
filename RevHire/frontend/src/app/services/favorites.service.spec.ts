import { TestBed } from '@angular/core/testing';

import { FavoritesService } from './favorites.service';
import { AuthService } from './auth.service';

describe('FavoritesService', () => {
  let service: FavoritesService;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    localStorage.clear();
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getCurrentUser']);
    authService.getCurrentUser.and.returnValue({
      userId: 99,
      username: 'u',
      email: 'u@mail.com',
      fullName: 'U',
      mobileNumber: '9999999999',
      location: 'NY',
      employmentStatus: 'FRESHER',
      role: 'JOB_SEEKER',
      token: 't'
    });

    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }]
    });
    service = TestBed.inject(FavoritesService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should toggle favorites add and remove', () => {
    const job = {
      id: 1,
      title: 'Dev',
      company: 'Acme',
      location: 'NY',
      type: 'FULL_TIME',
      minSalary: 1000,
      maxSalary: 2000,
      maxExperienceYears: 2
    };

    service.toggle(job);
    expect(service.isFavorite(1)).toBeTrue();

    service.toggle(job);
    expect(service.isFavorite(1)).toBeFalse();
  });
});
