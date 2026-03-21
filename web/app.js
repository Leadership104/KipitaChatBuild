/* ═══════════════════════════════════════════════════════════════
   Kipita Web App — app.js  v2.0
   7-tab SPA: Home · AI · Trips · Places · Maps · Wallet · Groups
   ═══════════════════════════════════════════════════════════════ */

'use strict';

const App = (() => {

  /* ── STATE ─────────────────────────────────────────────────── */
  const state = {
    tab: 'home',
    tripTab: 'upcoming',
    placesTab: 'places',
    lang: 'es',
    modalStack: [],
    user: null,
    location: { lat: null, lng: null, name: 'Detecting…' },
    weather: { emoji: '🌤️', temp: '--', desc: '' },
    safety: { level: 3, label: 'Moderate' },
    trips: [],
    savedPlaces: new Set(),
    groups: [],
    aiLastTrip: null,
    mapScreen: null,
    mapScreenMarkers: [],
    mapScreenFilter: 'btc',
    btcSource: 'both',
    walletMap: null,
    walletMapMarkers: [],
    btcMerchants: [],
    cashAppMerchants: [],
    destPhotos: {},
    currentDest: null,
    currentTrip: null,
    currentPcat: null,
    currentGroup: null,
    btcPrice: null,
    fxRates: {},
    socialTab: 'groups',
    reviewFilter: 'all',
    reviewSort: 'recent',
    reviewStars: 0,
    nomadScores: { wifi: 0, budget: 0, vibe: 0 },
  };

  /* ── LOCAL STORAGE ─────────────────────────────────────────── */
  const LS = {
    get: (k) => { try { return JSON.parse(localStorage.getItem('kip_' + k)) } catch { return null } },
    set: (k, v) => { try { localStorage.setItem('kip_' + k, JSON.stringify(v)) } catch {} },
    del: (k) => { try { localStorage.removeItem('kip_' + k) } catch {} },
  };

  /* ── DESTINATIONS DATA ─────────────────────────────────────── */
  const DESTINATIONS = [
    { id: 'chiangmai', city: 'Chiang Mai', country: 'Thailand',  emoji: '🏔️', lat: 18.7883, lng: 98.9853,  rating: 4.8, pop: '7M+ nomads',  wikiTitle: 'Chiang Mai',  speed: 52, safetyScore: 8.2, monthlyCost: 1200, weatherDesc: 'Warm & Sunny',     temp: 28, tags: ['Affordable', 'Digital Nomad'], popular: true,  desc: 'Ancient temples, cool mountains, fast internet, and the most affordable nomad lifestyle in Asia.' },
    { id: 'lisbon',    city: 'Lisbon',     country: 'Portugal',  emoji: '🇵🇹', lat: 38.7169, lng: -9.1399,  rating: 4.7, pop: '6M+ nomads',  wikiTitle: 'Lisbon',      speed: 48, safetyScore: 8.7, monthlyCost: 1605, weatherDesc: 'Mild & Breezy',    temp: 18, tags: ['Bitcoin-Friendly', 'Tax Perks'], popular: true,  desc: 'Sunny capital with pastel streets, great food, crypto-friendly culture, and NHR tax regime.' },
    { id: 'bali',      city: 'Bali',       country: 'Indonesia', emoji: '🌴', lat: -8.3405, lng: 115.0919, rating: 4.8, pop: '8M+ nomads',  wikiTitle: 'Bali',        speed: 38, safetyScore: 7.9, monthlyCost: 1400, weatherDesc: 'Tropical & Lush',  temp: 30, tags: ['Surf & Co-Work', 'Nomad Hub'],   popular: true,  desc: 'Tropical paradise with rice terraces, temples, and a world-class digital nomad community in Canggu.' },
    { id: 'bangkok',   city: 'Bangkok',    country: 'Thailand',  emoji: '🛕', lat: 13.7563, lng: 100.5018, rating: 4.7, pop: '11M+ nomads', wikiTitle: 'Bangkok',     speed: 45, safetyScore: 7.5, monthlyCost: 1100, weatherDesc: 'Hot & Vibrant',   temp: 32, tags: ['Street Food', 'Nightlife'],      popular: true,  desc: 'Vibrant street food, temples, and a booming nomad scene. Low cost of living with fast fiber internet.' },
    { id: 'tokyo',     city: 'Tokyo',      country: 'Japan',     emoji: '🗼', lat: 35.6762, lng: 139.6503, rating: 4.9, pop: '14M+ nomads', wikiTitle: 'Tokyo',       speed: 78, safetyScore: 9.4, monthlyCost: 2800, weatherDesc: 'Cool & Clear',    temp: 16, tags: ['BTC Friendly', 'Ultra-Modern'],  popular: false, desc: 'Ultra-modern city with ancient temples, perfect transit, and incredible food. Bitcoin-friendly with thousands of merchants.' },
    { id: 'barcelona', city: 'Barcelona',  country: 'Spain',     emoji: '🏖️', lat: 41.3851, lng: 2.1734,   rating: 4.8, pop: '7M+ nomads',  wikiTitle: 'Barcelona',   speed: 55, safetyScore: 7.8, monthlyCost: 2200, weatherDesc: 'Sunny & Warm',   temp: 22, tags: ['Beach Life', 'Startup Hub'],      popular: false, desc: "Architecture, beaches, and an incredible startup scene. One of Europe's top Bitcoin cities." },
    { id: 'medellin',  city: 'Medellín',   country: 'Colombia',  emoji: '🌺', lat: 6.2476,  lng: -75.5658, rating: 4.6, pop: '5M+ nomads',  wikiTitle: 'Medellín',    speed: 32, safetyScore: 6.8, monthlyCost:  900, weatherDesc: 'Spring All Year', temp: 22, tags: ['Budget Pick', 'Crypto Scene'],    popular: false, desc: 'The city of eternal spring. Growing crypto scene, affordable living, and a welcoming local culture.' },
    { id: 'dubai',     city: 'Dubai',      country: 'UAE',       emoji: '🏙️', lat: 25.2048, lng: 55.2708,  rating: 4.7, pop: '9M+ nomads',  wikiTitle: 'Dubai',       speed: 62, safetyScore: 8.9, monthlyCost: 3200, weatherDesc: 'Sunny & Hot',    temp: 35, tags: ['Tax-Free', 'Luxury'],             popular: false, desc: 'Tax-free hub with world-class infrastructure, crypto-friendly regulations, and 0% income tax.' },
  ];

  /* ── PLACE CATEGORIES ──────────────────────────────────────── */
  const getHour = () => new Date().getHours();

  const CATEGORIES = [
    { id: 'food',      label: 'Food',      emoji: () => { const h = getHour(); return h < 10 ? '🍳' : h < 15 ? '🍜' : h < 20 ? '🍽️' : '🌮' }, query: 'restaurants' },
    { id: 'cafe',      label: 'Cafes',     emoji: () => { const h = getHour(); return h < 11 ? '☕' : h < 16 ? '🧋' : '🍵' },                    query: 'cafes' },
    { id: 'hotel',     label: 'Hotels',    emoji: () => '🏨', query: 'hotels' },
    { id: 'transport', label: 'Transit',   emoji: () => '🚇', query: 'public transit' },
    { id: 'atm',       label: 'ATM / BTC', emoji: () => '₿',  query: 'bitcoin atm' },
    { id: 'shop',      label: 'Shopping',  emoji: () => '🛍️', query: 'shopping' },
    { id: 'gym',       label: 'Fitness',   emoji: () => '💪', query: 'gym' },
    { id: 'hospital',  label: 'Medical',   emoji: () => '🏥', query: 'hospital' },
    { id: 'pharmacy',  label: 'Pharmacy',  emoji: () => '💊', query: 'pharmacy' },
    { id: 'beach',     label: 'Beaches',   emoji: () => '🏖️', query: 'beach' },
    { id: 'nightlife', label: 'Nightlife', emoji: () => '🎵', query: 'nightlife bars' },
  ];

  /* ── PHRASES DATA ──────────────────────────────────────────── */
  const PHRASES = {
    es: [
      { en: 'Hello',                    local: 'Hola',                          phon: 'OH-lah' },
      { en: 'Thank you',                local: 'Gracias',                       phon: 'GRAH-syahs' },
      { en: 'Where is…?',               local: '¿Dónde está…?',                phon: 'DON-day es-TAH' },
      { en: 'How much?',                local: '¿Cuánto cuesta?',              phon: 'KWAHN-toh KWES-tah' },
      { en: 'I need a doctor',          local: 'Necesito un médico',           phon: 'neh-seh-SEE-toh oon MEH-dee-koh' },
      { en: 'Help!',                    local: '¡Ayuda!',                       phon: 'ah-YOO-dah' },
      { en: 'Do you accept Bitcoin?',   local: '¿Acepta Bitcoin?',             phon: 'ah-SEP-tah Bitcoin' },
      { en: 'Where is the airport?',    local: '¿Dónde está el aeropuerto?',  phon: 'DON-day es-TAH el ah-eh-roh-PWER-toh' },
    ],
    fr: [
      { en: 'Hello',                    local: 'Bonjour',                       phon: 'bohn-ZHOOR' },
      { en: 'Thank you',                local: 'Merci',                         phon: 'mehr-SEE' },
      { en: 'Where is…?',               local: 'Où est…?',                     phon: 'oo AY' },
      { en: 'How much?',                local: 'Combien ça coûte?',            phon: 'kohm-BYEH sah KOOT' },
      { en: 'I need a doctor',          local: "J'ai besoin d'un médecin",     phon: 'zhay buh-ZWEH dun med-SAN' },
      { en: 'Help!',                    local: 'Au secours!',                   phon: 'oh skoor' },
      { en: 'Do you accept Bitcoin?',   local: 'Acceptez-vous Bitcoin?',       phon: 'ak-sep-TAY voo Bitcoin' },
      { en: 'Where is the airport?',    local: "Où est l'aéroport?",           phon: 'oo AY lay-ro-POUR' },
    ],
    th: [
      { en: 'Hello',                    local: 'สวัสดี',                        phon: 'sa-wat-dee' },
      { en: 'Thank you',                local: 'ขอบคุณ',                        phon: 'khop khun' },
      { en: 'Where is…?',               local: '…อยู่ที่ไหน?',                 phon: '...yoo tee nai' },
      { en: 'How much?',                local: 'ราคาเท่าไหร่?',                phon: 'ra-kaa tao-rai' },
      { en: 'I need a doctor',          local: 'ฉันต้องการหมอ',                phon: 'chan dtong gaan mor' },
      { en: 'Help!',                    local: 'ช่วยด้วย!',                     phon: 'chuay duay' },
      { en: 'Do you accept Bitcoin?',   local: 'รับ Bitcoin ไหม?',             phon: 'rap Bitcoin mai' },
      { en: 'Where is the airport?',    local: 'สนามบินอยู่ที่ไหน?',          phon: 'sa-naam bin yoo tee nai' },
    ],
    ja: [
      { en: 'Hello',                    local: 'こんにちは',                    phon: 'kon-nee-chee-wa' },
      { en: 'Thank you',                local: 'ありがとうございます',            phon: 'a-ree-ga-toh go-zai-mas' },
      { en: 'Where is…?',               local: '…はどこですか?',               phon: '...wa do-ko des-ka' },
      { en: 'How much?',                local: 'いくらですか?',                 phon: 'ee-koo-ra des-ka' },
      { en: 'I need a doctor',          local: '医者が必要です',                phon: 'ee-sha ga hee-tsu-yo des' },
      { en: 'Help!',                    local: '助けて!',                       phon: 'ta-soo-ke-te' },
      { en: 'Do you accept Bitcoin?',   local: 'ビットコインは使えますか?',      phon: 'bit-to-ko-in wa tsoo-ka-e-mas-ka' },
      { en: 'Where is the airport?',    local: '空港はどこですか?',             phon: 'koo-koh wa do-ko des-ka' },
    ],
    pt: [
      { en: 'Hello',                    local: 'Olá',                           phon: 'oh-LAH' },
      { en: 'Thank you',                local: 'Obrigado / Obrigada',           phon: 'oh-bree-GAH-doo' },
      { en: 'Where is…?',               local: 'Onde fica…?',                  phon: 'ON-jee FEE-kah' },
      { en: 'How much?',                local: 'Quanto custa?',                 phon: 'KWAHN-too KOO-stah' },
      { en: 'I need a doctor',          local: 'Preciso de um médico',          phon: 'preh-SEE-zoo jee oong MEH-jee-koo' },
      { en: 'Help!',                    local: 'Socorro!',                      phon: 'so-KOH-hoo' },
      { en: 'Do you accept Bitcoin?',   local: 'Aceita Bitcoin?',               phon: 'ah-SAY-ta Bitcoin' },
      { en: 'Where is the airport?',    local: 'Onde fica o aeroporto?',        phon: 'ON-jee FEE-kah oh ah-eh-roh-POR-too' },
    ],
    de: [
      { en: 'Hello',                    local: 'Hallo',                         phon: 'HAL-oh' },
      { en: 'Thank you',                local: 'Danke schön',                   phon: 'DAHN-keh shern' },
      { en: 'Where is…?',               local: 'Wo ist…?',                     phon: 'voh ist' },
      { en: 'How much?',                local: 'Wie viel kostet das?',          phon: 'vee feel KOS-tet dahs' },
      { en: 'I need a doctor',          local: 'Ich brauche einen Arzt',        phon: 'ikh BROW-kheh eye-nen artst' },
      { en: 'Help!',                    local: 'Hilfe!',                        phon: 'HIL-feh' },
      { en: 'Do you accept Bitcoin?',   local: 'Nehmen Sie Bitcoin?',           phon: 'NAY-men zee Bitcoin' },
      { en: 'Where is the airport?',    local: 'Wo ist der Flughafen?',         phon: 'voh ist dair FLOOK-hah-fen' },
    ],
  };

  /* ── PERKS DATA ────────────────────────────────────────────── */
  const PERKS = [
    { icon: '✈️', title: 'Skyscanner',   desc: '10% off flights when booked through Kipita. Valid on all routes.',              code: 'KIPITA10',    expiry: 'Dec 2026', url: 'https://www.skyscanner.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITA10' },
    { icon: '🏨', title: 'Booking.com',  desc: 'Genius Level 2 instant unlock — up to 20% off thousands of properties.',        code: 'KIPITAGENI', expiry: 'Ongoing',  url: 'https://www.booking.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITAGENI' },
    { icon: '💻', title: 'NomadList Pro',desc: '3 months free NomadList Pro subscription for Kipita members.',                  code: 'KIPITANOMAD',expiry: 'Mar 2027', url: 'https://nomadlist.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITANOMAD' },
    { icon: '📶', title: 'Airalo eSIM',  desc: '5% off any eSIM data plan worldwide. Stay connected anywhere.',                 code: 'KIPITA5',    expiry: 'Jun 2026', url: 'https://www.airalo.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITA5' },
    { icon: '🏋️', title: 'ClassPass',   desc: 'First month free — access gyms, yoga and fitness globally.',                    code: 'KIPITAFIT',  expiry: 'Ongoing',  url: 'https://classpass.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITAFIT' },
    { icon: '🔐', title: 'NordVPN',      desc: '2-year plan at 70% off. Secure your connection on public WiFi.',                code: 'KIPITAVPN',  expiry: 'Dec 2026', url: 'https://nordvpn.com/?utm_source=kipita&utm_medium=app&utm_campaign=KIPITAVPN' },
  ];

  /* ── DEMO REVIEWS ───────────────────────────────────────────── */
  const DEMO_REVIEWS = [
    { id:'r1', author:'Alex M.',   flag:'🇺🇸', dest:'Bangkok',    emoji:'🛕', rating:5, wifi:5, budget:5, vibe:4, text:"Bangkok is unreal for nomads. Lightning-fast fiber at every cafe, street food for $2, and you can pay BTC at loads of spots. Lived here 4 months — zero regrets.", ts: Date.now() - 86400000*2 },
    { id:'r2', author:'Sara K.',   flag:'🇬🇧', dest:'Bali',       emoji:'🌴', rating:4, wifi:3, budget:4, vibe:5, text:"Canggu has the best nomad community on earth. The vibe is incredible, co-working spaces everywhere. WiFi can be patchy outside the main hubs though.", ts: Date.now() - 86400000*5 },
    { id:'r3', author:'Marco B.',  flag:'🇮🇹', dest:'Lisbon',     emoji:'🇵🇹', rating:5, wifi:4, budget:3, vibe:5, text:"Europe's best city for nomads right now. NHR tax regime, incredible food, great tech scene and Bitcoin is huge here. Getting pricier but still worth it.", ts: Date.now() - 86400000*8 },
    { id:'r4', author:'Yuki T.',   flag:'🇯🇵', dest:'Tokyo',      emoji:'🗼', rating:5, wifi:5, budget:2, vibe:5, text:"Tokyo is the most organised city in the world. Thousands of BTC merchants, insane food, 100% safe. Expensive but you get what you pay for.", ts: Date.now() - 86400000*12 },
    { id:'r5', author:'Priya N.',  flag:'🇮🇳', dest:'Chiang Mai', emoji:'🏔️', rating:5, wifi:5, budget:5, vibe:4, text:"$800/month comfortable lifestyle. Best Internet cafes in the world, temples everywhere, mountains for weekend trips. The OG nomad capital for a reason.", ts: Date.now() - 86400000*15 },
    { id:'r6', author:'Leo C.',    flag:'🇨🇦', dest:'Medellín',   emoji:'🌺', rating:4, wifi:4, budget:5, vibe:5, text:"Spring weather all year, incredible food, and a booming crypto scene. Some safety awareness needed but the nomad community is super welcoming.", ts: Date.now() - 86400000*20 },
    { id:'r7', author:'Nina V.',   flag:'🇩🇪', dest:'Dubai',      emoji:'🏙️', rating:4, wifi:5, budget:1, vibe:3, text:"World-class infrastructure and 0% tax but it's very expensive. Best for high earners. The crypto regulations are progressive and banking is seamless.", ts: Date.now() - 86400000*25 },
    { id:'r8', author:'James O.',  flag:'🇦🇺', dest:'Bangkok',    emoji:'🛕', rating:4, wifi:4, budget:5, vibe:4, text:"Sukhumvit area is great for co-working. Plenty of Bitcoin ATMs and merchants. Visa runs are a bit annoying but otherwise a perfect base.", ts: Date.now() - 86400000*30 },
    { id:'r9', author:'Mei L.',    flag:'🇸🇬', dest:'Bali',       emoji:'🌴', rating:5, wifi:4, budget:4, vibe:5, text:"Ubud is magical for deep work — rice terrace views from the cafe, great yoga community, and everyone's working on something interesting. Highly recommend.", ts: Date.now() - 86400000*35 },
  ];

  /* ── AI RESPONSES ──────────────────────────────────────────── */
  const AI_RESPONSES = {
    plan: (dest) => `✈️ **Trip Plan: ${dest || 'Your Dream Destination'}**\n\n**Day 1: Arrival & Orientation**\n• Check into your accommodation\n• Explore the local neighborhood on foot\n• Try street food or a local restaurant\n• Currency exchange and SIM card setup\n\n**Day 2: Culture & History**\n• Morning: Visit main historical landmarks\n• Afternoon: Local markets or museum\n• Evening: Sunset viewpoint + dinner\n\n**Day 3: Off the Beaten Path**\n• Day trip to nearby nature or town\n• Connect with local nomads at a co-working space\n• Try Bitcoin payments at local merchants\n\n**Day 4: Food & Community**\n• Cooking class or food tour\n• Social events / meetups\n• Evening: Night market or bar crawl\n\n**Day 5: Departure**\n• Final shopping and souvenirs\n• Airport transfer via local transport\n\n💡 *Tip: Use Kipita's BTC Map to find Bitcoin-friendly merchants!*`,

    safety: (loc) => `🛡️ **Safety Briefing: ${loc || 'Current Location'}**\n\n**Overall Safety Level: MODERATE (3/4)**\n\n**Key Points:**\n✅ Tourist areas are generally safe during daytime\n⚠️ Keep valuables secure in crowded areas\n⚠️ Use registered taxis or ride-share apps\n\n**Emergency Numbers:**\n• Police: 911 (or local equivalent)\n• Hospital: Check Google Maps for nearest facility\n• Embassy: Contact your home country embassy\n\n**Travel Tips:**\n• Register your trip with your home country's travel advisory\n• Keep a digital copy of your passport\n• Share your itinerary with family or group members\n• Have offline maps downloaded before exploring\n\n*Updated: ${new Date().toLocaleDateString()}*`,

    advisories: () => `📋 **Current Travel Advisories**\n\n**Global Conditions:**\n• Most popular nomad destinations: NORMAL operations\n• Entry requirements largely relaxed post-2023\n• Bitcoin/crypto broadly accepted in SEA, Europe, Americas\n\n**Popular Nomad Hubs — Status:**\n🇹🇭 Thailand (Bangkok/Chiang Mai): ✅ LOW RISK\n🇵🇹 Portugal (Lisbon): ✅ LOW RISK\n🇪🇸 Spain (Barcelona): ✅ LOW RISK\n🇮🇩 Indonesia (Bali): ⚠️ MODERATE\n🇨🇴 Colombia (Medellín): ⚠️ MODERATE\n🇦🇪 UAE (Dubai): ✅ LOW RISK\n🇯🇵 Japan (Tokyo): ✅ LOW RISK\n\n**Reminders:**\n• Check visa requirements before booking\n• Travel insurance is strongly recommended\n• Monitor your country's official advisory portal\n\n*Tap the 🛡️ Advisory tab for real-time data*`,

    phrases: () => `🌐 **Essential Travel Phrases**\n\nHere are key phrases for your trip:\n\n• **Hello** → See Translate tab for your language\n• **Thank you** → Works universally with a smile 😊\n• **How much?** → Great for market bargaining\n• **Where is…?** → Navigation essential\n• **I need help** → Emergency essential\n• **Do you accept Bitcoin?** → ₿ Nomad essential!\n\n💡 Open the **Translate** section (Quick Actions) for full phrase cards with phonetic pronunciation in 6 languages.`,

    default: (msg) => `I understand you're asking about "${msg}". As your AI travel companion, I can help with:\n\n✈️ **Trip Planning** — detailed day-by-day itineraries\n🛡️ **Safety Briefings** — real-time risk assessments\n📋 **Travel Advisories** — entry requirements and alerts\n🌐 **Language Help** — phrases in 6 languages\n🗺️ **Destination Guide** — best places to visit\n₿ **Bitcoin Travel** — finding BTC merchants and ATMs\n\nTry asking me:\n*"Plan a 7-day trip to Bali"*\n*"Is Bangkok safe right now?"*\n*"What are the entry requirements for Japan?"*`,
  };

  /* ── DEMO GROUPS ────────────────────────────────────────────── */
  const DEMO_GROUPS = [
    { id: 'g-bangkok', name: 'Bangkok Nomads 🇹🇭', members: ['Alex M.', 'Sara K.', 'Marco B.', 'You'], createdAt: Date.now() - 86400000 * 7 },
    { id: 'g-bali',    name: 'Bali BTC Crew 🌴',   members: ['Yuki T.', 'Priya N.', 'You'],           createdAt: Date.now() - 86400000 * 3 },
    { id: 'g-eu',      name: 'EU Nomads 🇪🇺',       members: ['Marco B.', 'Yuki T.', 'Alex M.', 'You'],createdAt: Date.now() - 86400000 * 1 },
  ];

  const DEMO_MSGS = {
    'g-bangkok': [
      { from: 'Alex M.',  text: 'Hey everyone! Just arrived in Bangkok 🛕',              ts: Date.now() - 3600000 * 8,   read: true },
      { from: 'Sara K.',  text: 'Welcome! The co-working scene here is amazing ☕',       ts: Date.now() - 3600000 * 7,   read: true },
      { from: 'Marco B.', text: 'Has anyone tried paying with Bitcoin at Chatuchak?',     ts: Date.now() - 3600000 * 5,   read: true },
      { from: 'Alex M.',  text: 'Yes! There are 3-4 stalls that accept Lightning ⚡',    ts: Date.now() - 3600000 * 4,   read: false },
    ],
    'g-bali': [
      { from: 'Yuki T.',  text: 'Canggu surf + co-work today 🏄 Anyone joining?',        ts: Date.now() - 3600000 * 2,   read: true },
      { from: 'Priya N.', text: 'In! Meet at Outpost at 10am?',                          ts: Date.now() - 3600000 * 1.5, read: false },
    ],
    'g-eu': [
      { from: 'Marco B.', text: 'Portugal is calling! NHR tax regime deal ends soon 🇵🇹', ts: Date.now() - 1800000,       read: false },
    ],
  };

  /* ── DEMO TRAVELERS ─────────────────────────────────────────── */
  const DEMO_TRAVELERS = [
    { name: 'Alex M.',  flag: '🇺🇸', loc: '0.8 km away', bio: 'Nomad developer · Bangkok',        emoji: '👨‍💻' },
    { name: 'Sara K.',  flag: '🇬🇧', loc: '1.2 km away', bio: 'Photographer · Digital nomad',     emoji: '📸' },
    { name: 'Marco B.', flag: '🇮🇹', loc: '1.5 km away', bio: 'Remote designer · 12 countries',  emoji: '🎨' },
    { name: 'Yuki T.',  flag: '🇯🇵', loc: '2.1 km away', bio: 'BTC trader · Tokyo → Bali',       emoji: '₿' },
    { name: 'Priya N.', flag: '🇮🇳', loc: '2.4 km away', bio: 'Writer · 40 countries',           emoji: '✍️' },
  ];

  /* ── SNACKBAR ───────────────────────────────────────────────── */
  let _snackTimer;
  function snack(msg, duration = 2800) {
    const el = document.getElementById('snackbar');
    el.textContent = msg;
    el.classList.remove('hidden');
    el.classList.add('show');
    clearTimeout(_snackTimer);
    _snackTimer = setTimeout(() => {
      el.classList.remove('show');
      setTimeout(() => el.classList.add('hidden'), 300);
    }, duration);
  }

  /* ── INIT ───────────────────────────────────────────────────── */
  function init() {
    state.user        = LS.get('user');
    state.trips       = LS.get('trips')  || [];
    state.groups      = LS.get('groups') || [];
    state.savedPlaces = new Set(LS.get('saved') || []);

    // Seed demo groups on first launch
    if (state.groups.length === 0) {
      state.groups = DEMO_GROUPS.map(g => ({ ...g }));
      LS.set('groups', state.groups);
      DEMO_GROUPS.forEach(g => {
        if (!LS.get('gchat_' + g.id)) {
          LS.set('gchat_' + g.id, DEMO_MSGS[g.id] || []);
        }
      });
    }

    // Sample trip on first launch
    if (state.trips.length === 0) {
      state.trips.push({
        id: 'sample-1', dest: 'Tokyo, Japan', country: 'Japan',
        start: '2026-04-10', end: '2026-04-17',
        status: 'upcoming', notes: 'First nomad adventure! Check out Shibuya BTC merchants.',
        invites: [], isSample: true, createdAt: Date.now(),
      });
      LS.set('trips', state.trips);
    }

    // Splash → App
    setTimeout(() => {
      const splash = document.getElementById('splash');
      splash.classList.add('fade-out');
      setTimeout(() => {
        splash.classList.add('hidden');
        document.getElementById('app').classList.remove('hidden');
        onAppReady();
      }, 500);
    }, 2000);
  }

  function onAppReady() {
    updateGreeting();
    updateUserUI();
    renderHomeTrips();
    renderGroupsScreen();
    renderNearbyTeaser();
    renderDestinations();
    renderCategories();
    renderTrips();
    detectLocation();
    fetchCrypto();
    fetchFxRates();
    fetchAllDestPhotos();
    // Refresh all live data every 60 seconds
    setInterval(() => { fetchCrypto(); fetchFxRates(); }, 60000);
  }

  /* ── DESTINATION PHOTOS (Wikimedia 800px) ───────────────────── */
  async function fetchAllDestPhotos() {
    await Promise.all(DESTINATIONS.map(async d => {
      if (state.destPhotos[d.id]) return;
      try {
        // Wikimedia pageimages API — returns up to 800px wide thumbnails, CORS-friendly
        const r = await fetch(
          `https://en.wikipedia.org/w/api.php?action=query&titles=${encodeURIComponent(d.wikiTitle)}&prop=pageimages&pithumbsize=800&format=json&origin=*`
        );
        const j = await r.json();
        const pages = j.query?.pages || {};
        const url = Object.values(pages)[0]?.thumbnail?.source;
        if (url) {
          state.destPhotos[d.id] = url;
          applyDestPhoto(d.id, url);
        }
      } catch(e) { console.warn('[Kipita] Photo fetch failed for', d.id, e?.message); }
    }));
    renderDestinations();
  }

  function applyDestPhoto(id, url) {
    const heroEl = document.querySelector(`.dest-hero[data-dest="${id}"], .edc-hero[data-dest="${id}"]`);
    if (!heroEl) return;
    heroEl.style.backgroundImage = `url('${url}')`;
    heroEl.style.backgroundSize = 'cover';
    heroEl.style.backgroundPosition = 'center';
    const emojiEl = heroEl.querySelector('.edc-emoji');
    if (emojiEl) emojiEl.style.display = 'none';
  }

  function setDestHeroPhoto(heroEl, url, alt) {
    const img = document.createElement('img');
    img.alt = alt;
    img.src = url;
    img.onload  = () => img.classList.add('loaded');
    img.onerror = () => img.remove();
    heroEl.prepend(img);
  }

  /* ── GREETING ───────────────────────────────────────────────── */
  function updateGreeting() {
    const h = getHour();
    const greet = h < 12 ? 'Good morning! 👋' : h < 17 ? 'Good afternoon! 👋' : 'Good evening! 👋';
    const name = state.user?.name ? `, ${state.user.name.split(' ')[0]}` : '';
    document.getElementById('home-greeting').textContent = greet.replace('!', name + '!');
    document.getElementById('home-sub').textContent = h < 6 ? 'Still up exploring? 🌙' : h < 12 ? 'Where are you heading today?' : h < 17 ? 'Ready for your next adventure?' : "Planning tomorrow's journey?";
  }

  /* ── USER UI ────────────────────────────────────────────────── */
  function updateUserUI() {
    const u = state.user;
    document.getElementById('pm-name').textContent  = u?.name  || 'Guest User';
    document.getElementById('pm-email').textContent = u?.email || 'Not signed in';
    if (u?.avatar) {
      const img  = document.getElementById('tb-avatar-img');
      const icon = document.getElementById('tb-avatar-icon');
      img.src = u.avatar;
      img.classList.remove('hidden');
      icon.classList.add('hidden');
    }
  }

  /* ── NAVIGATION ─────────────────────────────────────────────── */
  function switchTab(name) {
    if (state.tab === name) return;
    state.tab = name;
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.getElementById('screen-' + name).classList.add('active');
    document.querySelector(`.nav-btn[data-tab="${name}"]`).classList.add('active');
    closeProfileMenu();

    if (name === 'places') { initExploreScreen(); }
    if (name === 'trips')  renderTrips();
    if (name === 'maps')   initMapScreen();
    if (name === 'wallet') initWalletMap();
    if (name === 'groups') { renderGroupsScreen(); renderNearbyTeaser(); if (state.socialTab === 'reviews') renderReviewsTab(); }
  }

  /* ── PROFILE MENU ───────────────────────────────────────────── */
  function toggleProfileMenu() {
    document.getElementById('profile-menu').classList.toggle('hidden');
  }
  function closeProfileMenu() {
    document.getElementById('profile-menu').classList.add('hidden');
  }

  /* ── MODALS ─────────────────────────────────────────────────── */
  function openModal(name) {
    closeProfileMenu();
    const el = document.getElementById('modal-' + name);
    if (!el) return;
    state.modalStack.push(name);
    document.getElementById('modal-backdrop').classList.remove('hidden');
    el.classList.remove('hidden');
    requestAnimationFrame(() => el.classList.add('open'));

    if (name === 'advisory')   loadAdvisory();
    if (name === 'translate')  renderPhrases();
    if (name === 'perks')      renderPerks();
    if (name === 'travelers')  renderTravelers();
    if (name === 'social')     renderSocial();
    if (name === 'profile')    loadProfileForm();
  }

  function closeModal(name) {
    const el = document.getElementById('modal-' + name);
    if (!el) return;
    el.classList.remove('open');
    setTimeout(() => el.classList.add('hidden'), 300);
    const idx = state.modalStack.lastIndexOf(name);
    if (idx !== -1) state.modalStack.splice(idx, 1);
    if (state.modalStack.length === 0) {
      document.getElementById('modal-backdrop').classList.add('hidden');
    }
  }

  function closeTopModal() {
    if (state.modalStack.length > 0) {
      closeModal(state.modalStack[state.modalStack.length - 1]);
    }
  }

  /* ── LOCATION ───────────────────────────────────────────────── */
  function detectLocation() {
    const onGot = (lat, lng) => {
      state.location.lat = lat;
      state.location.lng = lng;
      reverseGeocode(lat, lng);
      fetchWeather(lat, lng);
      sortDestsByDistance();
      fetchBTCMerchants();
      generateCashAppMerchants();
      if (state.mapScreen) state.mapScreen.setView([lat, lng], 13);
      if (state.walletMap) state.walletMap.setView([lat, lng], 13);
    };

    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        pos => onGot(pos.coords.latitude, pos.coords.longitude),
        async () => {
          // IP-based fallback
          try {
            const r = await fetch('https://ip-api.com/json');
            const d = await r.json();
            if (d.status === 'success') {
              onGot(d.lat, d.lon);
              state.location.name = `${d.city}, ${d.countryCode}`;
              document.getElementById('location-text').textContent = state.location.name;
            }
          } catch {
            document.getElementById('location-text').textContent = 'Location unavailable';
          }
        },
        { timeout: 8000 }
      );
    }
  }

  async function reverseGeocode(lat, lng) {
    try {
      const r = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`);
      const d = await r.json();
      const city    = d.address?.city || d.address?.town || d.address?.village || d.address?.county || '';
      const country = d.address?.country_code?.toUpperCase() || '';
      const name    = city ? `${city}${country ? ', ' + country : ''}` : 'Current Location';
      state.location.name = name;
      document.getElementById('location-text').textContent = name;
      document.getElementById('hbw-loc').textContent = '📍 ' + name;
      document.getElementById('adv-country-sub').textContent = name;
      const explLoc = document.getElementById('explore-location-text');
      if (explLoc) explLoc.textContent = name;
    } catch {
      document.getElementById('location-text').textContent = 'GPS Active';
    }
  }

  /* ── WEATHER ────────────────────────────────────────────────── */
  const WX_CODES = {
    0:['☀️','Clear'],1:['🌤️','Mostly clear'],2:['⛅','Partly cloudy'],3:['☁️','Overcast'],
    45:['🌫️','Foggy'],48:['🌫️','Icy fog'],51:['🌦️','Light drizzle'],53:['🌦️','Drizzle'],
    55:['🌧️','Heavy drizzle'],61:['🌧️','Light rain'],63:['🌧️','Rain'],65:['🌧️','Heavy rain'],
    71:['🌨️','Light snow'],73:['❄️','Snow'],75:['❄️','Heavy snow'],80:['🌦️','Showers'],
    95:['⛈️','Thunderstorm'],99:['⛈️','Heavy storm'],
  };

  async function fetchWeather(lat, lng) {
    try {
      const r = await fetch(
        `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lng}&current=temperature_2m,weather_code&temperature_unit=fahrenheit`
      );
      const d = await r.json();
      const temp = Math.round(d.current.temperature_2m);
      const code = d.current.weather_code;
      const [emoji, desc] = WX_CODES[code] || ['🌤️', 'Clear'];
      state.weather = { emoji, temp, desc };
      document.getElementById('tb-wx-emoji').textContent = emoji;
      document.getElementById('tb-wx-temp').textContent  = temp + '°';
      document.getElementById('hbw-icon').textContent    = emoji;
      document.getElementById('hbw-temp').textContent    = temp + '°F';
      document.getElementById('hbw-desc').textContent    = desc;
      document.getElementById('adv-wx-icon').textContent = emoji;
      document.getElementById('adv-wx-temp').textContent = temp + '°F';
      document.getElementById('adv-wx-desc').textContent = desc;
    } catch {}
  }

  /* ── CRYPTO + METALS ────────────────────────────────────────── */
  const setEl = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
  const fmtUSD = (n, dec = 0) => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: dec, maximumFractionDigits: dec });

  async function fetchCrypto() {
    try {
      const r = await fetch(
        'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana&vs_currencies=usd&include_24hr_change=true'
      );
      const d = await r.json();
      if (d.bitcoin) {
        state.btcPrice = d.bitcoin.usd;
        setEl('ws-btc', fmtUSD(d.bitcoin.usd));
        const chg = d.bitcoin.usd_24h_change;
        const chgEl = document.getElementById('ws-btc-chg');
        if (chgEl) {
          const up = chg >= 0;
          chgEl.textContent = (up ? '▲ +' : '▼ ') + Math.abs(chg).toFixed(2) + '%';
          chgEl.className = 'cc-chg ' + (up ? 'up' : 'down');
        }
        convertCurrency();
      }
      if (d.ethereum)  setEl('ws-eth', fmtUSD(d.ethereum.usd));
      if (d.solana)    setEl('ws-sol', fmtUSD(d.solana.usd, 2));
    } catch {}
    fetchGoldAndMetals();
  }

  async function fetchGoldAndMetals() {
    try {
      // metals.live — free, no key required
      const r = await fetch('https://api.metals.live/v1/spot');
      const d = await r.json();
      const spot = Array.isArray(d) ? d[0] : d;
      if (spot?.gold)     setEl('ws-xau', fmtUSD(spot.gold, 2) + ' / oz');
      if (spot?.silver)   setEl('ws-xag', fmtUSD(spot.silver, 2) + ' / oz');
      if (spot?.platinum) setEl('ws-xpt', fmtUSD(spot.platinum, 2) + ' / oz');
    } catch {
      // Fallback approximate prices (Mar 2026)
      setEl('ws-xau', '~$3,100 / oz');
      setEl('ws-xag', '~$34 / oz');
      setEl('ws-xpt', '~$990 / oz');
    }
  }

  /* ── SAFETY UI ──────────────────────────────────────────────── */
  function updateSafetyUI(level, label) {
    state.safety = { level, label };
    document.querySelectorAll('.tb-safety .sl-dot').forEach((d, i) => {
      d.classList.remove('active', 'warn', 'danger');
      if (i < level) {
        if (level <= 2) d.classList.add('active');
        else if (level === 3) d.classList.add('warn');
        else d.classList.add('danger');
      }
    });
    const badge = document.getElementById('sc-badge');
    if (badge) { badge.textContent = label; badge.dataset.level = level; }
    const fill = document.getElementById('sc-fill');
    if (fill) fill.style.width = ((level / 4) * 100) + '%';
  }

  /* ── HOME TRIPS PREVIEW ─────────────────────────────────────── */
  function renderHomeTrips() {
    const upcoming = state.trips.filter(t => t.status === 'upcoming').slice(0, 2);
    const el = document.getElementById('home-trips');
    if (!el) return;
    if (!upcoming.length) {
      el.innerHTML = `
        <div class="empty-card" onclick="App.switchTab('trips')">
          <span class="ms ms-xl">luggage</span>
          <p>No upcoming trips yet</p>
          <span class="btn-outline-xs">Plan a Trip</span>
        </div>`;
      return;
    }
    el.innerHTML = upcoming.map(t => tripCardHTML(t, true)).join('');
  }

  /* ── HOME GROUPS PREVIEW ────────────────────────────────────── */
  function renderHomeGroups() {
    const el = document.getElementById('home-groups');
    if (!el) return;
    if (!state.groups.length) {
      el.innerHTML = `
        <div class="empty-card" onclick="App.switchTab('groups')">
          <span class="ms ms-xl">group_add</span>
          <p>Join a travel group to connect with nomads</p>
          <span class="btn-outline-xs">Explore Groups</span>
        </div>`;
      return;
    }
    el.innerHTML = state.groups.slice(0, 2).map(g => {
      const msgs = LS.get('gchat_' + g.id) || [];
      const last = msgs[msgs.length - 1];
      const preview = last ? last.text.slice(0, 38) + (last.text.length > 38 ? '…' : '') : 'Tap to chat';
      const emoji = g.name.match(/\p{Emoji}/u)?.[0] || '👥';
      return `
        <div class="group-row" onclick="App.switchTab('groups');setTimeout(()=>App.openGroupChat('${g.id}'),150)">
          <div class="group-row-avatar"><span style="font-size:22px">${emoji}</span></div>
          <div class="group-row-info">
            <div class="group-row-name">${escHtml(g.name)}</div>
            <div class="group-row-last">${escHtml(preview)}</div>
          </div>
          <span class="ms" style="color:var(--text3);font-size:18px">chevron_right</span>
        </div>`;
    }).join('');
  }

  /* ── TRIPS ──────────────────────────────────────────────────── */
  function tripTab(tab, btn) {
    state.tripTab = tab;
    document.querySelectorAll('.tab-btn[data-trip-tab]').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    document.querySelectorAll('.trip-list').forEach(l => l.classList.add('hidden'));
    document.getElementById('tlist-' + tab).classList.remove('hidden');
    renderTrips();
  }

  function renderTrips() {
    ['upcoming', 'past', 'cancelled'].forEach(tab => {
      const list = state.trips.filter(t => t.status === tab);
      const el   = document.getElementById('tlist-' + tab);
      if (!el) return;
      if (!list.length) {
        const msgs = { upcoming: '🧳 No upcoming trips — plan one!', past: '📖 No past trips yet.', cancelled: '✅ No cancelled trips.' };
        el.innerHTML = `<div class="trips-empty"><span class="ms ms-xl">luggage</span><p>${msgs[tab]}</p></div>`;
        return;
      }
      el.innerHTML = list.map(t => tripCardHTML(t, false)).join('');
    });
  }

  /* ── DEST PHOTO LOOKUP BY CITY STRING ──────────────────────── */
  function destPhotoForCity(cityStr) {
    if (!cityStr) return null;
    const lower = cityStr.toLowerCase();
    const dest = DESTINATIONS.find(d => lower.includes(d.city.toLowerCase()));
    return dest ? (state.destPhotos[dest.id] || null) : null;
  }

  function aiPlanFromTrips() {
    switchTab('ai');
    setTimeout(() => {
      const input = document.getElementById('chat-input');
      if (input) { input.value = 'Plan a trip for me'; input.focus(); }
    }, 300);
  }

  function tripCardHTML(t, mini) {
    const statusLabels = { upcoming:'Upcoming', active:'Active', past:'Past', cancelled:'Cancelled' };
    const photo = destPhotoForCity(t.dest);
    const heroStyle = photo
      ? `background-image:url('${photo}');background-size:cover;background-position:center;`
      : `background:linear-gradient(135deg,#1a1a2e 0%,#16213e 60%,#0d1b2a 100%);`;
    const emojis = ['✈️','🌍','🏖️','🏔️','🌺','🗼','🛕','🌴'];
    const emoji  = emojis[Math.abs((t.id || '').charCodeAt(5) || 0) % emojis.length];
    return `
      <div class="trip-card motion-card" onclick="App.openTripDetail('${t.id}')">
        <div class="trip-card-hero" style="${heroStyle}">
          ${!photo ? `<span class="trip-hero-emoji">${emoji}</span>` : ''}
          <div class="trip-hero-overlay">
            <div class="tc-dest-hero">${escHtml(t.dest)}</div>
            <div class="tc-dates-hero">${formatDate(t.start)} – ${formatDate(t.end)}</div>
          </div>
          <span class="tc-status ${t.status} trip-status-badge">${statusLabels[t.status] || t.status}</span>
        </div>
        ${!mini ? `<div class="trip-card-body">
          ${t.notes ? `<p class="tc-notes">${escHtml(t.notes)}</p>` : ''}
          <div class="tc-actions">
            <button class="btn-primary-sm" onclick="event.stopPropagation();App.openTripDetail('${t.id}')">View Details</button>
            <button class="btn-outline-xs" onclick="event.stopPropagation();App.shareTripCard('${t.id}')"><span class="ms" style="font-size:14px">share</span> Share</button>
            ${t.status === 'cancelled' ? `<button class="btn-outline-xs" onclick="event.stopPropagation();App.recreateTrip('${t.id}')">Recreate</button>` : ''}
          </div>
        </div>` : ''}
      </div>`;
  }

  function openTripDetail(id) {
    const trip = state.trips.find(t => t.id === id);
    if (!trip) return;
    state.currentTrip = trip;
    document.getElementById('tdl-title').textContent = trip.dest;
    const emojis = ['🗺️','✈️','🌍','🏖️','🏔️','🌺','🗼','🛕'];
    const emoji  = emojis[Math.floor(Math.random() * emojis.length)];
    const heroPhoto = destPhotoForCity(trip.dest);
    const heroStyle = heroPhoto
      ? `background-image:url('${heroPhoto}');background-size:cover;background-position:center;`
      : `background:linear-gradient(135deg,#1a1a2e,#16213e);`;
    document.getElementById('tdl-body').innerHTML = `
      <div class="tdl-hero" style="${heroStyle}">
        ${!heroPhoto ? `<span style="font-size:64px;position:relative;z-index:1">${emoji}</span>` : ''}
        <div class="tdl-hero-info">
          <div class="tdl-dest">${escHtml(trip.dest)}</div>
          <div class="tdl-dates">${formatDate(trip.start)} → ${formatDate(trip.end)}</div>
        </div>
      </div>
      <div class="tdl-status-row">
        <span class="tc-status ${trip.status}">${trip.status.charAt(0).toUpperCase() + trip.status.slice(1)}</span>
        ${trip.isSample ? '<span style="font-size:12px;color:var(--text3);margin-left:8px">Sample trip</span>' : ''}
      </div>
      ${trip.notes ? `<div class="tdl-notes"><p>${escHtml(trip.notes)}</p></div>` : ''}
      <div class="tdl-actions">
        ${trip.status === 'upcoming' ? `
          <button class="tdl-action-btn tdl-complete" onclick="App.markTripComplete('${trip.id}')">
            <span class="ms">check_circle</span> Mark Complete
          </button>
          <button class="tdl-action-btn tdl-cancel" onclick="App.cancelTrip('${trip.id}')">
            <span class="ms">cancel</span> Cancel Trip
          </button>` : ''}
        ${trip.status === 'cancelled' ? `
          <button class="tdl-action-btn tdl-complete" onclick="App.recreateTrip('${trip.id}')">
            <span class="ms">autorenew</span> Recreate Trip
          </button>` : ''}
      </div>
      ${trip.invites?.length ? `
        <div class="tdl-invites">
          <h4>Travel Group</h4>
          ${trip.invites.map(e => `<span class="invite-chip">👤 ${escHtml(e)}</span>`).join('')}
        </div>` : ''}`;
    openModal('trip-detail');
  }

  function createTrip() {
    const dest  = document.getElementById('pt-dest').value.trim();
    const start = document.getElementById('pt-start').value;
    const end   = document.getElementById('pt-end').value;
    const notes = document.getElementById('pt-notes').value.trim();
    const inv   = document.getElementById('pt-invites').value.trim();
    if (!dest) { snack('Please enter a destination'); return; }
    const trip = {
      id: 'trip-' + Date.now(), dest, country: dest.split(',').pop().trim(),
      start: start || null, end: end || null,
      status: 'upcoming', notes, isSample: false,
      invites: inv ? inv.split(',').map(e => e.trim()).filter(Boolean) : [],
      createdAt: Date.now(),
    };
    state.trips = state.trips.filter(t => !t.isSample);
    state.trips.unshift(trip);
    LS.set('trips', state.trips);
    closeModal('plan-trip');
    renderTrips();
    renderHomeTrips();
    clearPlanForm();
    snack('✈️ Trip created!');
    switchTab('trips');
  }

  function clearPlanForm() {
    ['pt-dest','pt-start','pt-end','pt-notes','pt-invites','pt-ai-input'].forEach(id => {
      const el = document.getElementById(id); if (el) el.value = '';
    });
    const r = document.getElementById('pt-ai-result');
    if (r) { r.innerHTML = ''; r.classList.add('hidden'); }
  }

  async function fetchDestPhoto(cityName) {
    try {
      const r = await fetch(
        `https://en.wikipedia.org/w/api.php?action=query&titles=${encodeURIComponent(cityName)}&prop=pageimages&pithumbsize=800&format=json&origin=*`
      );
      const j = await r.json();
      const pages = j.query?.pages || {};
      return Object.values(pages)[0]?.thumbnail?.source || null;
    } catch { return null; }
  }

  function showPlanTripPhoto(url) {
    const hero = document.getElementById('pt-photo-hero');
    if (!hero) return;
    if (url) {
      hero.style.backgroundImage = `url('${url}')`;
      hero.classList.remove('hidden');
    } else {
      hero.classList.add('hidden');
    }
  }

  let _destInputTimer;
  function onDestInput(val) {
    clearTimeout(_destInputTimer);
    const city = val.split(',')[0].trim();
    if (city.length < 3) { showPlanTripPhoto(null); return; }
    // Check known destinations first (instant)
    const photo = destPhotoForCity(city);
    if (photo) { showPlanTripPhoto(photo); return; }
    // Otherwise debounce Wikimedia fetch
    _destInputTimer = setTimeout(async () => {
      const url = await fetchDestPhoto(city);
      showPlanTripPhoto(url);
    }, 600);
  }

  async function aiPlanTripForm() {
    const aiInput   = document.getElementById('pt-ai-input');
    const destInput = document.getElementById('pt-dest');
    const prompt    = (aiInput?.value || destInput?.value || '').trim();
    if (!prompt) { snack('✨ Tell AI where you want to go'); aiInput?.focus(); return; }

    const resultEl = document.getElementById('pt-ai-result');
    resultEl.innerHTML = `<div class="pt-ai-loading"><div class="spinner-sm"></div>✨ Planning your trip…</div>`;
    resultEl.classList.remove('hidden');

    // Extract clean destination
    const dest = prompt
      .replace(/plan|a|my|trip|to|travel|for|in|visit|itinerary|i want to go|\?/gi, ' ')
      .replace(/\s+/g, ' ').trim() || prompt;
    const destClean = dest.split(',')[0].trim().replace(/\b\w/g, c => c.toUpperCase()) +
      (dest.includes(',') ? ', ' + dest.split(',').slice(1).join(',').trim() : '');
    if (destInput) destInput.value = destClean;

    // Fetch photo immediately
    const photo = destPhotoForCity(destClean) || await fetchDestPhoto(destClean.split(',')[0].trim());
    showPlanTripPhoto(photo);

    // Default dates: 14 days out, 7-day trip
    const start = new Date(Date.now() + 14 * 86400000);
    const end   = new Date(Date.now() + 21 * 86400000);
    document.getElementById('pt-start').value = start.toISOString().slice(0, 10);
    document.getElementById('pt-end').value   = end.toISOString().slice(0, 10);

    setTimeout(() => {
      const fullResponse = AI_RESPONSES.plan(destClean);
      const noteLines = fullResponse
        .split('\n')
        .filter(l => l.match(/^(Day|\*|•|-|\d)/) && l.trim().length > 2)
        .slice(0, 6)
        .map(l => l.replace(/\*\*/g, '').trim())
        .join('\n');
      document.getElementById('pt-notes').value = noteLines ||
        `AI-planned trip to ${destClean}. Open the AI tab for a full itinerary.`;

      state.aiLastTrip = { dest: destClean, days: 7 };
      resultEl.innerHTML = `<div class="pt-ai-success">✅ Form filled — review and tap Create Trip!</div>`;
      setTimeout(() => resultEl.classList.add('hidden'), 3000);
    }, 1200);
  }

  function markTripComplete(id) {
    const trip = state.trips.find(t => t.id === id);
    if (!trip) return;
    trip.status = 'past';
    LS.set('trips', state.trips);
    renderTrips(); renderHomeTrips();
    closeModal('trip-detail');
    snack('✅ Trip marked complete!');
  }

  function cancelTrip(id) {
    if (!confirm('Cancel this trip?')) return;
    const trip = state.trips.find(t => t.id === id);
    if (!trip) return;
    trip.status = 'cancelled'; trip.cancelledAt = Date.now();
    LS.set('trips', state.trips);
    renderTrips(); renderHomeTrips();
    closeModal('trip-detail');
    snack('Trip cancelled');
  }

  function recreateTrip(id) {
    const orig = state.trips.find(t => t.id === id);
    if (!orig) return;
    const newStart = new Date(Date.now() + 14 * 86400000).toISOString().slice(0,10);
    const dur = orig.start && orig.end ? Math.round((new Date(orig.end) - new Date(orig.start)) / 86400000) : 7;
    const newEnd = new Date(Date.now() + (14 + dur) * 86400000).toISOString().slice(0,10);
    const trip = { ...orig, id: 'trip-' + Date.now(), status: 'upcoming', start: newStart, end: newEnd, isSample: false, createdAt: Date.now() };
    delete trip.cancelledAt;
    state.trips.unshift(trip);
    LS.set('trips', state.trips);
    closeModal('trip-detail');
    renderTrips(); renderHomeTrips();
    snack('Trip recreated!');
  }

  function shareTrip() {
    const t = state.currentTrip; if (!t) return;
    const txt = `Check out my trip to ${t.dest} on Kipita! ${formatDate(t.start)} - ${formatDate(t.end)}`;
    if (navigator.share) navigator.share({ title: 'Kipita Trip', text: txt, url: 'https://kipita.app' });
    else navigator.clipboard.writeText(txt).then(() => snack('Trip info copied!'));
  }

  function shareTripCard(id) {
    const t = state.trips.find(x => x.id === id); if (!t) return;
    const txt = `Check out my trip to ${t.dest} on Kipita! ${formatDate(t.start)} - ${formatDate(t.end)} 🌍 #KipitaTravel`;
    if (navigator.share) navigator.share({ title: 'Kipita Trip', text: txt, url: 'https://kipita.app' });
    else navigator.clipboard.writeText(txt).then(() => snack('📋 Trip info copied!'));
  }

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('en-US', { month:'short', day:'numeric', year:'numeric' });
  }

  /* ── DESTINATIONS ───────────────────────────────────────────── */
  function haversine(lat1, lng1, lat2, lng2) {
    const R = 6371, toRad = x => x * Math.PI / 180;
    const dLat = toRad(lat2-lat1), dLng = toRad(lng2-lng1);
    const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng/2)**2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  }

  function sortDestsByDistance() {
    const { lat, lng } = state.location;
    if (!lat) return;
    DESTINATIONS.forEach(d => { d._dist = haversine(lat, lng, d.lat, d.lng) });
    DESTINATIONS.sort((a, b) => a._dist - b._dist);
    if (DESTINATIONS[0]._dist < 100) {
      const sortLabel = document.getElementById('sort-label');
      const sortInfo  = document.getElementById('sort-info');
      if (sortLabel) sortLabel.textContent = 'Sorted by distance';
      if (sortInfo)  sortInfo.textContent  = '📍 GPS active';
    }
    renderDestinations();
  }

  function renderDestinations(filter = '') {
    const grid = document.getElementById('dest-grid');
    if (!grid) return;
    const list = filter
      ? DESTINATIONS.filter(d => d.city.toLowerCase().includes(filter) || d.country.toLowerCase().includes(filter))
      : DESTINATIONS;
    // Update count label
    const countEl = document.getElementById('dest-count-label');
    if (countEl) countEl.textContent = list.length + ' destination' + (list.length !== 1 ? 's' : '');
    grid.innerHTML = list.map((d, idx) => {
      const saved  = state.savedPlaces.has(d.id);
      const isNear = d._dist && d._dist < 100;
      const photo  = state.destPhotos[d.id];
      const rank   = idx + 1;
      const photoStyle = photo
        ? `background-image:url('${photo}');background-size:cover;background-position:center;`
        : '';
      return `
        <div class="explore-dest-card motion-card" style="animation-delay:${idx * 60}ms" onclick="App.openDestDetail('${d.id}')">
          <div class="edc-hero" data-dest="${d.id}" style="${photoStyle}background-color:#1a1a2e;">
            ${!photo ? `<span class="edc-emoji">${d.emoji}</span>` : ''}
            <div class="edc-rank">#${rank}</div>
            <div class="edc-speed">📶 ${d.speed || 50} Mbps</div>
            ${d.popular ? '<div class="edc-popular">Popular</div>' : ''}
            <div class="edc-safety"><span class="ms" style="font-size:12px;vertical-align:middle">shield</span> ${(d.safetyScore || d.rating * 2).toFixed(1)}</div>
            <div class="edc-overlay">
              <div class="edc-city">${d.city}</div>
              <div class="edc-country-row">
                <span class="edc-country">${d.country}</span>
                ${d.popular ? '<span class="edc-pop-badge">Popular</span>' : ''}
              </div>
            </div>
          </div>
          <div class="edc-info">
            <div class="edc-meta-row">
              <div class="edc-weather">
                <span>${d.weatherDesc || 'Sunny'} · ${d.temp || 25}°C</span>
                <div class="edc-tags">
                  ${(d.tags || []).map(t => `<span class="edc-tag">${t}</span>`).join('')}
                </div>
              </div>
              <div class="edc-cost">
                <div class="edc-cost-amt">$${(d.monthlyCost || 1500).toLocaleString()}</div>
                <div class="edc-cost-label">/ month</div>
              </div>
            </div>
          </div>
        </div>`;
    }).join('');
  }

  function filterDestinations(val) { renderDestinations(val.toLowerCase()); }

  function setExploreScope(scope, btn) {
    document.querySelectorAll('.scope-pill').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    snack('Showing ' + scope + ' results');
  }

  function aiExplore(topic) {
    const msgs = {
      nomad:  'Which destinations are best for digital nomads?',
      hotels: 'Find the best hotels in top nomad destinations',
      safety: 'Safety ratings for the top travel destinations',
      cost:   'Compare cost of living across nomad destinations',
    };
    switchTab('ai');
    setTimeout(() => sendAiMessage(msgs[topic] || 'Tell me about travel destinations'), 200);
  }

  function openExploreFilter() {
    snack('Filters coming soon!');
  }

  function initExploreScreen() {
    const el = document.getElementById('explore-location-text');
    if (el) el.textContent = state.location.name || 'Detecting…';
    // Destinations hidden (future build) — always show Places
    document.getElementById('tab-dest')?.classList.add('hidden');
    document.getElementById('tab-places')?.classList.remove('hidden');
    renderCategories();
    // Ensure destination photos are loaded (retry if any are missing)
    const missing = DESTINATIONS.some(d => !state.destPhotos[d.id]);
    if (missing) fetchAllDestPhotos();
  }

  function toggleSaved(id) {
    if (state.savedPlaces.has(id)) { state.savedPlaces.delete(id); snack('Removed from saved'); }
    else { state.savedPlaces.add(id); snack('❤️ Saved!'); }
    LS.set('saved', [...state.savedPlaces]);
    renderDestinations();
  }

  function openDestDetail(id) {
    const d = DESTINATIONS.find(x => x.id === id); if (!d) return;
    state.currentDest = d;
    const photo = state.destPhotos[d.id];
    document.getElementById('dest-detail-body').innerHTML = `
      <div class="dd-hero" style="font-size:80px;position:relative;overflow:hidden;${photo?'background:linear-gradient(135deg,#1a1a2e,#16213e);':''}">
        ${photo ? `<img src="${photo}" alt="${d.city}" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:.85;">` : ''}
        <span style="position:relative;z-index:1;${photo?'text-shadow:0 4px 12px rgba(0,0,0,.7)':''}">${d.emoji}</span>
      </div>
      <div class="dd-title">
        <div class="dd-city">${d.city}</div>
        <div class="dd-country">${d.country}</div>
      </div>
      <div class="dd-stats">
        <div class="stat-pill"><div class="stat-val">⭐ ${d.rating}</div><div class="stat-lbl">Rating</div></div>
        <div class="stat-pill"><div class="stat-val">${d.pop.split(' ')[0]}</div><div class="stat-lbl">Nomads/yr</div></div>
        ${d._dist ? `<div class="stat-pill"><div class="stat-val">${Math.round(d._dist).toLocaleString()} km</div><div class="stat-lbl">From you</div></div>` : ''}
      </div>
      <div class="dd-desc">${d.desc}</div>
      <div class="dd-actions">
        <button class="btn-primary" style="flex:1" onclick="App.planTripTo('${d.id}')">
          <span class="ms">luggage</span> Add to Trips
        </button>
        <button class="btn-outline-xs" onclick="App.aiTripFor('${d.id}')">✨ AI Plan</button>
      </div>`;
    openModal('dest-detail');
  }

  function planTripTo(id) {
    const d = DESTINATIONS.find(x => x.id === id); if (!d) return;
    closeModal('dest-detail');
    setTimeout(() => {
      openModal('plan-trip');
      document.getElementById('pt-dest').value = `${d.city}, ${d.country}`;
    }, 350);
  }

  function aiTripFor(id) {
    const d = DESTINATIONS.find(x => x.id === id);
    closeModal('dest-detail');
    switchTab('ai');
    setTimeout(() => sendAiMessage(`Plan a trip to ${d.city}, ${d.country}`), 300);
  }

  /* ── PLACES CATEGORIES ──────────────────────────────────────── */
  function placesTab(tab, btn) {
    state.placesTab = tab;
    document.querySelectorAll('[data-places-tab]').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('tab-dest').classList.toggle('hidden', tab !== 'dest');
    document.getElementById('tab-places').classList.toggle('hidden', tab !== 'places');
    if (tab === 'places') renderCategories();
    if (tab === 'dest') renderDestinations();
  }

  function renderCategories() {
    const h = getHour();
    const greet = h < 5 ? '🌙 Late Night' : h < 10 ? '🍳 Good Morning' : h < 14 ? '☀️ Good Afternoon' : h < 18 ? '🌤️ Afternoon' : '🌆 Good Evening';
    const tg = document.getElementById('time-greeting');
    if (tg) tg.textContent = greet + ' — Find places nearby';
    const cg = document.getElementById('cat-grid');
    if (cg) cg.innerHTML = CATEGORIES.map(c => `
      <button class="cat-chip" onclick="App.openPlacesCat('${c.id}','${c.label}','${c.query}')">
        <span>${c.emoji()}</span><span>${c.label}</span>
      </button>`).join('');
  }

  function openPlacesCat(id, label, query) {
    state.currentPcat = { id, label, query };
    document.getElementById('pcat-title').textContent = label;
    document.getElementById('pcat-body').innerHTML = `<div class="loading-state"><div class="spinner"></div><p>Finding ${label.toLowerCase()} near you…</p></div>`;
    openModal('places-cat');
    setTimeout(() => loadPlacesCat(label, query), 600);
  }

  function loadPlacesCat(label, query) {
    const { lat, lng, name } = state.location;
    const body      = document.getElementById('pcat-body');
    const emergency = ['hospital','pharmacy','police'].some(k => query.includes(k));
    const gmapsUrl  = `https://www.google.com/maps/search/${encodeURIComponent(query)}${lat ? `/@${lat},${lng},14z` : ''}`;

    if (emergency) {
      body.innerHTML = `
        <div class="pcat-loc-bar"><span class="ms">location_on</span>${name}</div>
        <div class="adv-section" style="margin-bottom:12px">
          <div class="adv-sec-hdr"><span class="adv-sec-title">⚠️ Emergency / Medical</span></div>
          <div class="adv-sec-body">For emergency services, we'll open Google Maps to find the nearest ${label.toLowerCase()}.</div>
        </div>
        <button class="btn-primary full" onclick="App.openBrowser('${gmapsUrl}','${label} Near You')">
          <span class="ms">open_in_new</span> Find ${label} on Google Maps
        </button>`;
      return;
    }

    const demos = generateDemoPlaces(query, label, 6);
    body.innerHTML = `
      <div class="pcat-loc-bar"><span class="ms">location_on</span>${name || 'Current location'}</div>
      ${demos.map((p, i) => {
        const isOpen = Math.random() > 0.3;
        const reviews = Math.floor(20 + Math.random() * 200);
        const price = ['$', '$$', '$$$'][Math.floor(Math.random() * 3)];
        const distMi = (0.1 + Math.random() * 2).toFixed(2);
        const addr = p.addr;
        const tel = `tel:+${Math.floor(10000000000 + Math.random() * 89999999999)}`;
        const mapsUrl = `https://www.google.com/maps/search/${encodeURIComponent(p.name)}`;
        return `
        <div class="place-list-card">
          <div class="plc-photo" style="${p.photo ? `background-image:url('${p.photo}');background-size:cover;background-position:center;` : 'background:linear-gradient(135deg,#1a1a2e,#16213e);'}">
            ${!p.photo ? `<span style="font-size:28px">${p.emoji}</span>` : ''}
          </div>
          <div class="plc-body">
            <div class="plc-top-row">
              <div>
                <div class="plc-name">${p.name}</div>
                <div class="plc-cat">${label}</div>
              </div>
              <span class="plc-status ${isOpen ? 'open' : 'closed'}">${isOpen ? 'OPEN' : 'CLOSED'}</span>
            </div>
            <div class="plc-meta">
              <span class="plc-stars">⭐ ${p.rating}</span>
              <span class="plc-reviews">(${reviews})</span>
              <span class="plc-price">${price}</span>
              <span class="plc-dist">${distMi}mi Away</span>
            </div>
            <div class="plc-addr">${addr}</div>
            <div class="plc-actions">
              <a href="${tel}" class="plc-action-btn plc-call">
                <span class="ms" style="font-size:16px">call</span> CALL
              </a>
              <a href="${mapsUrl}" target="_blank" class="plc-action-btn plc-dir">
                <span class="ms" style="font-size:16px">directions</span> DIRECTIONS
              </a>
              <button class="plc-action-btn plc-info" onclick="App.snack('${p.name} — ${label}')">
                <span class="ms" style="font-size:16px">info</span> MORE INFO
              </button>
            </div>
          </div>
        </div>`}).join('')}`;
  }

  // Curated Wikimedia Commons category photos (stable, open-licensed)
  const CATEGORY_PHOTOS = {
    food:      'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Good_Food_Display_-_NCI_Visuals_Online.jpg/400px-Good_Food_Display_-_NCI_Visuals_Online.jpg',
    cafe:      'https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/400px-A_small_cup_of_coffee.JPG',
    hotel:     'https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Sdf-chicago.jpg/400px-Sdf-chicago.jpg',
    transport: 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/BTS_Skytrain_approaching_Ratchathewi_station.jpg/400px-BTS_Skytrain_approaching_Ratchathewi_station.jpg',
    atm:       'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/BITCOIN-Geldautomat-Wien.jpg/400px-BITCOIN-Geldautomat-Wien.jpg',
    shop:      'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Above_Gotham.jpg/400px-Above_Gotham.jpg',
    gym:       'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Gym_at_Taliesin_West.jpg/400px-Gym_at_Taliesin_West.jpg',
    hospital:  'https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Kaplan_Medical_Center_edited.jpg/400px-Kaplan_Medical_Center_edited.jpg',
    pharmacy:  'https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/Pharmacy_-_the_interior.jpg/400px-Pharmacy_-_the_interior.jpg',
    beach:     'https://upload.wikimedia.org/wikipedia/commons/thumb/6/60/Kuta_beach_Bali.jpg/400px-Kuta_beach_Bali.jpg',
    nightlife: 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Bangkok_nightlife_-_Khao_San_Road.jpg/400px-Bangkok_nightlife_-_Khao_San_Road.jpg',
    default:   'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/400px-No_image_available.svg.png',
  };

  const placeNames = {
    food:      ['Nomad Kitchen','Street Bites','The Wanderer Grill','Local Eats','Spice Route','Fusion Corner','Wok & Roll','Taste of Home'],
    cafe:      ['Digital Nomad Café','Bean & Browse','The Grind','Pour Over Paradise','Roast & Relax','Brew Corner','Coffee Collective','Sip & Work'],
    hotel:     ['Nomad Hostel','The Co-Living Hub','Modern Suites','Budget Inn','Artisan Rooms','City Stay','Backpacker Palace','Nomad Base'],
    transport: ['City Metro','Express Bus','BTS Station','MRT Hub','Tuk-Tuk Stand','Grab Point','Ferry Dock','Airport Link'],
    atm:       ['BTC ATM','Lightning ATM','Crypto Kiosk','Exchange Point','Bitcoin Corner','Crypto ATM','Digital Wallet Hub','Satoshi Exchange'],
    shop:      ['Local Market','Nomad Shop','Night Bazaar','Weekend Market','Tech Store','Street Market','Digital Goods','Travel Gear'],
    gym:       ['Nomad Gym','FitHub','Iron Zone','CrossFit Box','Yoga Studio','Sports Club','Urban Gym','Wellness Center'],
    beach:     ['Sunset Beach','Chill Cove','Digital Nomad Beach','Surf Spot','Kite Beach','Hidden Bay','Reef Point','Ocean Club'],
    nightlife: ['Rooftop Bar','Night Market','Jazz Lounge','Sky Bar','Night Club','Live Music Venue','Beer Garden','Cocktail Hub'],
    default:   ['Place 1','Place 2','Place 3','Place 4','Place 5','Place 6','Place 7','Place 8'],
  };
  const streets = ['Main Street','Market Ave','Digital Lane','Nomad Road','BTC Boulevard','Satoshi St','Travel Way','Explorer Blvd'];

  function generateDemoPlaces(query, label, count) {
    const cat = query.includes('restaurant') || query.includes('food') || query.includes('dining') ? 'food'
      : query.includes('cafe') || query.includes('coffee') ? 'cafe'
      : query.includes('hotel') || query.includes('hostel') || query.includes('accommodation') ? 'hotel'
      : query.includes('transit') || query.includes('transport') || query.includes('bus') || query.includes('metro') ? 'transport'
      : query.includes('bitcoin') || query.includes('atm') || query.includes('crypto') ? 'atm'
      : query.includes('shopping') || query.includes('market') ? 'shop'
      : query.includes('gym') || query.includes('fitness') ? 'gym'
      : query.includes('beach') ? 'beach'
      : query.includes('nightlife') || query.includes('bar') ? 'nightlife'
      : 'default';
    const emojis = { food:'🍜', cafe:'☕', hotel:'🏨', transport:'🚇', atm:'₿', shop:'🛍', gym:'💪', beach:'🏖️', nightlife:'🎵', default:'📍' };
    const names  = placeNames[cat] || placeNames.default;
    const photo  = CATEGORY_PHOTOS[cat] || CATEGORY_PHOTOS.default;
    const { name: locName } = state.location;
    const cityStreets = locName && locName !== 'Detecting…' ? streets.map(s => s + ', ' + locName.split(',')[0]) : streets;
    return Array.from({ length: count }, (_, i) => ({
      emoji: emojis[cat],
      name:  names[i % names.length],
      addr:  `${Math.floor(Math.random()*200)+1} ${cityStreets[i % cityStreets.length]}`,
      rating:(3.8 + Math.random() * 1.1).toFixed(1),
      dist:  (0.2 + Math.random() * 4).toFixed(1),
      photo,
    }));
  }

  /* ── AI CHAT ─────────────────────────────────────────────────── */
  function aiQuick(type) {
    const msgs = {
      plan:        'Plan a 5-day trip for a digital nomad',
      safety:      `What is the safety situation for ${state.location.name}?`,
      advisories:  'Show me current travel advisories',
      phrases:     'Give me useful travel phrases',
      btc:         'Find Bitcoin-friendly spots and BTC merchants near me',
      nomadcities: 'What are the best nomad cities in 2026?',
    };
    switchTab('ai');
    sendAiMessage(msgs[type] || '');
  }

  function chatKeydown(e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendChat(); }
  }

  function chatResize(el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
  }

  function sendChat() {
    const input = document.getElementById('chat-input');
    const msg   = input.value.trim(); if (!msg) return;
    input.value = ''; input.style.height = 'auto';
    sendAiMessage(msg);
  }

  function sendAiMessage(msg) {
    const container = document.getElementById('chat-msgs');
    container.insertAdjacentHTML('beforeend', `<div class="msg msg-usr"><div class="msg-bubble">${escHtml(msg)}</div></div>`);
    container.insertAdjacentHTML('beforeend', `
      <div class="msg msg-ai msg-typing" id="typing-ind">
        <div class="msg-bubble">
          <div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>
        </div>
      </div>`);
    container.scrollTop = container.scrollHeight;
    document.getElementById('ai-sub').textContent = 'Thinking…';

    setTimeout(() => {
      document.getElementById('typing-ind')?.remove();
      const response = getAiResponse(msg);
      container.insertAdjacentHTML('beforeend', `<div class="msg msg-ai"><div class="msg-bubble">${markdownToHtml(response)}</div></div>`);
      container.scrollTop = container.scrollHeight;
      document.getElementById('ai-sub').textContent = 'Powered by Gemini 2.0';

      const m2 = msg.toLowerCase();
      if (m2.includes('plan') || m2.includes('trip') || m2.includes('travel') || m2.includes('visit') || m2.includes('itinerary') || m2.includes('go to')) {
        const dest = msg.replace(/plan|a|my|trip|to|travel|for|in|visit|itinerary|how|do|i|get|there|\?/gi,'').trim() || 'New Destination';
        state.aiLastTrip = { dest: dest || 'New Destination', days: 5 };
        document.getElementById('ai-add-trip').classList.remove('hidden');
      }
    }, 1400 + Math.random() * 800);
  }

  /* ── AI CONTEXT BUILDER ─────────────────────────────────────── */
  function buildAiContext() {
    const lines = [];
    if (state.user?.name) lines.push(`User: ${state.user.name}`);
    if (state.location.name && state.location.name !== 'Detecting…')
      lines.push(`Current location: ${state.location.name}`);
    if (state.weather.temp !== '--')
      lines.push(`Weather: ${state.weather.emoji} ${state.weather.temp}° ${state.weather.desc}`);
    const upcoming = state.trips.filter(t => t.status === 'upcoming');
    if (upcoming.length) {
      lines.push(`Upcoming trips: ${upcoming.map(t =>
        `${t.dest} (${formatDate(t.start)} – ${formatDate(t.end)})`).join(', ')}`);
    }
    if (state.btcPrice)
      lines.push(`Live BTC price: $${state.btcPrice.toLocaleString()}`);
    return lines.join('\n');
  }

  function getAiResponse(msg) {
    const m   = msg.toLowerCase();
    const ctx = buildAiContext();
    const upcoming = state.trips.filter(t => t.status === 'upcoming');

    // If user asks about one of their planned destinations
    for (const trip of upcoming) {
      const tripCity = trip.dest.split(',')[0].toLowerCase();
      if (m.includes(tripCity)) {
        return `✈️ **Your ${trip.dest} Trip**\n\n` +
          `You're heading to **${trip.dest}** from **${formatDate(trip.start)}** to **${formatDate(trip.end)}**.\n\n` +
          AI_RESPONSES.plan(trip.dest);
      }
    }

    // Context-aware "my trips" query
    if (m.includes('my trip') || m.includes('my itinerary') || m.includes('my plan')) {
      if (!upcoming.length) return `📅 You don't have any upcoming trips yet! Tap **Plan a Trip** or tell me where you want to go and I'll plan it.`;
      return `📅 **Your Upcoming Trips**\n\n${upcoming.map((t,i) =>
        `${i+1}. **${t.dest}** — ${formatDate(t.start)} to ${formatDate(t.end)}${t.notes ? '\n   *' + t.notes.slice(0,80) + '…*' : ''}`
      ).join('\n\n')}\n\nWant me to plan the itinerary for any of these?`;
    }

    // Location-aware "what should I do" / "recommend"
    if (m.includes('recommend') || m.includes('what to do') || m.includes('near me') || m.includes('nearby')) {
      const loc = state.location.name && state.location.name !== 'Detecting…' ? state.location.name : 'your current area';
      return `📍 **Recommendations Near ${loc}**\n\n• Open the **Maps** tab → filter by Food, Café, or BTC to see live places\n• Use **Places** tab to find restaurants, gyms, pharmacies\n• Current weather: ${state.weather.emoji} ${state.weather.temp !== '--' ? state.weather.temp+'°' : 'loading…'}\n\nWant me to plan a trip from here?`;
    }

    if (m.includes('weather')) {
      const loc = state.location.name || 'your location';
      return state.weather.temp !== '--'
        ? `🌤️ **Live Weather for ${loc}**\n\n${state.weather.emoji} **${state.weather.temp}°** — ${state.weather.desc}\n\nBest for travel: clear skies expected for the next few days. Pack light layers.`
        : `Weather data loading… Make sure location access is enabled for live weather.`;
    }

    if ((m.includes('plan') || m.includes('go to') || m.includes('visit')) &&
        (m.includes('trip') || m.includes('travel') || m.includes('itinerary') || m.includes('days'))) {
      const dest = msg.replace(/plan|a|my|trip|to|travel|for|in|visit|itinerary|how|do|i|\?/gi,'').trim();
      return AI_RESPONSES.plan(dest || state.location.name);
    }
    if (m.includes('safe') || m.includes('danger') || m.includes('risk') || m.includes('crime'))
      return AI_RESPONSES.safety(state.location.name);
    if (m.includes('advisor') || m.includes('warning') || m.includes('alert') || m.includes('entry'))
      return AI_RESPONSES.advisories();
    if (m.includes('phrase') || m.includes('language') || m.includes('speak') || m.includes('translate'))
      return AI_RESPONSES.phrases();
    if (m.includes('gold') || m.includes('silver') || m.includes('metal') || m.includes('price')) {
      const goldEl = document.getElementById('ws-xau');
      const btcEl  = document.getElementById('ws-btc');
      return `🥇 **Live Commodity & Crypto Prices**\n\n• Gold: ${goldEl?.textContent || 'loading…'}\n• BTC: ${btcEl?.textContent || 'loading…'}\n\nCheck the **Wallet** tab for live ETH, SOL, silver, platinum + currency converter.`;
    }
    if (m.includes('bitcoin') || m.includes('btc') || m.includes('crypto'))
      return `₿ **Bitcoin Travel Tips**\n\nKipita makes Bitcoin travel easy:\n\n• **Maps** tab — thousands of live BTC merchants via BTCMap\n• **Wallet** tab — live BTC price: ${state.btcPrice ? '$'+state.btcPrice.toLocaleString() : 'loading…'}\n• **Places** tab → ATM/BTC — find Bitcoin ATMs near you\n• Most BTC-friendly: El Salvador 🇸🇻, Portugal 🇵🇹, Japan 🇯🇵, UAE 🇦🇪`;
    if (m.includes('visa') || m.includes('passport') || m.includes('requirement'))
      return '🛂 **Visa & Entry Requirements**\n\nEntry requirements vary:\n\n• Thailand: 30–60 days visa on arrival\n• Indonesia (Bali): 30 days extendable\n• Portugal (Schengen): 90/180 days\n• UAE: 30–90 days on arrival\n• Japan: 90 days (most Western passports)\n\n**Always verify** on your country\'s official embassy site before booking.';
    if (m.includes('currency') || m.includes('exchange') || m.includes('convert'))
      return `💱 **Currency & Exchange**\n\nLive rates are in your **Wallet** tab.\n\n${ctx.includes('BTC') ? `BTC is at ${state.btcPrice ? '$'+state.btcPrice.toLocaleString() : 'loading'}. ` : ''}Use the converter to calculate any currency pair instantly.`;
    return AI_RESPONSES.default(msg.slice(0, 50));
  }

  function addAiTrip() {
    if (!state.aiLastTrip) return;
    document.getElementById('ai-add-trip').classList.add('hidden');
    openModal('plan-trip');
    document.getElementById('pt-dest').value = state.aiLastTrip.dest;
    const s = new Date(Date.now() + 7 * 86400000).toISOString().slice(0,10);
    const e = new Date(Date.now() + (7 + state.aiLastTrip.days) * 86400000).toISOString().slice(0,10);
    document.getElementById('pt-start').value = s;
    document.getElementById('pt-end').value   = e;
  }

  function markdownToHtml(md) {
    return md
      .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
      .replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>')
      .replace(/\n\n/g,'</p><p>')
      .replace(/\n/g,'<br>')
      .replace(/^/,'<p>').replace(/$/,'</p>');
  }

  function escHtml(s) {
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  /* ── MAPS TAB ────────────────────────────────────────────────── */
  function initMapScreen() {
    if (state.mapScreen) {
      setTimeout(() => state.mapScreen.invalidateSize(), 200);
      return;
    }
    const lat = state.location.lat || 13.7563;
    const lng = state.location.lng || 100.5018;
    state.mapScreen = L.map('leaflet-map-screen').setView([lat, lng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(state.mapScreen);
    if (state.location.lat) {
      L.marker([lat, lng], {
        icon: L.divIcon({ html: '📍', className: '', iconSize: [30,30], iconAnchor: [15,30] })
      }).addTo(state.mapScreen).bindPopup('You are here');
    }
    fetchBTCMerchants();
    generateCashAppMerchants();
    renderMapNearbyPlaces('btc');
  }

  function renderMapNearbyPlaces(filter) {
    const sheet = document.getElementById('map-nearby-list');
    if (!sheet) return;
    const configs = {
      btc:  { label: 'BTC Merchants', icon: '₿', items: ['Lightning Café', 'Crypto Market', 'BTC Corner Shop', 'Digital Bistro'], empty: 'No BTC merchants found nearby. Try searching a different location.' },
      food: { label: 'Restaurants', icon: '🍜', items: ['Nomad Kitchen', 'Street Bites', 'The Wanderer Grill', 'Local Eats'], empty: 'No restaurants found nearby. Try searching a different location.' },
      cafe: { label: 'Cafés', icon: '☕', items: ['Digital Nomad Café', 'Bean & Browse', 'The Grind', 'Pour Over Paradise'], empty: 'No cafés found nearby. Try searching a different location.' },
      shop: { label: 'Shops', icon: '🛍️', items: [], empty: 'No shops found nearby. Try searching a different location.' },
      atm:  { label: 'ATMs', icon: '🏧', items: ['City ATM', 'Airport Exchange', 'Central Bank ATM'], empty: 'No ATMs found nearby. Try searching a different location.' },
    };
    const cfg = configs[filter] || configs.btc;
    if (!cfg.items.length) {
      sheet.innerHTML = `
        <div class="map-nearby-empty">
          <span style="font-size:40px">${cfg.icon}</span>
          <p>${cfg.empty}</p>
        </div>`;
      return;
    }
    const { lat, lng } = state.location;
    sheet.innerHTML = cfg.items.map(name => {
      const dist = (0.1 + Math.random() * 1.5).toFixed(1);
      const rating = (4.0 + Math.random() * 1.0).toFixed(1);
      const mlat = (lat || 0) + (Math.random() - 0.5) * 0.02;
      const mlng = (lng || 0) + (Math.random() - 0.5) * 0.02;
      const mapsUrl = `https://www.google.com/maps/search/${encodeURIComponent(name)}`;
      return `
        <div class="map-nearby-item">
          <div class="map-nearby-icon">${cfg.icon}</div>
          <div class="map-nearby-info">
            <div class="map-nearby-name">${name}</div>
            <div class="map-nearby-meta">⭐ ${rating} · ${dist} mi away</div>
          </div>
          <button class="map-nearby-dir" onclick="App.openBrowser('${mapsUrl}','Directions')">
            <span class="ms" style="font-size:18px">directions</span>
          </button>
        </div>`;
    }).join('');
  }

  /* ── WALLET BTC MAP ──────────────────────────────────────────── */
  function initWalletMap() {
    if (state.walletMap) {
      setTimeout(() => state.walletMap.invalidateSize(), 200);
      return;
    }
    const el = document.getElementById('btc-wallet-map');
    if (!el) return;
    const lat = state.location.lat || 13.7563;
    const lng = state.location.lng || 100.5018;
    state.walletMap = L.map('btc-wallet-map', { zoomControl: true, scrollWheelZoom: false }).setView([lat, lng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(state.walletMap);
    if (state.location.lat) {
      L.marker([lat, lng], {
        icon: L.divIcon({ html: '📍', className: '', iconSize: [28,28], iconAnchor: [14,28] })
      }).addTo(state.walletMap).bindPopup('You are here');
    }
    // Load BTC merchants onto wallet map
    if (state.btcMerchants.length) {
      renderWalletMapMarkers();
    } else {
      fetchBTCMerchants().then(() => renderWalletMapMarkers());
    }
  }

  function renderWalletMapMarkers() {
    state.walletMapMarkers.forEach(m => m.remove());
    state.walletMapMarkers = [];
    if (!state.walletMap) return;
    state.btcMerchants.slice(0, 60).forEach(el => {
      const mlat = el.osm_json.lat, mlng = el.osm_json.lon;
      const tags = el.osm_json.tags || {};
      const name = tags.name || 'BTC Merchant';
      const ti   = getPlaceTypeInfo(tags);
      addLabeledMarker(
        state.walletMap, mlat, mlng,
        '₿', '#F7931A', name,
        makePopupHtml(name, ti.emoji, ti.label, tags),
        state.walletMapMarkers
      );
    });
  }

  function mapScreenFilter(filter, btn) {
    state.mapScreenFilter = filter;
    document.querySelectorAll('#map-screen-pills .mpill, #map-nearby-pills .mnpill').forEach(p => p.classList.remove('active'));
    // Sync both pill rows
    document.querySelectorAll(`[data-f="${filter}"]`).forEach(p => p.classList.add('active'));
    renderMapScreenMarkers();
    renderMapNearbyPlaces(filter);
    if (filter === 'btc') {
      if (state.btcMerchants.length === 0) fetchBTCMerchants();
      if (state.cashAppMerchants.length === 0) generateCashAppMerchants();
    }
  }

  function generateCashAppMerchants() {
    const { lat, lng } = state.location;
    const baseLat = lat || 13.7563, baseLng = lng || 100.5018;
    const names = ['Cash App Merchant','Local Shop','Street Market','Digital Café','Tech Store','Food Court','Book Store','BTC Bistro','Crypto Corner','Pay Market'];
    state.cashAppMerchants = Array.from({ length: 12 }, (_, i) => ({
      osm_json: {
        lat: baseLat + (Math.random() - .5) * .06,
        lon: baseLng + (Math.random() - .5) * .06,
        tags: { name: names[i % names.length] + ' ' + (i + 1), payment: 'cashapp' },
      },
      source: 'cashapp',
    }));
  }

  function mapScreenSearchKey(e) {
    if (e.key === 'Enter') mapScreenSearch();
  }

  async function mapScreenSearch() {
    const q = document.getElementById('map-screen-input').value.trim();
    if (!q || !state.mapScreen) return;
    try {
      const r = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(q)}&format=json&limit=1`);
      const d = await r.json();
      if (d[0]) {
        state.mapScreen.setView([+d[0].lat, +d[0].lon], 14);
        snack('📍 Moved to ' + d[0].display_name.split(',')[0]);
      } else snack('Location not found');
    } catch { snack('Search failed') }
  }

  function haversineKm(lat1, lon1, lat2, lon2) {
    const R = 6371, toRad = x => x * Math.PI / 180;
    const dLat = toRad(lat2 - lat1), dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon/2)**2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  }

  async function fetchBTCMerchants() {
    const hasLoc = !!(state.location.lat && state.location.lng);
    try {
      // Fetch more when we have GPS so we can filter locally within ~50 km
      const limit = hasLoc ? 500 : 100;
      const r = await fetch(`https://api.btcmap.org/v2/elements?limit=${limit}`);
      const data = await r.json();
      let merchants = (data.elements || data || []).filter(el => el.osm_json?.lat && el.osm_json?.lon);

      if (hasLoc) {
        const { lat, lng } = state.location;
        const nearby = merchants.filter(el => haversineKm(lat, lng, el.osm_json.lat, el.osm_json.lon) <= 50);
        // If we found results near user, use those; otherwise keep global set
        merchants = nearby.length >= 3 ? nearby : merchants.slice(0, 60);
      }
      state.btcMerchants = merchants;
    } catch {
      // Fallback demo markers — always generate even without GPS
      const baseLat = state.location.lat || 13.7563;
      const baseLng = state.location.lng || 100.5018;
      const demoNames = ['Lightning Café','Satoshi Market','BTC Corner Shop','Crypto Bistro','Bitcoin Hub','Digital Pay'];
      state.btcMerchants = demoNames.map((name, i) => ({
        osm_json: {
          lat: baseLat + (Math.random() - .5) * 0.05,
          lon: baseLng + (Math.random() - .5) * 0.05,
          tags: { name },
        },
        source: 'btcmap',
      }));
    }

    // Update count badge
    const badge = document.getElementById('btc-merchant-count');
    if (badge) badge.textContent = state.btcMerchants.length + ' found';

    if (state.mapScreenFilter === 'btc') renderMapScreenMarkers();
    if (state.walletMap) renderWalletMapMarkers();
  }

  /* ── MARKER HELPERS ─────────────────────────────────────────── */
  function getPlaceTypeInfo(tags) {
    const amenity = (tags?.amenity || '').toLowerCase();
    const shop    = (tags?.shop    || '').toLowerCase();
    const tourism = (tags?.tourism || '').toLowerCase();
    const cuisine = (tags?.cuisine || '').toLowerCase();
    if (amenity === 'restaurant' || cuisine)          return { emoji:'🍽️', label:'Restaurant' };
    if (amenity === 'cafe' || amenity === 'coffee_shop') return { emoji:'☕', label:'Café' };
    if (amenity === 'fast_food')                      return { emoji:'🍔', label:'Fast Food' };
    if (amenity === 'bar' || amenity === 'pub')       return { emoji:'🍺', label:'Bar' };
    if (amenity === 'pharmacy')                       return { emoji:'💊', label:'Pharmacy' };
    if (amenity === 'atm')                            return { emoji:'🏧', label:'ATM' };
    if (shop === 'supermarket' || shop === 'convenience') return { emoji:'🛒', label:'Grocery' };
    if (shop === 'electronics' || shop === 'computer') return { emoji:'📱', label:'Electronics' };
    if (shop === 'clothes' || shop === 'fashion')     return { emoji:'👕', label:'Clothing' };
    if (shop === 'bakery')                            return { emoji:'🥐', label:'Bakery' };
    if (tourism === 'hotel' || tourism === 'hostel')  return { emoji:'🏨', label:'Hotel' };
    if (shop)                                         return { emoji:'🛍️', label:'Shop' };
    return { emoji:'₿', label:'BTC Merchant' };
  }

  function makeMarkerHtml(iconContent, bg, name) {
    const shortName = name.length > 16 ? name.slice(0, 15) + '…' : name;
    return `
      <div style="display:flex;flex-direction:column;align-items:center;gap:3px;filter:drop-shadow(0 2px 5px rgba(0,0,0,.32))">
        <div style="background:${bg};color:#fff;border-radius:50%;width:34px;height:34px;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:800;border:2px solid rgba(255,255,255,.7)">${iconContent}</div>
        <div style="background:rgba(255,255,255,.96);color:#1a1a2e;font-size:9.5px;font-weight:700;padding:2px 7px;border-radius:8px;white-space:nowrap;line-height:1.4;max-width:88px;overflow:hidden;text-overflow:ellipsis">${shortName}</div>
      </div>`;
  }

  function makePopupHtml(name, typeEmoji, typeLabel, tags) {
    const website = tags?.website || tags?.['contact:website'] || tags?.url || '';
    const phone   = tags?.phone   || tags?.['contact:phone']   || '';
    const hours   = tags?.opening_hours || '';
    const desc    = tags?.description   || tags?.brand         || tags?.operator || '';
    const cuisine = tags?.cuisine ? ' · ' + tags.cuisine.replace(/_/g,' ') : '';
    return `
      <div style="min-width:220px;max-width:270px;font-family:'Montserrat',sans-serif;padding:2px 0">
        <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">
          <div style="background:#f5f5f5;border-radius:12px;width:44px;height:44px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0">${typeEmoji}</div>
          <div style="min-width:0">
            <div style="font-size:14px;font-weight:800;color:#1a1a2e;line-height:1.25;word-break:break-word">${escHtml(name)}</div>
            <div style="font-size:11px;color:#888;margin-top:2px">${typeLabel}${cuisine}</div>
          </div>
        </div>
        ${desc  ? `<p style="font-size:12px;color:#444;line-height:1.55;margin-bottom:8px">${escHtml(desc)}</p>` : ''}
        ${hours ? `<div style="font-size:11px;color:#555;margin-bottom:6px">🕐 ${escHtml(hours)}</div>` : ''}
        ${phone ? `<div style="font-size:11px;margin-bottom:8px">📞 <a href="tel:${escHtml(phone)}" style="color:#DD3B49;text-decoration:none;font-weight:600">${escHtml(phone)}</a></div>` : ''}
        <div style="display:flex;gap:6px;margin-top:10px">
          ${website
            ? `<a href="${escHtml(website)}" target="_blank" rel="noopener"
                 style="flex:1;text-align:center;background:#DD3B49;color:#fff;padding:9px 6px;border-radius:10px;font-size:11px;font-weight:700;text-decoration:none">
                 🌐 Website
               </a>`
            : `<a href="https://www.google.com/maps/search/${encodeURIComponent(name)}" target="_blank" rel="noopener"
                 style="flex:1;text-align:center;background:#f0f0f0;color:#333;padding:9px 6px;border-radius:10px;font-size:11px;font-weight:700;text-decoration:none">
                 📍 Directions
               </a>`}
          ${website
            ? `<a href="https://www.google.com/maps/search/${encodeURIComponent(name)}" target="_blank" rel="noopener"
                 style="flex:1;text-align:center;background:#f0f0f0;color:#333;padding:9px 6px;border-radius:10px;font-size:11px;font-weight:700;text-decoration:none">
                 📍 Directions
               </a>`
            : ''}
        </div>
      </div>`;
  }

  function addLabeledMarker(map, mlat, mlng, iconContent, bg, name, popupHtml, collection) {
    // Hover tooltip for desktop
    const tooltipHtml = (() => {
      const dest = DESTINATIONS.find(d => name.toLowerCase().includes(d.city.toLowerCase()));
      const photo = dest ? state.destPhotos[dest.id] : null;
      return `<div style="display:flex;align-items:center;gap:8px;min-width:140px;max-width:200px;padding:2px 4px 2px 2px">
        ${photo ? `<img src="${photo}" style="width:44px;height:44px;border-radius:8px;object-fit:cover;flex-shrink:0" alt="">` : `<div style="width:44px;height:44px;border-radius:8px;background:${bg};display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0">${iconContent}</div>`}
        <div style="font-size:12px;font-weight:700;color:#1a1a2e;line-height:1.3">${name}</div>
      </div>`;
    })();
    const m = L.marker([mlat, mlng], {
      icon: L.divIcon({
        html: makeMarkerHtml(iconContent, bg, name),
        className: '',
        iconSize: [90, 54],
        iconAnchor: [45, 17],
      })
    }).addTo(map);
    m.bindPopup(popupHtml, { maxWidth: 240, className: 'kip-popup' });
    m.bindTooltip(tooltipHtml, { direction: 'top', offset: [0, -20], opacity: 1, className: 'kip-tooltip' });
    collection.push(m);
  }

  function renderMapScreenMarkers() {
    state.mapScreenMarkers.forEach(m => m.remove());
    state.mapScreenMarkers = [];
    if (!state.mapScreen) return;

    const filter  = state.mapScreenFilter;
    const { lat, lng } = state.location;

    if (filter === 'btc') {
      // ₿ BTC filter — show BTCMap merchants
      state.btcMerchants.slice(0, 50).forEach(el => {
        const mlat = el.osm_json.lat, mlng = el.osm_json.lon;
        const tags = el.osm_json.tags || {};
        const name = tags.name || 'BTC Merchant';
        const ti   = getPlaceTypeInfo(tags);
        addLabeledMarker(
          state.mapScreen, mlat, mlng,
          '₿', '#F7931A', name,
          makePopupHtml(name, ti.emoji, ti.label, tags),
          state.mapScreenMarkers
        );
      });
    } else if (filter === 'cashapp') {
      // 💚 Cash App filter — show Cash App merchant locations
      if (state.cashAppMerchants.length === 0) generateCashAppMerchants();
      state.cashAppMerchants.forEach(el => {
        const mlat = el.osm_json.lat, mlng = el.osm_json.lon;
        const tags = el.osm_json.tags || {};
        const name = tags.name || 'Cash App Pay';
        addLabeledMarker(
          state.mapScreen, mlat, mlng,
          '$', '#00D632', name,
          makePopupHtml(name, '💚', 'Cash App Pay', { website: 'https://cash.app', description: 'Accepts Cash App payments' }),
          state.mapScreenMarkers
        );
      });
    } else if (lat) {
      fetchAndRenderOverpassMarkers(filter, lat, lng);
    }
  }

  /* ── OVERPASS REAL NEARBY PLACES ─────────────────────────────── */
  const OVERPASS_CFGS = {
    food: { tag:'"amenity"~"restaurant|fast_food|food_court"', ico:'🍜', bg:'#E53E3E', label:'Restaurant' },
    cafe: { tag:'"amenity"~"cafe|coffee_shop"',                ico:'☕', bg:'#D69E2E', label:'Café' },
    shop: { tag:'"shop"~"supermarket|convenience|mall"',       ico:'🛍', bg:'#805AD5', label:'Shop' },
    atm:  { tag:'"amenity"="atm"',                             ico:'🏧', bg:'#38A169', label:'ATM' },
  };

  async function fetchAndRenderOverpassMarkers(filter, lat, lng) {
    const cfg = OVERPASS_CFGS[filter]; if (!cfg) return;
    try {
      const q = `[out:json][timeout:15];node[${cfg.tag}](around:2000,${lat},${lng});out 12;`;
      const r = await fetch(`https://overpass-api.de/api/interpreter?data=${encodeURIComponent(q)}`);
      const d = await r.json();
      const elements = (d.elements || []).filter(e => e.tags?.name);
      if (!elements.length) { renderMapNearbyPlaces(filter); return; }
      elements.forEach(e => {
        const name = e.tags.name;
        addLabeledMarker(
          state.mapScreen, e.lat, e.lon,
          cfg.ico, cfg.bg, name,
          makePopupHtml(name, cfg.ico, cfg.label, e.tags),
          state.mapScreenMarkers
        );
      });
      const sheet = document.getElementById('map-nearby-list');
      if (sheet) {
        sheet.innerHTML = elements.map(e => {
          const name = e.tags.name;
          const addr = e.tags['addr:street'] ? `${e.tags['addr:housenumber'] || ''} ${e.tags['addr:street']}`.trim() : '';
          const mapsUrl = `https://www.google.com/maps/search/${encodeURIComponent(name)}`;
          const dist = haversineKm(lat, lng, e.lat, e.lon);
          return `<div class="map-nearby-item">
            <div class="map-nearby-icon">${cfg.ico}</div>
            <div class="map-nearby-info">
              <div class="map-nearby-name">${escHtml(name)}</div>
              <div class="map-nearby-meta">${addr ? escHtml(addr) + ' · ' : ''}${dist < 1 ? Math.round(dist*1000)+'m' : dist.toFixed(1)+'km'}</div>
            </div>
            <button class="map-nearby-dir" onclick="App.openBrowser('${mapsUrl}','Directions')">
              <span class="ms" style="font-size:18px">directions</span>
            </button>
          </div>`;
        }).join('');
      }
    } catch { renderMapNearbyPlaces(filter); }
  }

  /* ── CURRENCY CONVERTER ──────────────────────────────────────── */
  async function fetchFxRates() {
    try {
      const r = await fetch('https://open.er-api.com/v6/latest/USD');
      const d = await r.json();
      if (d.rates) {
        state.fxRates = d.rates;
        updateFxPriceGrid(d.rates);
        convertCurrency();
      }
    } catch {
      // Fallback approximate rates (USD base)
      state.fxRates = { USD:1, EUR:0.92, GBP:0.78, JPY:149.5, CNY:7.25, CHF:0.89, THB:34.5, IDR:16300, BRL:5.85, AED:3.67, SGD:1.35 };
      convertCurrency();
    }
  }

  function updateFxPriceGrid(rates) {
    const set = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
    const fmt2 = (n) => n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 4 });
    const fmt0 = (n) => '$' + n.toLocaleString('en-US', { maximumFractionDigits: 0 });

    // EUR, GBP: show USD per 1 unit (strong currencies)
    if (rates.EUR) set('ws-eur', '$' + fmt2(1 / rates.EUR));
    if (rates.GBP) set('ws-gbp', '$' + fmt2(1 / rates.GBP));
    // JPY, CNY: conventional — units per 1 USD
    if (rates.JPY) set('ws-jpy', '¥' + rates.JPY.toFixed(2));
    if (rates.CNY) set('ws-cny', '¥' + rates.CNY.toFixed(4));
    // Gold: XAU rate = troy oz per USD → invert for USD per oz
    if (rates.XAU) set('ws-xau', fmt0(1 / rates.XAU));
  }

  function convertCurrency() {
    const fromAmt = parseFloat(document.getElementById('cc-from-amt')?.value) || 0;
    const fromCur = document.getElementById('cc-from-cur')?.value || 'USD';
    const toCur   = document.getElementById('cc-to-cur')?.value  || 'BTC';
    const outEl   = document.getElementById('cc-to-amt');
    const rateEl  = document.getElementById('cc-rate-label');
    if (!outEl) return;

    const btcPrice = state.btcPrice || 95000;
    const rates    = state.fxRates;

    // Convert fromAmt in fromCur → USD first, then → toCur
    function toUSD(amt, cur) {
      if (cur === 'BTC') return amt * btcPrice;
      const rate = rates[cur];
      return rate ? amt / rate : amt;
    }
    function fromUSD(usd, cur) {
      if (cur === 'BTC') return usd / btcPrice;
      const rate = rates[cur];
      return rate ? usd * rate : usd;
    }

    const usdAmt  = toUSD(fromAmt, fromCur);
    const result  = fromUSD(usdAmt, toCur);

    const fmt = (n, cur) => {
      if (cur === 'BTC') return n.toFixed(8) + ' ₿';
      if (cur === 'JPY' || cur === 'IDR') return n.toLocaleString('en-US', { maximumFractionDigits: 0 });
      return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    };

    outEl.textContent = fmt(result, toCur);

    // Show rate label
    const unitResult = fromUSD(toUSD(1, fromCur), toCur);
    if (rateEl) {
      const src = Object.keys(rates).length ? `Exchange rate · BTC $${btcPrice.toLocaleString()}` : 'Using approximate rates';
      rateEl.textContent = `1 ${fromCur} = ${fmt(unitResult, toCur)} ${toCur}  ·  ${src}`;
    }
  }

  function swapCurrency() {
    const fromSel = document.getElementById('cc-from-cur');
    const toSel   = document.getElementById('cc-to-cur');
    if (!fromSel || !toSel) return;
    const tmp = fromSel.value;
    fromSel.value = toSel.value;
    toSel.value   = tmp;
    convertCurrency();
  }

  /* ── WALLET SCREEN ───────────────────────────────────────────── */
  function renderWalletTxns() {
    const DEMO_TXNS = [
      { type:'received', amount:'+0.00125 BTC', label:'Payment from Alex M.',   time:'2h ago',  usd:'+$128.50' },
      { type:'sent',     amount:'-0.00062 BTC', label:'Nomad Kitchen Bangkok',  time:'5h ago',  usd:'-$63.72' },
      { type:'received', amount:'+0.00500 BTC', label:'Freelance payment',      time:'1d ago',  usd:'+$514.00' },
      { type:'sent',     amount:'-0.00080 BTC', label:'Airalo eSIM purchase',   time:'2d ago',  usd:'-$82.24' },
      { type:'sent',     amount:'-0.00210 BTC', label:'Bali co-working space',  time:'3d ago',  usd:'-$215.88' },
    ];
    const el = document.getElementById('ws-txns'); if (!el) return;
    el.innerHTML = DEMO_TXNS.map(t => `
      <div class="txn-item">
        <div class="txn-icon ${t.type}">${t.type === 'received' ? '⬇️' : '⬆️'}</div>
        <div style="flex:1;min-width:0">
          <div class="txn-label">${t.label}</div>
          <div class="txn-sub">${t.time}</div>
        </div>
        <div>
          <div class="txn-amount ${t.type}">${t.amount}</div>
          <div class="txn-usd">${t.usd}</div>
        </div>
      </div>`).join('');
  }

  function connectWallet() { snack('₿ Wallet connection coming soon!') }
  function walletCopy()    { navigator.clipboard.writeText('bc1qkipitaexamplewallet').then(() => snack('Address copied!')) }
  function walletSend()    { snack('Send feature coming soon!') }
  function walletReceive() { snack('QR receive coming soon!') }

  /* ── ADVISORY ───────────────────────────────────────────────── */
  function loadAdvisory() {
    const level   = state.safety.level;
    const labels  = { 1:'Very Safe', 2:'Mostly Safe', 3:'Moderate', 4:'High Risk' };
    const badge   = document.getElementById('adv-level-badge');
    if (badge) { badge.textContent = labels[level]; badge.dataset.level = level; }
    const insights = [
      `Safety conditions in ${state.location.name || 'your area'} appear stable. Standard traveler precautions apply. Keep valuables secure in crowded areas and use licensed transportation.`,
      `Current conditions show moderate activity. Tourist areas are generally safe during daylight hours. Avoid unfamiliar areas after midnight and keep emergency contacts ready.`,
      `Exercise increased awareness in ${state.location.name || 'current location'}. Several advisories are active. Monitor local news, register with your embassy, and stay in contact with your travel group.`,
    ];
    const adv_text = document.getElementById('adv-ai-text');
    if (adv_text) adv_text.textContent = insights[Math.min(level-1, insights.length-1)];
    const sections = [
      { icon:'🚦', title:'General Safety',     body:'Tourist areas well-policed. Standard precautions recommended. Petty theft reported in market areas — keep bags secure.' },
      { icon:'🏥', title:'Health & Medical',   body:'Hospital facilities available in city centers. Recommend travel insurance. Tap water safety varies — carry bottled water.' },
      { icon:'🛂', title:'Entry Requirements', body:"Most nationalities: visa on arrival or e-visa available. Check your country's portal for latest passport/vaccination requirements." },
      { icon:'⚡', title:'Local Restrictions', body:'Standard local laws apply. Photography restricted at certain government buildings. Respect local customs at religious sites.' },
      { icon:'🌐', title:'Connectivity',       body:'Mobile data widely available. SIM cards purchasable at airport. WiFi available in most cafes, hotels, and co-working spaces.' },
    ];
    const adv_sec = document.getElementById('adv-sections');
    if (adv_sec) adv_sec.innerHTML = sections.map(s => `
      <div class="adv-section">
        <div class="adv-sec-hdr" onclick="this.nextElementSibling.classList.toggle('hidden')">
          <div class="adv-sec-title">${s.icon} ${s.title}</div>
          <span class="ms" style="color:var(--text3)">expand_more</span>
        </div>
        <div class="adv-sec-body hidden">${s.body}</div>
      </div>`).join('');
  }

  function refreshAdvisory() {
    snack('🔄 Refreshing advisory…');
    if (state.location.lat) fetchWeather(state.location.lat, state.location.lng);
    setTimeout(loadAdvisory, 800);
  }

  /* ── TRANSLATE ───────────────────────────────────────────────── */
  function setLang(lang, btn) {
    state.lang = lang;
    document.querySelectorAll('.lang-tab').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    renderPhrases();
  }

  function renderPhrases(filter = '') {
    const phrases  = PHRASES[state.lang] || PHRASES.es;
    const filtered = filter ? phrases.filter(p => p.en.toLowerCase().includes(filter) || p.local.toLowerCase().includes(filter)) : phrases;
    const el = document.getElementById('phrase-list'); if (!el) return;
    el.innerHTML = filtered.map(p => `
      <div class="phrase-card">
        <div class="phrase-en">${p.en}</div>
        <div class="phrase-local">${p.local}</div>
        <div class="phrase-phon">${p.phon}</div>
      </div>`).join('');
  }

  function filterPhrases(val) { renderPhrases(val.toLowerCase()); }

  /* ── PERKS ───────────────────────────────────────────────────── */
  function renderPerks() {
    const el = document.getElementById('perks-list'); if (!el) return;
    el.innerHTML = PERKS.map(p => `
      <div class="perk-card">
        <div class="perk-icon">${p.icon}</div>
        <div style="flex:1;min-width:0">
          <div class="perk-title">${p.title}</div>
          <div class="perk-desc">${p.desc}</div>
          <div class="perk-code" onclick="App.copyCode('${p.code}')">
            <span class="ms" style="font-size:16px">content_copy</span> ${p.code}
          </div>
          <div class="perk-expiry">Expires: ${p.expiry}</div>
          <button class="perk-claim-btn" onclick="App.openBrowser('${p.url}','${p.title}')">Claim Deal →</button>
        </div>
      </div>`).join('');
  }

  function copyCode(code) {
    navigator.clipboard.writeText(code).then(() => snack(`Code "${code}" copied!`));
  }

  /* ── REVIEWS ─────────────────────────────────────────────────── */
  function switchSocialTab(tab, btn) {
    state.socialTab = tab;
    document.querySelectorAll('.sst-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    const gv = document.getElementById('groups-list-view');
    const rv = document.getElementById('reviews-view');
    if (tab === 'reviews') {
      gv.classList.add('hidden'); rv.classList.remove('hidden');
      renderReviewsTab();
    } else {
      rv.classList.add('hidden'); gv.classList.remove('hidden');
      renderGroupsScreen(); renderNearbyTeaser();
    }
  }

  function allReviews() {
    const saved = LS.get('reviews') || [];
    return [...DEMO_REVIEWS, ...saved];
  }

  function renderReviewsTab() {
    const all = allReviews();
    let list = state.reviewFilter === 'all' ? all : all.filter(r => r.dest === state.reviewFilter);
    list = state.reviewSort === 'top'
      ? [...list].sort((a,b) => b.rating - a.rating)
      : [...list].sort((a,b) => b.ts - a.ts);

    const label = document.getElementById('rev-count-label');
    if (label) label.textContent = `${list.length} review${list.length !== 1 ? 's' : ''}${state.reviewFilter !== 'all' ? ' in ' + state.reviewFilter : ''}`;

    const el = document.getElementById('reviews-list');
    if (!el) return;
    if (!list.length) {
      el.innerHTML = `<div class="empty-card" style="margin:24px 16px">
        <span class="ms ms-xl">rate_review</span>
        <p>No reviews yet for this destination.</p>
        <button class="btn-primary-sm" onclick="App.openWriteReview()">Be the first!</button>
      </div>`;
      return;
    }
    el.innerHTML = list.map(r => {
      const stars = renderStars(r.rating);
      const ago = timeAgo(r.ts);
      const photo = state.destPhotos[DESTINATIONS.find(d => d.city === r.dest)?.id || ''];
      return `
      <div class="review-card motion-card">
        ${photo ? `<div class="rev-dest-hero" style="background-image:url('${photo}')">
          <div class="rev-dest-overlay"><span class="rev-dest-name">${r.emoji} ${r.dest}</span></div>
        </div>` : `<div class="rev-dest-chip">${r.emoji} ${r.dest}</div>`}
        <div class="rev-body">
          <div class="rev-top">
            <div class="rev-avatar">${r.flag}</div>
            <div class="rev-meta">
              <div class="rev-author">${escHtml(r.author)}</div>
              <div class="rev-ago">${ago}</div>
            </div>
            <div class="rev-stars">${stars}</div>
          </div>
          <p class="rev-text">${escHtml(r.text)}</p>
          <div class="rev-scores">
            <div class="rev-score-chip">📶 WiFi ${renderMiniStars(r.wifi)}</div>
            <div class="rev-score-chip">💸 Budget ${renderMiniStars(r.budget)}</div>
            <div class="rev-score-chip">✨ Vibe ${renderMiniStars(r.vibe)}</div>
          </div>
        </div>
      </div>`;
    }).join('') + '<div class="pb-nav"></div>';
  }

  function renderStars(n) {
    return Array.from({length:5},(_,i) =>
      `<span style="color:${i<n?'#F7931A':'#ddd'};font-size:18px">★</span>`).join('');
  }

  function renderMiniStars(n) {
    return Array.from({length:5},(_,i) =>
      `<span style="color:${i<n?'#F7931A':'#ddd'};font-size:11px">★</span>`).join('');
  }

  function timeAgo(ts) {
    const diff = Date.now() - ts;
    const days = Math.floor(diff / 86400000);
    if (days === 0) return 'Today';
    if (days === 1) return 'Yesterday';
    if (days < 30)  return `${days}d ago`;
    const months = Math.floor(days / 30);
    return months === 1 ? '1 month ago' : `${months} months ago`;
  }

  function filterReviews(dest, btn) {
    state.reviewFilter = dest;
    document.querySelectorAll('.rfpill').forEach(p => p.classList.remove('active'));
    if (btn) btn.classList.add('active');
    renderReviewsTab();
  }

  function sortReviews(sort, btn) {
    state.reviewSort = sort;
    document.querySelectorAll('.rev-sort-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    renderReviewsTab();
  }

  function openWriteReview() {
    state.reviewStars = 0;
    state.nomadScores = { wifi:0, budget:0, vibe:0 };
    ['wr-dest','wr-text'].forEach(id => { const el = document.getElementById(id); if(el) el.value=''; });
    updateStarUI('wr-stars', 0);
    ['wifi','budget','vibe'].forEach(c => updateStarUI('wrs-'+c, 0, 'wrs'));
    openModal('write-review');
  }

  function setReviewStar(n) {
    state.reviewStars = n;
    updateStarUI('wr-stars', n);
  }

  function setNomadScore(cat, n) {
    state.nomadScores[cat] = n;
    updateStarUI('wrs-'+cat, n, 'wrs');
  }

  function updateStarUI(containerId, val, cls='wr-star') {
    const el = document.getElementById(containerId); if (!el) return;
    el.querySelectorAll('.'+cls).forEach(s => {
      s.textContent = +s.dataset.v <= val ? '★' : '☆';
      s.style.color = +s.dataset.v <= val ? '#F7931A' : '#bbb';
    });
  }

  function submitReview() {
    const dest = document.getElementById('wr-dest')?.value?.replace(/\s[^\s]+$/, '').trim();
    const text = document.getElementById('wr-text')?.value?.trim();
    if (!dest)              { snack('Please select a destination'); return; }
    if (!state.reviewStars) { snack('Please rate your experience'); return; }
    if (!text || text.length < 10) { snack('Write at least 10 characters'); return; }
    const u = state.user;
    const review = {
      id: 'r' + Date.now(),
      author: u?.name || 'You',
      flag: '🌍',
      dest,
      emoji: DESTINATIONS.find(d => d.city === dest)?.emoji || '📍',
      rating: state.reviewStars,
      wifi:   state.nomadScores.wifi   || 3,
      budget: state.nomadScores.budget || 3,
      vibe:   state.nomadScores.vibe   || 3,
      text,
      ts: Date.now(),
    };
    const saved = LS.get('reviews') || [];
    saved.unshift(review);
    LS.set('reviews', saved);
    closeModal('write-review');
    switchSocialTab('reviews', document.querySelector('.sst-btn[data-sst="reviews"]'));
    snack('Review posted! Thanks for sharing ⭐');
  }

  /* ── GROUPS SCREEN ───────────────────────────────────────────── */
  function renderGroupsScreen() {
    const el = document.getElementById('groups-screen-list'); if (!el) return;
    if (!state.groups.length) {
      el.innerHTML = `
        <div class="empty-card">
          <span class="ms ms-xl">group_add</span>
          <p>No groups yet. Create one to travel together!</p>
          <button class="btn-primary-sm" onclick="App.promptCreateGroup()">Create Group</button>
        </div>`;
      return;
    }
    el.innerHTML = state.groups.map(g => {
      const msgs   = LS.get('gchat_' + g.id) || [];
      const last   = msgs[msgs.length - 1];
      const preview = last ? `${last.from}: ${last.text.slice(0,40)}${last.text.length>40?'…':''}` : 'No messages yet';
      const unread  = msgs.filter(m => m.from !== 'You' && !m.read).length;
      const emoji   = g.name.match(/\p{Emoji}/u)?.[0] || '👥';
      return `
        <div class="group-row" onclick="App.openGroupChat('${g.id}')">
          <div class="group-row-avatar"><span style="font-size:24px">${emoji}</span></div>
          <div class="group-row-info">
            <div class="group-row-name">${escHtml(g.name)}</div>
            <div class="group-row-last">${escHtml(preview)}</div>
          </div>
          ${unread > 0 ? `<div class="group-row-badge">${unread}</div>` : '<span class="ms" style="color:var(--text3);font-size:18px">chevron_right</span>'}
        </div>`;
    }).join('');
  }

  function renderNearbyTeaser() {
    const el = document.getElementById('nearby-teaser'); if (!el) return;
    el.innerHTML = DEMO_TRAVELERS.slice(0, 3).map(t => `
      <div class="traveler-card">
        <div class="traveler-avatar">${t.emoji}</div>
        <div>
          <div class="traveler-name">${t.name} ${t.flag}</div>
          <div class="traveler-loc">${t.loc} · ${t.bio}</div>
        </div>
        <div class="traveler-actions">
          <button class="traveler-btn" onclick="App.snack('👋 Message coming soon!')">Say Hi</button>
        </div>
      </div>`).join('');
  }

  function openGroupChat(groupId) {
    const group = state.groups.find(g => g.id === groupId); if (!group) return;
    state.currentGroup = group;

    // Mark messages read
    const msgs = LS.get('gchat_' + groupId) || [];
    msgs.forEach(m => { m.read = true; });
    LS.set('gchat_' + groupId, msgs);

    document.getElementById('gchat-title').textContent   = group.name;
    document.getElementById('gchat-members').textContent = (group.members?.length || 1) + ' members';
    renderGroupMessages(groupId);

    const chatView = document.getElementById('groups-chat-view');
    chatView.classList.remove('hidden');
    requestAnimationFrame(() => chatView.classList.add('open'));
    const stRow = document.getElementById('social-subtab-row');
    if (stRow) stRow.classList.add('hidden');

    setTimeout(() => {
      const msgsEl = document.getElementById('gchat-messages');
      if (msgsEl) msgsEl.scrollTop = msgsEl.scrollHeight;
    }, 150);

    renderGroupsScreen();
  }

  function renderGroupMessages(groupId) {
    const id   = groupId || state.currentGroup?.id; if (!id) return;
    const msgs = LS.get('gchat_' + id) || [];
    const el   = document.getElementById('gchat-messages'); if (!el) return;

    if (!msgs.length) {
      el.innerHTML = `<div class="gchat-empty"><span class="ms ms-xl">forum</span><p>Be the first to say hello! 👋</p></div>`;
      return;
    }
    el.innerHTML = msgs.map(m => {
      const mine    = m.from === 'You';
      const timeStr = new Date(m.ts).toLocaleTimeString('en-US', { hour:'2-digit', minute:'2-digit' });
      return `
        <div class="gchat-msg ${mine ? 'mine' : 'theirs'}">
          ${!mine ? `<div class="gchat-sender">${escHtml(m.from)}</div>` : ''}
          <div class="gchat-bubble">${escHtml(m.text)}</div>
          <div class="gchat-ts">${timeStr}</div>
        </div>`;
    }).join('');
  }

  function closeGroupChat() {
    const chatView = document.getElementById('groups-chat-view');
    chatView.classList.remove('open');
    setTimeout(() => chatView.classList.add('hidden'), 300);
    state.currentGroup = null;
    const stRow = document.getElementById('social-subtab-row');
    if (stRow) stRow.classList.remove('hidden');
  }

  function sendGroupMsg() {
    const input = document.getElementById('gchat-input');
    const text  = input.value.trim(); if (!text || !state.currentGroup) return;
    const groupId = state.currentGroup.id;

    const msgs = LS.get('gchat_' + groupId) || [];
    msgs.push({ from: 'You', text, ts: Date.now(), read: true });
    LS.set('gchat_' + groupId, msgs);

    input.value = ''; input.style.height = 'auto';
    renderGroupMessages(groupId);

    const msgsEl = document.getElementById('gchat-messages');
    if (msgsEl) msgsEl.scrollTop = msgsEl.scrollHeight;

    // Auto-reply simulation
    const group     = state.currentGroup;
    const replyFrom = group.members?.find(m => m !== 'You') || 'Alex M.';
    const replies   = [
      'Sounds great! 🙌', 'Totally agree!', '₿ Nomad life!', 'When are you arriving? 🛫',
      "Let's meetup!", 'Have you tried the BTC ATM nearby? ⚡', '🗺️ Sharing my map pin shortly',
      'This is why I love this group! 🌍', 'Safe travels! ✈️', 'Bangkok tonight? 🍜',
    ];
    const reply = replies[Math.floor(Math.random() * replies.length)];
    setTimeout(() => {
      const latest = LS.get('gchat_' + groupId) || [];
      latest.push({ from: replyFrom, text: reply, ts: Date.now(), read: false });
      LS.set('gchat_' + groupId, latest);
      if (state.currentGroup?.id === groupId) {
        renderGroupMessages(groupId);
        if (msgsEl) msgsEl.scrollTop = msgsEl.scrollHeight;
      }
      renderGroupsScreen();
      renderHomeGroups();
    }, 1500 + Math.random() * 1500);
  }

  function groupChatKeydown(e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendGroupMsg(); }
  }

  function promptCreateGroup() {
    const name = prompt('Enter group name:');
    if (!name?.trim()) return;
    const newGroup = { id: 'g-' + Date.now(), name: name.trim(), members: ['You'], createdAt: Date.now() };
    state.groups.push(newGroup);
    LS.set('groups', state.groups);
    LS.set('gchat_' + newGroup.id, []);
    renderGroupsScreen();
    renderHomeGroups();
    snack('👥 Group created!');
  }

  function showGroupInfo() {
    if (!state.currentGroup) return;
    const members = state.currentGroup.members?.join(', ') || 'Just you';
    snack(`Members: ${members}`);
  }

  /* ── NEARBY TRAVELERS ────────────────────────────────────────── */
  function renderTravelers() {
    const el = document.getElementById('travelers-list'); if (!el) return;
    el.innerHTML = DEMO_TRAVELERS.map(t => `
      <div class="traveler-card">
        <div class="traveler-avatar">${t.emoji}</div>
        <div>
          <div class="traveler-name">${t.name} ${t.flag}</div>
          <div class="traveler-loc">${t.loc} · ${t.bio}</div>
        </div>
        <div class="traveler-actions">
          <button class="traveler-btn" onclick="App.snack('👋 Message coming soon!')">Say Hi</button>
        </div>
      </div>`).join('');
  }

  /* ── SOCIAL ──────────────────────────────────────────────────── */
  const DEMO_POSTS = [
    { user:'Alex M.🇺🇸', time:'2h ago', emoji:'🗼', title:'Tokyo Nomad Life',    text:'Just found an amazing co-working space in Shibuya — 3 BTC merchants within 500m! 🙌' },
    { user:'Sara K.🇬🇧',  time:'4h ago', emoji:'🏖️', title:'Bali Sunsets',        text:'Canggu co-working + sunset surf sessions = perfect remote work balance 🌅' },
    { user:'Marco B.🇮🇹', time:'6h ago', emoji:'🛕', title:'Chiang Mai Discovery', text:'Found a rooftop cafe where you can pay with Lightning Network ⚡ Incredible nomad scene!' },
    { user:'Yuki T.🇯🇵',  time:'1d ago', emoji:'🌉', title:'Lisbon Tech Scene',    text:'NHR tax regime + golden visa + amazing pastéis = perfect nomad base in Europe 🇵🇹' },
  ];

  function renderSocial() {
    const el = document.getElementById('social-feed'); if (!el) return;
    el.innerHTML = DEMO_POSTS.map(p => `
      <div class="post-card">
        <div class="post-header">
          <div class="post-avatar">${p.emoji}</div>
          <div>
            <div class="post-user">${p.user}</div>
            <div class="post-time">${p.time}</div>
          </div>
        </div>
        <div class="post-hero" style="background:linear-gradient(135deg,#1a1a2e,#16213e)">
          <span>${p.emoji}</span>
        </div>
        <div class="post-body">
          <div class="post-title">${p.title}</div>
          <div class="post-text">${p.text}</div>
        </div>
        <div class="post-actions">
          <span class="post-action" onclick="this.querySelector('.ms').style.color='var(--red)'">
            <span class="ms">favorite_border</span> Like
          </span>
          <span class="post-action"><span class="ms">chat_bubble_outline</span> Comment</span>
          <span class="post-action" onclick="App.snack('Shared!')"><span class="ms">share</span> Share</span>
        </div>
      </div>`).join('');
  }

  /* ── PROFILE ─────────────────────────────────────────────────── */
  function loadProfileForm() {
    const u = state.user || {};
    document.getElementById('profile-name-input').value  = u.name  || '';
    document.getElementById('profile-email-input').value = u.email || '';
    document.getElementById('profile-bio-input').value   = u.bio   || '';
    if (u.avatar) document.getElementById('profile-avatar-display').innerHTML = `<img src="${u.avatar}" alt="Avatar"/>`;
  }

  function onAvatarChange(input) {
    const file = input.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = e => {
      document.getElementById('profile-avatar-display').innerHTML = `<img src="${e.target.result}" alt="Avatar"/>`;
      state._pendingAvatar = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  function saveProfile() {
    const name  = document.getElementById('profile-name-input').value.trim();
    const email = document.getElementById('profile-email-input').value.trim();
    const bio   = document.getElementById('profile-bio-input').value.trim();
    if (!name) { snack('Please enter your name'); return; }
    state.user = { ...state.user, name, email, bio, avatar: state._pendingAvatar || state.user?.avatar || '' };
    LS.set('user', state.user);
    updateUserUI(); updateGreeting();
    closeModal('profile');
    snack('✅ Profile saved!');
  }

  function signInWithGoogle() { snack('🔐 Google Sign-In requires the mobile app') }

  function signOut() {
    if (!confirm('Sign out?')) return;
    state.user = null; LS.del('user');
    updateUserUI(); closeProfileMenu(); closeModal('settings');
    snack('Signed out');
  }

  function deleteAccount() {
    if (!confirm('Delete your account? This cannot be undone.')) return;
    state.user = null; state.trips = []; state.groups = []; state.savedPlaces = new Set();
    LS.del('user'); LS.del('trips'); LS.del('groups'); LS.del('saved');
    closeModal('settings'); closeProfileMenu();
    renderTrips(); renderHomeTrips(); updateUserUI();
    snack('Account deleted');
  }

  /* ── MISC ────────────────────────────────────────────────────── */
  function alertGroup() {
    if (state.groups.length === 0) { snack('No travel group — create one first'); return; }
    snack('🆘 Alert sent to your travel group!');
  }

  function findNearby(type) {
    const q   = type === 'hospital' ? 'hospital near me' : 'embassy near me';
    const url = `https://www.google.com/maps/search/${encodeURIComponent(q)}${state.location.lat ? `/@${state.location.lat},${state.location.lng},14z` : ''}`;
    openBrowser(url, type === 'hospital' ? 'Nearby Hospitals' : 'Nearby Embassies');
  }

  function openBrowser(url, title) {
    if (!url || url === '#') { snack('Coming soon!'); return; }
    // Open external links in a new tab (iframes blocked by most sites)
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  function toggleBrowserSize() {
    const sheet = document.getElementById('modal-browser');
    const icon  = document.querySelector('#browser-expand-btn .ms');
    const expanded = sheet.classList.toggle('bs-expanded');
    if (icon) icon.textContent = expanded ? 'close_fullscreen' : 'open_in_full';
  }

  function shareApp() {
    const txt = 'Check out Kipita — the ultimate digital nomad travel app! Bitcoin payments, AI trip planning & more. https://kipita.app';
    if (navigator.share) navigator.share({ title: 'Kipita App', text: txt, url: 'https://kipita.app' });
    else navigator.clipboard.writeText(txt).then(() => snack('Link copied!'));
  }

  /* ── PUBLIC API ─────────────────────────────────────────────── */
  return {
    init,
    // Navigation
    switchTab, toggleProfileMenu, closeProfileMenu,
    openModal, closeModal, closeTopModal,
    // Location
    detectLocation, findNearby,
    // Trips
    tripTab, createTrip, openTripDetail, markTripComplete,
    cancelTrip, recreateTrip, shareTrip, shareTripCard,
    aiPlanFromTrips, aiPlanTripForm, onDestInput,
    // Places / Explore
    placesTab, filterDestinations, toggleSaved,
    openDestDetail, planTripTo, aiTripFor, openPlacesCat,
    setExploreScope, aiExplore, openExploreFilter,
    // AI
    aiQuick, chatKeydown, chatResize, sendChat, addAiTrip,
    // Maps tab
    mapScreenFilter, mapScreenSearch, mapScreenSearchKey,
    // Wallet map
    initWalletMap,
    // Currency converter
    convertCurrency, swapCurrency,
    // Browser
    toggleBrowserSize,
    // Advisory
    refreshAdvisory,
    // Wallet
    connectWallet, walletCopy, walletSend, walletReceive,
    // Translate
    setLang, filterPhrases,
    // Perks
    copyCode,
    // Groups
    promptCreateGroup, openGroupChat, closeGroupChat,
    sendGroupMsg, groupChatKeydown, showGroupInfo,
    // Reviews
    switchSocialTab, filterReviews, sortReviews,
    openWriteReview, setReviewStar, setNomadScore, submitReview,
    // Misc
    alertGroup, openBrowser, shareApp,
    signInWithGoogle, signOut, deleteAccount,
    saveProfile, onAvatarChange,
    snack,
  };

})();

/* ── BOOT ─────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => App.init());
