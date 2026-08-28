#!/usr/bin/env python3
import argparse
import glob
import os
import re
import sys
from datetime import date
from typing import Dict, List, Optional, Tuple

SQL_INSERT = "INSERT"
SQL_VALUES = "VALUES"

INSERT_INTO_EQUIPMENT = "INSERT INTO equipment"
INSERT_INTO_MUSCLE = "INSERT INTO muscle"
INSERT_INTO_EXERCISE_MUSCLE = "INSERT INTO exercise_muscle"
INSERT_INTO_EXERCISE_EQUIPMENT = "INSERT INTO exercise_equipment"
INSERT_INTO_EXERCISE_WORKOUT_TYPE = "INSERT INTO exercise_workout_type"
EXERCISE_INSERT_MARKER = (
    "INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)"
)


def skip_ws(s: str, i: int) -> int:
    while i < len(s) and s[i] in " \t\n\r":
        i += 1
    return i


def parse_sql_string(s: str, i: int) -> Tuple[str, int]:
    if i >= len(s) or s[i] != "'":
        raise ValueError("expected opening quote")
    i += 1
    parts: List[str] = []
    while i < len(s):
        c = s[i]
        if c == "'":
            if i + 1 < len(s) and s[i + 1] == "'":
                parts.append("'")
                i += 2
                continue
            return "".join(parts), i + 1
        parts.append(c)
        i += 1
    raise ValueError("unterminated string literal")


def take_until_semicolon(text: str, start: int) -> Tuple[str, int]:
    i = start
    n = len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            if c == "'":
                if i + 1 < n and text[i + 1] == "'":
                    i += 2
                    continue
                in_str = False
            i += 1
            continue
        if c == "'":
            in_str = True
            i += 1
            continue
        if c == ";":
            return text[start : i + 1], i + 1
        i += 1
    return text[start:], n


def skip_line_and_block_comments(text: str, i: int) -> int:
    n = len(text)
    while i < n:
        if text[i] in " \t\r\n":
            i += 1
            continue
        if text[i : i + 2] == "--":
            while i < n and text[i] != "\n":
                i += 1
            continue
        if text[i : i + 2] == "/*":
            i += 2
            while i + 1 < n and not (text[i] == "*" and text[i + 1] == "/"):
                i += 1
            i = min(i + 2, n)
            continue
        break
    return i


def iter_insert_statements(text: str) -> List[str]:
    out: List[str] = []
    i = 0
    n = len(text)
    while i < n:
        i = skip_line_and_block_comments(text, i)
        if i >= n:
            break
        if text[i : i + len(SQL_INSERT)].upper() == SQL_INSERT:
            stmt, j = take_until_semicolon(text, i)
            out.append(stmt.strip())
            i = j
        else:
            i += 1
    return out


def extract_exercise_name_from_exercise_insert(stmt: str) -> str:
    name = extract_first_values_string(stmt)
    if name is None:
        raise ValueError("VALUES not found in exercise INSERT")
    return name


def first_column_strings_for_join_insert(stmt: str, insert_into_table: str) -> List[str]:
    lower = stmt.lower()
    needle = insert_into_table.lower()
    if needle not in lower:
        return []
    values_kw = SQL_VALUES.lower()
    idx = lower.find(values_kw)
    if idx < 0:
        return []
    names: List[str] = []
    i = idx + len(SQL_VALUES)
    n = len(stmt)
    while i < n:
        i = skip_ws(stmt, i)
        if i >= n:
            break
        if stmt[i] != "(":
            i += 1
            continue
        i += 1
        i = skip_ws(stmt, i)
        if i < n and stmt[i] == "'":
            sval, j = parse_sql_string(stmt, i)
            names.append(sval)
            i = j
        while i < n and stmt[i] != ")":
            i += 1
        if i < n and stmt[i] == ")":
            i += 1
        i = skip_ws(stmt, i)
        if i < n and stmt[i] == ",":
            i += 1
            continue
        if i < n and stmt[i] == ";":
            break
    return names


def extract_equipment_name_from_equipment_insert(stmt: str) -> Optional[str]:
    lower = stmt.lower()
    if INSERT_INTO_EQUIPMENT.lower() not in lower:
        return None
    values_kw = SQL_VALUES.lower()
    idx = lower.find(values_kw)
    if idx < 0:
        return None
    i = skip_ws(stmt, idx + len(SQL_VALUES))
    if i < len(stmt) and stmt[i] == "(":
        i += 1
    if i < len(stmt) and stmt[i] == "'":
        name, _ = parse_sql_string(stmt, i)
        return name
    return None


def extract_first_values_string(stmt: str) -> Optional[str]:
    lower = stmt.lower()
    values_kw = SQL_VALUES.lower()
    idx = lower.find(values_kw)
    if idx < 0:
        return None
    i = skip_ws(stmt, idx + len(SQL_VALUES))
    if i < len(stmt) and stmt[i] == "(":
        i += 1
    i = skip_ws(stmt, i)
    if i < len(stmt) and stmt[i] == "'":
        name, _ = parse_sql_string(stmt, i)
        return name
    return None


