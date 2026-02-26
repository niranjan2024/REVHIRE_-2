import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  message = '';
  showPassword = false;

  form = this.fb.group({
    usernameOrEmail: ['', Validators.required],
    password: ['', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.authService.login(this.form.getRawValue() as { usernameOrEmail: string; password: string }).subscribe({
      next: (res) => {
        this.authService.setCurrentUser(res);
        const route = res.role === 'EMPLOYER' ? '/employer/dashboard' : '/jobs';
        this.router.navigateByUrl(route).then((ok) => {
          if (!ok) {
            window.location.href = route;
          }
        });
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Login failed';
      }
    });
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }
}
