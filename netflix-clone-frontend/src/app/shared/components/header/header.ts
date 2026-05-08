import { Component, Input, OnInit } from '@angular/core';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../shared/auth-service';
import { DialogService } from '../dialog-service';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit {
  @Input() showRouterOutlet: boolean = true;
  currentUser: any = null;
  isAdminMode: boolean = false;
  private routerSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private dialogService: DialogService,
    private router: Router
  ) {
    // You can initialize any necessary services here
  }

  ngOnInit(): void {
    // Initialize the component
    this.currentUser = this.authService.getCurrentUser();
    this.updateMode();

    this.routerSubscription = this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.updateMode();
    });
  }

  private updateMode(): void {
    this.isAdminMode = this.router.url.startsWith('/admin');
  }
  
  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  switchMode(): void {
    if (this.isAdminMode) {
      this.router.navigate(['/home']);
    } else {
      this.router.navigate(['/admin']);
    }
  }

  openChangePassword(): void {
    this.dialogService.openChangePasswordDialog();
  }

  logout(){
    this.dialogService.openConfirmation('Logout', 'Are you sure you want to logout?', 'Logout', 'Cancel', 'info').subscribe(confirmed => {
      if (confirmed) {
        this.authService.logout();
      }
    });
  }
}
