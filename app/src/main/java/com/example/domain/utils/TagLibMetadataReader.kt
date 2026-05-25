package com.example.domain.utils

object TagLibMetadataReader {
    fun readAudioMetadata(filePath: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            // Using Android's built-in MediaMetadataRetriever as a safer alternative to jaudiotagger on device
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            result["title"] = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown Title"
            result["artist"] = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            result["album"] = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace() // Gracefully ignore file access/format errors
        }
        return result
    }
}
