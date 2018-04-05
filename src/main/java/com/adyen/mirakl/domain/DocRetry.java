package com.adyen.mirakl.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DocRetry.
 */
@Entity
@Table(name = "doc_retry")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DocRetry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id")
    private String docId;

    @Column(name = "shop_id")
    private String shopId;

    @Column(name = "error")
    private String error;

    @Column(name = "times_failed")
    private Integer timesFailed;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocId() {
        return docId;
    }

    public DocRetry docId(String docId) {
        this.docId = docId;
        return this;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getShopId() {
        return shopId;
    }

    public DocRetry shopId(String shopId) {
        this.shopId = shopId;
        return this;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getError() {
        return error;
    }

    public DocRetry error(String error) {
        this.error = error;
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getTimesFailed() {
        return timesFailed;
    }

    public DocRetry timesFailed(Integer timesFailed) {
        this.timesFailed = timesFailed;
        return this;
    }

    public void setTimesFailed(Integer timesFailed) {
        this.timesFailed = timesFailed;
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
        DocRetry docRetry = (DocRetry) o;
        if (docRetry.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), docRetry.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DocRetry{" +
            "id=" + getId() +
            ", docId='" + getDocId() + "'" +
            ", shopId='" + getShopId() + "'" +
            ", error='" + getError() + "'" +
            ", timesFailed=" + getTimesFailed() +
            "}";
    }
}
