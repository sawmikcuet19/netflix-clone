import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { VIDEO_CATEGORIES, RATINGS } from '../../../shared/constants/app.constants';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { NotificationService } from '../../../shared/shared/notification-service';
import { MediaService } from '../../../shared/shared/media-service';
import { VideoService } from '../../../shared/shared/video-service';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { required } from '@angular/forms/signals';
import { ErrorHandlerService } from '../../../shared/shared/error-handler-service';
import { D } from '@angular/cdk/keycodes';
@Component({
  selector: 'app-manage-video',
  standalone: false,
  templateUrl: './manage-video.html',
  styleUrl: './manage-video.css',
})
export class ManageVideo implements OnInit {
  isSaving = false;
  uploadProgress = 0;
  posterProgress = 0;

  categoriesAll = VIDEO_CATEGORIES;
  ratings = RATINGS;
  videoForm: any;

  videoPreviewUrl: string | null = null;
  posterPreviewUrl: string | null = null;
  videoLoading = false;
  posterLoading = false;
  isEditMode : boolean = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService,
    private cdr : ChangeDetectorRef,
    private mediaService: MediaService,
    private videoService: VideoService,
    public dialogRef: MatDialogRef<ManageVideo>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { 
    this.isEditMode = data.mode === 'edit';

    this.videoForm = this.fb.group({
      title: ['', [Validators.required]],
      description: ['', [Validators.required]],
      year: [new Date().getFullYear(), [Validators.required]],
      rating: ['', [Validators.required]],
      categories: [[] as string[], [Validators.required, ManageVideo.arrayNotEmpty]],
      duration: [0],
      src: ['', [Validators.required]],
      poster: ['', [Validators.required]],
      published: [false]
    })
  }

  static arrayNotEmpty(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (Array.isArray(value) && value.length === 0 || !value) {
      return {required: true};
    }
    return null;
  }

  private loadVideoPreview(value: string | null): void {
    this.videoPreviewUrl = this.mediaService.getMediaUrl(value, 'video');
    this.videoLoading = false;
    this.cdr.detectChanges();
  }

  private loadPosterPreview(value: string | null): void {
    this.posterPreviewUrl = this.mediaService.getMediaUrl(value, 'image');
    this.posterLoading = false;
    this.cdr.detectChanges();
  }

  private extractUuidFromUrl(value: string | undefined | null): string {
    if (!value) return '';
    if (!value.includes('/')) {
      return value;
    }
    const segments = value.split('/');
    return segments[segments.length - 1] || '';
  }

  ngOnInit(): void {
    if (this.isEditMode) {
      const video = this.data.video;
      this.videoForm.patchValue({
        title: video.title,
        description: video.description,
        year: video.year,
        rating: video.rating,
        categories: video.categories || [],
        duration: video.duration,
        src: this.extractUuidFromUrl(video.src),
        poster: this.extractUuidFromUrl(video.poster),
        published: video.published
      });
      if(video.src) {
        this.loadVideoPreview(video.src);
      }
      if(video.poster) {
        this.loadPosterPreview(video.poster);
      }
    }
  }

