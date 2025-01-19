package Zephyr.entities;

import jakarta.persistence.*;

  @Entity
  @Table(name = "uploads")
  public class Uploads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String file_path;


    @Column(name = "processed", nullable = false)
    private boolean processed;


    /**
     * Get the name of the file.
     *
     * @return the path of the file as String
     */
    public String getFile_path() {
      return file_path;
    }

    /**
     * @return true if the file is processed by processFile
     * method in JackRoutes
     */

    public boolean getProcessed() {
      return processed;
    }

    /**
     * Set the processed flag of the file
     *
     * @param flag the new status of the file
     */
    public void setProcessed(boolean flag) {
      this.processed = flag;
    }


    public void setFile_path(String file_path) {
      this.file_path = file_path;
    }
  }
