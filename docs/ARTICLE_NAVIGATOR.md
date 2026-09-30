# Article Navigator - Gesture Map and Behavior Specification

## Overview

The Article Navigator provides a Flipboard-inspired full-screen reading experience with swipe-based navigation across publishers and stories.

## Gesture Map

### Horizontal Navigation (Within Event Cluster)
- **Swipe Left**: Navigate to next publisher's article in same event cluster
- **Swipe Right**: Navigate to previous publisher's article in same event cluster
- **Availability**: Only for confident event clusters with 2+ distinct publishers
- **Threshold**: 50px horizontal drag for swipe detection

### Vertical Navigation (Across Feed Stories)
- **Swipe Up**: Navigate to next story in feed sequence
- **Swipe Down**: Navigate to previous story in feed sequence
- **Availability**: Always available (respects feed boundaries)
- **Threshold**: 50px vertical drag for swipe detection

### Top Tap Action
- **Tap top navigation area**: Return to first story in feed and trigger refresh
- **Target**: 72dp height top bar with CrossLens logo and position indicator
- **Behavior**: Resets feed position to 0 and calls repository.refresh()

## Navigation State Model

### Feed Navigation
- Track current feed index (vertical position)
- Total feed items count
- Has previous in feed: currentFeedIndex > 0
- Has next in feed: currentFeedIndex < totalFeedItems - 1

### Cluster Navigation
- Track current article index within cluster (horizontal position)
- Total cluster items count
- Has horizontal navigation: isEventCluster AND clusterSize >= 2 AND distinctPublishers >= 2
- Has previous in cluster: hasHorizontalNavigation AND currentArticleIndex > 0
- Has next in cluster: hasHorizontalNavigation AND currentArticleIndex < clusterSize - 1

### State Resets
- Changing stories (vertical navigation): resets to first article (index 0) in new story
- Return to first action: resets to feed index 0, article index 0

## Eligibility Rules

### Horizontal Navigation Eligibility
1. Story must be marked as event cluster (isEventCluster = true)
2. Cluster must have at least 2 articles from distinct publishers
3. At least 2 distinct publisherNames in cluster metadata

### Single-Source Behavior
- Stories with only 1 source: no horizontal navigation available
- UI shows "Single-source story (no horizontal navigation)" message
- Horizontal swipes have no effect
- Vertical navigation still works normally

## Boundary States

### At Cluster Boundaries
- **First article in cluster**: No previous in cluster
  - Swipe right shows: "First publisher in this event"
  - Hint: "Swipe vertically for other stories"
- **Last article in cluster**: No next in cluster
  - Swipe left shows: "Last publisher in this event"
  - Hint: "Swipe vertically for other stories"

### At Feed Boundaries
- **First story in feed**: No previous in feed
  - Swipe down shows: "First story in feed"
  - Hint: "Tap top to refresh"
- **Last story in feed**: No next in feed
  - Swipe up shows: "Last story in feed"
  - Hint: "End of feed"

### Boundary Feedback Overlay
- Appears centered on screen
- Card-based UI with MaterialTheme.colorScheme.surfaceVariant
- Auto-dismisses after 1 second
- Animated fade-in with 0.9 alpha
- Clear message and contextual hint

## Animations

### Transition Types
- **Horizontal swipes**: slideInHorizontally/slideOutHorizontally with fadeIn/fadeOut
  - Swipe left: content slides in from right, exits left
  - Swipe right: content slides in from left, exits right
- **Vertical swipes**: slideInVertically/slideOutVertically with fadeIn/fadeOut
  - Swipe up: content slides in from bottom, exits top
  - Swipe down: content slides in from top, exits bottom
- **Fallback**: fadeIn/fadeOut at 300ms when no gesture direction available

### Animation Duration
- All transitions: 300ms (comfortable reading pace)
- Boundary feedback: 200ms fade-in, 1000ms hold, auto-dismiss
- SizeTransform with clip = false for smooth content size changes

## Accessibility

### Screen Reader Support
- Top navigation bar: "CrossLens. Story X of Y. Tap to return to first story and refresh feed."
- Article container: "Article: [headline]" semantic label
- Open Original button: "Open original article on [publisher name]"
- Navigation hints card: Textual descriptions of all gestures