def sanitize_filename(name: str) -> str:
    s = name.lower()
    s = re.sub(r"[^a-z0-9]+", "_", s)
    s = re.sub(r"_+", "_", s)
    s = s.strip("_")
    return s or "exercise"


def next_sequence_for_date(data_dir: str, day: str) -> int:
    max_seq = 0
    pattern = os.path.join(data_dir, f"changelog-{day}_*.sql")
    for path in glob.glob(pattern):
        base = os.path.basename(path)
        m = re.match(r"^changelog-" + re.escape(day) + r"_([0-9]+)_", base)
        if m:
            max_seq = max(max_seq, int(m.group(1), 10))
    return max_seq + 1


def load_source_files(data_dir: str) -> str:
    paths: List[str] = []
    for entry in os.scandir(data_dir):
        if not entry.is_file():
            continue
        if not entry.name.endswith(".sql"):
            continue
        paths.append(entry.path)
    paths.sort()
    chunks: List[str] = []
    for p in paths:
        with open(p, "r", encoding="utf-8") as fh:
            chunks.append(fh.read())
    return "\n\n".join(chunks)


def ensure_stmt_terminated(stmt: str) -> str:
    text = stmt.rstrip()
    if text.endswith(";"):
        return text
    return f"{text};"


def make_changeset_sql(author: str, comment: str, body: str) -> str:
    header = (
        "--liquibase formatted sql\n\n"
        f"--changeset {author}:1 labels:prod,test\n"
        f"--comment: {comment}\n"
        "--rollback SELECT 1\n\n"
    )
    return header + body


def unique_filename(
    day: str, seq: int, safe_name: str, used_filenames: set[str]
) -> str:
    fn = f"changelog-{day}_{seq:02d}_{safe_name}.sql"
    dup = 1
    while fn in used_filenames:
        dup += 1
        fn = f"changelog-{day}_{seq:02d}_{safe_name}__{dup}.sql"
    used_filenames.add(fn)
    return fn


def write_or_print_migration(
    out_dir: str, filename: str, sql: str, dry_run: bool
) -> int:
    out_path = os.path.join(out_dir, filename)
    if dry_run:
        print(out_path)
        return 0
    os.makedirs(out_dir, exist_ok=True)
    if os.path.exists(out_path):
        print(f"Error: file already exists: {out_path}", file=sys.stderr)
        return 2
    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write(sql)
    print(f"Wrote {out_path}")
    return 0


def emit_single_statement_migrations(
    entries: List[Tuple[Optional[str], str]],
    out_dir: str,
    day: str,
    seq: int,
    author: str,
    prefix: str,
    fallback_name: str,
    comment_prefix: str,
    dry_run: bool,
) -> Tuple[int, int, int]:
    used_filenames: set[str] = set()
    written = 0
    for entry_name, raw_stmt in entries:
        name_for_file = entry_name if entry_name else fallback_name
        safe = sanitize_filename(f"{prefix}_{name_for_file}")
        fn = unique_filename(day, seq, safe, used_filenames)
        body = f"{ensure_stmt_terminated(raw_stmt)}\n"
        sql = make_changeset_sql(author, f"{comment_prefix} {name_for_file}", body)
        result = write_or_print_migration(out_dir, fn, sql, dry_run)
        if result != 0:
            return seq, written, result
        seq += 1
        written += 1
    return seq, written, 0


def stmts_for_exercise(name: str, pool: List[str], insert_into_table: str) -> List[str]:
    want: List[str] = []
    needle = insert_into_table.lower()
    values_kw = SQL_VALUES.lower()
    for stmt in pool:
        sl = stmt.lower()
        if needle not in sl:
            continue
        if values_kw not in sl:
            continue
        first_cols = first_column_strings_for_join_insert(stmt, insert_into_table)
        if name in first_cols:
            want.append(stmt)
    return want


