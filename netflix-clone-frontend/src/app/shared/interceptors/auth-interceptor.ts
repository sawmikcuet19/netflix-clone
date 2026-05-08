import { HttpInterceptorFn } from '@angular/common/http';
import { Inject } from '@angular/core';
import { AuthService } from '../shared/auth-service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = Inject(AuthService);
  const token = localStorage.getItem('token');
  let request = req;
  if (token) {
    request = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    
  } else {
    console.log("No token found , request sent wothout auth");
  }
  return next(request).pipe(
    catchError((error)=> {
      if(error.status === 401 || error.status === 403) {
        authService.logout();
      }
      return throwError(()=> error);
    })
  );
};
