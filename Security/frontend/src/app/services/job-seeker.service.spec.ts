import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { JobSeekerService } from './job-seeker.service';

describe('JobSeekerService', () => {
  let service: JobSeekerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(JobSeekerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should search jobs with params', () => {
    service.searchJobs({ title: 'java', minSalary: 1000 }).subscribe();

    const req = httpMock.expectOne((r) =>
      r.url === '/api/jobseeker/jobs/search' &&
      r.params.get('title') === 'java' &&
      r.params.get('minSalary') === '1000'
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });

  it('should post apply request', () => {
    service.applyToJob(11, 'cover').subscribe();

    const req = httpMock.expectOne('/api/jobseeker/jobs/11/apply');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.coverLetter).toBe('cover');
    req.flush({ message: 'ok' });
  });
});
