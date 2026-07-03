package com.huraira.murshid.data

import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.data.model.WallpaperItem

object DummyDataProvider {

    fun getWallpapers(): List<WallpaperItem> = listOf(
        WallpaperItem("w1", "Unshaken", "https://picsum.photos/seed/murshid1/720/1280", "Discipline"),
        WallpaperItem("w2", "Rise Again", "https://picsum.photos/seed/murshid2/720/1280", "Resilience"),
        WallpaperItem("w3", "Silent Storm", "https://picsum.photos/seed/murshid3/720/1280", "Focus"),
        WallpaperItem("w4", "Iron Will", "https://picsum.photos/seed/murshid4/720/1280", "Discipline"),
        WallpaperItem("w5", "The Long Road", "https://picsum.photos/seed/murshid5/720/1280", "Patience"),
        WallpaperItem("w6", "No Retreat", "https://picsum.photos/seed/murshid6/720/1280", "Resilience"),
        WallpaperItem("w7", "Command Yourself", "https://picsum.photos/seed/murshid7/720/1280", "Leadership"),
        WallpaperItem("w8", "Quiet Power", "https://picsum.photos/seed/murshid8/720/1280", "Focus"),
        WallpaperItem("w9", "Built to Last", "https://picsum.photos/seed/murshid9/720/1280", "Discipline"),
        WallpaperItem("w10", "Forward Only", "https://picsum.photos/seed/murshid10/720/1280", "Momentum"),
        WallpaperItem("w11", "Unshaken Resolve", "android.resource://com.huraira.murshid/drawable/imran_khan_1", "Leadership"),
        WallpaperItem("w12", "The Long Road", "android.resource://com.huraira.murshid/drawable/imran_khan_2", "Resilience"),
    )

    fun getLibraryItems(): List<LibraryItem> = listOf(
        LibraryItem(
            id = "l1",
            type = LibraryContentType.QUOTE,
            quoteText = "Discipline is the bridge between who you are and who you are meant to become.",
            author = "Murshid"
        ),
        LibraryItem(
            id = "l2",
            type = LibraryContentType.IMAGE_QUOTE,
            quoteText = "Storms don't last. Character does.",
            author = "Murshid",
            imageUrl = "https://picsum.photos/seed/murshidlib2/800/1000"
        ),
        LibraryItem(
            id = "l3",
            type = LibraryContentType.IMAGE,
            imageUrl = "https://picsum.photos/seed/murshidlib3/800/800"
        ),
        LibraryItem(
            id = "l4",
            type = LibraryContentType.VIDEO,
            quoteText = "Five minutes on mastering your morning routine.",
            videoThumbnailUrl = "https://picsum.photos/seed/murshidlib4/800/450",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        ),
        LibraryItem(
            id = "l5",
            type = LibraryContentType.QUOTE,
            quoteText = "A leader who cannot command himself has nothing left to command.",
            author = "Murshid"
        ),
        LibraryItem(
            id = "l6",
            type = LibraryContentType.IMAGE_QUOTE,
            quoteText = "Patience is the quietest form of strength.",
            author = "Murshid",
            imageUrl = "https://picsum.photos/seed/murshidlib6/800/1000"
        ),
        LibraryItem(
            id = "l7",
            type = LibraryContentType.IMAGE,
            imageUrl = "https://picsum.photos/seed/murshidlib7/800/800"
        ),
        LibraryItem(
            id = "l8",
            type = LibraryContentType.VIDEO,
            quoteText = "On resilience: how setbacks forge purpose.",
            videoThumbnailUrl = "https://picsum.photos/seed/murshidlib8/800/450",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        ),
    )

    fun getUpdates(): List<UpdateItem> = listOf(
        UpdateItem(
            id = "u1",
            title = "New Wallpaper Collection: Discipline",
            date = "June 28, 2026",
            thumbnailUrl = "https://picsum.photos/seed/murshidupdate1/600/400",
            summary = "Ten new cinematic wallpapers built around a single idea: discipline.",
            fullContent = "This week's collection is built around a single idea: discipline. " +
                    "Every wallpaper in the set was designed to sit on your lock screen as a quiet, " +
                    "constant reminder of the standard you've set for yourself. Expect more themed " +
                    "drops like this every two weeks, each tied to a principle rather than a mood.",
            detailImageUrl = "https://picsum.photos/seed/murshidupdate1detail/800/500"
        ),
        UpdateItem(
            id = "u2",
            title = "Weekly Reflection: On Resilience",
            date = "June 21, 2026",
            thumbnailUrl = "https://picsum.photos/seed/murshidupdate2/600/400",
            summary = "A short video reflection on turning setbacks into forward motion.",
            fullContent = "Resilience isn't the absence of setbacks — it's what you build in the " +
                    "space right after one. This week's reflection walks through a simple framework " +
                    "for turning a bad week into useful information instead of a story you keep telling " +
                    "yourself.",
            youtubeVideoId = "dQw4w9WgXcQ"
        ),
        UpdateItem(
            id = "u3",
            title = "Library Refresh: 12 New Quotes",
            date = "June 14, 2026",
            thumbnailUrl = "https://picsum.photos/seed/murshidupdate3/600/400",
            summary = "Twelve new quotes on leadership and focus have been added to the Library.",
            fullContent = "We've added twelve new entries to the Library, focused on leadership " +
                    "and focus. As always, they're written to be short enough to remember and sharp " +
                    "enough to act on."
        ),
        UpdateItem(
            id = "u4",
            title = "App Update: Smoother Transitions",
            date = "June 5, 2026",
            thumbnailUrl = "https://picsum.photos/seed/murshidupdate4/600/400",
            summary = "Performance improvements and smoother screen transitions across the app.",
            fullContent = "This release focuses entirely on feel: faster image loading, smoother " +
                    "shared-element transitions into wallpaper previews, and a more responsive bottom " +
                    "navigation bar. No new features this time — just a more premium day-to-day " +
                    "experience."
        ),
    )
}