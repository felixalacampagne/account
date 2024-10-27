package com.felixalacampagne.account.persistence.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



/**
 * The persistent class for the "prefs" database table.
 *
 */
@Entity
@Table(name="prefs")
//@NamedQuery(name="Pref.findAll", query="SELECT p FROM Pref p")
public class Prefs implements Serializable {
   private static final long serialVersionUID = 1L;
   private String prefsName;
   private int prefsNumeric;
   private String prefsText;

   public Prefs() {
   }


   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(name="prefs_name", unique=true, nullable=false, length=80)
   public String getPrefsName() {
      return this.prefsName;
   }

   public void setPrefsName(String prefsName) {
      this.prefsName = prefsName;
   }


   @Column(name="prefs_numeric")
   public int getPrefsNumeric() {
      return this.prefsNumeric;
   }

   public void setPrefsNumeric(int prefsNumeric) {
      this.prefsNumeric = prefsNumeric;
   }


   @Column(name="prefs_text", length=255)
   public String getPrefsText() {
      return this.prefsText;
   }

   public void setPrefsText(String prefsText) {
      this.prefsText = prefsText;
   }

}