def first_column_names(pool: List[str], insert_into_table: str) -> set[str]:
    names: set[str] = set()
    for stmt in pool:
        for name in first_column_strings_for_join_insert(stmt, insert_into_table):
            names.add(name)
    return names


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Read Liquibase data migrations and emit structured changelogs under "
            "data/exercises/, data/equipment/, and data/muscle/."
        )
    )
    parser.add_argument(
        "--data-dir",
        help="Path to resources/migrations/data (default: next to scripts/)",
    )
    parser.add_argument(
        "--date",
        help="YYYY-MM-DD for changelog filenames (default: today)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print planned output paths only (no files written)",
    )
    parser.add_argument(
        "--author",
        default=os.environ.get("CONGEN_MIGRATION_AUTHOR", "alphaeron"),
        help="Liquibase changeset author",
    )
    args = parser.parse_args()
    script_dir = os.path.dirname(os.path.abspath(__file__))
    data_dir = args.data_dir or os.path.join(script_dir, "..", "resources", "migrations", "data")
    data_dir = os.path.normpath(data_dir)
    exercises_dir = os.path.join(data_dir, "exercises")
    equipment_dir = os.path.join(data_dir, "equipment")
    muscle_dir = os.path.join(data_dir, "muscle")
    if args.date:
        day = args.date
    else:
        day = date.today().isoformat()
    if not re.match(r"^[0-9]{4}-[0-9]{2}-[0-9]{2}$", day):
        print("Error: --date must be YYYY-MM-DD", file=sys.stderr)
        return 2
    author = (args.author or "").strip() or "alphaeron"

    if not os.path.isdir(data_dir):
        print(f"Error: data migrations directory not found: {data_dir}", file=sys.stderr)
        return 2

    text = load_source_files(data_dir)
    statements = iter_insert_statements(text)

    equipment_inserts: List[Tuple[Optional[str], str]] = []
    muscle_inserts: List[Tuple[Optional[str], str]] = []
    exercise_insert_by_name: Dict[str, str] = {}

    muscle_stmts: List[str] = []
    equipment_join_stmts: List[str] = []
    workout_type_stmts: List[str] = []

    for stmt in statements:
        lower = stmt.lower()
        if lower.startswith(INSERT_INTO_EQUIPMENT.lower()):
            en = extract_equipment_name_from_equipment_insert(stmt)
            equipment_inserts.append((en, stmt))
            continue
        if lower.startswith(INSERT_INTO_MUSCLE.lower()):
            mn = extract_first_values_string(stmt)
            muscle_inserts.append((mn, stmt))
            continue
        if lower.startswith("insert into test_protocol_config"):
            continue
        if re.match(r"^\s*insert\s+into\s+exercise\s*\(", lower):
            try:
                en = extract_exercise_name_from_exercise_insert(stmt)
            except ValueError:
                continue
            exercise_insert_by_name[en] = stmt
            continue
        if lower.startswith(INSERT_INTO_EXERCISE_MUSCLE.lower()):
            muscle_stmts.append(stmt)
            continue
        if lower.startswith(INSERT_INTO_EXERCISE_EQUIPMENT.lower()):
            equipment_join_stmts.append(stmt)
            continue
        if lower.startswith(INSERT_INTO_EXERCISE_WORKOUT_TYPE.lower()):
            workout_type_stmts.append(stmt)
            continue

    exercise_seq = next_sequence_for_date(exercises_dir, day)
    exercise_used_filenames: set[str] = set()
    equipment_seq = next_sequence_for_date(equipment_dir, day)
    muscle_seq = next_sequence_for_date(muscle_dir, day)
    written = 0

    exercise_names = set(exercise_insert_by_name.keys())
    exercise_names |= first_column_names(muscle_stmts, INSERT_INTO_EXERCISE_MUSCLE)
    exercise_names |= first_column_names(equipment_join_stmts, INSERT_INTO_EXERCISE_EQUIPMENT)
    exercise_names |= first_column_names(workout_type_stmts, INSERT_INTO_EXERCISE_WORKOUT_TYPE)

    for name in sorted(exercise_names):
        ex_stmt = exercise_insert_by_name.get(name)
        m_stmts = stmts_for_exercise(name, muscle_stmts, INSERT_INTO_EXERCISE_MUSCLE)
        ej_stmts = stmts_for_exercise(
            name, equipment_join_stmts, INSERT_INTO_EXERCISE_EQUIPMENT
        )
        wt_stmts = stmts_for_exercise(
            name, workout_type_stmts, INSERT_INTO_EXERCISE_WORKOUT_TYPE
        )
        body_parts: List[str] = []
        if ex_stmt is not None:
            body_parts.append(ensure_stmt_terminated(ex_stmt))
        for ms in m_stmts:
            body_parts.append(ensure_stmt_terminated(ms))
        for es in ej_stmts:
            body_parts.append(ensure_stmt_terminated(es))
        for ws in wt_stmts:
            body_parts.append(ensure_stmt_terminated(ws))
        if len(body_parts) == 0:
            continue

        body = "\n\n".join(body_parts) + "\n"

        safe = sanitize_filename(name)
        fn = unique_filename(day, exercise_seq, safe, exercise_used_filenames)
        out_sql = make_changeset_sql(author, f"Add exercise {name}", body)
        result = write_or_print_migration(exercises_dir, fn, out_sql, args.dry_run)
        if result != 0:
            return result
        exercise_seq += 1
        written += 1

    equipment_seq, equipment_written, equipment_error = emit_single_statement_migrations(
        entries=equipment_inserts,
        out_dir=equipment_dir,
        day=day,
        seq=equipment_seq,
        author=author,
        prefix="equipment",
        fallback_name="equipment",
        comment_prefix="Add equipment",
        dry_run=args.dry_run,
    )
    if equipment_error != 0:
        return equipment_error
    written += equipment_written

    muscle_seq, muscle_written, muscle_error = emit_single_statement_migrations(
        entries=muscle_inserts,
        out_dir=muscle_dir,
        day=day,
        seq=muscle_seq,
        author=author,
        prefix="muscle",
        fallback_name="muscle",
        comment_prefix="Add muscle",
        dry_run=args.dry_run,
    )
    if muscle_error != 0:
        return muscle_error
    written += muscle_written

    if written == 0:
        print("No exercises found in data migrations.", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
