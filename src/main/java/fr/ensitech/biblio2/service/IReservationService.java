package fr.ensitech.biblio2.service;

import fr.ensitech.biblio2.entity.Reservation;

import java.util.List;

public interface IReservationService {

  Reservation reserveBook(long bookId, String email) throws Exception;

  List<Reservation> getUserReservations(String email) throws Exception;

  void cancelReservation(long reservationId) throws Exception;

  int getAvailableStock(long bookId) throws Exception;
}