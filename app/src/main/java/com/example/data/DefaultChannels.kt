package com.example.data

data class DefaultChannel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val category: String,
    val country: String,
    val description: String = ""
) {
    fun toTvChannel(isFavorite: Boolean = false): TvChannel {
        return TvChannel(
            id = id,
            name = name,
            logoUrl = logoUrl,
            streamUrl = streamUrl,
            category = category,
            country = country,
            isCustom = false,
            isFavorite = isFavorite,
            description = description
        )
    }
}

data class TvChannel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val category: String,
    val country: String,
    val isCustom: Boolean,
    val isFavorite: Boolean,
    val description: String = "",
    val playlistUrl: String? = null
)

object DefaultChannels {
    val list = listOf(
        // High-Reliability Web & Android Compatible Streams
        DefaultChannel(
            id = "mux_wildlife",
            name = "Mux Wildlife Live Loop (CORS-Compliant)",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/db/Red_Bull_TV_logo.svg/330px-Red_Bull_TV_logo.svg.png",
            streamUrl = "https://stream.mux.com/v69ElvGePl29u00bTcHgFrZZ29Vb2C7w5.m3u8",
            category = "Entertainment (বিনোদন)",
            country: "International",
            description = "Multi-bitrate standard CORS-enabled streaming broadcast featuring beautiful high-definition wildlife scenery."
        ),
        DefaultChannel(
            id = "sintel_cinematic",
            name = "Sintel Cinematic Theme (CORS-Compliant)",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/FilmRise_logo.svg/512px-FilmRise_logo.svg.png",
            streamUrl = "https://test-streams.mux.dev/x36xhg/master.m3u8",
            category = "Movies (চলচ্চিত্র)",
            country: "Europe / Global",
            description = "Multi-language audio cinematic broadcast showing the beautiful Sintel trailer in standard high-definition adaptive stream."
        ),
        DefaultChannel(
            id = "big_buck_bunny",
            name = "Big Buck Bunny HLS HD (CORS-Compliant)",
            logoUrl = "https://images.foxtel.com.au/channel-logos/sky-sports-news.png",
            streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
            category = "Entertainment (বিনোদন)",
            country: "Europe",
            description = "Standard open source live stream of Big Buck Bunny cartoon with multiple quality selector tracks."
        ),

        // Sports Category
        DefaultChannel(
            id = "sky_sports_news",
            name = "Sky Sports News HD",
            logoUrl = "https://images.foxtel.com.au/channel-logos/sky-sports-news.png",
            streamUrl = "https://skynews-skynews-us-edgesuite-net.akamaized.net/hls/live/2012028/skynews_us_hd/master.m3u8", // Sky HLS broadcast
            category = "Sports (খেলাধুলা)",
            country = "United Kingdom",
            description = "Get the latest live breaking sports news, transfer updates, stats, and scores instantly from Sky Sports."
        ),
        DefaultChannel(
            id = "red_bull_tv",
            name = "Red Bull TV Live",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/d/db/Red_Bull_TV_logo.svg/330px-Red_Bull_TV_logo.svg.png",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoLa-Ch1/index.m3u8",
            category = "Sports (খেলাধুলা)",
            country = "International",
            description = "Live motorsports, extreme sports, cycling, snowboarding, racing, global adventure movies, and live streams."
        ),
        DefaultChannel(
            id = "rakuten_sports",
            name = "Rakuten Sports Live",
            logoUrl = "https://cdn.sport.rakuten.tv/static/brand/color_horizontal.png",
            streamUrl = "https://1112139.rakuten-sports-global.wurl.tv/playlist.m3u8",
            category = "Sports (খেলাধুলা)",
            country = "Japan / Asia",
            description = "Premium live sports broadcast, featuring Asian football leagues, combat sports events, and fitness content."
        ),
        DefaultChannel(
            id = "bein_sports_xtra",
            name = "beIN SPORTS XTRA US",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/BeIN_Sports_logo.svg/1024px-BeIN_Sports_logo.svg.png",
            streamUrl = "https://beinsportsxtra-sinclairendeavor.amagi.tv/playlist.m3u8",
            category = "Sports (খেলাধুলা)",
            country = "USA / Global",
            description = "Selected matches, soccer analysis, live action sports, and sports highlights on beIN Sports."
        ),

        // News Category
        DefaultChannel(
            id = "somoy_tv",
            name = "Somoy TV Live (সময় টিভি)",
            logoUrl = "https://www.somoynews.tv/assets/images/logo.png",
            streamUrl = "https://somoylive.ebdcdn.com/live/somoyhd/playlist.m3u8",
            category = "News (সংবাদ)",
            country = "Bangladesh",
            description = "somoynews.tv is one of the leading online news portals in Bangladesh. High quality live news 24/7."
        ),
        DefaultChannel(
            id = "jamuna_tv",
            name = "Jamuna TV Live (যমুনা টিভি)",
            logoUrl = "https://www.jamuna.tv/wp-content/themes/jamuna-pro/img/jamuna-tv-f-logo.png",
            streamUrl = "https://jamunalivey.ebdcdn.com/live/jamunahd/playlist.m3u8",
            category = "News (সংবাদ)",
            country = "Bangladesh",
            description = "Jamuna Television is a 24-hour private television channel in Bangladesh. Delivering unbiased, fast live news feed."
        ),
        DefaultChannel(
            id = "al_jazeera_en",
            name = "Al Jazeera English News",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Aljazeera_eng.svg/300px-Aljazeera_eng.svg.png",
            streamUrl = "https://live-amg-eng.akamaized.net/hls/live/736417/aljazeera/user_dev_high.m3u8",
            category = "News (সংবাদ)",
            country = "Qatar / International",
            description = "Deep investigative international reports, live current events and world news from Al Jazeera."
        ),
        DefaultChannel(
            id = "cbs_news",
            name = "CBS News Live US",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/CBS_News_logo_2020.svg/512px-CBS_News_logo_2020.svg.png",
            streamUrl = "https://cbsn-us-edge.cbsnstream.cbsnews.com/main/main.m3u8",
            category = "News (সংবাদ)",
            country = "USA",
            description = "CBS News streaming channel brings you live, unfiltered American news broadcasts and talk shows 24 hours a day."
        ),
        DefaultChannel(
            id = "dw_news",
            name = "DW News English Live",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Deutsche_Welle_logo.svg/330px-Deutsche_Welle_logo.svg.png",
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2013532/dwstream102/index.m3u8",
            category = "News (সংবাদ)",
            country = "Germany / Europe",
            description = "Deutsche Welle international broadcast with direct, reliable news, analytics, and documentaries from Germany."
        ),
        DefaultChannel(
            id = "france_24_en",
            name = "France 24 English Live",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/FRANCE_24_Logo.svg/330px-FRANCE_24_Logo.svg.png",
            streamUrl = "https://static.france24.com/live/F24_EN_HI_HLS/live_web.m3u8",
            category = "News (সংবাদ)",
            country = "France / Europe",
            description = "France 24 English provides around-the-clock international news coverage from a French perspective."
        ),
        DefaultChannel(
            id = "bloomberg_tv",
            name = "Bloomberg Global Finance",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Bloomberg_Business_logo.svg/512px-Bloomberg_Business_logo.svg.png",
            streamUrl = "https://live.bloomberg.com/active/hls/us/index.m3u8",
            category = "News (সংবাদ)",
            country = "International / Finance",
            description = "Bloomberg TV's standard-setting premium service covering stock markets, global indexes, and corporate updates."
        ),

        // Entertainment Category
        DefaultChannel(
            id = "sangshad_tv",
            name = "Sangshad TV (সংসদ বাংলাদেশ)",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/0/05/Sangshad_Television_Logo_2011.png",
            streamUrl = "https://sangshadlive.ebdcdn.com/live/sangshad/playlist.m3u8",
            category = "Entertainment (বিনোদন)",
            country = "Bangladesh",
            description = "Official parliamentary and educational Channel of Bangladesh, broadcasting live parliament debates and classes."
        ),
        DefaultChannel(
            id = "nasa_tv",
            name = "NASA TV Live HD",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/300px-NASA_logo.svg.png",
            streamUrl = "https://ntv1.nasatv.live/nasatv1/index.m3u8",
            category = "Entertainment (বিনোদন)",
            country = "USA",
            description = "Live coverage of spacecraft launches, astronauts on the ISS, deep space visualizers and scientific developments."
        ),
        DefaultChannel(
            id = "fashion_tv",
            name = "Fashion TV Global",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/FashionTV_logo.svg/300px-FashionTV_logo.svg.png",
            streamUrl = "https://fashiontv-fast.amagi.tv/playlist.m3u8",
            category = "Entertainment (বিনোদন)",
            country = "International",
            description = "High fashion runway shows, world-famous model portfolios, designers diaries, and lifestyle trends."
        ),

        // Movies Category
        DefaultChannel(
            id = "filmrise_action",
            name = "FilmRise Action Movies",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/FilmRise_logo.svg/512px-FilmRise_logo.svg.png",
            streamUrl = "https://filmrise-action-samsungus.wurl.tv/playlist.m3u8",
            category = "Movies (চলচ্চিত্র)",
            country = "USA / Global",
            description = "Free action movies, thrillers, martial arts, blockbuster adventures, and action series with stars from FilmRise."
        ),
        DefaultChannel(
            id = "ifc_action",
            name = "IFC Film Classics Live",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/IFC_logo_2014.svg/300px-IFC_logo_2014.svg.png",
            streamUrl = "https://ifcinternationalclassic-samsung.wurl.tv/playlist.m3u8",
            category = "Movies (চলচ্চিত্র)",
            country = "USA",
            description = "Enjoy award-winning independent films, classic movie archives, and nostalgic masterpieces."
        )
    )
}
