import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ErrorHandlerService } from '../shared/shared/error-handler-service';
import { NotificationService } from '../shared/shared/notification-service';
import { Router } from '@angular/router';
import { AuthService } from '../shared/shared/auth-service';
import { email } from '@angular/forms/signals';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  forgotPassword !: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService
  ) {
    // Initialize the forgot password form here
    this.forgotPassword = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  submit() {
    this.loading = true;
    const email = this.forgotPassword.value.email?.trim().toLowerCase();
    this.authService.forgotPassword(email).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.notification.success(response?.message || 'Password reset email sent. Please check your inbox.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.notification.error(err.error?.error || 'Failed to reset email, please try again');
        this.loading = false;
      }
    });
  }
}
