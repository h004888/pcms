---
target: customer homepage
total_score: 19
p0_count: 2
p1_count: 2
timestamp: 2026-07-01T09-52-30Z
slug: src-components-shop-longchauhomecontent-tsx
---
Method: dual-agent (A: design-review · B: detector-scan)

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2/4 | Carousel arrows permanently invisible (bug) |
| 2 | Match System / Real World | 3/4 | Emoji icons inconsistent; no OTC/Rx distinction |
| 3 | User Control and Freedom | 2/4 | No back-to-top; 11 sections no nav aids |
| 4 | Consistency and Standards | 3/4 | Strong design system; minor icon inconsistency |
| 5 | Error Prevention | 1/4 | No pharmacy safeguards (Rx, dosage, interactions) |
| 6 | Recognition Rather Than Recall | 3/4 | Most things visible; no sticky section nav |
| 7 | Flexibility and Efficiency | 1/4 | No keyboard shortcuts; no search in content |
| 8 | Aesthetic and Minimalist Design | 3/4 | Clean but 11 sections = content overload |
| 9 | Error Recovery | 0/4 | No undo, no retry, no recovery patterns |
| 10 | Help and Documentation | 1/4 | No FAQ, chatbot, or contextual help |
| **Total** | | **19/40** | **Poor — significant UX improvements needed** |

## Anti-Patterns Verdict

**LLM assessment**: LOW SLOP — Would NOT be immediately perceived as AI-made. The design system discipline (Rarity Rule, Flat-By-Default, One-Family Rule) is consistently followed. Banned patterns (gradient text, side-stripe borders, numbered sections) are absent. Residual AI tells: pervasive `bg-gradient-to-br` on 7+ decorative elements, emoji-as-icon content decoration, identical hover-shadow pattern across all card types.

**Deterministic scan**: 14 discrepancies found between code and DESIGN.md:
- 4 HIGH: glassmorphism overuse, danger-600 on CTAs, emoji icons, shadows at rest
- 4 MEDIUM: teal overuse, border+shadow combo, category grid sameness, section overload
- 6 LOW: hero heading sizing, shadow intensity, press scale, text-wrap, side-stripe-equivalent, uppercase eyebrow

## Overall Impression

Strong design system foundation with clean code implementation, but suffers from **content overload (11 sections)**, **carousel navigation bug**, and **medically inappropriate use of red for promotions**. The page tries to do everything at once. When it works, it looks professional; when it breaks (invisible arrows, blank page while 11 APIs load), the gaps are significant for a healthcare platform.

## What's Working

1. **Design system discipline**: Named rules from DESIGN.md are faithfully implemented — rare and commendable.
2. **Empty state handling**: Every data section has a graceful fallback — production-ready.
3. **Accessibility foundations**: reduced-motion support, semantic HTML, aria-labels at system level.

## Priority Issues

### [P0] Cookie banner placed mid-page — legal risk
The cookie consent banner appears between Quick Links and Flash Sale, not as a fixed overlay. Users scroll past it. **Fix**: Move to fixed bottom/top banner.

### [P0] Carousel arrows permanently invisible (implementation bug)
`group-hover:opacity-100` used but parent container lacks `group` class. Arrows never show. Users on touch devices have zero manual carousel control.

### [P1] 11-section page has no loading states + single point of failure
`Promise.all` over 11 API calls means one slow endpoint blocks the entire page. No skeleton screens. **Fix**: Suspense boundaries per section.

### [P1] danger-600 (red) used for promotional badges and CTAs
In a pharmacy context, red signals poison/warning — emotionally counterproductive. **Fix**: Use accent-600 teal for CTAs, amber for badges.

### [P2] No wayfinding on a very long page
11 sections with no sticky nav, no back-to-top, no section progress indicator. Users must scroll endlessly.

## Persona Red Flags

**Alex (Power User)**: No keyboard shortcuts. No bulk actions. No way to customize the 11-section layout.

**Jordan (First-Timer)**: 11 sections are overwhelming. Cookie banner has no "Decline" option. No search bar in content area.

**Sam (Accessibility)**: Carousel arrows unreachable (invisible). line-clamp truncation hidden from screen readers. No focus-visible styles defined.

**Casey (Mobile)**: Extremely long scroll. Hover-only carousel controls don't work on touch. No persistent cart access from content area.

## Minor Observations

- SectionHeading + section `mt-10` creates ~64px gaps (double the intended 40px)
- Emoji sub-promo icons (🤖🥗) inconsistent with Lucide icon system
- `flatFlashSales` mixes items from different flash sales without boundaries
- No prescription/age indicators on product cards
- HealthCheckCard `<button>` has no onClick handler — clicking does nothing
- Video carousel links all go to `/video` with no slug differentiation

## Questions to Consider

- Does a pharmacy homepage need 11 sections, or is this a kitchen sink without clear prioritization?
- Why is red used for promotions when the brand is "trustworthy" and "reassuring"?
- Has the carousel been tested on a real mobile device (where hover doesn't exist)?
- What happens to the entire page when one of 11 API calls fails?
- Where is the search bar for a user whose primary job is "find medicine fast"?