  onVideoPicked(ev: Event){
    const file = (ev.target as HTMLInputElement).files?.[0];
    if(!file) {
      return;
    }

    const validVideoExtensions = ['.mp4', '.mkv', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mpeg', '.ogg', '.m4v', '.3gp', '.mpg', '.mpeg4'];
    const fileName = file.name.toLowerCase();
    const hasValidExtension = validVideoExtensions.some(ext => fileName.endsWith(ext));
    const hasValidMimeType = file.type.startsWith('video/') || file.type === 'application/octet-stream';

    if (!hasValidExtension && !hasValidMimeType) {
      this.notification.error('Invalid video file. Please select a valid video file.');
      return;
    }

    const localBlobUrl = URL.createObjectURL(file);
    this.videoPreviewUrl = localBlobUrl;

    this.extractDurationFromVideo(file);

    this.uploadProgress = 0;

    this.mediaService.uploadFile(file).subscribe({
      next: ({ progress, uuid }) => {
        this.uploadProgress = progress; 
        if (uuid) {
          this.videoForm.patchValue({ src: uuid });
          this.notification.success('Video uploaded successfully!');
        }
      },
      error: (err) => {
        console.error('Video upload failed:', err);
        this.notification.error('Failed to upload video. Please try again.');

        this.uploadProgress = 0;
        if (this.videoPreviewUrl) {
          URL.revokeObjectURL(localBlobUrl);
          this.videoPreviewUrl = null;
        }
      }
    });
  }

  onPosterPicked(ev: Event){
    const file = (ev.target as HTMLInputElement).files?.[0];
    if(!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.notification.error('Invalid image file. Please select a valid image file.');
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => {
      this.posterPreviewUrl = e.target?.result as string;
      this.cdr.detectChanges();
      
    };

    reader.readAsDataURL(file);
    this.posterProgress = 0;
     this.mediaService.uploadFile(file).subscribe({
      next: ({ progress, uuid }) => {
        this.posterProgress = progress; 
        if (uuid) {
          this.videoForm.patchValue({ poster: uuid });
          this.notification.success('Poster uploaded successfully!');
        }
      },
      error: (err) => {
        console.error('Poster upload failed:', err);
        this.notification.error('Failed to upload poster. Please try again.');

        this.posterProgress = 0;
        this.posterPreviewUrl = null;
      }
    });
  }

  private extractDurationFromVideo(file: File) {
    const videoElement = document.createElement('video');
    videoElement.preload = 'metadata';

    const blobUrl = URL.createObjectURL(file);
    videoElement.src = blobUrl;

    videoElement.onloadedmetadata = () => {
      const duration = isFinite(videoElement.duration) ? Math.round(videoElement.duration) : 0;
      this.videoForm.patchValue({ duration: duration });
      URL.revokeObjectURL(blobUrl);
    };

    videoElement.onerror = (e) => {
      console.error('Error occurred while loading video metadata.', e);
      URL.revokeObjectURL(blobUrl);
    };
  }

  onSave() {
  if (this.videoForm.invalid) {
    this.notification.error('Please fill in all required fields.');
    this.videoForm.markAllAsTouched();
    return;
  }

  this.isSaving = true;
  const formData = this.videoForm.value;

  const op$ = this.isEditMode 
              ? this.videoService.updateVideoByAdmin(this.data.video.id, formData) 
              : this.videoService.createVideoByAdmin(formData);

  op$.subscribe({
    next: (response: any) => {
      this.isSaving = false;
      this.notification.success(response?.message || `Video ${this.isEditMode ? 'updated' : 'created'} successfully!`);
      this.dialogRef.close(true);
    },
    error: (err: any) => {
      this.isSaving = false;
      // Use the error handler service but also log the error for debugging
      console.error('Error saving video:', err);
      
      // If your backend returns a nested error message, extract it here
      const errorMessage = err?.error?.message || err?.message || `Failed to ${this.isEditMode ? 'update' : 'create'} video.`;
      
      this.errorHandlerService.handle(err, errorMessage);
    }
  });
}

  closeDialog() {
    this.dialogRef.close();
  }

  removeVideo() {
    if (this.videoPreviewUrl && this.videoPreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.videoPreviewUrl);
    }
    this.videoPreviewUrl = null;
    this.videoForm.patchValue({ src: '', duration: 0 });
    this.uploadProgress = 0;
  }

  removePoster() {
    if (this.posterPreviewUrl && this.posterPreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.posterPreviewUrl);
    }
    this.posterPreviewUrl = null;
    this.videoForm.patchValue({ poster: '' });
    this.posterProgress = 0;
  }

  onCategoryRemoved(category: string) {
  const categories = this.videoForm.get('categories')?.value as string[];
  if (categories) {
    const index = categories.indexOf(category);
    if (index >= 0) {
      categories.splice(index, 1);
      // Trigger a change so the form and select stay in sync
      this.videoForm.get('categories')?.setValue([...categories]); 
    }
  }
}
}
