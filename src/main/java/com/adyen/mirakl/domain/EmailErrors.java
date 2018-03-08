package com.adyen.mirakl.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A EmailErrors.
 */
@Entity
@Table(name = "email_errors")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EmailErrors implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "errors")
    private String errors;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErrors() {
        return errors;
    }

    public EmailErrors errors(String errors) {
        this.errors = errors;
        return this;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailErrors emailErrors = (EmailErrors) o;
        if (emailErrors.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), emailErrors.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "EmailErrors{" +
            "id=" + getId() +
            ", errors='" + getErrors() + "'" +
            "}";
    }
}
