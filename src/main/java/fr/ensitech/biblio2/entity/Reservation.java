package fr.ensitech.biblio2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "reservation", catalog = "biblio-database")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Column(name = "reservation_date", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date reservationDate;

  @Column(name = "status", nullable = false, length = 20)
  private String status = "ACTIVE"; // ACTIVE, RETURNED, CANCELLED
}