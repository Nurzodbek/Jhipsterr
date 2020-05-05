import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { JhiEventManager, JhiDataUtils } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IBook } from 'app/shared/model/book.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { BookService } from './book.service';
import { BookDeleteDialogComponent } from './book-delete-dialog.component';

@Component({
  selector: 'jhi-book',
  templateUrl: './book.component.html'
})
export class BookComponent implements OnInit, OnDestroy {
  books?: IBook[];
  eventSubscriber?: Subscription;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  ngbPaginationPage = 1;
  criteria!: any;

  constructor(
    protected bookService: BookService,
    protected activatedRoute: ActivatedRoute,
    protected dataUtils: JhiDataUtils,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.criteria = {
      bookTitle: null,
      author: null,
      publisher: null,
      publicationDate: null,
      categoryDescription: null,
      areSet(): boolean {
        return (
          this.bookTitle != null ||
          this.author != null ||
          this.publisher != null ||
          this.publicationDate != null ||
          this.categoryDescription != null
        );
      },
      clear(): void {
        this.bookTitle = null;
        this.author = null;
        this.publisher = null;
        this.publicationDate = null;
        this.categoryDescription = null;
      }
    };
  }

  loadPage(page?: number): void {
    const criteria: any = [];
    this.setCriteria(criteria);
    const pageToLoad: number = page || this.page;
    this.bookService
      .query({
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
        criteria
      })
      .subscribe(
        (res: HttpResponse<IBook[]>) => this.onSuccess(res.body, res.headers, pageToLoad),
        () => this.onError()
      );
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(data => {
      this.page = data.pagingParams.page;
      this.ascending = data.pagingParams.ascending;
      this.predicate = data.pagingParams.predicate;
      this.ngbPaginationPage = data.pagingParams.page;
      this.loadPage();
    });
    this.registerChangeInBooks();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: IBook): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(contentType: string, base64String: string): void {
    return this.dataUtils.openFile(contentType, base64String);
  }

  registerChangeInBooks(): void {
    this.eventSubscriber = this.eventManager.subscribe('bookListModification', () => this.loadPage());
  }

  delete(book: IBook): void {
    const modalRef = this.modalService.open(BookDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.book = book;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected onSuccess(data: IBook[] | null, headers: HttpHeaders, page: number): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    this.router.navigate(['/book'], {
      queryParams: {
        page: this.page,
        size: this.itemsPerPage,
        sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc')
      }
    });
    this.books = data || [];
  }

  protected onError(): void {
    this.ngbPaginationPage = this.page;
  }

  reset(): void {
    this.predicate = 'id';
    this.criteria.clear();
    this.loadPage();
  }

  search(): void {
    this.predicate = 'id';
    this.loadPage();
  }

  setCriteria(criteria: any): any {
    if (this.criteria.areSet()) {
      if (this.criteria.bookTitle !== null && this.criteria.bookTitle !== '') {
        criteria.push({ key: 'bookTitle.contains', value: this.criteria.bookTitle });
      }
      if (this.criteria.author !== null && this.criteria.author !== '') {
        criteria.push({ key: 'author.contains', value: this.criteria.author });
      }
      if (this.criteria.publisher !== null && this.criteria.publisher !== '') {
        criteria.push({ key: 'publisher.contains', value: this.criteria.publisher });
      }
      if (this.criteria.publicationDate !== null && this.criteria.publicationDate !== '') {
        criteria.push({ key: 'publicationDate.equals', value: this.criteria.publicationDate });
      }
      if (this.criteria.categoryDescription !== null && this.criteria.categoryDescription !== '') {
        criteria.push({ key: 'categoryDescription.contains', value: this.criteria.categoryDescription });
      }
    }
  }
}
//   bookTitle: null,
//   author: null,
//   publisher: null,
//   publicationDate: null,
//   categoryDescription: null
