import { HttpClient, HttpEventType, HttpParams, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from './auth-service';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class VideoService {
  private apiUrl = environment.apiUrl + '/videos';
  private apiUrlAdmin = environment.apiUrl + '/videos/admin';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  getAllAdminVideos(page: number, size: number, search?: string){
    let params  = new HttpParams()
      .set('page', page)
      .set('size', size);
    if(search){
      params = params.set('search', search);
    }
    return this.http.get(this.apiUrlAdmin, { params });
  }

  createVideoByAdmin(data: any) {
    return this.http.post(this.apiUrlAdmin, data);
  }

  updateVideoByAdmin(id: string | number, data: any) {
    return this.http.put(`${this.apiUrlAdmin}/${id}`, data);
  }

  deleteVideoByAdmin(id: string | number) {
    return this.http.delete(`${this.apiUrlAdmin}/${id}`);
  }

  setPublishedByAdmin(id: string | number, published: boolean) {
    return this.http.patch(`${this.apiUrlAdmin}/${id}/publish?value=${published}`, {});
  }

  getStatsByAdmin() {
    return this.http.get(`${this.apiUrlAdmin}/stats`);
  }

  getPublishedVideosPaginated(page: number, size: number, search?: string){
    let params  = new HttpParams()
      .set('page', page)
      .set('size', size);
    if(search){
      params = params.set('search', search);
    }
    return this.http.get(this.apiUrl+'/published', { params });
  }

  getFeaturedVideos(){
    return this.http.get(this.apiUrl+'/featured');
  }
}
