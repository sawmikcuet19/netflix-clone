import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { VideoService } from '../../shared/shared/video-service';
import { WatchlistService } from '../../shared/shared/watchlist-service';
import { NotificationService } from '../../shared/shared/notification-service';
import { UtilityService } from '../../shared/shared/utility-service';
import { MediaService } from '../../shared/shared/media-service';
import { DialogService } from '../../shared/shared/dialog-service';
import { ErrorHandlerService } from '../../shared/shared/error-handler-service';

@Component({
  selector: 'app-my-favorities',
  standalone: false,
  templateUrl: './my-favorities.html',
  styleUrl: './my-favorities.css',
})
export class MyFavorities implements OnInit, OnDestroy {
  allVideos: any = [];
  filteredVideos: any = [];
  loading = true;
  loadingMore = false;
  error = false;
  searchQuery: string = '';

  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  hasMoreVideos = true;

  private searchSubject = new Subject<string>();

  constructor(
    private videoService: VideoService,
    private watchlistService: WatchlistService,
    private notification: NotificationService,
    public utilityService: UtilityService,
    public mediaService: MediaService,
    private dialogService: DialogService,
    private errorHandlerService: ErrorHandlerService
  ) { }

  ngOnInit(): void {
    this.loadVideos();
    this.initializeSearchDebounce();
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  initializeSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.performSearch();
    });
  }


  private performSearch() {
    this.currentPage = 0;
    this.loadVideos();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const scrollPosition = window.pageYOffset + window.innerHeight;
    const pageHeight = document.documentElement.scrollHeight;

    if (scrollPosition >= pageHeight - 200 && !this.loadingMore && this.hasMoreVideos && !this.loading) {
      this.loadMoreVideos();
    }
  }

  clearSearch() {
    this.searchQuery = '';
    this.currentPage = 0;
    this.loadVideos();
  }

  loadMoreVideos() {
    if (this.loading || !this.hasMoreVideos) return;
    this.loadingMore = true;
    const nextPage = this.currentPage + 1;
    const search = this.searchQuery.trim() || undefined;

    this.watchlistService.getWatchlist(nextPage, this.pageSize, search).subscribe({
      next: (response: any) => {
        this.allVideos = [...this.allVideos, ...response.content];
        this.filteredVideos = [...this.filteredVideos, ...response.content];
        this.currentPage = response.number;
        this.hasMoreVideos = this.currentPage < this.totalPages - 1;
        this.loadingMore = false;
      },
      error: (err) => {
        this.notification.error('Error loading videos:', err);
        this.loadingMore = false;
      }
    });

  }

  loadVideos(page: number = 0) {
    this.error = false;
    this.currentPage = 0;
    this.allVideos = [];
    this.filteredVideos = [];
    const search = this.searchQuery.trim() || undefined;
    this.loading = true;

    this.watchlistService.getWatchlist(page, this.pageSize, search).subscribe({
      next: (response: any) => {
        this.allVideos = response.content;
        this.filteredVideos = response.content;
        this.currentPage = response.number;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.hasMoreVideos = this.currentPage < this.totalPages - 1;
        this.loading = false;

      },
      error: (err) => {
        console.error('Error loading videos:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  onSearch() {
    this.searchSubject.next(this.searchQuery);
  }

  getPosterUrl(video: any) {
    return this.mediaService.getMediaUrl(video, 'image', {
      useCache: true
    }) || '';
  }

  playVideo(video: any) {
    this.dialogService.openVideoPlayer(video);
  }

  formatDuration(seconds: number | undefined): string {
    return this.utilityService.formatDuration(seconds);
  }

  toggleWatchlist(video: any, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const videoId = video.id!;
      this.watchlistService.removeFromWatchlist(videoId).subscribe({
        next: () => {
          this.allVideos = this.allVideos.filter((v:any) => v.id !==videoId);
          this.filteredVideos = this.filteredVideos.filter((v:any) => v.id !==videoId);
          this.notification.success("Remomed from my favourites");
        },
        error: (err) => {
          video.isInWatchlist = true;
          this.errorHandlerService.handle(err, "Failed to remove from favorites , please try again");
        }
      });
  }

  isInWatchlist(video: any): boolean {
    return video.isInWatchlist === true;
  }
}
