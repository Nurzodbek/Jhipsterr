package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.JhipsterApp;
import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.BookRepository;
import com.mycompany.myapp.service.BookService;
import com.mycompany.myapp.service.dto.BookDTO;
import com.mycompany.myapp.service.mapper.BookMapper;
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator;
import com.mycompany.myapp.service.dto.BookCriteria;
import com.mycompany.myapp.service.BookQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.mycompany.myapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link BookResource} REST controller.
 */
@SpringBootTest(classes = JhipsterApp.class)
public class BookResourceIT {

    private static final String DEFAULT_BOOK_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_BOOK_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final String DEFAULT_PUBLISHER = "AAAAAAAAAA";
    private static final String UPDATED_PUBLISHER = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_PUBLICATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PUBLICATION_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_PUBLICATION_DATE = LocalDate.ofEpochDay(-1L);

    private static final byte[] DEFAULT_DATA = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_DATA = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_DATA_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_DATA_CONTENT_TYPE = "image/png";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookQueryService bookQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restBookMockMvc;

    private Book book;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BookResource bookResource = new BookResource(bookService, bookQueryService);
        this.restBookMockMvc = MockMvcBuilders.standaloneSetup(bookResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createEntity(EntityManager em) {
        Book book = new Book()
            .bookTitle(DEFAULT_BOOK_TITLE)
            .author(DEFAULT_AUTHOR)
            .publisher(DEFAULT_PUBLISHER)
            .publicationDate(DEFAULT_PUBLICATION_DATE)
            .data(DEFAULT_DATA)
            .dataContentType(DEFAULT_DATA_CONTENT_TYPE);
        return book;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createUpdatedEntity(EntityManager em) {
        Book book = new Book()
            .bookTitle(UPDATED_BOOK_TITLE)
            .author(UPDATED_AUTHOR)
            .publisher(UPDATED_PUBLISHER)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .data(UPDATED_DATA)
            .dataContentType(UPDATED_DATA_CONTENT_TYPE);
        return book;
    }

    @BeforeEach
    public void initTest() {
        book = createEntity(em);
    }

    @Test
    @Transactional
    public void createBook() throws Exception {
        int databaseSizeBeforeCreate = bookRepository.findAll().size();

        // Create the Book
        BookDTO bookDTO = bookMapper.toDto(book);
        restBookMockMvc.perform(post("/api/books")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bookDTO)))
            .andExpect(status().isCreated());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getBookTitle()).isEqualTo(DEFAULT_BOOK_TITLE);
        assertThat(testBook.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testBook.getPublisher()).isEqualTo(DEFAULT_PUBLISHER);
        assertThat(testBook.getPublicationDate()).isEqualTo(DEFAULT_PUBLICATION_DATE);
        assertThat(testBook.getData()).isEqualTo(DEFAULT_DATA);
        assertThat(testBook.getDataContentType()).isEqualTo(DEFAULT_DATA_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void createBookWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bookRepository.findAll().size();

        // Create the Book with an existing ID
        book.setId(1L);
        BookDTO bookDTO = bookMapper.toDto(book);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc.perform(post("/api/books")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bookDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllBooks() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList
        restBookMockMvc.perform(get("/api/books?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.getId().intValue())))
            .andExpect(jsonPath("$.[*].bookTitle").value(hasItem(DEFAULT_BOOK_TITLE)))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].publisher").value(hasItem(DEFAULT_PUBLISHER)))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(DEFAULT_PUBLICATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].dataContentType").value(hasItem(DEFAULT_DATA_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].data").value(hasItem(Base64Utils.encodeToString(DEFAULT_DATA))));
    }
    
    @Test
    @Transactional
    public void getBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", book.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.getId().intValue()))
            .andExpect(jsonPath("$.bookTitle").value(DEFAULT_BOOK_TITLE))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.publisher").value(DEFAULT_PUBLISHER))
            .andExpect(jsonPath("$.publicationDate").value(DEFAULT_PUBLICATION_DATE.toString()))
            .andExpect(jsonPath("$.dataContentType").value(DEFAULT_DATA_CONTENT_TYPE))
            .andExpect(jsonPath("$.data").value(Base64Utils.encodeToString(DEFAULT_DATA)));
    }


    @Test
    @Transactional
    public void getBooksByIdFiltering() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        Long id = book.getId();

        defaultBookShouldBeFound("id.equals=" + id);
        defaultBookShouldNotBeFound("id.notEquals=" + id);

        defaultBookShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultBookShouldNotBeFound("id.greaterThan=" + id);

        defaultBookShouldBeFound("id.lessThanOrEqual=" + id);
        defaultBookShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllBooksByBookTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle equals to DEFAULT_BOOK_TITLE
        defaultBookShouldBeFound("bookTitle.equals=" + DEFAULT_BOOK_TITLE);

        // Get all the bookList where bookTitle equals to UPDATED_BOOK_TITLE
        defaultBookShouldNotBeFound("bookTitle.equals=" + UPDATED_BOOK_TITLE);
    }

