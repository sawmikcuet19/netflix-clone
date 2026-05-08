import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../shared/shared/auth-service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-verify-email',
  standalone: false,
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css',
})
export class VerifyEmail implements OnInit {
  loading = true;
  success = false;
  message = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.success = false;
      this.message = 'Invalid verification link. No token provided.';
      this.cdr.detectChanges();
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.success = true;
        this.message = response.message || 'Email verified successfully. you can now log in.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.success = false;
        this.message = err.error?.error || 'Failed to verify email. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

}
