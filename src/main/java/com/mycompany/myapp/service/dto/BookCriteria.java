package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.LocalDateFilter;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Book} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.BookResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /books?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BookCriteria implements Serializable, Criteria {

        private static final long serialVersionUID = 1L;

        private LongFilter id;

        private StringFilter bookTitle;

        private StringFilter author;

        private StringFilter publisher;

        private LocalDateFilter publicationDate;

        private LongFilter categoryId;

        private StringFilter categoryDescription;

        public BookCriteria() {
        }

        public BookCriteria(BookCriteria other) {
            this.id = other.id == null ? null : other.id.copy();
            this.bookTitle = other.bookTitle == null ? null : other.bookTitle.copy();
            this.author = other.author == null ? null : other.author.copy();
            this.publisher = other.publisher == null ? null : other.publisher.copy();
            this.publicationDate = other.publicationDate == null ? null : other.publicationDate.copy();
            this.categoryId = other.categoryId == null ? null : other.categoryId.copy();
            this.categoryDescription = other.categoryDescription == null ? null : other.categoryDescription.copy();
        }

        @Override
        public BookCriteria copy() {
            return new BookCriteria(this);
        }

        public LongFilter getId() {
            return id;
        }

        public void setId(LongFilter id) {
            this.id = id;
        }

        public StringFilter getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(StringFilter bookTitle) {
            this.bookTitle = bookTitle;
        }

        public StringFilter getAuthor() {
            return author;
        }

        public void setAuthor(StringFilter author) {
            this.author = author;
        }

        public StringFilter getPublisher() {
            return publisher;
        }

        public void setPublisher(StringFilter publisher) {
            this.publisher = publisher;
        }

        public LocalDateFilter getPublicationDate() {
            return publicationDate;
        }

        public void setPublicationDate(LocalDateFilter publicationDate) {
            this.publicationDate = publicationDate;
        }

        public LongFilter getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(LongFilter categoryId) {
            this.categoryId = categoryId;
        }

        public StringFilter getCategoryDescription() { return categoryDescription; }

        public void setCategoryDescription(StringFilter categoryDescription) { this.categoryDescription = categoryDescription; }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final BookCriteria that = (BookCriteria) o;
            return
                Objects.equals(id, that.id) &&
                Objects.equals(bookTitle, that.bookTitle) &&
                Objects.equals(author, that.author) &&
                Objects.equals(publisher, that.publisher) &&
                Objects.equals(publicationDate, that.publicationDate) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(categoryDescription, that.categoryDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
            id,
            bookTitle,
            author,
            publisher,
            publicationDate,
            categoryId,
            categoryDescription
            );
        }

        @Override
        public String toString() {
            return "BookCriteria{" +
                    (id != null ? "id=" + id + ", " : "") +
                    (bookTitle != null ? "bookTitle=" + bookTitle + ", " : "") +
                    (author != null ? "author=" + author + ", " : "") +
                    (publisher != null ? "publisher=" + publisher + ", " : "") +
                    (publicationDate != null ? "publicationDate=" + publicationDate + ", " : "") +
                    (categoryId != null ? "categoryId=" + categoryId + ", " : "") +
                    (categoryDescription != null ? "categoryDescription=" + categoryDescription + ", " : "") +
                "}";
        }

}
