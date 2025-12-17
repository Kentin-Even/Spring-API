package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.repository.IAuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AuthorService - Coverage 100%")
class AuthorServiceTest {

  @Mock
  private IAuthorRepository authorRepository;

  @InjectMocks
  private AuthorService authorService;

  private Author author1;
  private Author author2;
  private Author author3;

  @BeforeEach
  void setUp() {
    author1 = new Author();
    author1.setId(1L);
    author1.setFirstName("Victor");
    author1.setLastName("Hugo");

    author2 = new Author();
    author2.setId(2L);
    author2.setFirstName("Victor");
    author2.setLastName("Duruy");

    author3 = new Author();
    author3.setId(3L);
    author3.setFirstName("Albert");
    author3.setLastName("Camus");
  }

  @Nested
  @DisplayName("getAuthors(firstName) - Recherche par prénom uniquement")
  class GetAuthorsByFirstName {

    @Test
    @DisplayName("Devrait retourner tous les auteurs avec le prénom donné")
    void shouldReturnAllAuthorsWithGivenFirstName() {
      // Given
      List<Author> expectedAuthors = Arrays.asList(author1, author2);
      when(authorRepository.findByFirstNameIgnoreCase("Victor")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Victor");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(author1, author2);

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Victor");
      verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("Devrait retourner un seul auteur si un seul correspond")
    void shouldReturnSingleAuthorWhenOnlyOneMatches() {
      // Given
      List<Author> expectedAuthors = Collections.singletonList(author3);
      when(authorRepository.findByFirstNameIgnoreCase("Albert")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Albert");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getFirstName()).isEqualTo("Albert");
      assertThat(result.get(0).getLastName()).isEqualTo("Camus");

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Albert");
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si aucun auteur ne correspond")
    void shouldReturnEmptyListWhenNoAuthorMatches() {
      // Given
      when(authorRepository.findByFirstNameIgnoreCase("Nonexistent")).thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("Nonexistent");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Nonexistent");
    }

    @Test
    @DisplayName("Devrait ignorer la casse lors de la recherche")
    void shouldIgnoreCaseWhenSearching() {
      // Given
      List<Author> expectedAuthors = Arrays.asList(author1, author2);
      when(authorRepository.findByFirstNameIgnoreCase("victor")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("victor");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("victor");
    }

    @Test
    @DisplayName("Devrait gérer les prénoms avec majuscules mixtes")
    void shouldHandleMixedCaseFirstNames() {
      // Given
      List<Author> expectedAuthors = Collections.singletonList(author1);
      when(authorRepository.findByFirstNameIgnoreCase("VicTor")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("VicTor");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isNotEmpty();

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("VicTor");
    }

    @Test
    @DisplayName("Devrait gérer les prénoms avec espaces")
    void shouldHandleFirstNamesWithSpaces() {
      // Given
      Author composedNameAuthor = new Author();
      composedNameAuthor.setId(4L);
      composedNameAuthor.setFirstName("Jean Pierre");
      composedNameAuthor.setLastName("Martin");

      when(authorRepository.findByFirstNameIgnoreCase("Jean Pierre"))
              .thenReturn(Collections.singletonList(composedNameAuthor));

      // When
      List<Author> result = authorService.getAuthors("Jean Pierre");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getFirstName()).isEqualTo("Jean Pierre");

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Jean Pierre");
    }

    @Test
    @DisplayName("Devrait gérer les prénoms avec caractères accentués")
    void shouldHandleAccentedFirstNames() {
      // Given
      Author accentedAuthor = new Author();
      accentedAuthor.setId(5L);
      accentedAuthor.setFirstName("François");
      accentedAuthor.setLastName("Mauriac");

      when(authorRepository.findByFirstNameIgnoreCase("François"))
              .thenReturn(Collections.singletonList(accentedAuthor));

      // When
      List<Author> result = authorService.getAuthors("François");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getFirstName()).isEqualTo("François");

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("François");
    }
  }

  @Nested
  @DisplayName("getAuthors(firstName, lastName) - Recherche par prénom et nom")
  class GetAuthorsByFirstNameAndLastName {

    @Test
    @DisplayName("Devrait retourner l'auteur correspondant au prénom et nom")
    void shouldReturnAuthorMatchingFirstNameAndLastName() {
      // Given
      List<Author> expectedAuthors = Collections.singletonList(author1);
      when(authorRepository.findAuthors("Victor", "Hugo")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Victor", "Hugo");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getFirstName()).isEqualTo("Victor");
      assertThat(result.get(0).getLastName()).isEqualTo("Hugo");

      verify(authorRepository, times(1)).findAuthors("Victor", "Hugo");
      verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("Devrait retourner plusieurs auteurs si plusieurs correspondent")
    void shouldReturnMultipleAuthorsWhenMultipleMatch() {
      // Given
      // Cas où deux auteurs ont le même nom complet (homonymes)
      Author author4 = new Author();
      author4.setId(4L);
      author4.setFirstName("Victor");
      author4.setLastName("Hugo");

      List<Author> expectedAuthors = Arrays.asList(author1, author4);
      when(authorRepository.findAuthors("Victor", "Hugo")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Victor", "Hugo");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(author1, author4);

      verify(authorRepository, times(1)).findAuthors("Victor", "Hugo");
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si aucun auteur ne correspond")
    void shouldReturnEmptyListWhenNoMatch() {
      // Given
      when(authorRepository.findAuthors("Unknown", "Author")).thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("Unknown", "Author");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(authorRepository, times(1)).findAuthors("Unknown", "Author");
    }

    @Test
    @DisplayName("Devrait distinguer les auteurs avec même prénom mais noms différents")
    void shouldDistinguishAuthorsSameFirstNameDifferentLastName() {
      // Given
      List<Author> expectedAuthors = Collections.singletonList(author2);
      when(authorRepository.findAuthors("Victor", "Duruy")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Victor", "Duruy");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getLastName()).isEqualTo("Duruy");
      assertThat(result.get(0).getLastName()).isNotEqualTo("Hugo");

      verify(authorRepository, times(1)).findAuthors("Victor", "Duruy");
    }

    @Test
    @DisplayName("Devrait gérer les noms avec casse mixte")
    void shouldHandleMixedCaseNames() {
      // Given
      List<Author> expectedAuthors = Collections.singletonList(author1);
      when(authorRepository.findAuthors("victor", "hugo")).thenReturn(expectedAuthors);

      // When
      List<Author> result = authorService.getAuthors("victor", "hugo");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isNotEmpty();

      verify(authorRepository, times(1)).findAuthors("victor", "hugo");
    }

    @Test
    @DisplayName("Devrait gérer les noms composés")
    void shouldHandleComposedLastNames() {
      // Given
      Author composedAuthor = new Author();
      composedAuthor.setId(6L);
      composedAuthor.setFirstName("Jean");
      composedAuthor.setLastName("de La Fontaine");

      when(authorRepository.findAuthors("Jean", "de La Fontaine"))
              .thenReturn(Collections.singletonList(composedAuthor));

      // When
      List<Author> result = authorService.getAuthors("Jean", "de La Fontaine");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getLastName()).isEqualTo("de La Fontaine");

      verify(authorRepository, times(1)).findAuthors("Jean", "de La Fontaine");
    }

    @Test
    @DisplayName("Devrait gérer les noms avec tirets")
    void shouldHandleHyphenatedNames() {
      // Given
      Author hyphenatedAuthor = new Author();
      hyphenatedAuthor.setId(7L);
      hyphenatedAuthor.setFirstName("Marie");
      hyphenatedAuthor.setLastName("Dupont-Martin");

      when(authorRepository.findAuthors("Marie", "Dupont-Martin"))
              .thenReturn(Collections.singletonList(hyphenatedAuthor));

      // When
      List<Author> result = authorService.getAuthors("Marie", "Dupont-Martin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getLastName()).isEqualTo("Dupont-Martin");

      verify(authorRepository, times(1)).findAuthors("Marie", "Dupont-Martin");
    }

    @Test
    @DisplayName("Devrait gérer les noms avec apostrophes")
    void shouldHandleNamesWithApostrophes() {
      // Given
      Author apostropheAuthor = new Author();
      apostropheAuthor.setId(8L);
      apostropheAuthor.setFirstName("Patrick");
      apostropheAuthor.setLastName("O'Brien");

      when(authorRepository.findAuthors("Patrick", "O'Brien"))
              .thenReturn(Collections.singletonList(apostropheAuthor));

      // When
      List<Author> result = authorService.getAuthors("Patrick", "O'Brien");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getLastName()).isEqualTo("O'Brien");

      verify(authorRepository, times(1)).findAuthors("Patrick", "O'Brien");
    }

    @Test
    @DisplayName("Devrait gérer les caractères accentués dans prénom et nom")
    void shouldHandleAccentedCharacters() {
      // Given
      Author accentedAuthor = new Author();
      accentedAuthor.setId(9L);
      accentedAuthor.setFirstName("François");
      accentedAuthor.setLastName("Bégaudeau");

      when(authorRepository.findAuthors("François", "Bégaudeau"))
              .thenReturn(Collections.singletonList(accentedAuthor));

      // When
      List<Author> result = authorService.getAuthors("François", "Bégaudeau");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getFirstName()).isEqualTo("François");
      assertThat(result.get(0).getLastName()).isEqualTo("Bégaudeau");

      verify(authorRepository, times(1)).findAuthors("François", "Bégaudeau");
    }
  }

  @Nested
  @DisplayName("Vérification des interactions avec le repository")
  class RepositoryInteractions {

    @Test
    @DisplayName("Devrait appeler findByFirstNameIgnoreCase exactement une fois")
    void shouldCallFindByFirstNameIgnoreCaseOnce() {
      // Given
      when(authorRepository.findByFirstNameIgnoreCase("Victor"))
              .thenReturn(Collections.singletonList(author1));

      // When
      authorService.getAuthors("Victor");

      // Then
      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Victor");
      verify(authorRepository, never()).findAuthors(anyString(), anyString());
      verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("Devrait appeler findAuthors exactement une fois")
    void shouldCallFindAuthorsOnce() {
      // Given
      when(authorRepository.findAuthors("Victor", "Hugo"))
              .thenReturn(Collections.singletonList(author1));

      // When
      authorService.getAuthors("Victor", "Hugo");

      // Then
      verify(authorRepository, times(1)).findAuthors("Victor", "Hugo");
      verify(authorRepository, never()).findByFirstNameIgnoreCase(anyString());
      verifyNoMoreInteractions(authorRepository);
    }

    @Test
    @DisplayName("Ne devrait jamais modifier les données retournées par le repository")
    void shouldNeverModifyRepositoryData() {
      // Given
      List<Author> originalList = Arrays.asList(author1, author2);
      when(authorRepository.findByFirstNameIgnoreCase("Victor")).thenReturn(originalList);

      // When
      List<Author> result = authorService.getAuthors("Victor");

      // Then
      assertThat(result).isSameAs(originalList);
      assertThat(result).hasSize(2);

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("Victor");
    }

    @Test
    @DisplayName("Devrait passer les paramètres exacts au repository sans modification")
    void shouldPassExactParametersToRepository() {
      // Given
      String firstName = "Victor";
      String lastName = "Hugo";

      when(authorRepository.findAuthors(firstName, lastName))
              .thenReturn(Collections.singletonList(author1));

      // When
      authorService.getAuthors(firstName, lastName);

      // Then
      verify(authorRepository, times(1)).findAuthors(firstName, lastName);
      verify(authorRepository, times(1)).findAuthors(eq("Victor"), eq("Hugo"));
    }
  }

  @Nested
  @DisplayName("Cas limites et edge cases")
  class EdgeCases {

    @Test
    @DisplayName("Devrait gérer une chaîne vide pour le prénom")
    void shouldHandleEmptyFirstName() {
      // Given
      when(authorRepository.findByFirstNameIgnoreCase("")).thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase("");
    }

    @Test
    @DisplayName("Devrait gérer des chaînes vides pour prénom et nom")
    void shouldHandleEmptyFirstNameAndLastName() {
      // Given
      when(authorRepository.findAuthors("", "")).thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("", "");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(authorRepository, times(1)).findAuthors("", "");
    }

    @Test
    @DisplayName("Devrait gérer des prénoms très longs")
    void shouldHandleVeryLongFirstNames() {
      // Given
      String longFirstName = "A".repeat(255);
      when(authorRepository.findByFirstNameIgnoreCase(longFirstName)).thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors(longFirstName);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(authorRepository, times(1)).findByFirstNameIgnoreCase(longFirstName);
    }

    @Test
    @DisplayName("Devrait gérer des caractères spéciaux dans les noms")
    void shouldHandleSpecialCharactersInNames() {
      // Given
      Author specialAuthor = new Author();
      specialAuthor.setId(10L);
      specialAuthor.setFirstName("Øyvind");
      specialAuthor.setLastName("Müller");

      when(authorRepository.findAuthors("Øyvind", "Müller"))
              .thenReturn(Collections.singletonList(specialAuthor));

      // When
      List<Author> result = authorService.getAuthors("Øyvind", "Müller");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);

      verify(authorRepository, times(1)).findAuthors("Øyvind", "Müller");
    }

    @Test
    @DisplayName("Devrait gérer des noms avec chiffres")
    void shouldHandleNamesWithNumbers() {
      // Given
      Author numberAuthor = new Author();
      numberAuthor.setId(11L);
      numberAuthor.setFirstName("Jean");
      numberAuthor.setLastName("Martin2");

      when(authorRepository.findAuthors("Jean", "Martin2"))
              .thenReturn(Collections.singletonList(numberAuthor));

      // When
      List<Author> result = authorService.getAuthors("Jean", "Martin2");

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);

      verify(authorRepository, times(1)).findAuthors("Jean", "Martin2");
    }
  }

  @Nested
  @DisplayName("Cohérence des résultats")
  class ResultConsistency {

    @Test
    @DisplayName("Devrait toujours retourner une liste non-null")
    void shouldAlwaysReturnNonNullList() {
      // Given
      when(authorRepository.findByFirstNameIgnoreCase(anyString()))
              .thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("AnyName");

      // Then
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Devrait toujours retourner une liste non-null pour la recherche double")
    void shouldAlwaysReturnNonNullListForDoubleSearch() {
      // Given
      when(authorRepository.findAuthors(anyString(), anyString()))
              .thenReturn(Collections.emptyList());

      // When
      List<Author> result = authorService.getAuthors("Any", "Name");

      // Then
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Devrait retourner les auteurs dans l'ordre du repository")
    void shouldReturnAuthorsInRepositoryOrder() {
      // Given
      List<Author> orderedAuthors = Arrays.asList(author3, author1, author2);
      when(authorRepository.findByFirstNameIgnoreCase("Victor")).thenReturn(orderedAuthors);

      // When
      List<Author> result = authorService.getAuthors("Victor");

      // Then
      assertThat(result).containsExactly(author3, author1, author2);
    }
  }
}