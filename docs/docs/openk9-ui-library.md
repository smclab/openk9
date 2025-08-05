---
id: openk9-ui-library
title: Openk9 Ui Library
sidebar_label: Openk9 Ui Library
slug: /openk9-ui-library
---

OpenK9 UI is a component library designed to facilitate integration with OpenK9. It provides tools for search, card management, filters, and other advanced functionalities.

## Installation and Configuration

Install with:

  - with npm  `npm install @openk9/search-frontend`
  - or with yarn `yarn add @openk9/search-frontend`



```javascript
import { OpenK9 } from "@openk9/search-frontend";

const openk9 = new OpenK9();
```

## Configurable Parameters

- **debounceTimeSearch** *(number, default: 600)*: Delay (ms) before sending the query to optimize performance.
- **languageSelect** *(string, default: "en_US")*: Search language (e.g., `it_IT`, `fr_FR`).
- **enabled** *(boolean, default: true)*: Enables or disables the OpenK9 instance.
- **initialSearchTerm** *(string, default: "")*: Default initial search term.
- **searchAutoselect** *(boolean)*: Activates automatic recognition of search terms.
- **searchReplaceText** *(boolean)*: Enables automatic correction of the searched text.
- **useKeycloak** *(boolean)*: Activates authentication via Keycloak.
- **useGenerativeApi** *(boolean, default: true)*: Enables automatic content generation via AI.
- **buttonDetailBackToCards** *(boolean, default: false)*: Displays a button to return to the previous card.
- **tenant** *(string)*: API URL for the OpenK9 instance (e.g., `https://demo.openk9.io`).
- **useQueryAnalysis** *(boolean, default: true)*: Enables advanced query analysis.
- **template** *(array)*: Allows customization of the result layout.

## Main Components

### Search
The primary component for searching content in OpenK9.

#### Properties
- **element** *(HTML element)*: DOM node where the component is mounted.
- **btnSearch** *(boolean)*: Adds a button to manually start the search.
- **defaultValue** *(string)*: Sets a default search value.
- **callbackClickSearch** *(function)*: Function executed when the user initiates a search.
- **actionOnClick** *(function)*: Allows customization of the query submission action.

---

### Active Filters
Manages active search filters.

#### Properties
- **actionRemoveFilters** *(function)*: Callback to remove all filters at once.
- **callbackRemoveFilter** *(function)*: Callback to remove a selected filter.

---

### Detail Mobile
Component for displaying details on mobile devices.

#### Properties
- **Possible improvement**: Add a callback to customize the detail modal.

---

### Calendar Mobile
Component for selecting dates in search filters.

#### Properties
- **isVisible** *(boolean)*: Controls the visibility of the calendar modal.
- **setIsVisible** *(function)*: Changes the open/close state of the calendar.
- **startDate/endDate** *(state)*: Controls the selected dates.

---

### Change Language
Component for changing the interface language.

- **No additional configuration required.**

---

### Generate Response
Uses artificial intelligence to generate responses based on the search query.

---

### Tabs Configurable
Manages navigation through tabs.

#### Properties
- **onAction** *(function)*: Callback executed when switching tabs.
- **scrollMode** *(string)*: Configures scrolling style (`classic` or `with arrows`).
- **speed** *(number)*: Scrolling speed.
- **distance** *(number)*: Distance traveled per scroll.
- **reset** *(object)*: Specifies which elements to reset when switching tabs.
- **textLabelScreenReader** *(string)*: Descriptive label for screen readers.

---

### Result List
Displays search results in a list of cards.

#### Variants
1. **ResultList**: Standard version.
2. **ResultListConfigurable**: Advanced version with customization options.

#### Additional Properties
- **noResultsCustom** *(React.ReactNode)*: Custom component displayed when no results are found.
- **changeOnOver** *(boolean)*: If enabled (`true`), changes the card detail on hover instead of click.

---

### Result List Pagination
Component for managing the pagination of search results.

#### Properties
- **Available callback:** Executed when the user changes pages (useful for updating the interface or tracking the event).

---

### Skeleton
Loading component that displays an animation while content is being loaded.

#### Properties
- **width** *(string)*: Skeleton width.
- **height** *(string)*: Skeleton height.
- **counter** *(number)*: Number of skeletons displayed simultaneously.
- **circle** *(boolean)*: Defines the shape of the skeleton (`true` for circle, `false` for rectangle).
- **backgroundColor** *(string)*: Skeleton background color.
- **gap** *(string)*: Space between skeletons in the layout.

---

## Conclusion
This documentation outlines the main components and configurations of OpenK9 UI. For advanced implementations, further exploration of template configurations and available APIs is recommended.
