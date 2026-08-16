package com.example.catchcompass.catchlog;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "catch_photos")
public class CatchPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catch_id", nullable = false)
    private Catch catchRecord;

    @Column(nullable = false, unique = true, length = 255)
    private String storageKey;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private Integer widthPx;

    @Column(nullable = false)
    private Integer heightPx;

    @Column(nullable = false)
    private Instant createdAt;

    protected CatchPhoto() {
        // JPA requires a no-argument constructor; not for your use
    }

    public CatchPhoto(Catch catchRecord,
                      String storageKey,
                      String contentType,
                      long sizeBytes,
                      int widthPx,
                      int heightPx) {
        this.catchRecord = catchRecord;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Catch getCatchRecord() { return catchRecord; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public Integer getWidthPx() { return widthPx; }
    public Integer getHeightPx() { return heightPx; }
    public Instant getCreatedAt() { return createdAt; }
}