    @Test
    @Transactional
    public void getAllBooksByBookTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle not equals to DEFAULT_BOOK_TITLE
        defaultBookShouldNotBeFound("bookTitle.notEquals=" + DEFAULT_BOOK_TITLE);

        // Get all the bookList where bookTitle not equals to UPDATED_BOOK_TITLE
        defaultBookShouldBeFound("bookTitle.notEquals=" + UPDATED_BOOK_TITLE);
    }

    @Test
    @Transactional
    public void getAllBooksByBookTitleIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle in DEFAULT_BOOK_TITLE or UPDATED_BOOK_TITLE
        defaultBookShouldBeFound("bookTitle.in=" + DEFAULT_BOOK_TITLE + "," + UPDATED_BOOK_TITLE);

        // Get all the bookList where bookTitle equals to UPDATED_BOOK_TITLE
        defaultBookShouldNotBeFound("bookTitle.in=" + UPDATED_BOOK_TITLE);
    }

    @Test
    @Transactional
    public void getAllBooksByBookTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle is not null
        defaultBookShouldBeFound("bookTitle.specified=true");

        // Get all the bookList where bookTitle is null
        defaultBookShouldNotBeFound("bookTitle.specified=false");
    }
                @Test
    @Transactional
    public void getAllBooksByBookTitleContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle contains DEFAULT_BOOK_TITLE
        defaultBookShouldBeFound("bookTitle.contains=" + DEFAULT_BOOK_TITLE);

        // Get all the bookList where bookTitle contains UPDATED_BOOK_TITLE
        defaultBookShouldNotBeFound("bookTitle.contains=" + UPDATED_BOOK_TITLE);
    }

    @Test
    @Transactional
    public void getAllBooksByBookTitleNotContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where bookTitle does not contain DEFAULT_BOOK_TITLE
        defaultBookShouldNotBeFound("bookTitle.doesNotContain=" + DEFAULT_BOOK_TITLE);

        // Get all the bookList where bookTitle does not contain UPDATED_BOOK_TITLE
        defaultBookShouldBeFound("bookTitle.doesNotContain=" + UPDATED_BOOK_TITLE);
    }


    @Test
    @Transactional
    public void getAllBooksByAuthorIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author equals to DEFAULT_AUTHOR
        defaultBookShouldBeFound("author.equals=" + DEFAULT_AUTHOR);

        // Get all the bookList where author equals to UPDATED_AUTHOR
        defaultBookShouldNotBeFound("author.equals=" + UPDATED_AUTHOR);
    }

    @Test
    @Transactional
    public void getAllBooksByAuthorIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author not equals to DEFAULT_AUTHOR
        defaultBookShouldNotBeFound("author.notEquals=" + DEFAULT_AUTHOR);

        // Get all the bookList where author not equals to UPDATED_AUTHOR
        defaultBookShouldBeFound("author.notEquals=" + UPDATED_AUTHOR);
    }

    @Test
    @Transactional
    public void getAllBooksByAuthorIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author in DEFAULT_AUTHOR or UPDATED_AUTHOR
        defaultBookShouldBeFound("author.in=" + DEFAULT_AUTHOR + "," + UPDATED_AUTHOR);

        // Get all the bookList where author equals to UPDATED_AUTHOR
        defaultBookShouldNotBeFound("author.in=" + UPDATED_AUTHOR);
    }

    @Test
    @Transactional
    public void getAllBooksByAuthorIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author is not null
        defaultBookShouldBeFound("author.specified=true");

        // Get all the bookList where author is null
        defaultBookShouldNotBeFound("author.specified=false");
    }
                @Test
    @Transactional
    public void getAllBooksByAuthorContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author contains DEFAULT_AUTHOR
        defaultBookShouldBeFound("author.contains=" + DEFAULT_AUTHOR);

        // Get all the bookList where author contains UPDATED_AUTHOR
        defaultBookShouldNotBeFound("author.contains=" + UPDATED_AUTHOR);
    }

    @Test
    @Transactional
    public void getAllBooksByAuthorNotContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where author does not contain DEFAULT_AUTHOR
        defaultBookShouldNotBeFound("author.doesNotContain=" + DEFAULT_AUTHOR);

        // Get all the bookList where author does not contain UPDATED_AUTHOR
        defaultBookShouldBeFound("author.doesNotContain=" + UPDATED_AUTHOR);
    }


    @Test
    @Transactional
    public void getAllBooksByPublisherIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher equals to DEFAULT_PUBLISHER
        defaultBookShouldBeFound("publisher.equals=" + DEFAULT_PUBLISHER);

        // Get all the bookList where publisher equals to UPDATED_PUBLISHER
        defaultBookShouldNotBeFound("publisher.equals=" + UPDATED_PUBLISHER);
    }

    @Test
    @Transactional
    public void getAllBooksByPublisherIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher not equals to DEFAULT_PUBLISHER
        defaultBookShouldNotBeFound("publisher.notEquals=" + DEFAULT_PUBLISHER);

        // Get all the bookList where publisher not equals to UPDATED_PUBLISHER
        defaultBookShouldBeFound("publisher.notEquals=" + UPDATED_PUBLISHER);
    }

    @Test
    @Transactional
    public void getAllBooksByPublisherIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher in DEFAULT_PUBLISHER or UPDATED_PUBLISHER
        defaultBookShouldBeFound("publisher.in=" + DEFAULT_PUBLISHER + "," + UPDATED_PUBLISHER);

        // Get all the bookList where publisher equals to UPDATED_PUBLISHER
        defaultBookShouldNotBeFound("publisher.in=" + UPDATED_PUBLISHER);
    }

    @Test
    @Transactional
    public void getAllBooksByPublisherIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher is not null
        defaultBookShouldBeFound("publisher.specified=true");

        // Get all the bookList where publisher is null
        defaultBookShouldNotBeFound("publisher.specified=false");
    }
                @Test
    @Transactional
    public void getAllBooksByPublisherContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher contains DEFAULT_PUBLISHER
        defaultBookShouldBeFound("publisher.contains=" + DEFAULT_PUBLISHER);

        // Get all the bookList where publisher contains UPDATED_PUBLISHER
        defaultBookShouldNotBeFound("publisher.contains=" + UPDATED_PUBLISHER);
    }

    @Test
    @Transactional
    public void getAllBooksByPublisherNotContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publisher does not contain DEFAULT_PUBLISHER
        defaultBookShouldNotBeFound("publisher.doesNotContain=" + DEFAULT_PUBLISHER);

        // Get all the bookList where publisher does not contain UPDATED_PUBLISHER
        defaultBookShouldBeFound("publisher.doesNotContain=" + UPDATED_PUBLISHER);
    }


    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate equals to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.equals=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.equals=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate not equals to DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.notEquals=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate not equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.notEquals=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate in DEFAULT_PUBLICATION_DATE or UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.in=" + DEFAULT_PUBLICATION_DATE + "," + UPDATED_PUBLICATION_DATE);

        // Get all the bookList where publicationDate equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.in=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is not null
        defaultBookShouldBeFound("publicationDate.specified=true");

        // Get all the bookList where publicationDate is null
        defaultBookShouldNotBeFound("publicationDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is greater than or equal to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.greaterThanOrEqual=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is greater than or equal to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.greaterThanOrEqual=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is less than or equal to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.lessThanOrEqual=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is less than or equal to SMALLER_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.lessThanOrEqual=" + SMALLER_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsLessThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is less than DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.lessThan=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is less than UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.lessThan=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void getAllBooksByPublicationDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is greater than DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.greaterThan=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is greater than SMALLER_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.greaterThan=" + SMALLER_PUBLICATION_DATE);
    }


    @Test
    @Transactional
    public void getAllBooksByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);
        Category category = CategoryResourceIT.createEntity(em);
        em.persist(category);
        em.flush();
        book.setCategory(category);
        bookRepository.saveAndFlush(book);
        Long categoryId = category.getId();

        // Get all the bookList where category equals to categoryId
        defaultBookShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the bookList where category equals to categoryId + 1
        defaultBookShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBookShouldBeFound(String filter) throws Exception {
        restBookMockMvc.perform(get("/api/books?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.getId().intValue())))
            .andExpect(jsonPath("$.[*].bookTitle").value(hasItem(DEFAULT_BOOK_TITLE)))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].publisher").value(hasItem(DEFAULT_PUBLISHER)))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(DEFAULT_PUBLICATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].dataContentType").value(hasItem(DEFAULT_DATA_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].data").value(hasItem(Base64Utils.encodeToString(DEFAULT_DATA))));

        // Check, that the count call also returns 1
        restBookMockMvc.perform(get("/api/books/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBookShouldNotBeFound(String filter) throws Exception {
        restBookMockMvc.perform(get("/api/books?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBookMockMvc.perform(get("/api/books/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingBook() throws Exception {
        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Update the book
        Book updatedBook = bookRepository.findById(book.getId()).get();
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook);
        updatedBook
            .bookTitle(UPDATED_BOOK_TITLE)
            .author(UPDATED_AUTHOR)
            .publisher(UPDATED_PUBLISHER)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .data(UPDATED_DATA)
            .dataContentType(UPDATED_DATA_CONTENT_TYPE);
        BookDTO bookDTO = bookMapper.toDto(updatedBook);

        restBookMockMvc.perform(put("/api/books")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bookDTO)))
            .andExpect(status().isOk());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getBookTitle()).isEqualTo(UPDATED_BOOK_TITLE);
        assertThat(testBook.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testBook.getPublisher()).isEqualTo(UPDATED_PUBLISHER);
        assertThat(testBook.getPublicationDate()).isEqualTo(UPDATED_PUBLICATION_DATE);
        assertThat(testBook.getData()).isEqualTo(UPDATED_DATA);
        assertThat(testBook.getDataContentType()).isEqualTo(UPDATED_DATA_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Create the Book
        BookDTO bookDTO = bookMapper.toDto(book);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(put("/api/books")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(bookDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeDelete = bookRepository.findAll().size();

        // Delete the book
        restBookMockMvc.perform(delete("/api/books/{id}", book.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
