import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { JobSeekerAuthGuard } from './guards/job-seeker-auth.guard';
import { JobSeekerLayoutComponent } from './components/job-seeker-layout/job-seeker-layout.component';
import { SeekerDashboardComponent } from './components/seeker-dashboard/seeker-dashboard.component';
import { SeekerApplicationsComponent } from './components/seeker-applications/seeker-applications.component';
import { SeekerProfileComponent } from './components/seeker-profile/seeker-profile.component';
import { SeekerResumeComponent } from './components/seeker-resume/seeker-resume.component';
import { SeekerFavoritesComponent } from './components/seeker-favorites/seeker-favorites.component';
import { SeekerNotificationsComponent } from './components/seeker-notifications/seeker-notifications.component';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  {
    path: '',
    component: JobSeekerLayoutComponent,
    canActivate: [JobSeekerAuthGuard],
    children: [
      { path: 'jobs', component: SeekerDashboardComponent },
      { path: 'applications', component: SeekerApplicationsComponent },
      { path: 'complete-profile', component: SeekerProfileComponent },
      { path: 'resume', component: SeekerResumeComponent },
      { path: 'favorites', component: SeekerFavoritesComponent },
      { path: 'notifications', component: SeekerNotificationsComponent },
      { path: 'change-password', component: ChangePasswordComponent }
    ]
  }
];

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    ChangePasswordComponent,
    ResetPasswordComponent,
    JobSeekerLayoutComponent,
    SeekerDashboardComponent,
    SeekerApplicationsComponent,
    SeekerProfileComponent,
    SeekerResumeComponent,
    SeekerFavoritesComponent,
    SeekerNotificationsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