### Reduced Motion
- Animations respect system reduced-motion settings (when implemented in future)
- Transitions degrade to simple fades when motion is disabled
- Boundary feedback remains visible for clarity

## Article Content Display

### Full-Screen Layout
1. **Top Bar (72dp)**
   - CrossLens signature (small)
   - Position indicator: "X of Y"
   - Tap to return and refresh

2. **Article Content (Scrollable)**
   - Publisher info card:
     * Publisher name (bold, primary color)
     * Country • Language • Editorial description
     * Published timestamp
     * Attribution line
   - Article image (if available, 240dp height)
   - Headline (headlineMedium, bold)
   - Language indicator
   - Full article content (bodyLarge, proper line height)
   - "Open Original" button (CustomTabs)
   - Navigation hints card

### Preserved Metadata
- Original publisher name, attribution
- Publish time, language tag
- Source country, editorial type
- Full article URL (no modifications)

## Navigation Hints

Always visible at bottom of article content:

```
Navigation

← Swipe left/right for other publishers (2/4)
[or: Single-source story (no horizontal navigation)]

↕ Swipe up/down for next/previous story

Tap top area to return to first story and refresh
```

## Integration Points

### Entry Points
- Feed story cards: tap → ArticleNavigator with storyId
- Feed event cards: tap → ArticleNavigator with storyId
- Direct navigation: ArticleNavigator.createRoute(storyId, articleId?)

### Data Flow
1. Load feed stories via repository.observeStories()
2. Find story position in feed by storyId
3. Load articles for story via repository.getArticlesForStory(storyId)
4. Enrich with SourceMetadata from SourceMetadataRegistry
5. Track navigation state (feed position, article position)

### State Management
- ArticleNavigatorViewModel manages all navigation state
- Flow-based UI state with Loading/Success/Error
- Separate NavigationState for position tracking and direction availability

## Testing Scenarios

### Horizontal Navigation Test
1. Navigate to 4-source event cluster (e.g., Paramount/Warner Bros merger)
2. Verify initial article displays (publisher 1)
3. Swipe left → should show publisher 2 with slide animation
4. Swipe left → should show publisher 3
5. Swipe left → should show publisher 4
6. Swipe left again → boundary feedback: "Last publisher in this event"
7. Swipe right → should return to publisher 3
8. Continue right to publisher 1
9. Swipe right again → boundary feedback: "First publisher in this event"

### Vertical Navigation Test
1. From any article, swipe up
2. Should navigate to next story's first article
3. Verify article resets to position 0 (first article if cluster)
4. Continue swiping up through feed
5. At last story, swipe up → boundary feedback: "Last story in feed"
6. Swipe down to navigate backwards
7. At first story, swipe down → boundary feedback: "First story in feed"

### Top Tap Test
1. Navigate to middle of feed (story 5+)
2. Navigate to middle of cluster (article 2+)
3. Tap top bar area
4. Should return to first story (position 0)
5. Should show first article of that story

### Single-Source Test
1. Navigate to single-source story
2. Verify navigation hints show "Single-source story"
3. Attempt horizontal swipes → no navigation occurs
4. Vertical swipes still work normally

### Mixed Navigation Test
1. Start at 4-source cluster, article 2
2. Swipe up → next story, resets to article 0
3. Swipe left → next article in new cluster
4. Swipe down → previous story, resets to article 0
5. Tap top → returns to first story, article 0

## Performance Considerations

- Gesture detection threshold: 50px (balance between responsiveness and accidental triggers)
- Animation duration: 300ms (smooth without feeling sluggish)
- Boundary feedback auto-dismiss: 1000ms (enough time to read message)
- Article content loads on-demand (not preloaded)
- Feed stories loaded once during initialization
- State updates trigger recomposition with AnimatedContent

## Known Limitations

- No preloading of adjacent articles (loads on navigation)
- No swipe preview/peek gesture (commit on drag end)
- Boundary feedback blocks gestures during display (1s cooldown)
- Top tap area fixed height (may conflict with system gestures on some devices)
- No haptic feedback on boundaries (future enhancement)

## Future Enhancements

- Add preloading of next/previous articles
- Implement swipe preview (partial slide before commit)
- Add haptic feedback on boundaries
- Support for reduced-motion settings (system accessibility)
- Gesture tutorial overlay on first use
- Saved reading position (resume where left off)
- Share article functionality
- Bookmark/save for later
