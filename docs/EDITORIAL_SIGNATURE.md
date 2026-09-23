# CrossLens Editorial Signature

## Overview

A distinctive in-app editorial signature that translates the launcher icon's "crossing perspectives" concept into a masthead-appropriate design for the CrossLens interface.

## Design Philosophy

The editorial signature maintains the conceptual DNA of the launcher icon (overlapping perspectives crossed by a diagonal line) but reimagines it as an editorial masthead rather than repeating the icon's colored square composition.

## Components

### CrossLensSignature
Full editorial signature with:
- **Wordmark**: "CrossLens" in bold serif typography
- **Crossing rule**: Thin diagonal line that intersects the text
- **Perspective panes**: Subtle offset rectangles above and below (20-24% opacity), suggesting the "multiple views" concept

Available in three sizes:
- `Small`: For compact empty states (32dp text height, 80dp rule, 6dp pane height)
- `Medium`: For loading states and secondary placements (48dp text height, 120dp rule, 8dp pane height)
- `Large`: For home masthead and primary placements (64dp text height, 160dp rule, 10dp pane height)

### CrossLensWordmark
Lightweight variant with just text and crossing rule, for use in:
- Section headers where full signature would be too prominent
- Inline branding moments that need subtlety

## Theme Integration

The signature is fully theme-aware:
- **Wordmark text**: Uses `onBackground` for maximum readability
- **Crossing rule**: Uses `primary` color (teal accent in light, lighter teal in dark)
- **Perspective panes**: Use `primary` (20% opacity) and `tertiary` (24% opacity) for subtle but visible suggestion
- Automatically adapts to Light, Dark, and System themes

## Usage Locations

The signature appears strategically throughout the app:

1. **Home Screen** (Large)
   - Replaces plain text masthead with full editorial signature
   - Centers the brand at the top of the story feed

2. **Loading States** (Medium)
   - Home screen loading
   - Comparison screen loading
   - Provides branded experience during content load

3. **Empty States** (Small/Medium)
   - Home empty state
   - Explore no-results state
   - Comparison not-found state
   - Maintains brand presence in edge cases

4. **Section Headers** (Small wordmark)
   - "Framing Observations" section in comparison view
   - Subtle branding that doesn't compete with content

## Accessibility

- **TalkBack silent**: Uses `clearAndSetSemantics {}` to make the signature invisible to screen readers
- **Decorative only**: Never conveys functional information
- **Secondary to content**: Visual hierarchy keeps story content primary

## Implementation

Location: `app/src/main/java/com/crosslens/app/core/ui/components/EditorialSignature.kt`

```kotlin
// Full signature for masthead placement
CrossLensSignature(
    size = SignatureSize.Large,
    modifier = Modifier.padding(vertical = 16.dp)
)

// Lightweight wordmark for section headers
CrossLensWordmark(
    size = SignatureSize.Small
)
```

## Visual Relationship to Launcher Icon

- **Shared concept**: Both use "crossing perspectives" as core motif
- **Differentiated execution**: 
  - Launcher: Overlapping colored rounded rectangles with diagonal line
  - Signature: Text-based masthead with thin rule and subtle pane indicators
- **Color continuity**: Both use the teal accent as primary brand color
- **Scale appropriateness**: Icon optimized for small sizes, signature for editorial layout

## Design Rationale

The signature avoids duplicating the launcher icon because:
1. App icon compositions rarely work well at larger editorial scales
2. A text-based masthead feels more like a publication/magazine
3. The user explicitly requested "editorial masthead, not a small app-icon tile"
4. Provides visual variety while maintaining brand consistency

The crossing rule and offset panes are abstract enough to:
- Suggest "multiple perspectives" without being literal
- Feel sophisticated and editorial rather than illustrative
- Scale cleanly across different sizes and themes
- Work in composition with content rather than competing with it
