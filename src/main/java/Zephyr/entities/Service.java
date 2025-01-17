package Zephyr.entities;

import jakarta.persistence.*;


/**
 * Entity class for the `services` table.
 * This class is used for ORM mapping with Hibernate.
 * It represents a service with fields for id, name, description, and status.
 * Each field is mapped to a column in the `services` table.
 * The id field is the primary key and is auto-generated.
 * The name and status fields are required.
 * The description field is optional.
 *
 * @author binaryYuki
 */
@Entity
@Table(name = "services")
public class Service {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "status", nullable = false)
  private String status;

  /**
   * Get the id of the service.
   *
   * @return the id of the service
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the id of the service.
   *
   * @param id the id of the service
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the name of the service.
   *
   * @return the name of the service
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the service.
   *
   * @param name the name of the service
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the description of the service.
   *
   * @return the description of the service
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the service.
   *
   * @param description the description of the service
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the status of the service.
   *
   * @return the status of the service
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the status of the service.
   *
   * @param status the status of the service
   */
  public void setStatus(String status) {
    this.status = status;
  }
}
