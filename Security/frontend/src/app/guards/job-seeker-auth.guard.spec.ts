import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { JobSeekerAuthGuard } from './job-seeker-auth.guard';
import { AuthService } from '../services/auth.service';

describe('JobSeekerAuthGuard', () => {
  let guard: JobSeekerAuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isJobSeekerLoggedIn']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        JobSeekerAuthGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });

    guard = TestBed.inject(JobSeekerAuthGuard);
  });

  it('should allow when job seeker is logged in', () => {
    authService.isJobSeekerLoggedIn.and.returnValue(true);

    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to login when not logged in', () => {
    authService.isJobSeekerLoggedIn.and.returnValue(false);

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
