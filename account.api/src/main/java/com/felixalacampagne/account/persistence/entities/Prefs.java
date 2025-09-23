package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;



/**
 * The persistent class for the "prefs" database table.
 *
 * To map Access CSV to H2 CSV columns
 * prefs_name;prefs_text;prefs_numeric
 * name      ;text      ;numeric
 */
@Entity
@Table(name="prefs")
//@NamedQuery(name="Pref.findAll", query="SELECT p FROM Pref p")
public class Prefs implements Serializable {
   private static final long serialVersionUID = 1L;

   @Id
   // @GeneratedValue(strategy=GenerationType.IDENTITY)
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prefs_seq_gen")
   @SequenceGenerator(initialValue = 1, name = "prefs_seq_gen", sequenceName = "prefs_seq", allocationSize = 1)
   @Column(name="id")
   private Long id;

   @Column(name="name", unique=true, nullable=false)
   private String prefsName;

   @Column(name="numeric")
   private int prefsNumeric;

   @Column(name="text")
   private String prefsText;

   public Prefs() {
   }

   public String getPrefsName() {
      return this.prefsName;
   }

   public void setPrefsName(String prefsName) {
      this.prefsName = prefsName;
   }


   public int getPrefsNumeric() {
      return this.prefsNumeric;
   }

   public void setPrefsNumeric(int prefsNumeric) {
      this.prefsNumeric = prefsNumeric;
   }


   public String getPrefsText() {
      return this.prefsText;
   }

   public void setPrefsText(String prefsText) {
      this.prefsText = prefsText;
   }

}
