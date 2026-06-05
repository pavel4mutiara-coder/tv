import React, { useState, useEffect, useRef } from 'react';

// TS Interfaces matching Android counterpart data structures
interface TvChannel {
  id: string;
  name: string;
  logoUrl: string;
  streamUrl: string;
  category: string;
  country: string;
  description: string;
  isFavorite: boolean;
}

// Default channels imported from DefaultChannels.kt 
const INITIAL_CHANNELS: TvChannel[] = [
  {
    id: "somoy_tv",
    name: "Somoy TV Live (সময় টিভি)",
    logoUrl: "https://www.somoynews.tv/assets/images/logo.png",
    streamUrl: "https://somoylive.ebdcdn.com/live/somoyhd/playlist.m3u8",
    category: "News (সংবাদ)",
    country: "Bangladesh",
    description: "Somoy News is one of the leading online news portals in Bangladesh. High quality live news 24/7.",
    isFavorite: true
  },
  {
    id: "jamuna_tv",
    name: "Jamuna TV Live (যমুনা টিভি)",
    logoUrl: "https://www.jamuna.tv/wp-content/themes/jamuna-pro/img/jamuna-tv-f-logo.png",
    streamUrl: "https://jamunalivey.ebdcdn.com/live/jamunahd/playlist.m3u8",
    category: "News (সংবাদ)",
    country: "Bangladesh",
    description: "Jamuna Television is a 24-hour private television channel in Bangladesh. Delivering unbiased, fast live news feed.",
    isFavorite: false
  },
  {
    id: "sky_sports_news",
    name: "Sky Sports News HD",
    logoUrl: "https://images.foxtel.com.au/channel-logos/sky-sports-news.png",
    streamUrl: "https://skynews-skynews-us-edgesuite-net.akamaized.net/hls/live/2012028/skynews_us_hd/master.m3u8",
    category: "Sports (খেলাধুলা)",
    country: "United Kingdom",
    description: "Get the latest live breaking sports news, transfer updates, stats, and scores instantly from Sky Sports.",
    isFavorite: true
  },
  {
    id: "red_bull_tv",
    name: "Red Bull TV Live",
    logoUrl: "https://upload.wikimedia.org/wikipedia/en/thumb/d/db/Red_Bull_TV_logo.svg/330px-Red_Bull_TV_logo.svg.png",
    streamUrl: "https://rbmn-live.akamaized.net/hls/live/590964/BoLa-Ch1/index.m3u8",
    category: "Sports (খেলাধুলা)",
    country: "International",
    description: "Live motorsports, extreme sports, cycling, snowboarding, racing, global adventure movies, and live streams.",
    isFavorite: false
  },
  {
    id: "al_jazeera_en",
    name: "Al Jazeera English News",
    logoUrl: "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Aljazeera_eng.svg/300px-Aljazeera_eng.svg.png",
    streamUrl: "https://live-amg-eng.akamaized.net/hls/live/736417/aljazeera/user_dev_high.m3u8",
    category: "News (সংবাদ)",
    country: "Qatar / International",
    description: "Deep investigative international reports, live current events and world news from Al Jazeera.",
    isFavorite: false
  },
  {
    id: "dw_news",
    name: "DW News English Live",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Deutsche_Welle_logo.svg/330px-Deutsche_Welle_logo.svg.png",
    streamUrl: "https://dwamdstream102.akamaized.net/hls/live/2013532/dwstream102/index.m3u8",
    category: "News (সংবাদ)",
    country: "Germany / Europe",
    description: "Deutsche Welle international broadcast with direct, reliable news, analytics, and documentaries from Germany.",
    isFavorite: false
  },
  {
    id: "france_24_en",
    name: "France 24 English Live",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/FRANCE_24_Logo.svg/330px-FRANCE_24_Logo.svg.png",
    streamUrl: "https://static.france24.com/live/F24_EN_HI_HLS/live_web.m3u8",
    category: "News (সংবাদ)",
    country: "France / Europe",
    description: "France 24 English provides around-the-clock international news coverage from a French perspective.",
    isFavorite: false
  },
  {
    id: "bein_sports_xtra",
    name: "beIN SPORTS XTRA US",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/BeIN_Sports_logo.svg/1024px-BeIN_Sports_logo.svg.png",
    streamUrl: "https://beinsportsxtra-sinclairendeavor.amagi.tv/playlist.m3u8",
    category: "Sports (খেলাধুলা)",
    country: "USA / Global",
    description: "Selected matches, soccer analysis, live action sports, and sports highlights on beIN Sports.",
    isFavorite: false
  },
  {
    id: "rakuten_sports",
    name: "Rakuten Sports Live",
    logoUrl: "https://cdn.sport.rakuten.tv/static/brand/color_horizontal.png",
    streamUrl: "https://1112139.rakuten-sports-global.wurl.tv/playlist.m3u8",
    category: "Sports (খেলাধুলা)",
    country: "Japan / Asia",
    description: "Premium live sports broadcast, featuring Asian football leagues, combat sports events, and fitness content.",
    isFavorite: false
  },
  {
    id: "nasa_tv",
    name: "NASA TV Live HD",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/300px-NASA_logo.svg.png",
    streamUrl: "https://ntv1.nasatv.live/nasatv1/index.m3u8",
    category: "Entertainment (বিনোদন)",
    country: "USA",
    description: "Live coverage of spacecraft launches, astronauts on the ISS, deep space visualizers and scientific developments.",
    isFavorite: false
  },
  {
    id: "fashion_tv",
    name: "Fashion TV Global",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/FashionTV_logo.svg/300px-FashionTV_logo.svg.png",
    streamUrl: "https://fashiontv-fast.amagi.tv/playlist.m3u8",
    category: "Entertainment (বিনোদন)",
    country: "International",
    description: "High fashion runway shows, world-famous model portfolios, designers diaries, and lifestyle trends.",
    isFavorite: false
  },
  {
    id: "filmrise_action",
    name: "FilmRise Action Movies",
    logoUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/FilmRise_logo.svg/512px-FilmRise_logo.svg.png",
    streamUrl: "https://filmrise-action-samsungus.wurl.tv/playlist.m3u8",
    category: "Movies (চলচ্চিত্র)",
    country: "USA / Global",
    description: "Free action movies, thrillers, martial arts, blockbuster adventures, and action series with stars from FilmRise.",
    isFavorite: false
  }
];

