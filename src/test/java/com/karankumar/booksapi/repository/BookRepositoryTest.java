/*
 * Copyright (C) 2020  Karan Kumar
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.karankumar.booksapi.repository;

import com.karankumar.booksapi.annotations.DataJpaIntegrationTest;
import com.karankumar.booksapi.model.Author;
import com.karankumar.booksapi.model.Book;
import com.karankumar.booksapi.model.BookFormat;
import com.karankumar.booksapi.model.BookGenre;
import com.karankumar.booksapi.model.BookSeries;
import com.karankumar.booksapi.model.BookSeriesMapping;
import com.karankumar.booksapi.model.Language;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static com.karankumar.booksapi.repository.RepositoryTestUtils.createBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaIntegrationTest
@DisplayName("BookRepository should")
class BookRepositoryTest {
    private static final String ISBN = "978-3-16-148410-0";
    private static final String TITLE = "Harry Potter";
    private static final String AUTHOR1_FULL_NAME = "Kevlin Henney";

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookSeriesRepository bookSeriesRepository;
    private final BookSeriesMappingRepository bookSeriesMappingRepository;
    private final String harryPotter1Title = "Harry Potter and the Philosopher's stone";
    private final String harryPotterSeriesName = "Harry Potter Series";

    private Author author1;
    private Author author2;

    @Autowired
    BookRepositoryTest(BookRepository bookRepository, AuthorRepository authorRepository,
                       BookSeriesRepository bookSeriesRepository,
                       BookSeriesMappingRepository bookSeriesMappingRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookSeriesRepository = bookSeriesRepository;
        this.bookSeriesMappingRepository = bookSeriesMappingRepository;
    }
    @BeforeEach
    void setUp() {
        bookSeriesMappingRepository.deleteAll();
        authorRepository.deleteAll();
        bookRepository.deleteAll();
        bookSeriesRepository.deleteAll();
    }

    @Test
    @DisplayName("find saved books")
    void findSavedBooks() {
        // given
        Book book = createBook();
        bookRepository.save(book);

        // when
        List<Book> result = bookRepository.findAllBooks();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isOne();
            softly.assertThat(result).containsExactlyInAnyOrder(book);
        });
    }

    @Test
    @DisplayName("find book with isbn")
    void findBookByIsbn() {
        // given
        Book book = createBookWithIsbn13();
        bookRepository.save(book);

        // when
        Book result = bookRepository.findBookByIsbn13(ISBN);

        // then
        assertThat(result).isEqualTo(book);
    }
  
    private Book createBookWithIsbn13() {
        Book book = new Book(
                "Game of APIs",
                Language.ENGLISH,
                "",
                BookGenre.SATIRE,
                BookFormat.HARDCOVER
        );
        book.setIsbn13(ISBN);
        return book;
    }
    
    @Test
    @DisplayName("find by author")
    void findByAuthor() {
        // given
        Book book = createBookWithIsbn13();
        bookRepository.save(book);
        String authorName = "Kevlin Henney";
        Author author = new Author(authorName, new HashSet<>());
        author.addBook(book);
        authorRepository.save(author);

        // when
        List<Book> result = bookRepository.findByAuthor(authorName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isOne();
            softly.assertThat(result.get(0)).isEqualTo(book);
        });
    }

    @Test
      void findBookByTitle() {
        // given
        Book book = new Book(
                TITLE,
                Language.ENGLISH,
                "",
                BookGenre.ART,
                BookFormat.PAPERBACK
        );
        bookRepository.save(book);

        // when
        Book result = bookRepository.findByTitleIgnoreCase(TITLE);

        // then
        assertThat(result).isEqualTo(book);
    }

    @Test
    @DisplayName("find book by title case insensitive")
    void findBookByTitleCaseInsensitive() {
        // given
        Book book = new Book(
                TITLE,
                Language.ENGLISH,
                "",
                BookGenre.MYSTERY,
                BookFormat.PAPERBACK
        );
        bookRepository.save(book);

        // when
        Book result = bookRepository.findByTitleIgnoreCase(TITLE.toLowerCase());

        // then
        assertThat(result).isEqualTo(book);
    }
    
    private void saveAllAuthors(Author... authors) {
        Arrays.stream(authors).forEach(authorRepository::save);
    }

    @Test
    @DisplayName("find all Book Series for a Book")
    void findBookSeriesForBook(){
        List<BookSeries> assertion = addBookToSeries();
        Book book = bookRepository.findByTitleIgnoreCase(harryPotter1Title);

        List<BookSeries> result = bookRepository.getAllBookSeriesForBook(book);

        assertThat(result).containsExactlyInAnyOrderElementsOf(assertion);
    }

    @Test
    @DisplayName("find book position in Book Series")
    void findBookPositionInSeries() {
        // Get book and book series from repo
        List <BookSeries> allSeries = addBookToSeries();
        Book book = bookRepository.findByTitleIgnoreCase(harryPotter1Title);
        BookSeries assertion = null;
        for (BookSeries series : allSeries) {
            if (series.getSeriesName().equals(harryPotterSeriesName)) {
                assertion = series;
                break;
            }
        }

        assertThat(bookRepository.getBookPositionInBookSeries(book, assertion)).isEqualTo(1);
    }

    private List<BookSeries> addBookToSeries() {

        Book book1 = createSeriesBook(harryPotter1Title);
        bookRepository.save(book1);

        Book book2 = createSeriesBook("Harry Potter and the Chamber of Secrets");
        bookRepository.save(book2);

        BookSeries bookSeries1 = new BookSeries(harryPotterSeriesName);
        bookSeriesRepository.save(bookSeries1);

        BookSeries bookSeries2 = new BookSeries("J.K. Rowling Specials");
        bookSeriesRepository.save(bookSeries2);

        bookSeriesMappingRepository.save(new BookSeriesMapping(bookSeries1, book1, 1));
        bookSeriesMappingRepository.save(new BookSeriesMapping(bookSeries1, book2, 2));

        bookSeriesMappingRepository.save(new BookSeriesMapping(bookSeries2, book1, 1));
        bookSeriesMappingRepository.save(new BookSeriesMapping(bookSeries2, book2, 2));

        return List.of(bookSeries1, bookSeries2);
    }

    private Book createSeriesBook(String title) {
        return new Book(
                title,
                Language.ENGLISH,
                "Sample blurb value",
                BookGenre.FANTASY,
                BookFormat.PAPERBACK
        );
    }
}
