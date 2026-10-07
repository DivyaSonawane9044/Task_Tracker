# NOTES

## Summary of changes

1. **Search SQL precedence (P0)**: `AND` binds tighter than `OR`, so description matches bypassed `archived = FALSE` and the status filter. Archived tasks leaked, and `?status=DONE` returned all 49 rows. Parenthesised the `OR` in `TaskRepository`, `db/queries/search_tasks.sql` and the Oracle package.
2. **Artificial latency (P1)**: removed the `Thread.sleep` "complexity" block in `TaskController`. It added up to 1s per request, longest for the shortest queries.
3. **Validation (P1)**: an unknown `status`, `page < 1` or a `pageSize` outside 1–100 now return 400 instead of 500.
4. **Loading/error state (P1)**: `useTasks` never cleared `loading` on failure, so errors were never shown, and errors were never cleared once set.
5. **Stale responses (P1)**: an `ignore` flag in the effect cleanup stops an older, slower request from overwriting newer results.
6. **Page reset (P1)**: changing search or status resets to page 1 (previously "Page 4 of 1 / No tasks found").
7. **Debounce (P2)**: search input is debounced by 300ms instead of firing a request per keystroke.

Added `TaskControllerTest` (6 MockMvc tests). All fail on the original code and pass after the fixes.

## Not changed

- In-memory pagination (`subList` over all matches). It's correct here, and moving it into SQL is a larger change.
- `System.out.println`/`console.log` logging, unescaped `LIKE` wildcards, and ordering with no tiebreaker. All low impact.

## Biggest remaining risk

Search is a full scan (`LOWER(...) LIKE '%term%'`) and loads every match into memory before paginating, so it degrades as data grows. The H2 console and `show-sql` are enabled, which is not production-safe.

## Tools/AI used

Claude Code to read the code, reproduce bugs with curl, and draft fixes and tests. I confirmed the tests fail on the original code and pass on the fixed code.
