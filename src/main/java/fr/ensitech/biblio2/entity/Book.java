package fr.ensitech.biblio2.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="book", catalog = "biblio-database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", nullable = false, length = 250)
  private String description;

  @Column(name = "published", nullable = false)
  private boolean published;

  @Column(name = "editor", nullable = false, length = 100)
  private String editor;

  @Column(name = "publicationDate", nullable = false)
  @Temporal(TemporalType.DATE)
  private Date publicationDate;

  @Column(name = "isbn", nullable = false, length = 13, unique = true)
  private String isbn;

  @Column(name = "nbPage", nullable = false)
  private short nbPage;

  @Column(name = "category", nullable = false, length = 48)
  private String category;

  @Column(name = "language", nullable = false, length = 5)
  private String language;

  @Column(name = "stock", nullable = false)
  private int stock = 0;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(name = "author-book",
          joinColumns = @JoinColumn(name = "author-id"),
          inverseJoinColumns = @JoinColumn(name = "book-id")
  )
  private Set<Author> authors = new HashSet<Author>();

  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Reservation> reservations = new HashSet<>();
}