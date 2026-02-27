import { Component } from '@angular/core';
import { FormBuilder, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { EmploymentStatus, RegisterRequest, Role } from '../../models/auth.models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  message = '';
  showPassword = false;
  showConfirmPassword = false;
  private redirectTimer?: ReturnType<typeof setTimeout>;
  private readonly employerRole: Role = 'EMPLOYER';
  private readonly passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=]).{8,64}$/;
  private readonly nameRegex = /^[A-Za-z ]+$/;
  private readonly websiteRegex = /^(https?:\/\/)?([^\s.]+\.\S{2,})/i;

  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(this.nameRegex)]],
    email: ['', [Validators.required, Validators.email]],
    mobileNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    password: ['', [Validators.required, Validators.pattern(this.passwordRegex)]],
    confirmPassword: ['', [Validators.required]],
    securityQuestion: ['', [Validators.required]],
    securityAnswer: ['', [Validators.required, Validators.minLength(2)]],
    location: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    employmentStatus: ['', Validators.required],
    role: ['', Validators.required],
    companyName: [''],
    industry: [''],
    companySize: [''],
    companyDescription: [''],
    website: [''],
    companyLocation: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.form.controls.role.valueChanges.subscribe(() => this.applyRoleValidators());
  }

  get isEmployer(): boolean {
    return this.form.controls.role.value === this.employerRole;
  }

  submit(): void {
    this.message = '';
    this.applyRoleValidators();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const invalidFields = this.getInvalidFieldLabels();
      this.message = invalidFields.length
        ? `Please correct: ${invalidFields.join(', ')}`
        : 'Please complete all required fields correctly';
      return;
    }

    if (this.form.value.password !== this.form.value.confirmPassword) {
      this.message = 'Password and confirm password do not match';
      return;
    }

    const formValue = this.form.getRawValue();
    const request: RegisterRequest = {
      username: formValue.username ?? '',
      fullName: formValue.fullName ?? '',
      email: formValue.email ?? '',
      password: formValue.password ?? '',
      confirmPassword: formValue.confirmPassword ?? '',
      mobileNumber: formValue.mobileNumber ?? '',
      role: (formValue.role as Role | null) ?? 'JOB_SEEKER',
      securityQuestion: formValue.securityQuestion ?? '',
      securityAnswer: formValue.securityAnswer ?? '',
      location: this.isEmployer ? (formValue.companyLocation ?? '') : (formValue.location ?? ''),
      employmentStatus: this.isEmployer
        ? 'EMPLOYED'
        : ((formValue.employmentStatus as EmploymentStatus | null) ?? 'FRESHER'),
      companyName: this.isEmployer ? (formValue.companyName ?? '') : undefined,
      industry: this.isEmployer ? (formValue.industry ?? '') : undefined,
      companySize: this.isEmployer ? (formValue.companySize ?? '') : undefined,
      companyDescription: this.isEmployer ? (formValue.companyDescription ?? '') : undefined,
      website: this.isEmployer ? (formValue.website ?? '') : undefined,
      companyLocation: this.isEmployer ? (formValue.companyLocation ?? '') : undefined
    };

    this.authService.register(request).subscribe({
      next: (res) => {
        this.message = res?.message ?? 'Registration successful';
        if (this.redirectTimer) {
          clearTimeout(this.redirectTimer);
        }
        this.redirectTimer = setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Registration failed';
      }
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  private applyRoleValidators(): void {
    this.setControlValidators('location', this.isEmployer ? [] : [Validators.required, Validators.minLength(2), Validators.maxLength(100)]);
    this.setControlValidators('employmentStatus', this.isEmployer ? [] : [Validators.required]);
    this.setControlEnabled('location', !this.isEmployer);
    this.setControlEnabled('employmentStatus', !this.isEmployer);

    const employerNameValidators = this.isEmployer ? [Validators.required, Validators.minLength(3), Validators.maxLength(100)] : [];
    this.setControlValidators('companyName', employerNameValidators);
    this.setControlValidators('industry', this.isEmployer ? [Validators.required, Validators.minLength(3)] : []);
    this.setControlValidators('companySize', this.isEmployer ? [Validators.required, Validators.pattern(/^[0-9]+$/)] : []);
    this.setControlValidators('companyDescription', this.isEmployer ? [Validators.required, Validators.minLength(10)] : []);
    this.setControlValidators('companyLocation', this.isEmployer ? [Validators.required, Validators.minLength(2)] : []);
    this.setControlValidators('website', this.isEmployer ? [Validators.required, Validators.pattern(this.websiteRegex)] : []);
    this.setControlEnabled('companyName', this.isEmployer);
    this.setControlEnabled('industry', this.isEmployer);
    this.setControlEnabled('companySize', this.isEmployer);
    this.setControlEnabled('companyDescription', this.isEmployer);
    this.setControlEnabled('website', this.isEmployer);
    this.setControlEnabled('companyLocation', this.isEmployer);

    this.form.updateValueAndValidity({ emitEvent: false });
  }

  private setControlValidators(controlName: keyof typeof this.form.controls, validators: ValidatorFn[]): void {
    const control = this.form.controls[controlName];
    control.setValidators(validators);
    control.updateValueAndValidity({ emitEvent: false });
  }

  private setControlEnabled(controlName: keyof typeof this.form.controls, enabled: boolean): void {
    const control = this.form.controls[controlName];
    if (enabled) {
      control.enable({ emitEvent: false });
    } else {
      control.disable({ emitEvent: false });
    }
  }

  private getInvalidFieldLabels(): string[] {
    const labels: Partial<Record<keyof typeof this.form.controls, string>> = {
      username: 'Username',
      fullName: 'Full Name',
      email: 'Email',
      mobileNumber: 'Mobile Number',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      role: 'Role',
      securityQuestion: 'Security Question',
      securityAnswer: 'Security Answer',
      location: 'Location',
      employmentStatus: 'Employment Status',
      companyName: 'Company Name',
      industry: 'Industry',
      companySize: 'Company Size',
      companyDescription: 'Company Description',
      website: 'Website',
      companyLocation: 'Company Location'
    };

    return Object.entries(this.form.controls)
      .filter(([, control]) => control.enabled && control.invalid)
      .map(([name]) => labels[name as keyof typeof this.form.controls] ?? name);
  }
}
