import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { JhiDataUtils, JhiFileLoadError, JhiEventManager, JhiEventWithContent } from 'ng-jhipster';

import { IBook, Book } from 'app/shared/model/book.model';
import { BookService } from './book.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { ICategory } from 'app/shared/model/category.model';
import { CategoryService } from 'app/entities/category/category.service';

@Component({
  selector: 'jhi-book-update',
  templateUrl: './book-update.component.html'
})
export class BookUpdateComponent implements OnInit {
  isSaving = false;
  categories: ICategory[] = [];
  publicationDateDp: any;

  editForm = this.fb.group({
    id: [],
    bookTitle: [],
    author: [],
    publisher: [],
    publicationDate: [],
    data: [],
    dataContentType: [],
    categoryId: []
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected eventManager: JhiEventManager,
    protected bookService: BookService,
    protected categoryService: CategoryService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ book }) => {
      this.updateForm(book);

      this.categoryService
        .query({ 'bookId.specified': 'false' })
        .pipe(
          map((res: HttpResponse<ICategory[]>) => {
            return res.body || [];
          })
        )
        .subscribe((resBody: ICategory[]) => {
          if (!book.categoryId) {
            this.categories = resBody;
          } else {
            this.categoryService
              .find(book.categoryId)
              .pipe(
                map((subRes: HttpResponse<ICategory>) => {
                  return subRes.body ? [subRes.body].concat(resBody) : resBody;
                })
              )
              .subscribe((concatRes: ICategory[]) => (this.categories = concatRes));
          }
        });
    });
  }

  updateForm(book: IBook): void {
    this.editForm.patchValue({
      id: book.id,
      bookTitle: book.bookTitle,
      author: book.author,
      publisher: book.publisher,
      publicationDate: book.publicationDate,
      data: book.data,
      dataContentType: book.dataContentType,
      categoryId: book.categoryId
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(contentType: string, base64String: string): void {
    this.dataUtils.openFile(contentType, base64String);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe(null, (err: JhiFileLoadError) => {
      this.eventManager.broadcast(
        new JhiEventWithContent<AlertError>('jhipsterApp.error', { ...err, key: 'error.file.' + err.key })
      );
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const book = this.createFromForm();
    if (book.id !== undefined) {
      this.subscribeToSaveResponse(this.bookService.update(book));
    } else {
      this.subscribeToSaveResponse(this.bookService.create(book));
    }
  }

  private createFromForm(): IBook {
    return {
      ...new Book(),
      id: this.editForm.get(['id'])!.value,
      bookTitle: this.editForm.get(['bookTitle'])!.value,
      author: this.editForm.get(['author'])!.value,
      publisher: this.editForm.get(['publisher'])!.value,
      publicationDate: this.editForm.get(['publicationDate'])!.value,
      dataContentType: this.editForm.get(['dataContentType'])!.value,
      data: this.editForm.get(['data'])!.value,
      categoryId: this.editForm.get(['categoryId'])!.value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBook>>): void {
    result.subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError(): void {
    this.isSaving = false;
  }

  trackById(index: number, item: ICategory): any {
    return item.id;
  }
}
