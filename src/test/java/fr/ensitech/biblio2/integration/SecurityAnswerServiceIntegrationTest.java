package fr.ensitech.biblio2.integration;

import fr.ensitech.biblio2.service.SecurityAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityAnswerServiceIntegrationTest {

  @Autowired
  private SecurityAnswerService securityAnswerService;

  @BeforeEach
  void setUp() {
    // Aucune configuration spécifique nécessaire
  }

  @Test
  @DisplayName("Hashage d'une réponse de sécurité simple")
  void shouldHashSecurityAnswer() {
    //GIVEN
    String answer = "Paris";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    assertThat(hashedAnswer).isNotEqualTo(answer);
  }

  @Test
  @DisplayName("Hashage produit toujours le même résultat pour la même entrée")
  void shouldProduceSameHashForSameInput() {
    //GIVEN
    String answer = "Médor";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  @DisplayName("Hashage produit des résultats différents pour des entrées différentes")
  void shouldProduceDifferentHashesForDifferentInputs() {
    //GIVEN
    String answer1 = "Paris";
    String answer2 = "Lyon";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);

    //THEN
    assertThat(hash1).isNotEqualTo(hash2);
  }

  @Test
  @DisplayName("Hashage ignore la casse")
  void shouldIgnoreCaseWhenHashing() {
    //GIVEN
    String answer1 = "Paris";
    String answer2 = "PARIS";
    String answer3 = "paris";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash2).isEqualTo(hash3);
  }

  @Test
  @DisplayName("Hashage supprime les espaces en début et fin")
  void shouldTrimWhitespaceWhenHashing() {
    //GIVEN
    String answer1 = "Paris";
    String answer2 = "  Paris  ";
    String answer3 = "\tParis\n";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash2).isEqualTo(hash3);
  }

  @Test
  @DisplayName("Hashage combine trim et ignore case")
  void shouldTrimAndIgnoreCaseTogether() {
    //GIVEN
    String answer1 = "paris";
    String answer2 = "  PARIS  ";
    String answer3 = "\tPaRiS\n";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash2).isEqualTo(hash3);
  }

  @Test
  @DisplayName("Hashage préserve les espaces internes")
  void shouldPreserveInternalSpaces() {
    //GIVEN
    String answer1 = "Jean Dupont";
    String answer2 = "JeanDupont";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);

    //THEN
    assertThat(hash1).isNotEqualTo(hash2);
  }

  @Test
  @DisplayName("Hashage gère les caractères accentués")
  void shouldHandleAccentedCharacters() {
    //GIVEN
    String answer1 = "Médor";
    String answer2 = "Medor";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);

    //THEN
    assertThat(hash1).isNotEqualTo(hash2);
  }

  @Test
  @DisplayName("Hashage gère les caractères spéciaux")
  void shouldHandleSpecialCharacters() {
    //GIVEN
    String answer = "L'Étoile-du-Nord";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
  }

  @Test
  @DisplayName("Hashage gère les chaînes vides")
  void shouldHandleEmptyString() {
    //GIVEN
    String answer = "";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
  }

  @Test
  @DisplayName("Hashage gère les chaînes avec uniquement des espaces")
  void shouldHandleWhitespaceOnlyString() {
    //GIVEN
    String answer = "   ";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    // Après trim, c'est une chaîne vide
    assertThat(hashedAnswer).isEqualTo(securityAnswerService.hashSecurityAnswer(""));
  }

  @Test
  @DisplayName("Hashage produit un résultat encodé en Base64")
  void shouldProduceBase64EncodedHash() {
    //GIVEN
    String answer = "Paris";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    // Base64 ne contient que des caractères alphanumériques, +, /, et =
    assertThat(hashedAnswer).matches("^[A-Za-z0-9+/=]+$");
  }

  @Test
  @DisplayName("Vérification d'une réponse correcte")
  void shouldVerifyCorrectAnswer() {
    //GIVEN
    String plainAnswer = "Paris";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(plainAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer(plainAnswer, hashedAnswer);

    //THEN
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Vérification d'une réponse incorrecte")
  void shouldRejectIncorrectAnswer() {
    //GIVEN
    String correctAnswer = "Paris";
    String wrongAnswer = "Lyon";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(correctAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer(wrongAnswer, hashedAnswer);

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Vérification ignore la casse")
  void shouldVerifyIgnoringCase() {
    //GIVEN
    String originalAnswer = "Paris";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid1 = securityAnswerService.verifySecurityAnswer("PARIS", hashedAnswer);
    boolean isValid2 = securityAnswerService.verifySecurityAnswer("paris", hashedAnswer);
    boolean isValid3 = securityAnswerService.verifySecurityAnswer("PaRiS", hashedAnswer);

    //THEN
    assertThat(isValid1).isTrue();
    assertThat(isValid2).isTrue();
    assertThat(isValid3).isTrue();
  }

  @Test
  @DisplayName("Vérification ignore les espaces en début et fin")
  void shouldVerifyTrimmingWhitespace() {
    //GIVEN
    String originalAnswer = "Paris";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid1 = securityAnswerService.verifySecurityAnswer("  Paris  ", hashedAnswer);
    boolean isValid2 = securityAnswerService.verifySecurityAnswer("\tParis\n", hashedAnswer);

    //THEN
    assertThat(isValid1).isTrue();
    assertThat(isValid2).isTrue();
  }

  @Test
  @DisplayName("Vérification combine trim et ignore case")
  void shouldVerifyWithTrimAndIgnoreCase() {
    //GIVEN
    String originalAnswer = "paris";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer("  PARIS  ", hashedAnswer);

    //THEN
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Vérification échoue avec des espaces internes différents")
  void shouldRejectDifferentInternalSpaces() {
    //GIVEN
    String originalAnswer = "Jean Dupont";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer("JeanDupont", hashedAnswer);

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Vérification échoue avec des accents différents")
  void shouldRejectDifferentAccents() {
    //GIVEN
    String originalAnswer = "Médor";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer("Medor", hashedAnswer);

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Vérification d'une chaîne vide")
  void shouldVerifyEmptyString() {
    //GIVEN
    String originalAnswer = "";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer("", hashedAnswer);

    //THEN
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Vérification avec hash invalide")
  void shouldRejectInvalidHash() {
    //GIVEN
    String plainAnswer = "Paris";
    String invalidHash = "InvalidHashValue123";

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer(plainAnswer, invalidHash);

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Hashage de réponses longues")
  void shouldHashLongAnswers() {
    //GIVEN
    String longAnswer = "Ceci est une très longue réponse de sécurité qui contient beaucoup de mots et de caractères pour tester le hashage";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(longAnswer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    
    // Vérifier que la vérification fonctionne
    boolean isValid = securityAnswerService.verifySecurityAnswer(longAnswer, hashedAnswer);
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Hashage de nombres")
  void shouldHashNumericAnswers() {
    //GIVEN
    String numericAnswer = "12345";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(numericAnswer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    
    boolean isValid = securityAnswerService.verifySecurityAnswer("12345", hashedAnswer);
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Hashage avec caractères Unicode")
  void shouldHashUnicodeCharacters() {
    //GIVEN
    String unicodeAnswer = "東京";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(unicodeAnswer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    
    boolean isValid = securityAnswerService.verifySecurityAnswer("東京", hashedAnswer);
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Hashage avec émojis")
  void shouldHashEmojis() {
    //GIVEN
    String emojiAnswer = "🐶";

    //WHEN
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(emojiAnswer);

    //THEN
    assertThat(hashedAnswer).isNotNull();
    assertThat(hashedAnswer).isNotEmpty();
    
    boolean isValid = securityAnswerService.verifySecurityAnswer("🐶", hashedAnswer);
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Vérification de plusieurs réponses différentes")
  void shouldVerifyMultipleDifferentAnswers() {
    //GIVEN
    String answer1 = "Paris";
    String answer2 = "Médor";
    String answer3 = "Dupont";
    
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

    //WHEN & THEN
    assertThat(securityAnswerService.verifySecurityAnswer(answer1, hash1)).isTrue();
    assertThat(securityAnswerService.verifySecurityAnswer(answer2, hash2)).isTrue();
    assertThat(securityAnswerService.verifySecurityAnswer(answer3, hash3)).isTrue();
    
    // Vérifier les cross-validations échouent
    assertThat(securityAnswerService.verifySecurityAnswer(answer1, hash2)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer(answer2, hash3)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer(answer3, hash1)).isFalse();
  }

  @Test
  @DisplayName("Hashage SHA-256 produit une longueur fixe")
  void shouldProduceFixedLengthHash() {
    //GIVEN
    String shortAnswer = "A";
    String longAnswer = "Ceci est une très longue réponse avec beaucoup de caractères";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(shortAnswer);
    String hash2 = securityAnswerService.hashSecurityAnswer(longAnswer);

    //THEN
    // SHA-256 produit 256 bits = 32 bytes, encodé en Base64 = 44 caractères
    assertThat(hash1).hasSize(44);
    assertThat(hash2).hasSize(44);
  }

  @Test
  @DisplayName("Vérification avec réponse sensiblement similaire échoue")
  void shouldRejectSimilarButNotIdenticalAnswers() {
    //GIVEN
    String originalAnswer = "Paris";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(originalAnswer);

    //WHEN & THEN
    assertThat(securityAnswerService.verifySecurityAnswer("Paris1", hashedAnswer)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer("Pari", hashedAnswer)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer("Pariss", hashedAnswer)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer("Pariz", hashedAnswer)).isFalse();
  }

  @Test
  @DisplayName("Hashage est déterministe sur plusieurs appels")
  void shouldBeDeterministicAcrossMultipleCalls() {
    //GIVEN
    String answer = "Réponse de test";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash2).isEqualTo(hash3);
  }

  @Test
  @DisplayName("Vérification avec hash null retourne false")
  void shouldReturnFalseForNullHash() {
    //GIVEN
    String plainAnswer = "Paris";

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer(plainAnswer, null);

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Vérification avec hash vide retourne false")
  void shouldReturnFalseForEmptyHash() {
    //GIVEN
    String plainAnswer = "Paris";

    //WHEN
    boolean isValid = securityAnswerService.verifySecurityAnswer(plainAnswer, "");

    //THEN
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Scénario complet : hashage puis vérification multiple")
  void shouldHandleCompleteScenario() {
    //GIVEN
    String correctAnswer = "Nom de jeune fille de ma mère";
    String hashedAnswer = securityAnswerService.hashSecurityAnswer(correctAnswer);

    //WHEN & THEN - Tentatives avec différentes variations
    assertThat(securityAnswerService.verifySecurityAnswer(correctAnswer, hashedAnswer)).isTrue();
    assertThat(securityAnswerService.verifySecurityAnswer("NOM DE JEUNE FILLE DE MA MÈRE", hashedAnswer)).isTrue();
    assertThat(securityAnswerService.verifySecurityAnswer("  nom de jeune fille de ma mère  ", hashedAnswer)).isTrue();
    
    // Tentatives incorrectes
    assertThat(securityAnswerService.verifySecurityAnswer("Nom de jeune fille", hashedAnswer)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer("Autre réponse", hashedAnswer)).isFalse();
    assertThat(securityAnswerService.verifySecurityAnswer("", hashedAnswer)).isFalse();
  }

  @Test
  @DisplayName("Hashage avec tabulations et retours à la ligne")
  void shouldHandleTabsAndNewlines() {
    //GIVEN
    String answer1 = "Paris";
    String answer2 = "\t\tParis\n\n";
    String answer3 = "\r\nParis\r\n";

    //WHEN
    String hash1 = securityAnswerService.hashSecurityAnswer(answer1);
    String hash2 = securityAnswerService.hashSecurityAnswer(answer2);
    String hash3 = securityAnswerService.hashSecurityAnswer(answer3);

    //THEN
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash2).isEqualTo(hash3);
  }

  @Test
  @DisplayName("Résistance aux attaques par force brute - hashes différents")
  void shouldProduceDifferentHashesForSimilarInputs() {
    //GIVEN
    String[] similarAnswers = {"Paris", "Paris1", "Paris2", "Parisa", "Parisb"};

    //WHEN
    String[] hashes = new String[similarAnswers.length];
    for (int i = 0; i < similarAnswers.length; i++) {
      hashes[i] = securityAnswerService.hashSecurityAnswer(similarAnswers[i]);
    }

    //THEN
    // Tous les hashes doivent être différents
    for (int i = 0; i < hashes.length; i++) {
      for (int j = i + 1; j < hashes.length; j++) {
        assertThat(hashes[i]).isNotEqualTo(hashes[j]);
      }
    }
  }
}
