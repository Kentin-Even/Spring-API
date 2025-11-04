package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Author;
import fr.ensitech.biblio2.repository.IAuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService implements IAuthorService {

  @Autowired
  private IAuthorRepository authorRepository;

  @Override
  public List<Author> getAuthors(String firstName) {
    return authorRepository.findByFirstNameIgnoreCase(firstName);
  }

  @Override
  public List<Author> getAuthors(String firstName, String lastName) {
    return authorRepository.findAuthors(firstName, lastName);
  }
}
