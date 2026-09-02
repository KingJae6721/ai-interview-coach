-- Manual PostgreSQL migration. Flyway/Liquibase is not configured in this project.
-- Run this script before deploying the entity mappings that require normalized_name NOT NULL.
BEGIN;

LOCK TABLE companies, job_positions IN SHARE ROW EXCLUSIVE MODE;

DO $migration$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM companies
        WHERE name IS NULL
           OR lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g'))) = ''
    ) THEN
        RAISE EXCEPTION 'Company name cannot be normalized. Resolve invalid names manually.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM job_positions
        WHERE name IS NULL
           OR lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g'))) = ''
    ) THEN
        RAISE EXCEPTION 'JobPosition name cannot be normalized. Resolve invalid names manually.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM companies
        GROUP BY lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Company normalized_name collision detected. Merge references manually before retrying.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM job_positions
        GROUP BY company_id,
                 lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'JobPosition normalized_name collision detected. Merge references manually before retrying.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM companies
        WHERE normalized_name IS NOT NULL
          AND normalized_name <> lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
    ) THEN
        RAISE EXCEPTION 'Existing Company normalized_name differs from the normalization policy.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM job_positions
        WHERE normalized_name IS NOT NULL
          AND normalized_name <> lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
    ) THEN
        RAISE EXCEPTION 'Existing JobPosition normalized_name differs from the normalization policy.';
    END IF;
END
$migration$;

UPDATE companies
SET normalized_name = lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
WHERE normalized_name IS NULL;

UPDATE job_positions
SET normalized_name = lower(trim(regexp_replace(normalize(name, NFKC), '[[:space:]]+', ' ', 'g')))
WHERE normalized_name IS NULL;

ALTER TABLE companies
    ALTER COLUMN normalized_name SET NOT NULL;

ALTER TABLE job_positions
    ALTER COLUMN normalized_name SET NOT NULL;

ALTER TABLE companies
    DROP CONSTRAINT IF EXISTS uk_companies_normalized_name;
ALTER TABLE companies
    ADD CONSTRAINT uk_companies_normalized_name UNIQUE (normalized_name);

ALTER TABLE job_positions
    DROP CONSTRAINT IF EXISTS uk_job_positions_company_normalized_name;
ALTER TABLE job_positions
    ADD CONSTRAINT uk_job_positions_company_normalized_name UNIQUE (company_id, normalized_name);

COMMIT;
