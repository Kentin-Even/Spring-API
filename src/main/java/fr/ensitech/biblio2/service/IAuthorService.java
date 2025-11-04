package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;

import java.util.List;

public interface IAuthorService {

  List<Author> getAuthors(String firstName);
  List<Author> getAuthors(String firstName, String lastName);
}
