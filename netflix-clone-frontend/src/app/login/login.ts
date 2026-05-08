import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ErrorHandlerService } from '../shared/shared/error-handler-service';
import { NotificationService } from '../shared/shared/notification-service';
import { Router } from '@angular/router';
import { AuthService } from '../shared/shared/auth-service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  hidePassword = true;
  loginForm!: FormGroup;
  loading = false;
  showResendLink = false;
  userEmail: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.authService.redirectBasedOnRole();
    }
  }

  submit() {
    this.loading = true;
    const formData = this.loginForm.value;
    const authData = {
      email: formData.email?.trim().toLowerCase(),
      password: formData.password,
    };

    this.authService.login(authData).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.authService.redirectBasedOnRole();
      },
      error: (err) => {
        this.loading = false;
        const errorMsq = err.error?.error || 'Login failed. Please try again.';

        if (err.status === 403 && errorMsq.toLowerCase().includes('verify')) {
          this.showResendLink = true;
          this.userEmail = this.loginForm.value.email;
        } else {
          this.showResendLink = false;
        }
        this.notification.error(errorMsq);
        console.error('Login error:', err);
      }
    });
  }

  resendVerification() {
    if (!this.userEmail) {
      this.notification.error('Email is required to resend verification link.');
      return;
    }

    this.showResendLink = false;
    this.loading = true;
    this.authService.resendVerificationEmail(this.userEmail).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.notification.success(response.message || 'Verification email resent. Please check your inbox.');
      },
      error: (err) => {
        this.loading = false;
        this.errorHandlerService.handle(err, 'Failed to resend verification email. Please try again.');
        
      }
    });
  }

  forgot() {
    this.router.navigate(['/forgot-password']);
  }

}