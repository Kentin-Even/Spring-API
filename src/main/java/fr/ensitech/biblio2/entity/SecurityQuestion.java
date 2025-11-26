package fr.ensitech.biblio2.entity;

public enum SecurityQuestion {
  CHILDHOOD_CITY("Dans quelle ville avez-vous grandi ?"),
  FIRST_PET("Quel était le nom de votre premier animal de compagnie ?"),
  MOTHERS_MAIDEN_NAME("Quel est le nom de jeune fille de votre mère ?"),
  FAVORITE_TEACHER("Quel était le nom de votre professeur préféré ?"),
  FIRST_CAR("Quelle était la marque de votre première voiture ?");

  private final String questionText;

  SecurityQuestion(String questionText) {
    this.questionText = questionText;
  }

  public String getQuestionText() {
    return questionText;
  }
}