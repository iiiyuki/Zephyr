package Zephyr.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "accepted_sequences")
public class AcceptedSequences {
  @Id
  private CharSequence acceptedSequence;


  @Column(name = "marker", nullable = false)
  private String user_id;

  public String getMarker(){
    return user_id;
  }

  public void setMarker(String newMarker){
    this.user_id = newMarker;
  }

  public CharSequence getAcceptedSequence(){
    return acceptedSequence;
  }

  public void setAcceptedSequence(CharSequence newSequence){
    this.acceptedSequence = newSequence;
  }
}
