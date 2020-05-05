package com.mycompany.myapp.service.mapper;


import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.service.dto.BookDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Book} and its DTO {@link BookDTO}.
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface BookMapper extends EntityMapper<BookDTO, Book> {

    @Mapping(source = "category.id", target = "categoryId")
    BookDTO toDto(Book book);

    @Mapping(source = "categoryId", target = "category")
    Book toEntity(BookDTO bookDTO);

    default Book fromId(Long id) {
        if (id == null) {
            return null;
        }
        Book book = new Book();
        book.setId(id);
        return book;
    }
}
