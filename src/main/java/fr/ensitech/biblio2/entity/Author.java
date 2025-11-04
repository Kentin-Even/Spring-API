package fr.ensitech.biblio2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author", catalog = "biblio-database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "firstname", nullable = false, length = 48)
  private String firstName;

  @Column(name = "lastname", nullable = false, length = 48)
  private String lastName;

  @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
  private Set<Book> books = new HashSet<Book>();
}
