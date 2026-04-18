#!/usr/bin/env bash
#
# Interactive generator for Liquibase data migrations that add a single exercise.
#
# Creates: resources/migrations/data/changelog-YYYY-MM-DD_NN_<sanitized_exercise_name>.sql
# NN increments per calendar day when multiple migrations share the same date.
#
# Usage: add_exercise_migration.sh [--date YYYY-MM-DD] [--dry-run]
# Env:   CONGEN_MIGRATION_AUTHOR (default: John Matty)

set -u

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
DATA_DIR="${SCRIPT_DIR}/../resources/migrations/data"

MOVEMENT_TYPES=(
  horizontal_push
  vertical_push
  horizontal_pull
  vertical_pull
  squat
  hinge
  lunge
  core
  plyometric
  carry
  isolation
)

sql_quote() {
  local s=$1
  s="${s//\'/\'\'}"
  printf "'%s'" "$s"
}

sanitize_filename() {
  local s
  s=$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')
  s=$(printf '%s' "$s" | sed 's/[^a-z0-9]/_/g')
  s=$(printf '%s' "$s" | sed 's/__*/_/g')
  s=$(printf '%s' "$s" | sed 's/^_//;s/_$//')
  if [ -z "$s" ]; then
    s="exercise"
  fi
  printf '%s' "$s"
}

