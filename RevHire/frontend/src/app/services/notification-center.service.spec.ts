import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { NotificationCenterService } from './notification-center.service';

describe('NotificationCenterService', () => {
  let service: NotificationCenterService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(NotificationCenterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request notifications for user', () => {
    service.getNotificationsForUser(15).subscribe();

    const req = httpMock.expectOne('/api/notifications/user/15');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });

  it('should return unread count from list', () => {
    const count = service.getUnreadCount(1, [
      { id: 1, jobId: null, message: 'a', type: 'SYSTEM', read: false, createdAt: '2024-01-01' },
      { id: 2, jobId: null, message: 'b', type: 'SYSTEM', read: true, createdAt: '2024-01-01' }
    ]);

    expect(count).toBe(1);
  });
});
