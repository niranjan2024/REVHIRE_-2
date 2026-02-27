import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { EmployerService } from './employer.service';

describe('EmployerService', () => {
  let service: EmployerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(EmployerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch jobs', () => {
    service.getJobs().subscribe();

    const req = httpMock.expectOne('/api/employer/jobs');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });

  it('should append query params for applicants filter', () => {
    service.getApplicants('UNDER_REVIEW', 'alice').subscribe();

    const req = httpMock.expectOne('/api/employer/applicants?status=UNDER_REVIEW&search=alice');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should request applicant resume file as blob', () => {
    service.getApplicantResumeFile(20).subscribe();

    const req = httpMock.expectOne('/api/employer/applications/20/resume');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(new Blob(['resume']));
  });
});
