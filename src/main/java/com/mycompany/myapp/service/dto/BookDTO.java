package com.mycompany.myapp.service.dto;

import java.time.LocalDate;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Book} entity.
 */
public class BookDTO implements Serializable {

    private Long id;

    private String bookTitle;

    private String author;

    private String publisher;

    private LocalDate publicationDate;

    @Lob
    private byte[] data;

    private String dataContentType;

    private Long categoryId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getDataContentType() {
        return dataContentType;
    }

    public void setDataContentType(String dataContentType) {
        this.dataContentType = dataContentType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BookDTO bookDTO = (BookDTO) o;
        if (bookDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bookDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BookDTO{" +
            "id=" + getId() +
            ", bookTitle='" + getBookTitle() + "'" +
            ", author='" + getAuthor() + "'" +
            ", publisher='" + getPublisher() + "'" +
            ", publicationDate='" + getPublicationDate() + "'" +
            ", data='" + getData() + "'" +
            ", categoryId=" + getCategoryId() +
            "}";
    }
}
