# Search and index recommendation for DB-backed links

When links and categories are persisted in PostgreSQL (instead of or in addition to in-memory/store), search performance will depend on indexes.

## Current state

- **Categories:** API can use in-memory store; DB model exists (`Category`: `name`, `emoji`, `is_visible`). If you query by `name`, an index on `name` is already implied by `unique=True` (and you can add an explicit index if needed).
- **Links:** Currently filtered in the frontend (e.g. by title, summary, url, keywords). Once links are stored in the DB, search should be done in the database for scalability.

## When you add a `links` (or similar) table

Suggested columns (adjust to your schema):

- `id`, `user_id`, `category_id`, `url`, `title`, `summary`, `keywords` (array or JSONB), `created_at`, etc.

### Indexes to add

1. **Category / user scope**
   - `CREATE INDEX idx_links_category_id ON links(category_id);`
   - `CREATE INDEX idx_links_user_id ON links(user_id);`
   - Composite if you often filter by both: `(user_id, category_id)`.

2. **Text search (title / summary)**
   - **Option A – simple `LIKE`/`ilike`:**  
     `CREATE INDEX idx_links_title_trgm ON links USING gin(title gin_trgm_ops);`  
     (requires `pg_trgm` extension.)
   - **Option B – full-text search:**  
     Add a generated column, e.g. `tsv tsvector GENERATED ALWAYS AS (to_tsvector('english', coalesce(title,'') || ' ' || coalesce(summary,''))) STORED`, then:
     - `CREATE INDEX idx_links_tsv ON links USING gin(tsv);`
   - Use the same pattern for `keywords` if you search by keyword (e.g. `jsonb`/array + GIN).

3. **Metadata columns**
   - Any column used in `WHERE` or `ORDER BY` (e.g. `created_at`, `category_id`) should have an index if the table grows large.

## Scalability summary

- **Current (client-side filter):** Fine for hundreds of links per category; no DB index needed for “metadata columns” yet because there is no link table.
- **After DB-backed links:** Add the indexes above (and optionally full-text search) so search remains fast as data grows.

Implement these when you introduce the link persistence layer and a search API that queries PostgreSQL.
