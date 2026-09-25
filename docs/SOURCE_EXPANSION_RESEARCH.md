# Source Expansion Research

## Objective
Expand from 15 to ~30-40 reputable international sources to improve event clustering coverage.

## Current Sources (15)

### Europe/UK (8)
1. BBC News (UK) - en-GB
2. The Guardian (UK) - en-GB
3. Deutsche Welle (Germany) - en
4. Der Spiegel International (Germany) - en
5. France 24 (France) - en
6. swissinfo.ch (Switzerland) - en
7. ABC (Spain) - es
8. Al Jazeera (Qatar) - en

### North America (2)
9. The New York Times (US) - en-US
10. CBC News (Canada) - en-CA

### Asia-Pacific (5)
11. ABC News (Australia) - en-AU
12. The Japan Times (Japan) - en
13. The Hindu (India) - en-IN
14. Channel NewsAsia (Singapore) - en-SG
15. 朝日新聞 Asahi Shimbun (Japan) - ja

## Target Sources for Batch 1 (Add ~5-8 sources)

### Priority 1: International Wire Services
These have the highest overlap potential for major events.

**Reuters**
- Publisher: Thomson Reuters
- Country: UK (global operations)
- Language: English
- Feed URL: https://www.reutersagency.com/feed/ (investigate)
- Type: International wire service
- Expected overlap: Very high - covers all major global events
- Provenance: Thomson Reuters corporate structure
- Status: VALIDATE

**Associated Press**
- Publisher: Associated Press
- Country: US (cooperative)
- Language: English
- Feed URL: https://feeds.apnews.com/ (investigate subdomain)
- Type: International wire service / cooperative
- Expected overlap: Very high - covers all major global events
- Provenance: AP cooperative structure
- Status: VALIDATE

### Priority 2: Additional Public Broadcasters

**NHK World (Japan)**
- Publisher: NHK (Japan Broadcasting Corporation)
- Country: Japan
- Language: English
- Feed URL: https://www3.nhk.or.jp/nhkworld/en/news/rss/... (investigate)
- Type: Public international broadcaster
- Expected overlap: High for Asia-Pacific and global events
- Provenance: NHK charter
- Status: VALIDATE

**TRT World (Turkey)**
- Publisher: Turkish Radio and Television Corporation
- Country: Turkey
- Language: English
- Feed URL: https://www.trtworld.com/feed/rss/news (investigate)
- Type: State-owned international broadcaster
- Expected overlap: Moderate-High for Middle East, Europe, global events
- Provenance: TRT corporate profile
- Status: VALIDATE

**RTE News (Ireland)**
- Publisher: Raidió Teilifís Éireann
- Country: Ireland
- Language: English
- Feed URL: https://www.rte.ie/rss/... (investigate)
- Type: Public broadcaster
- Expected overlap: Moderate for European and global events
- Provenance: RTE charter
- Status: VALIDATE

### Priority 3: Major International Newspapers

**The Washington Post (US)**
- Publisher: Nash Holdings (Jeff Bezos)
- Country: US
- Language: English
- Feed URL: https://feeds.washingtonpost.com/rss/world (investigate)
- Type: Major newspaper
- Expected overlap: High for global events
- Provenance: Washington Post ownership documentation
- Status: VALIDATE

**Le Monde (France)**
- Publisher: Le Monde Group
- Country: France
- Language: French
- Feed URL: https://www.lemonde.fr/rss/... (investigate)
- Type: Major newspaper
- Expected overlap: High for European and global events
- Provenance: Le Monde corporate structure
- Status: VALIDATE

**The Times of India**
- Publisher: Bennett, Coleman & Co. Ltd
- Country: India
- Language: English
- Feed URL: https://timesofindia.indiatimes.com/rss.cms (investigate)
- Type: Major newspaper
- Expected overlap: High for South Asian and global events
- Provenance: Times of India corporate profile
- Status: VALIDATE

## Target Sources for Batch 2 (Add 5-7 more)

**South China Morning Post (Hong Kong)**
- Publisher: Alibaba Group
- Country: Hong Kong SAR
- Language: English
- Expected overlap: High for Asian and global events

