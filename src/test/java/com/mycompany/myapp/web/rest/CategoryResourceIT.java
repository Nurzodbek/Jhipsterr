package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.JhipsterApp;
import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.service.CategoryService;
import com.mycompany.myapp.service.dto.CategoryDTO;
import com.mycompany.myapp.service.mapper.CategoryMapper;
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator;
import com.mycompany.myapp.service.dto.CategoryCriteria;
import com.mycompany.myapp.service.CategoryQueryService;

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
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static com.mycompany.myapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CategoryResource} REST controller.
 */
@SpringBootTest(classes = JhipsterApp.class)
public class CategoryResourceIT {

    private static final String DEFAULT_CATEGORY_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryQueryService categoryQueryService;

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

    private MockMvc restCategoryMockMvc;

    private Category category;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CategoryResource categoryResource = new CategoryResource(categoryService, categoryQueryService);
        this.restCategoryMockMvc = MockMvcBuilders.standaloneSetup(categoryResource)
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
    public static Category createEntity(EntityManager em) {
        Category category = new Category()
            .categoryDescription(DEFAULT_CATEGORY_DESCRIPTION);
        return category;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createUpdatedEntity(EntityManager em) {
        Category category = new Category()
            .categoryDescription(UPDATED_CATEGORY_DESCRIPTION);
        return category;
    }

    @BeforeEach
    public void initTest() {
        category = createEntity(em);
    }

    @Test
    @Transactional
    public void createCategory() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);
        restCategoryMockMvc.perform(post("/api/categories")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isCreated());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate + 1);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getCategoryDescription()).isEqualTo(DEFAULT_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void createCategoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category with an existing ID
        category.setId(1L);
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc.perform(post("/api/categories")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCategories() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList
        restCategoryMockMvc.perform(get("/api/categories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].categoryDescription").value(hasItem(DEFAULT_CATEGORY_DESCRIPTION)));
    }
    
    @Test
    @Transactional
    public void getCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the category
        restCategoryMockMvc.perform(get("/api/categories/{id}", category.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(category.getId().intValue()))
            .andExpect(jsonPath("$.categoryDescription").value(DEFAULT_CATEGORY_DESCRIPTION));
    }


    @Test
    @Transactional
    public void getCategoriesByIdFiltering() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        Long id = category.getId();

        defaultCategoryShouldBeFound("id.equals=" + id);
        defaultCategoryShouldNotBeFound("id.notEquals=" + id);

        defaultCategoryShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCategoryShouldNotBeFound("id.greaterThan=" + id);

        defaultCategoryShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCategoryShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription equals to DEFAULT_CATEGORY_DESCRIPTION
        defaultCategoryShouldBeFound("categoryDescription.equals=" + DEFAULT_CATEGORY_DESCRIPTION);

        // Get all the categoryList where categoryDescription equals to UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldNotBeFound("categoryDescription.equals=" + UPDATED_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription not equals to DEFAULT_CATEGORY_DESCRIPTION
        defaultCategoryShouldNotBeFound("categoryDescription.notEquals=" + DEFAULT_CATEGORY_DESCRIPTION);

        // Get all the categoryList where categoryDescription not equals to UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldBeFound("categoryDescription.notEquals=" + UPDATED_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription in DEFAULT_CATEGORY_DESCRIPTION or UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldBeFound("categoryDescription.in=" + DEFAULT_CATEGORY_DESCRIPTION + "," + UPDATED_CATEGORY_DESCRIPTION);

        // Get all the categoryList where categoryDescription equals to UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldNotBeFound("categoryDescription.in=" + UPDATED_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription is not null
        defaultCategoryShouldBeFound("categoryDescription.specified=true");

        // Get all the categoryList where categoryDescription is null
        defaultCategoryShouldNotBeFound("categoryDescription.specified=false");
    }
                @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription contains DEFAULT_CATEGORY_DESCRIPTION
        defaultCategoryShouldBeFound("categoryDescription.contains=" + DEFAULT_CATEGORY_DESCRIPTION);

        // Get all the categoryList where categoryDescription contains UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldNotBeFound("categoryDescription.contains=" + UPDATED_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCategoryDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryDescription does not contain DEFAULT_CATEGORY_DESCRIPTION
        defaultCategoryShouldNotBeFound("categoryDescription.doesNotContain=" + DEFAULT_CATEGORY_DESCRIPTION);

        // Get all the categoryList where categoryDescription does not contain UPDATED_CATEGORY_DESCRIPTION
        defaultCategoryShouldBeFound("categoryDescription.doesNotContain=" + UPDATED_CATEGORY_DESCRIPTION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCategoryShouldBeFound(String filter) throws Exception {
        restCategoryMockMvc.perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].categoryDescription").value(hasItem(DEFAULT_CATEGORY_DESCRIPTION)));

        // Check, that the count call also returns 1
        restCategoryMockMvc.perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCategoryShouldNotBeFound(String filter) throws Exception {
        restCategoryMockMvc.perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCategoryMockMvc.perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingCategory() throws Exception {
        // Get the category
        restCategoryMockMvc.perform(get("/api/categories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category
        Category updatedCategory = categoryRepository.findById(category.getId()).get();
        // Disconnect from session so that the updates on updatedCategory are not directly saved in db
        em.detach(updatedCategory);
        updatedCategory
            .categoryDescription(UPDATED_CATEGORY_DESCRIPTION);
        CategoryDTO categoryDTO = categoryMapper.toDto(updatedCategory);

        restCategoryMockMvc.perform(put("/api/categories")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isOk());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categoryList.get(categoryList.size() - 1);
        assertThat(testCategory.getCategoryDescription()).isEqualTo(UPDATED_CATEGORY_DESCRIPTION);
    }

    @Test
    @Transactional
    public void updateNonExistingCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(put("/api/categories")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeDelete = categoryRepository.findAll().size();

        // Delete the category
        restCategoryMockMvc.perform(delete("/api/categories/{id}", category.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
