import { HttpHandler, HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';

import { JwtInterceptor } from './jwt.interceptor';
import { AuthService } from './auth.service';

describe('JwtInterceptor', () => {
  it('should add authorization header when token exists', () => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);
    authService.getToken.and.returnValue('jwt-token');
    const interceptor = new JwtInterceptor(authService);
    const next = jasmine.createSpyObj<HttpHandler>('HttpHandler', ['handle']);
    next.handle.and.returnValue(of(new HttpResponse({ status: 200 })));
    const request = new HttpRequest('GET', '/api/jobs');

    interceptor.intercept(request, next).subscribe();

    const forwarded = next.handle.calls.mostRecent().args[0] as HttpRequest<unknown>;
    expect(forwarded.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('should forward unchanged request when token missing', () => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);
    authService.getToken.and.returnValue(null);
    const interceptor = new JwtInterceptor(authService);
    const next = jasmine.createSpyObj<HttpHandler>('HttpHandler', ['handle']);
    next.handle.and.returnValue(of(new HttpResponse({ status: 200 })));
    const request = new HttpRequest('GET', '/api/jobs');

    interceptor.intercept(request, next).subscribe();

    const forwarded = next.handle.calls.mostRecent().args[0] as HttpRequest<unknown>;
    expect(forwarded).toBe(request);
  });
});
