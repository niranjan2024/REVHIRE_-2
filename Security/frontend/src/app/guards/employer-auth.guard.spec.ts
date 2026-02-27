import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { EmployerAuthGuard } from './employer-auth.guard';
import { AuthService } from '../services/auth.service';

describe('EmployerAuthGuard', () => {
  let guard: EmployerAuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isEmployerLoggedIn']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        EmployerAuthGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });

    guard = TestBed.inject(EmployerAuthGuard);
  });

  it('should allow when employer is logged in', () => {
    authService.isEmployerLoggedIn.and.returnValue(true);

    expect(guard.canActivate()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to login when not logged in', () => {
    authService.isEmployerLoggedIn.and.returnValue(false);

    expect(guard.canActivate()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