export default function App() {
  // Application State
  const [channels, setChannels] = useState<TvChannel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeChannel, setActiveChannel] = useState<TvChannel | null>(null);
  const [activeCategory, setActiveCategory] = useState<string>("All");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [showFavoritesOnly, setShowFavoritesOnly] = useState<boolean>(false);
  
  // Custom channel drawer/form modal state
  const [showAddForm, setShowAddForm] = useState<boolean>(false);
  const [newChannelName, setNewChannelName] = useState("");
  const [newChannelUrl, setNewChannelUrl] = useState("");
  const [newChannelCategory, setNewChannelCategory] = useState("Entertainment (বিনোদন)");
  const [newChannelCountry, setNewChannelCountry] = useState("Unknown");
  const [newChannelLogo, setNewChannelLogo] = useState("");

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const hlsInstance = useRef<any>(null);

  // Load from local storage or static array
  useEffect(() => {
    const saved = localStorage.getItem('globus_channels');
    let listToLoad = INITIAL_CHANNELS;
    if (saved) {
      try {
        listToLoad = JSON.parse(saved);
      } catch (e) {
        listToLoad = INITIAL_CHANNELS;
      }
    } else {
      localStorage.setItem('globus_channels', JSON.stringify(INITIAL_CHANNELS));
    }
    
    // Simulate beautiful 1.2 second network async fetch
    const timer = setTimeout(() => {
      setChannels(listToLoad);
      setLoading(false);
      // Pre-select first channel
      if (listToLoad.length > 0) {
        setActiveChannel(listToLoad[0]);
      }
    }, 1200);

    return () => clearTimeout(timer);
  }, []);

  // Update localStorage when channels list changes
  const saveChannels = (updatedList: TvChannel[]) => {
    setChannels(updatedList);
    localStorage.setItem('globus_channels', JSON.stringify(updatedList));
  };

  // Video Streaming Playback Handler (using HLS.js)
  useEffect(() => {
    if (!videoRef.current || !activeChannel) return;

    const video = videoRef.current;
    const url = activeChannel.streamUrl;

    // Destroy existing instance gracefully
    if (hlsInstance.current) {
      hlsInstance.current.destroy();
      hlsInstance.current = null;
    }

    if (typeof (window as any).Hls !== 'undefined' && (window as any).Hls.isSupported()) {
      const hls = new (window as any).Hls({
        enableWorker: true,
        maxBufferLength: 30,
        lowLatencyMode: true,
      });
      hlsInstance.current = hls;
      hls.loadSource(url);
      hls.attachMedia(video);
      hls.on((window as any).Hls.Events.MANIFEST_PARSED, () => {
        video.play().catch(e => console.log("Auto play prevented:", e));
      });
      hls.on((window as any).Hls.Events.ERROR, function (_event: any, data: any) {
        if (data.fatal) {
          switch (data.type) {
            case (window as any).Hls.ErrorTypes.NETWORK_ERROR:
              console.log("HLS Network Error, attempting recovery...");
              hls.startLoad();
              break;
            case (window as any).Hls.ErrorTypes.MEDIA_ERROR:
              console.log("HLS Media Error, attempting recovery...");
              hls.recoverMediaError();
              break;
            default:
              console.log("Fatal HLS Error, cannot recover automatically.");
              break;
          }
        }
      });
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      // Fallback for native Safari stream
      video.src = url;
      video.addEventListener('loadedmetadata', () => {
        video.play().catch(e => console.log("iOS Auto play prevented:", e));
      });
    }

    return () => {
      if (hlsInstance.current) {
        hlsInstance.current.destroy();
        hlsInstance.current = null;
      }
    };
  }, [activeChannel]);

  // Handle addition of custom IPTV channel
  const handleAddChannel = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChannelName || !newChannelUrl) {
      alert("Please provide at least a channel name and HLS stream URL.");
      return;
    }

    const newChan: TvChannel = {
      id: "custom_" + Date.now(),
      name: newChannelName,
      logoUrl: newChannelLogo || "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/300px-NASA_logo.svg.png",
      streamUrl: newChannelUrl,
      category: newChannelCategory,
      country: newChannelCountry,
      description: "Custom user-imported live stream feed.",
      isFavorite: false
    };

    const newList = [...channels, newChan];
    saveChannels(newList);
    setActiveChannel(newChan);
    setShowAddForm(false);
    
    // Clear inputs
    setNewChannelName("");
    setNewChannelUrl("");
    setNewChannelLogo("");
    setNewChannelCountry("Unknown");
  };

  // Toggle favorite channel state
  const toggleFavorite = (id: string, event?: React.MouseEvent) => {
    if (event) event.stopPropagation();
    const updated = channels.map(ch => {
      if (ch.id === id) {
        return { ...ch, isFavorite: !ch.isFavorite };
      }
      return ch;
    });
    saveChannels(updated);
    if (activeChannel && activeChannel.id === id) {
      setActiveChannel({ ...activeChannel, isFavorite: !activeChannel.isFavorite });
    }
  };

  // Get dynamic unique categories
  const categories = ["All", ...new Set(channels.map(c => c.category))];

  // Filter channels based on Search, Category, and Favorites Toggle
  const filteredChannels = channels.filter(ch => {
    const matchesSearch = ch.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          ch.country.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = activeCategory === "All" || ch.category === activeCategory;
    const matchesFavorite = !showFavoritesOnly || ch.isFavorite;
    return matchesSearch && matchesCategory && matchesFavorite;
  });

  // Color generator for logo fallback text graphics
  const getLogoColors = (name: string) => {
    const val = name.charCodeAt(0) % 4;
    switch (val) {
      case 0: return 'linear-gradient(135deg, #3b82f6, #06b6d4)';
      case 1: return 'linear-gradient(135deg, #ec4899, #f43f5e)';
      case 2: return 'linear-gradient(135deg, #10b981, #059669)';
      default: return 'linear-gradient(135deg, #8b5cf6, #d946ef)';
    }
  };

  return (
    <div style={styles.appContainer}>
      {/* HEADER SECTION */}
      <header style={styles.header}>
        <div style={styles.logoGroup}>
          <div style={styles.appIconPulse}>
            <span style={{ color: '#fff', fontWeight: 900, fontSize: '18px' }}>G</span>
          </div>
          <div>
            <h1 style={styles.brandTitle}>GlobalStream <span style={styles.liveAccent}>LIVE</span></h1>
            <p style={styles.brandSubtitle}>High Definition TV Companion Network</p>
          </div>
        </div>

        {/* Global Stats Overlay */}
        <div style={styles.headerControls}>
          <button 
            style={styles.addBtn}
            onClick={() => setShowAddForm(true)}
          >
            <svg style={styles.svgIcon} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
            </svg>
            ADD IPTV CHANNEL
          </button>
        </div>
      </header>

      {/* DETAILED STREAM VIEWER SECTION */}
      {activeChannel ? (
        <section style={styles.playerSection}>
          <div style={styles.playerContainer}>
            <video 
              ref={videoRef}
              controls 
              autoPlay 
              playsInline
              style={styles.videoPlayer}
              poster={activeChannel.logoUrl}
            />
            {/* Live Indicator overlay on top right */}
            <div style={styles.liveIndicatorBadge}>
              <span style={styles.liveDot}></span>
              LIVE BROADCAST
            </div>
          </div>

          <div style={styles.channelDetails}>
            <div style={styles.channelDetailsHeader}>
              <div style={styles.detailsLogoWrapper}>
                {activeChannel.logoUrl ? (
                  <img src={activeChannel.logoUrl} alt={activeChannel.name} style={styles.detailsLogo} onError={(e)=>{
                    (e.target as HTMLImageElement).style.display = 'none';
                  }} />
                ) : (
                  <div style={{...styles.detailsLogoFallback, background: getLogoColors(activeChannel.name)}}>
                    {activeChannel.name.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
              <div style={{ flex: 1 }}>
                <div style={styles.titleWithCountry}>
                  <h2 style={styles.detailsTitle}>{activeChannel.name}</h2>
                  <span style={styles.countryLabel}>{activeChannel.country}</span>
                </div>
                <div style={styles.metaRow}>
                  <span style={styles.categoryBadge}>{activeChannel.category}</span>
                  <span style={styles.protocolSpan}>HLS Native Stream (m3u8)</span>
                </div>
              </div>

              {/* Bookmark state */}
              <button 
                style={activeChannel.isFavorite ? styles.favoritedBtn : styles.favoriteBtn}
                onClick={() => toggleFavorite(activeChannel.id)}
              >
                <svg style={styles.heartIcon} viewBox="0 0 24 24" fill={activeChannel.isFavorite ? "currentColor" : "none"} stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                {activeChannel.isFavorite ? 'BOOMARKED' : 'BOOMARK'}
              </button>
            </div>
            <p style={styles.detailsDescription}>{activeChannel.description || "No supplemental details available for this channels. Connect your IPTV encoder to fetch detailed stream headers."}</p>
          </div>
        </section>
      ) : (
        <div style={styles.noActivePlayer}>
          <p>Please select a channel from the interactive list below to begin streaming live broadcast.</p>
        </div>
      )}

      {/* FILTER & INTERACTIVE DIRECTORY BAR */}
      <section style={styles.filterSection}>
        <div style={styles.filterLeft}>
          <div style={styles.searchContainer}>
            <svg style={styles.searchIcon} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input 
              type="text" 
              placeholder="Search by channels name or origin country..." 
              style={styles.searchInput}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          <button 
            onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
            style={showFavoritesOnly ? styles.toggleFavBtnOn : styles.toggleFavBtnOff}
          >
            <svg style={{width:'16px', height:'16px', marginRight:'6px'}} viewBox="0 0 24 24" fill={showFavoritesOnly ? "currentColor" : "none"} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            Bookmarks ({channels.filter(c => c.isFavorite).length})
          </button>
        </div>

        {/* Categories Horizontal Scrolling Rail */}
        <div style={styles.categoryScroll}>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              style={activeCategory === cat ? styles.categoryActive : styles.categoryInactive}
            >
              {cat}
            </button>
          ))}
        </div>
      </section>

      {/* CHANNELS GRID CONTAINER */}
      <main style={styles.gridContainer}>
        {loading ? (
          /* Shimmer Cards Pre-loader to show fetches and displays displays channels correctly */
          <div style={styles.channelsGrid}>
            {[...Array(8)].map((_, i) => (
              <div key={i} style={styles.shimmerCard}>
                <div style={styles.shimmerThum}></div>
                <div style={styles.shimmerDetails}>
                  <div style={styles.shimmerTitle}></div>
                  <div style={styles.shimmerSubtitle}></div>
                </div>
              </div>
            ))}
          </div>
        ) : filteredChannels.length === 0 ? (
          <div style={styles.emptyState}>
            <svg style={styles.emptyIcon} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3>No Active TV Channels Found</h3>
            <p>Ensure you entered the right search keywords, reset filters, or inject custom playlist M3U / stream feeds manually.</p>
            <button style={styles.resetFilterBtn} onClick={() => { setSearchQuery(""); setActiveCategory("All"); setShowFavoritesOnly(false); }}>
              Reset Filters
            </button>
          </div>
        ) : (
          <div style={styles.channelsGrid}>
            {filteredChannels.map(ch => {
              const isSelected = activeChannel?.id === ch.id;
              return (
                <div 
                  key={ch.id} 
                  style={{
                    ...styles.channelCard,
                    ...(isSelected ? styles.channelCardSelected : {})
                  }}
                  onClick={() => setActiveChannel(ch)}
                >
                  <div style={styles.cardThumbnailWrapper}>
                    {ch.logoUrl ? (
                      <img 
                        src={ch.logoUrl} 
                        alt={ch.name} 
                        style={styles.cardLogoImage}
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = `https://visuals.services.com/fallback-icon?text=${ch.name.substring(0, 1)}`;
                        }}
                      />
                    ) : (
                      <div style={{...styles.cardLogoFallback, background: getLogoColors(ch.name)}}>
                        {ch.name.charAt(0).toUpperCase()}
                      </div>
                    )}
                    
                    {/* Live overlay badge */}
                    <div style={styles.liveOverlayBadge}>
                      LIVE
                    </div>

                    {/* Bookmark Toggle Icon */}
                    <button 
                      style={styles.favoriteCardBadge}
                      onClick={(e) => toggleFavorite(ch.id, e)}
                    >
                      <svg style={{width:'15px', height:'15px'}} fill={ch.isFavorite ? "red" : "none"} viewBox="0 0 24 24" stroke={ch.isFavorite ? "red" : "#94a3b8"}>
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                      </svg>
                    </button>
                  </div>

                  <div style={styles.cardInfo}>
                    <h3 style={styles.cardChannelName}>{ch.name}</h3>
                    <div style={styles.cardMeta}>
                      <span style={styles.cardCountry}>{ch.country}</span>
                      <span style={styles.cardCategoryText}>{ch.category.split(" ")[0]}</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* FOOTER SECTION */}
      <footer style={styles.footer}>
        <p>© 2026 GlobalStream Companion Network. Built with React, TypeScript, and high-performance HLS engines.</p>
        <p style={{fontSize: '11px', color: '#64748b', marginTop: '4px'}}>Synched with your hardware Android TV tuner decoder (Globus TV Companion App)</p>
      </footer>

      {/* PORTAL ADD MODAL FEED DIALOG */}
      {showAddForm && (
        <div style={styles.modalOverlay} onClick={() => setShowAddForm(false)}>
          <div style={styles.modalBody} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalHeader}>
              <h2 style={styles.modalTitle}>Import New IPTV Stream</h2>
              <button style={styles.closeModalBtn} onClick={() => setShowAddForm(false)}>×</button>
            </div>
            
            <form onSubmit={handleAddChannel}>
              <div style={styles.formGroup}>
                <label style={styles.formLabel}>Channel Name *</label>
                <input 
                  type="text" 
                  style={styles.formInput} 
                  placeholder="e.g. Al Kass Sports English"
                  required
                  value={newChannelName}
                  onChange={(e) => setNewChannelName(e.target.value)}
                />
              </div>

              <div style={styles.formGroup}>
                <label style={styles.formLabel}>HLS M3U8 Stream URL *</label>
                <input 
                  type="url" 
                  style={styles.formInput} 
                  placeholder="https://example.com/live/stream/playlist.m3u8"
                  required
                  value={newChannelUrl}
                  onChange={(e) => setNewChannelUrl(e.target.value)}
                />
              </div>

              <div style={styles.formGroup}>
                <label style={styles.formLabel}>Logo URL (Optional)</label>
                <input 
                  type="url" 
                  style={styles.formInput} 
                  placeholder="https://images.com/source-logo.png"
                  value={newChannelLogo}
                  onChange={(e) => setNewChannelLogo(e.target.value)}
                />
              </div>

              <div style={{display: 'flex', gap: '12px'}}>
                <div style={{...styles.formGroup, flex: 1}}>
                  <label style={styles.formLabel}>Category</label>
                  <select 
                    style={styles.formInput}
                    value={newChannelCategory}
                    onChange={(e) => setNewChannelCategory(e.target.value)}
                  >
                    <option value="Sports (খেলাধুলা)">Sports (খেলাধুলা)</option>
                    <option value="News (সংবাদ)">News (সংবাদ)</option>
                    <option value="Entertainment (বিনোদন)">Entertainment (বিনোদন)</option>
                    <option value="Movies (চলচ্চিত্র)">Movies (চলচ্চিত্র)</option>
                  </select>
                </div>

                <div style={{...styles.formGroup, flex: 1}}>
                  <label style={styles.formLabel}>Country of Origin</label>
                  <input 
                    type="text" 
                    style={styles.formInput} 
                    placeholder="e.g. Bangladesh, United Kingdom"
                    value={newChannelCountry}
                    onChange={(e) => setNewChannelCountry(e.target.value)}
                  />
                </div>
              </div>

              <div style={styles.formActions}>
                <button type="button" style={styles.formCancelBtn} onClick={() => setShowAddForm(false)}>
                  Cancel
                </button>
                <button type="submit" style={styles.formSubmitBtn}>
                  Save Channel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

// INLINE JAVASCRIPT CSS SHEET FOR OPTIMAL GRAPHICAL PERFORMANCE
const styles: { [key: string]: React.CSSProperties } = {
  appContainer: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    fontFamily: 'system-ui, -apple-system, sans-serif',
    backgroundColor: '#070a13',
    color: '#e2e8f0',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 24px',
    backgroundColor: '#0b111e',
    borderBottom: '1px solid #141f35',
    flexWrap: 'wrap',
    gap: '12px',
  },
  logoGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  appIconPulse: {
    background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
    width: '38px',
    height: '38px',
    borderRadius: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    boxShadow: '0px 0px 10px rgba(59, 130, 246, 0.4)'
  },
  brandTitle: {
    fontSize: '20px',
    fontWeight: 800,
    margin: 0,
    color: '#ffffff',
    letterSpacing: '-0.3px',
  },
  liveAccent: {
    background: '#ef4444',
    padding: '1px 6px',
    fontSize: '11px',
    fontWeight: 900,
    borderRadius: '4px',
    marginLeft: '4px',
    color: '#fff',
    verticalAlign: 'middle',
  },
  brandSubtitle: {
    fontSize: '11px',
    color: '#64748b',
    margin: '1px 0 0 0',
  },
  headerControls: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  addBtn: {
    background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
    border: 'none',
    borderRadius: '8px',
    color: '#ffffff',
    padding: '8px 16px',
    fontWeight: 700,
    fontSize: '12px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    boxShadow: '0 4px 12px rgba(37, 99, 235, 0.25)',
    transition: 'transform 0.2s',
  },
  svgIcon: {
    width: '14px',
    height: '14px',
  },
  playerSection: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1.8fr) minmax(0, 1fr)',
    backgroundColor: '#0c101a',
    borderBottom: '1px solid #142036',
    gap: '0px',
  },
  playerContainer: {
    position: 'relative',
    backgroundColor: '#020306',
    aspectRatio: '16/9',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  videoPlayer: {
    width: '100%',
    height: '100%',
    objectFit: 'contain',
  },
  liveIndicatorBadge: {
    position: 'absolute',
    top: '16px',
    right: '16px',
    backgroundColor: 'rgba(239, 68, 68, 0.9)',
    fontSize: '11px',
    fontWeight: 800,
    letterSpacing: '0.4px',
    color: '#ffffff',
    padding: '4px 10px',
    borderRadius: '6px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    backdropFilter: 'blur(4px)',
  },
  liveDot: {
    width: '6px',
    height: '6px',
    backgroundColor: '#ffffff',
    borderRadius: '50%',
    display: 'inline-block',
    animation: 'pulse 1.2s infinite'
  },
  channelDetails: {
    padding: '24px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    backgroundColor: '#0d1322',
  },
  channelDetailsHeader: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '16px',
    marginBottom: '16px',
  },
  detailsLogoWrapper: {
    width: '54px',
    height: '54px',
    backgroundColor: '#1b243d',
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
    padding: '6px',
  },
  detailsLogo: {
    maxWidth: '100%',
    maxHeight: '100%',
    objectFit: 'contain',
  },
  detailsLogoFallback: {
    width: '100%',
    height: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#fff',
    fontWeight: 'black',
    fontSize: '22px',
    borderRadius: '8px',
  },
  titleWithCountry: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    flexWrap: 'wrap',
  },
  detailsTitle: {
    fontSize: '20px',
    fontWeight: 800,
    color: '#fff',
    margin: 0,
  },
  countryLabel: {
    fontSize: '11px',
    fontWeight: 600,
    color: '#64748b',
    backgroundColor: '#162035',
    padding: '2px 8px',
    borderRadius: '4px',
  },
  metaRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginTop: '6px',
  },
  categoryBadge: {
    fontSize: '11px',
    fontWeight: 700,
    color: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    padding: '2px 8px',
    borderRadius: '4px',
  },
  protocolSpan: {
    fontSize: '11px',
    color: '#64748b',
  },
  favoriteBtn: {
    backgroundColor: 'transparent',
    border: '1px solid #2d3e5f',
    color: '#94a3b8',
    borderRadius: '8px',
    padding: '6px 12px',
    fontSize: '11px',
    fontWeight: 700,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    transition: 'all 0.2s',
  },
  favoritedBtn: {
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    border: '1px solid #ef4444',
    color: '#ef4444',
    borderRadius: '8px',
    padding: '6px 12px',
    fontSize: '11px',
    fontWeight: 700,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    transition: 'all 0.2s',
  },
  heartIcon: {
    width: '14px',
    height: '14px',
  },
  detailsDescription: {
    fontSize: '13px',
    lineHeight: '1.5',
    color: '#94a3b8',
    margin: 0,
  },
  noActivePlayer: {
    padding: '40px',
    textAlign: 'center',
    backgroundColor: '#0c101a',
    color: '#64748b',
  },
  filterSection: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 24px',
    backgroundColor: '#090f1d',
    borderBottom: '1px solid #141f35',
    gap: '16px',
    flexWrap: 'wrap',
  },
  filterLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    flexWrap: 'wrap',
    flex: '1',
    maxWidth: '560px',
  },
  searchContainer: {
    position: 'relative',
    flex: '1',
    minWidth: '220px',
  },
  searchIcon: {
    position: 'absolute',
    left: '12px',
    top: '50%',
    transform: 'translateY(-50%)',
    width: '16px',
    height: '16px',
    color: '#475569',
  },
  searchInput: {
    width: '100%',
    backgroundColor: '#111827',
    border: '1px solid #1f2937',
    borderRadius: '8px',
    padding: '8px 12px 8px 36px',
    color: '#f3f4f6',
    fontSize: '13px',
    outline: 'none',
  },
  toggleFavBtnOff: {
    backgroundColor: 'transparent',
    border: '1px solid #1f2937',
    color: '#94a3b8',
    padding: '8px 14px',
    borderRadius: '8px',
    fontSize: '12px',
    fontWeight: 600,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
  },
  toggleFavBtnOn: {
    backgroundColor: 'rgba(239, 68, 68, 0.15)',
    border: '1px solid #ef4444',
    color: '#ef4444',
    padding: '8px 14px',
    borderRadius: '8px',
    fontSize: '12px',
    fontWeight: 600,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
  },
  categoryScroll: {
    display: 'flex',
    gap: '8px',
    overflowX: 'auto',
    whiteSpace: 'nowrap',
    paddingBottom: '2px',
  },
  categoryInactive: {
    backgroundColor: '#111827',
    border: '1px solid #1f2937',
    color: '#94a3b8',
    padding: '6px 14px',
    borderRadius: '16px',
    fontSize: '12px',
    fontWeight: 600,
    cursor: 'pointer',
  },
  categoryActive: {
    backgroundColor: '#3b82f6',
    border: '1px solid #3b82f6',
    color: '#ffffff',
    padding: '6px 14px',
    borderRadius: '16px',
    fontSize: '12px',
    fontWeight: 700,
    cursor: 'pointer',
  },
  gridContainer: {
    flex: 1,
    padding: '24px',
  },
  channelsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
    gap: '16px',
  },
  channelCard: {
    backgroundColor: '#111827',
    border: '1px solid #1f2937',
    borderRadius: '12px',
    overflow: 'hidden',
    cursor: 'pointer',
    transition: 'transform 0.2s, border-color 0.2s, box-shadow 0.2s',
  },
  channelCardSelected: {
    borderColor: '#3b82f6',
    boxShadow: '0 0 0 2px rgba(59, 130, 246, 0.3)',
    transform: 'scale(1.02)'
  },
  cardThumbnailWrapper: {
    position: 'relative',
    height: '110px',
    backgroundColor: '#0a0d14',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '12px',
  },
  cardLogoImage: {
    maxWidth: '100%',
    maxHeight: '100%',
    objectFit: 'contain',
  },
  cardLogoFallback: {
    width: '45px',
    height: '45px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#fff',
    fontWeight: 'black',
    fontSize: '20px',
  },
  liveOverlayBadge: {
    position: 'absolute',
    top: '8px',
    left: '8px',
    backgroundColor: '#ef4444',
    color: '#fff',
    fontSize: '8px',
    fontWeight: 900,
    padding: '1px 5px',
    borderRadius: '3px',
  },
  favoriteCardBadge: {
    position: 'absolute',
    top: '6px',
    right: '6px',
    backgroundColor: 'rgba(15, 23, 42, 0.65)',
    border: 'none',
    borderRadius: '50%',
    width: '24px',
    height: '24px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
  },
  cardInfo: {
    padding: '12px',
    borderTop: '1px solid #1f2937',
  },
  cardChannelName: {
    fontSize: '13px',
    fontWeight: 700,
    margin: '0 0 6px 0',
    color: '#ffffff',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  cardMeta: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardCountry: {
    fontSize: '10px',
    color: '#64748b',
    fontWeight: 600,
  },
  cardCategoryText: {
    fontSize: '9px',
    color: '#3b82f6',
    fontWeight: 700,
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    padding: '1px 5px',
    borderRadius: '3px',
  },
  shimmerCard: {
    backgroundColor: '#111827',
    border: '1px solid #1f2937',
    borderRadius: '12px',
    overflow: 'hidden',
    height: '168px',
    animation: 'pulse 1.2s infinite'
  },
  shimmerThum: {
    height: '110px',
    backgroundColor: '#1c2438',
  },
  shimmerDetails: {
    padding: '12px',
  },
  shimmerTitle: {
    height: '12px',
    backgroundColor: '#1c2438',
    width: '70%',
    marginBottom: '8px',
    borderRadius: '2px',
  },
  shimmerSubtitle: {
    height: '8px',
    backgroundColor: '#151d30',
    width: '40%',
    borderRadius: '2px',
  },
  emptyState: {
    textAlign: 'center',
    padding: '48px 24px',
    color: '#64748b',
  },
  emptyIcon: {
    width: '48px',
    height: '48px',
    color: '#334155',
    marginBottom: '12px',
  },
  resetFilterBtn: {
    backgroundColor: '#1f2937',
    border: '1px solid #374151',
    color: '#f3f4f6',
    padding: '8px 16px',
    borderRadius: '8px',
    marginTop: '16px',
    fontWeight: 600,
    fontSize: '12px',
    cursor: 'pointer',
  },
  footer: {
    backgroundColor: '#0b111e',
    borderTop: '1px solid #141f35',
    padding: '16px',
    textAlign: 'center',
    fontSize: '12px',
    color: '#475569',
  },
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(2, 4, 8, 0.85)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
    backdropFilter: 'blur(4px)',
  },
  modalBody: {
    backgroundColor: '#0f172a',
    border: '1px solid #1e293b',
    borderRadius: '16px',
    width: '100%',
    maxWidth: '460px',
    padding: '24px',
    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  modalTitle: {
    fontSize: '18px',
    fontWeight: 800,
    color: '#ffffff',
    margin: 0,
  },
  closeModalBtn: {
    background: 'none',
    border: 'none',
    color: '#64748b',
    fontSize: '24px',
    cursor: 'pointer',
    lineHeight: '1',
  },
  formGroup: {
    marginBottom: '16px',
  },
  formLabel: {
    display: 'block',
    fontSize: '12px',
    fontWeight: 700,
    color: '#94a3b8',
    marginBottom: '6px',
  },
  formInput: {
    width: '100%',
    backgroundColor: '#0b0f19',
    border: '1px solid #1e293b',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '13px',
    outline: 'none',
  },
  formActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '12px',
    marginTop: '24px',
  },
  formCancelBtn: {
    backgroundColor: 'transparent',
    border: '1px solid #1e293b',
    color: '#94a3b8',
    borderRadius: '8px',
    padding: '8px 16px',
    fontSize: '13px',
    fontWeight: 600,
    cursor: 'pointer',
  },
  formSubmitBtn: {
    backgroundColor: '#3b82f6',
    border: 'none',
    color: '#ffffff',
    borderRadius: '8px',
    padding: '8px 20px',
    fontSize: '13px',
    fontWeight: 700,
    cursor: 'pointer',
  }
};
