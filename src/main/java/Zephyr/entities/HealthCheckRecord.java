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
@Table(name = "health_check_records")
public class HealthCheckRecord {

  /*
   * When the health check was performed
   * Unique uuid for the health check
   * timestamp of the health check
   * by_ip
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uuid", nullable = false)
  private String uuid;

  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  @Column(name = "by_ip", nullable = false)
  private String byIp;

  @Column(name = "status", nullable = false)
  private Boolean status;

  public Long getId() {
    return id;
  }

  public boolean setHealthCheckRecord(String uuid, Long timestamp, String byIp, Boolean status) {
    // id will be auto-generated
    this.uuid = uuid;
    this.timestamp = timestamp;
    this.byIp = byIp;
    this.status = status;

    return true;
  }

}

