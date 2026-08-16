package com.example.catchcompass.storage;

/**
 * Where photo bytes are kept, kept behind an interface so the local filesystem
 * used in development can be swapped for private object storage in production
 * without touching the catch workflow.
 */
public interface PhotoStorage {

    /**
     * Stores the given bytes and returns a server-generated key.
     *
     * <p>The key is generated here and never derived from anything the browser
     * submitted. A filename from a client is untrusted input, not a path.
     */
    String store(byte[] content, String extension);

    byte[] load(String storageKey);

    void delete(String storageKey);
}
