import { Injectable } from '@angular/core';
import { NotificationService } from './notification-service';

@Injectable({
  providedIn: 'root',
})
export class ErrorHandlerService {
  constructor(private notification: NotificationService) { }

  handle(err: any, fallbackMessage: string) {
    const errorMessage = err?.error?.message || fallbackMessage || 'An unexpected error occurred.';
    this.notification.error(errorMessage);

    console.log('API Error: ', err);
  }
}
