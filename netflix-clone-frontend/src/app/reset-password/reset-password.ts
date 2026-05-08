import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ErrorHandlerService } from '../shared/shared/error-handler-service';
import { NotificationService } from '../shared/shared/notification-service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../shared/shared/auth-service';

@Component({
  selector: 'app-reset-password',
  standalone: false,
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css'],
})
export class ResetPassword implements OnInit {
  resetPasswordForm!: FormGroup;
  loading = false;
  tokenValid = false;
  token = '';
  hiddenPassword = true;
  hiddenConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService
  ) {
    this.resetPasswordForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [
        Validators.required,
        this.authService.passwordMatchValidator('password')
      ]]
    });
  }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.token = token;
      this.tokenValid = true;
    } else {
      this.tokenValid = false;
    }
  }

  submit() {
    this.loading = true;
    const newPassword = this.resetPasswordForm.value.password;

    this.authService.resetPassword(this.token, newPassword).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.notification.success(
          response?.message || 'Password reset successful. You can now log in with your new password.'
        );
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        const errorMessage = err.error?.error || 'Failed to reset password, please try again';

        if (errorMessage.toLowerCase().includes('expired') ||
            errorMessage.toLowerCase().includes('invalid')) {
          this.tokenValid = false;
        } else {
          this.notification.error(errorMessage);
        }
      }
    });
  }
}