trim() {
  local s=$1
  s=$(printf '%s' "$s" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  printf '%s' "$s"
}

next_sequence_for_date() {
  local data_dir=$1 day=$2
  local max_seq=0 f base seq
  shopt -s nullglob
  for f in "${data_dir}/changelog-${day}"_*.sql; do
    base=$(basename "$f")
    if [[ "$base" =~ ^changelog-${day}_([0-9]+)_ ]]; then
      seq=$((10#${BASH_REMATCH[1]}))
      if [ "$seq" -gt "$max_seq" ]; then
        max_seq=$seq
      fi
    fi
  done
  shopt -u nullglob
  echo $((max_seq + 1))
}

prompt_line() {
  local prompt=$1
  local default=${2-}
  local input
  if [ -n "${default}" ]; then
    read -r -p "${prompt} [${default}]: " input || true
    if [ -z "$input" ]; then
      printf '%s' "$default"
    else
      printf '%s' "$input"
    fi
  else
    read -r -p "${prompt}: " input || true
    printf '%s' "$input"
  fi
}

prompt_bool() {
  local prompt=$1 default=$2
  local hint raw
  if [ "$default" = "true" ] || [ "$default" = "1" ]; then
    hint="y"
  else
    hint="n"
  fi
  while true; do
    raw=$(prompt_line "$prompt" "$hint")
    raw=$(printf '%s' "$raw" | tr '[:upper:]' '[:lower:]')
    case "$raw" in
      y|yes|true|1) printf 'true'; return 0 ;;
      n|no|false|0) printf 'false'; return 0 ;;
    esac
    echo "Please enter y or n." >&2
  done
}

prompt_movement_type() {
  local i raw idx candidate
  echo "" >&2
  echo "Valid movement_type values:" >&2
  i=1
  for mt in "${MOVEMENT_TYPES[@]}"; do
    printf '  %2d. %s\n' "$i" "$mt" >&2
    i=$((i + 1))
  done
  while true; do
    raw=$(prompt_line "movement_type (name or number from list)")
    raw=$(trim "$raw")
    if [[ "$raw" =~ ^[0-9]+$ ]]; then
      idx=$raw
      if [ "$idx" -ge 1 ] && [ "$idx" -le "${#MOVEMENT_TYPES[@]}" ]; then
        printf '%s' "${MOVEMENT_TYPES[$((idx - 1))]}"
        return 0
      fi
    fi
    candidate=$(printf '%s' "$raw" | tr '[:upper:]' '[:lower:]')
    for mt in "${MOVEMENT_TYPES[@]}"; do
      if [ "$candidate" = "$mt" ]; then
        printf '%s' "$mt"
        return 0
      fi
    done
    echo "Invalid movement_type. Use a name from the list or its number." >&2
  done
}

comma_list_to_lines() {
  local line=$1
  local IFS=,
  local -a parts
  local part
  read -ra parts <<< "$line"
  for part in ${parts[@]+"${parts[@]}"}; do
    part=$(trim "$part")
    if [ -n "$part" ]; then
      printf '%s\n' "$part"
    fi
  done
}

usage() {
  echo "Usage: $0 [--date YYYY-MM-DD] [--dry-run]" >&2
}

main() {
  local DAY=""
  local DRY_RUN=0
  while [ $# -gt 0 ]; do
    case "$1" in
      --date)
        DAY=${2-}
        shift 2
        ;;
      --dry-run)
        DRY_RUN=1
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        usage
        exit 2
        ;;
    esac
  done

  if [ -z "$DAY" ]; then
    DAY=$(date +%Y-%m-%d)
  fi
  if ! [[ "$DAY" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
    echo "Error: --date must be YYYY-MM-DD" >&2
    exit 2
  fi

  if [ ! -d "$DATA_DIR" ]; then
    echo "Error: data migrations directory not found: $DATA_DIR" >&2
    exit 2
  fi

  local author
  author=${CONGEN_MIGRATION_AUTHOR:-John Matty}
  author=$(trim "$author")
  if [ -z "$author" ]; then
    author="John Matty"
  fi

  echo "New exercise data migration (Liquibase / PostgreSQL)."
  echo ""

  local exercise_name description movement_type
  local is_unilateral is_upper is_accessory
  exercise_name=$(prompt_line "Exercise name (display name, primary key)")
  exercise_name=$(trim "$exercise_name")
  if [ -z "$exercise_name" ]; then
    echo "Exercise name is required." >&2
    exit 2
  fi

  description=$(prompt_line "Exercise description")
  description=$(trim "$description")
  if [ -z "$description" ]; then
    echo "Description is required." >&2
    exit 2
  fi

  movement_type=$(prompt_movement_type)

  is_unilateral=$(prompt_bool "Unilateral exercise (is_unilateral)?" "false")
  is_upper=$(prompt_bool "Upper-body emphasis (is_upper)?" "true")
  is_accessory=$(prompt_bool "Accessory exercise (is_accessory)?" "true")

  local add_eq
  local NEW_EQ_LINES=()
  add_eq=$(prompt_bool "Add new equipment rows (INSERT ... ON CONFLICT DO NOTHING)?" "false")
  if [ "$add_eq" = "true" ]; then
    echo "Enter equipment name and description; empty name when done." >&2
    while true; do
      local ename edesc
      ename=$(prompt_line "New equipment name (empty to finish)")
      ename=$(trim "$ename")
      if [ -z "$ename" ]; then
        break
      fi
      edesc=$(prompt_line "Description for this equipment")
      edesc=$(trim "$edesc")
      if [ -z "$edesc" ]; then
        echo "Description is required for new equipment." >&2
        continue
      fi
      NEW_EQ_LINES+=("${ename}|${edesc}")
    done
  fi

  local muscles_line equipment_line
  muscles_line=$(prompt_line "Target muscles (comma-separated muscle_name values; must exist in muscle table)")
  equipment_line=$(prompt_line "Equipment (comma-separated names; leave empty for bodyweight — must exist unless added above)")

  local muscles=()
  local m
  while IFS= read -r m; do
    [ -n "$m" ] && muscles+=("$m")
  done < <(comma_list_to_lines "$muscles_line")

  local equipment_names=()
  while IFS= read -r m; do
    [ -n "$m" ] && equipment_names+=("$m")
  done < <(comma_list_to_lines "$equipment_line")

  if [ "${#muscles[@]}" -eq 0 ]; then
    echo "At least one muscle is required." >&2
    exit 2
  fi

  echo "" >&2
  echo "exercise_workout_type links exercises to dynamic_effort / maximal_effort for program generation." >&2
  local add_ewt inc_de inc_me
  add_ewt=$(prompt_bool "Add exercise_workout_type rows for this movement_type?" "true")
  inc_de="false"
  inc_me="false"
  if [ "$add_ewt" = "true" ]; then
    inc_de=$(prompt_bool "Include dynamic_effort?" "true")
    inc_me=$(prompt_bool "Include maximal_effort?" "true")
    if [ "$inc_de" != "true" ] && [ "$inc_me" != "true" ]; then
      echo "No workout types selected; skipping exercise_workout_type inserts." >&2
    fi
  fi

  local en_q desc_q mt_q
  en_q=$(sql_quote "$exercise_name")
  desc_q=$(sql_quote "$description")
  mt_q=$(sql_quote "$movement_type")

  local mu up ac
  mu=$is_unilateral
  up=$is_upper
  ac=$is_accessory

  local sql=""
  sql+="--liquibase formatted sql"$'\n'$'\n'
  sql+="--changeset ${author}:1 labels:prod,test"$'\n'
  sql+="--comment: Add exercise ${exercise_name}"$'\n'
  sql+="--rollback SELECT 1"$'\n'$'\n'

  local pair ename edesc
  for pair in ${NEW_EQ_LINES[@]+"${NEW_EQ_LINES[@]}"}; do
    ename=${pair%%|*}
    edesc=${pair#*|}
    sql+="INSERT INTO equipment (name, description) VALUES ($(sql_quote "$ename"), $(sql_quote "$edesc"))"$'\n'
    sql+="ON CONFLICT (name) DO NOTHING;"$'\n'$'\n'
  done

  sql+="INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)"$'\n'
  sql+="VALUES ("$'\n'
  sql+="  ${en_q},"$'\n'
  sql+="  ${desc_q},"$'\n'
  sql+="  ${mt_q},"$'\n'
  sql+="  ${mu},"$'\n'
  sql+="  ${up},"$'\n'
  sql+="  ${ac}"$'\n'
  sql+=");"$'\n'

  sql+="INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES"$'\n'
  local mi meq
  mi=0
  for m in "${muscles[@]}"; do
    mi=$((mi + 1))
    meq=$(sql_quote "$m")
    if [ "$mi" -lt "${#muscles[@]}" ]; then
      sql+="  (${en_q}, ${meq}),"$'\n'
    else
      sql+="  (${en_q}, ${meq});"$'\n'
    fi
  done

  if [ "${#equipment_names[@]}" -gt 0 ]; then
    sql+="INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES"$'\n'
    mi=0
    for m in "${equipment_names[@]}"; do
      mi=$((mi + 1))
      meq=$(sql_quote "$m")
      if [ "$mi" -lt "${#equipment_names[@]}" ]; then
        sql+="  (${en_q}, ${meq}),"$'\n'
      else
        sql+="  (${en_q}, ${meq});"$'\n'
      fi
    done
  fi

  if [ "$add_ewt" = "true" ] && { [ "$inc_de" = "true" ] || [ "$inc_me" = "true" ]; }; then
    sql+="INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES"$'\n'
    local wts=()
    [ "$inc_de" = "true" ] && wts+=("dynamic_effort")
    [ "$inc_me" = "true" ] && wts+=("maximal_effort")
    mi=0
    for m in "${wts[@]}"; do
      mi=$((mi + 1))
      meq=$(sql_quote "$m")
      if [ "$mi" -lt "${#wts[@]}" ]; then
        sql+="  (${en_q}, ${mt_q}, ${meq}),"$'\n'
      else
        sql+="  (${en_q}, ${mt_q}, ${meq});"$'\n'
      fi
    done
  fi

  sql+=$'\n'

  local seq seq_str safe filename out_path
  seq=$(next_sequence_for_date "$DATA_DIR" "$DAY")
  seq_str=$(printf '%02d' "$seq")
  safe=$(sanitize_filename "$exercise_name")
  filename="changelog-${DAY}_${seq_str}_${safe}.sql"
  out_path="${DATA_DIR}/${filename}"

  if [ "$DRY_RUN" -eq 1 ]; then
    printf '%s' "$sql"
    echo "" >&2
    echo "--- dry-run: would write ${out_path} ---" >&2
    exit 0
  fi

  if [ -e "$out_path" ]; then
    echo "Error: file already exists: $out_path" >&2
    exit 2
  fi

  printf '%s' "$sql" >"$out_path"
  echo "Wrote $out_path"
}

main "$@"