**El País (Spain)**
- Publisher: PRISA Media
- Country: Spain
- Language: Spanish
- Expected overlap: High for European, Latin American, global events

**The Independent (UK)**
- Publisher: Independent Digital News & Media
- Country: UK
- Language: English
- Expected overlap: High for UK and global events

**The Irish Times (Ireland)**
- Publisher: The Irish Times Trust
- Country: Ireland
- Language: English
- Expected overlap: Moderate for European and global events

**Arab News (Saudi Arabia)**
- Publisher: Saudi Research and Marketing Group
- Country: Saudi Arabia
- Language: English
- Expected overlap: High for Middle East and global events

**The Straits Times (Singapore)**
- Publisher: SPH Media
- Country: Singapore
- Language: English
- Expected overlap: High for Southeast Asian and global events

**Korea Herald (South Korea)**
- Publisher: Herald Corporation
- Country: South Korea
- Language: English
- Expected overlap: Moderate-High for East Asian and global events

## Target Sources for Batch 3 (Add 5-7 more)

**Jakarta Post (Indonesia)**
- Publisher: PT Bina Media Tenggara
- Country: Indonesia
- Language: English
- Expected overlap: Moderate for Southeast Asian and global events

**Dawn (Pakistan)**
- Publisher: Dawn Media Group
- Country: Pakistan
- Language: English
- Expected overlap: Moderate-High for South Asian and global events

**Business Day (South Africa)**
- Publisher: Arena Holdings
- Country: South Africa
- Language: English
- Expected overlap: Moderate for African and global events

**Haaretz (Israel)**
- Publisher: Haaretz Group
- Country: Israel
- Language: English
- Expected overlap: High for Middle East and global events

**Yomiuri Shimbun (Japan)**
- Publisher: Yomiuri Shimbun Holdings
- Country: Japan
- Language: Japanese
- Expected overlap: High for Japanese and global events

## Validation Checklist

For each source, verify:
- [ ] RSS feed URL is stable and HTTPS
- [ ] Feed returns valid XML/RSS
- [ ] Items have required fields: title, link, pubDate
- [ ] Links are HTTPS
- [ ] Publication dates are parseable
- [ ] Images available (enclosure or media:content)
- [ ] No malformed text in descriptions
- [ ] Fetch performance acceptable (<5s)
- [ ] Attribution/ownership documented
- [ ] Editorial description available with provenance

## Regional/Language Balance Target

**Current (15 sources)**:
- English: 14 sources (93%)
- Spanish: 1 source (7%)
- Japanese: 1 source (7%)
- Europe/UK: 8 (53%)
- North America: 2 (13%)
- Asia-Pacific: 5 (33%)
- Middle East: 1 (7%) - counted in Europe

**Target (30-35 sources)**:
- English: 22-25 sources (70-75%)
- Spanish: 2-3 sources (7-10%)
- French: 1-2 sources (3-5%)
- Japanese: 2-3 sources (6-9%)
- Other: 1-2 sources (3-5%)

- Europe/UK: 10-12 (30-35%)
- North America: 3-4 (9-12%)
- Asia-Pacific: 10-12 (30-35%)
- Middle East: 3-4 (9-12%)
- Latin America: 1-2 (3-6%)
- Africa: 1-2 (3-6%)

## Expected Clustering Improvements

**Current state**: 15 sources, low event overlap observed
**Target with 30 sources**: 
- Major breaking news: 3-5 sources within 24 hours
- Scheduled events (summits, elections): 4-8 sources within 24 hours
- Regional events: 2-4 sources within 48 hours
- Niche events: May still be 1 source (acceptable)

## Implementation Plan

1. **Batch 1** (next): Validate and add 5-8 sources → reach ~20-23 total
2. **Batch 2**: Validate and add 5-7 sources → reach ~25-30 total
3. **Batch 3** (optional): Validate and add remaining to reach 35-40 if needed

After each batch:
- Run real-feed clustering audit
- Measure image coverage
- Identify confident multi-publisher clusters
- Check for false matches
- Adjust thresholds if needed
