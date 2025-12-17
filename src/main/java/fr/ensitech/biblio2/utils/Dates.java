package fr.ensitech.biblio2.utils;

import java.text.SimpleDateFormat;

public final class Dates {

  private Dates() {
  }

  private static final String formatDate = "dd/MM/yyyy";

  public static final java.util.Date convertStringToDate(String dateStr) throws Exception {
    return (new SimpleDateFormat(formatDate)).parse(dateStr);
  }

  public static final String convertDateToString(java.util.Date date) throws Exception {
    return (new SimpleDateFormat(formatDate)).format(date);
  }

}
