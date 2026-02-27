import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should call login endpoint', () => {
    service.login({ usernameOrEmail: 'u', password: 'p' }).subscribe();

    const req = httpMock.expectOne('/api/login');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should persist and read current user token', () => {
    service.setCurrentUser({
      userId: 1,
      username: 'user',
      email: 'user@mail.com',
      fullName: 'User',
      mobileNumber: '9999999999',
      location: 'NY',
      employmentStatus: 'FRESHER',
      role: 'JOB_SEEKER',
      token: 'jwt'
    });

    expect(service.getToken()).toBe('jwt');
    expect(service.isJobSeekerLoggedIn()).toBeTrue();
  });
});
