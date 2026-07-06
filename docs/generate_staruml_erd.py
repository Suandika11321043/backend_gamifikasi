#!/usr/bin/env python3
"""Generate StarUML ERD (.mdj) for Gamifikasi PAUD database."""

from __future__ import annotations

import base64
import json
from pathlib import Path

OUTPUT = Path(__file__).parent / "gamifikasi_erd.mdj"

ENTITY_WIDTH = 260
H_GAP = 90
V_GAP = 110
START_X = 50
START_Y = 50

_counter = 0


def uid() -> str:
    global _counter
    _counter += 1
    raw = f"GP{_counter:05d}".encode()
    b = base64.b64encode(raw).decode().rstrip("=")
    return f"AAAAAA{b[:16]}="


# Entity definitions: name, columns[(name, type, length, pk?, fk_ref_entity?, fk_ref_col?)]
ENTITIES = {
    "users": {
        "name": "users",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("username", "VARCHAR", 255),
            ("nama", "VARCHAR", 255),
            ("role", "VARCHAR", 50),
            ("password", "VARCHAR", 255),
        ],
    },
    "Student": {
        "name": "Student",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("NAME", "VARCHAR", 255),
            ("student_group", "VARCHAR", 100),
            ("AVATAR", "VARCHAR", 255),
        ],
    },
    "Tema": {
        "name": "Tema",
        "columns": [
            ("ID", "BIGINT", 0, True),
            ("NAME_TOPIC", "VARCHAR", 255),
            ("DESCRIPTION", "VARCHAR", 500),
            ("ICON", "VARCHAR", 255),
            ("IS_ACTIVE", "BOOLEAN", 0),
        ],
    },
    "Questions": {
        "name": "Questions",
        "columns": [
            ("ID", "BIGINT", 0, True),
            ("topic_id", "BIGINT", 0, False, "Tema", "ID"),
            ("learning_date", "DATE", 0),
            ("question_type", "VARCHAR", 50),
            ("content_instruction", "TEXT", 0),
            ("content_image", "VARCHAR", 255),
            ("content_audio", "VARCHAR", 255),
            ("time_limit_minutes", "INT", 0),
            ("score_point", "INT", 0),
            ("is_available", "BOOLEAN", 0),
        ],
    },
    "opsi_soal": {
        "name": "opsi_soal",
        "columns": [
            ("id_opsi", "BIGINT", 0, True),
            ("id_soal", "BIGINT", 0, False, "Questions", "ID"),
            ("teks_opsi", "TEXT", 0),
            ("media_opsi", "VARCHAR", 255),
            ("kunci_jawaban", "BOOLEAN", 0),
            ("urutan_benar", "INT", 0),
            ("tipe_item", "VARCHAR", 20),
        ],
    },
    "relasi_matching": {
        "name": "relasi_matching",
        "columns": [
            ("id_relasi", "BIGINT", 0, True),
            ("id_soal", "BIGINT", 0, False, "Questions", "ID"),
            ("id_opsi_pertanyaan", "BIGINT", 0, False, "opsi_soal", "id_opsi"),
            ("id_opsi_jawaban", "BIGINT", 0, False, "opsi_soal", "id_opsi"),
        ],
    },
    "jigsaw_puzzle": {
        "name": "jigsaw_puzzle",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("question_id", "BIGINT", 0, False, "Questions", "ID"),
            ("image_url", "VARCHAR", 500),
            ("grid_rows", "INT", 0),
            ("grid_cols", "INT", 0),
        ],
    },
    "jigsaw_piece": {
        "name": "jigsaw_piece",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("puzzle_id", "BIGINT", 0, False, "jigsaw_puzzle", "id"),
            ("piece_index", "INT", 0),
            ("correct_position", "INT", 0),
            ("piece_image_url", "VARCHAR", 255),
        ],
    },
    "student_answer": {
        "name": "student_answer",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("id_student", "BIGINT", 0, False, "Student", "id"),
            ("id_question", "BIGINT", 0, False, "Questions", "ID"),
            ("student_answer", "TEXT", 0),
            ("is_correct", "BOOLEAN", 0),
            ("earned_score", "INT", 0),
        ],
    },
    "student_day_score": {
        "name": "student_day_score",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("id_student", "BIGINT", 0, False, "Student", "id"),
            ("id_topic", "BIGINT", 0, False, "Tema", "ID"),
            ("learning_date", "DATE", 0),
            ("correct_count", "INT", 0),
            ("total_earned_score", "INT", 0),
        ],
    },
    "question_timer_session": {
        "name": "question_timer_session",
        "columns": [
            ("id", "BIGINT", 0, True),
            ("student_id", "BIGINT", 0, False, "Student", "id"),
            ("topic_id", "BIGINT", 0, False, "Tema", "ID"),
            ("question_id", "BIGINT", 0, False, "Questions", "ID"),
            ("remaining_seconds", "INT", 0),
            ("updated_at", "DATETIME", 0),
        ],
    },
}

# Relationships: (owner_key, end1_key, end2_key, cardinality_end2, name)
RELATIONSHIPS = [
    ("Tema", "Tema", "Questions", "0..*", "memiliki"),
    ("Questions", "Questions", "opsi_soal", "0..*", "memiliki opsi"),
    ("Questions", "Questions", "relasi_matching", "0..*", "memiliki relasi"),
    ("Questions", "Questions", "jigsaw_puzzle", "0..1", "memiliki puzzle"),
    ("jigsaw_puzzle", "jigsaw_puzzle", "jigsaw_piece", "0..*", "memiliki keping"),
    ("Student", "Student", "student_answer", "0..*", "menjawab"),
    ("Questions", "Questions", "student_answer", "0..*", "dijawab"),
    ("Student", "Student", "student_day_score", "0..*", "mendapat skor"),
    ("Tema", "Tema", "student_day_score", "0..*", "dinilai"),
    ("Student", "Student", "question_timer_session", "0..*", "memiliki timer"),
    ("Tema", "Tema", "question_timer_session", "0..*", "timer tema"),
    ("Questions", "Questions", "question_timer_session", "0..*", "timer soal"),
    ("relasi_matching", "relasi_matching", "opsi_soal", "0..1", "opsi pertanyaan"),
    ("relasi_matching", "relasi_matching", "opsi_soal", "0..1", "opsi jawaban"),
]


def entity_height(spec: dict) -> int:
    return 20 + len(spec["columns"]) * 15


def fk_parents(key: str) -> set[str]:
    parents: set[str] = set()
    for col in ENTITIES[key]["columns"]:
        if len(col) > 4 and col[4]:
            parents.add(col[4])
    return parents


def compute_layers() -> dict[str, int]:
    layers: dict[str, int] = {}

    def layer_of(key: str) -> int:
        if key in layers:
            return layers[key]
        parents = fk_parents(key)
        if not parents:
            layers[key] = 0
        else:
            layers[key] = max(layer_of(p) for p in parents) + 1
        return layers[key]

    for key in ENTITIES:
        layer_of(key)
    return layers


def auto_layout() -> None:
    """Hierarchical layout: parent tables atas, child tables bawah."""
    layers = compute_layers()
    max_layer = max(layers.values())

    by_layer: dict[int, list[str]] = {}
    for key, layer in layers.items():
        by_layer.setdefault(layer, []).append(key)

    positions: dict[str, tuple[int, int]] = {}
    row_y = START_Y

    for layer_num in range(max_layer + 1):
        row = by_layer.get(layer_num, [])
        if not row:
            continue

        if layer_num > 0:
            for _ in range(3):
                row.sort(
                    key=lambda k: (
                        sum(positions[p][0] for p in fk_parents(k) if p in positions)
                        / max(len([p for p in fk_parents(k) if p in positions]), 1)
                    )
                )

        row_w = len(row) * (ENTITY_WIDTH + H_GAP) - H_GAP
        max_prev_w = max(
            (
                len(by_layer.get(i, [])) * (ENTITY_WIDTH + H_GAP) - H_GAP
                for i in range(layer_num)
            ),
            default=row_w,
        )
        start_x = START_X + max(0, (max_prev_w - row_w) // 2)

        if layer_num > 0:
            prev_keys = [k for i in range(layer_num) for k in by_layer.get(i, [])]
            row_y = max(
                positions[k][1] + entity_height(ENTITIES[k]) + V_GAP
                for k in prev_keys
            )

        for i, key in enumerate(row):
            x = start_x + i * (ENTITY_WIDTH + H_GAP)
            positions[key] = (x, row_y)
            ENTITIES[key]["x"] = x
            ENTITIES[key]["y"] = row_y


def connection_points(e1: str, e2: str) -> tuple[int, int, int, int]:
    s1, s2 = ENTITIES[e1], ENTITIES[e2]
    h1 = entity_height(s1)
    h2 = entity_height(s2)
    cx1 = s1["x"] + ENTITY_WIDTH / 2
    cy1 = s1["y"] + h1 / 2
    cx2 = s2["x"] + ENTITY_WIDTH / 2
    cy2 = s2["y"] + h2 / 2
    dx, dy = cx2 - cx1, cy2 - cy1

    if abs(dy) >= abs(dx):
        if dy >= 0:
            return int(cx1), int(s1["y"] + h1), int(cx2), int(s2["y"])
        return int(cx1), int(s1["y"]), int(cx2), int(s2["y"] + h2)
    if dx >= 0:
        return int(s1["x"] + ENTITY_WIDTH), int(cy1), int(s2["x"]), int(cy2)
    return int(s1["x"]), int(cy1), int(s2["x"] + ENTITY_WIDTH), int(cy2)


class Builder:
    def __init__(self) -> None:
        self.project_id = uid()
        self.model_id = uid()
        self.diagram_id = uid()
        self.entity_ids: dict[str, str] = {}
        self.entity_view_ids: dict[str, str] = {}
        self.column_ids: dict[tuple[str, str], str] = {}  # (entity, col_name) -> id
        self.rel_ids: dict[str, str] = {}
        self.rel_view_ids: dict[str, str] = {}

    def ref(self, id_: str) -> dict:
        return {"$ref": id_}

    def make_column(self, entity_key: str, entity_id: str, col_def) -> dict:
        name = col_def[0]
        col_type = col_def[1]
        length = col_def[2]
        is_pk = len(col_def) > 3 and col_def[3]
        fk_entity = col_def[4] if len(col_def) > 4 else None
        fk_col = col_def[5] if len(col_def) > 5 else None

        col_id = self.column_ids[(entity_key, name)]

        col: dict = {
            "_type": "ERDColumn",
            "_id": col_id,
            "_parent": self.ref(entity_id),
            "name": name,
            "type": col_type,
            "length": str(length) if length and col_type == "VARCHAR" else (length if length else 0),
        }
        if is_pk:
            col["primaryKey"] = True
        if fk_entity and fk_col:
            ref_col_id = self.column_ids.get((fk_entity, fk_col))
            if ref_col_id:
                col["foreignKey"] = True
                col["referenceTo"] = self.ref(ref_col_id)
        return col

    def make_entity(self, key: str, spec: dict) -> dict:
        eid = self.entity_ids[key]
        cols = []
        for c in spec["columns"]:
            cols.append(self.make_column(key, eid, c))

        owned = []
        for owner, e1, e2, card, name in RELATIONSHIPS:
            if owner != key:
                continue
            rel_key = f"{e1}->{e2}:{name}"
            rid = uid()
            e1id = uid()
            e2id = uid()
            self.rel_ids[rel_key] = rid
            rel = {
                "_type": "ERDRelationship",
                "_id": rid,
                "_parent": self.ref(eid),
                "end1": {
                    "_type": "ERDRelationshipEnd",
                    "_id": e1id,
                    "_parent": self.ref(rid),
                    "reference": self.ref(self.entity_ids[e1]),
                },
                "end2": {
                    "_type": "ERDRelationshipEnd",
                    "_id": e2id,
                    "_parent": self.ref(rid),
                    "reference": self.ref(self.entity_ids[e2]),
                    "cardinality": card,
                },
            }
            if name:
                rel["name"] = name
            owned.append(rel)

        return {
            "_type": "ERDEntity",
            "_id": eid,
            "_parent": self.ref(self.model_id),
            "name": spec["name"],
            **({"ownedElements": owned} if owned else {}),
            "columns": cols,
        }

    def entity_view(self, key: str, spec: dict) -> dict:
        eid = self.entity_ids[key]
        vid = uid()
        self.entity_view_ids[key] = vid
        label_id = uid()
        comp_id = uid()

        ncols = len(spec["columns"])
        width = ENTITY_WIDTH
        height = entity_height(spec)
        left = spec["x"]
        top = spec["y"]

        col_views = []
        for i, c in enumerate(spec["columns"]):
            col_name = c[0]
            cv_id = uid()
            col_views.append({
                "_type": "ERDColumnView",
                "_id": cv_id,
                "_parent": self.ref(comp_id),
                "model": self.ref(self.column_ids[(key, col_name)]),
                "font": "Arial;13;0",
                "left": left + 5,
                "top": top + 28 + i * 15,
                "width": width - 10,
                "height": 13,
            })

        return {
            "_type": "ERDEntityView",
            "_id": vid,
            "_parent": self.ref(self.diagram_id),
            "model": self.ref(eid),
            "subViews": [
                {
                    "_type": "LabelView",
                    "_id": label_id,
                    "_parent": self.ref(vid),
                    "font": "Arial;13;1",
                    "left": left,
                    "top": top + 5,
                    "width": width,
                    "height": 13,
                    "text": spec["name"],
                },
                {
                    "_type": "ERDColumnCompartmentView",
                    "_id": comp_id,
                    "_parent": self.ref(vid),
                    "model": self.ref(eid),
                    "subViews": col_views,
                    "font": "Arial;13;0",
                    "left": left,
                    "top": top + 23,
                    "width": width,
                    "height": ncols * 15 + 10,
                },
            ],
            "font": "Arial;13;0",
            "left": left,
            "top": top,
            "width": width,
            "height": height,
            "nameLabel": self.ref(label_id),
            "columnCompartment": self.ref(comp_id),
        }

    def rel_view(self, owner: str, e1: str, e2: str, name: str) -> dict:
        rel_key = f"{e1}->{e2}:{name}"
        rid = self.rel_ids[rel_key]
        vid = uid()
        self.rel_view_ids[rel_key] = vid

        v1 = self.entity_view_ids[e1]
        v2 = self.entity_view_ids[e2]
        x1, y1, x2, y2 = connection_points(e1, e2)

        nl = uid()
        tl = uid()
        hl = uid()

        return {
            "_type": "ERDRelationshipView",
            "_id": vid,
            "_parent": self.ref(self.diagram_id),
            "model": self.ref(rid),
            "subViews": [
                {
                    "_type": "EdgeLabelView", "_id": nl,
                    "_parent": self.ref(vid),
                    "font": "Arial;13;0",
                    "left": (x1 + x2) // 2, "top": (y1 + y2) // 2 - 10,
                    "height": 13, "visible": bool(name),
                    "text": name if name else "",
                    "hostEdge": self.ref(vid), "edgePosition": 1,
                },
                {
                    "_type": "EdgeLabelView", "_id": tl,
                    "_parent": self.ref(vid),
                    "font": "Arial;13;0",
                    "left": x1, "top": y1 - 15, "height": 13,
                    "hostEdge": self.ref(vid), "edgePosition": 2,
                },
                {
                    "_type": "EdgeLabelView", "_id": hl,
                    "_parent": self.ref(vid),
                    "font": "Arial;13;0",
                    "left": x2, "top": y2 - 15, "height": 13,
                    "hostEdge": self.ref(vid),
                },
            ],
            "font": "Arial;13;0",
            "head": self.ref(v2),
            "tail": self.ref(v1),
            "lineStyle": 2,
            "points": f"{x1}:{y1};{x2}:{y2}",
            "nameLabel": self.ref(nl),
            "tailNameLabel": self.ref(tl),
            "headNameLabel": self.ref(hl),
        }

    def build(self) -> dict:
        # Pass 1: assign entity ids by creating placeholder - need two passes for FK refs
        for key in ENTITIES:
            eid = uid()
            self.entity_ids[key] = eid
            for c in ENTITIES[key]["columns"]:
                col_id = uid()
                self.column_ids[(key, c[0])] = col_id

        entities = [self.make_entity(k, ENTITIES[k]) for k in ENTITIES]

        entity_views = [self.entity_view(k, ENTITIES[k]) for k in ENTITIES]

        rel_views = []
        for owner, e1, e2, card, name in RELATIONSHIPS:
            rel_views.append(self.rel_view(owner, e1, e2, name))

        diagram = {
            "_type": "ERDDiagram",
            "_id": self.diagram_id,
            "_parent": self.ref(self.model_id),
            "name": "ERD Gamifikasi PAUD",
            "defaultDiagram": True,
            "ownedViews": entity_views + rel_views,
        }

        model = {
            "_type": "ERDDataModel",
            "_id": self.model_id,
            "_parent": self.ref(self.project_id),
            "name": "Gamifikasi PAUD",
            "ownedElements": [diagram] + entities,
        }

        return {
            "_type": "Project",
            "_id": self.project_id,
            "name": "Gamifikasi PAUD ERD",
            "ownedElements": [model],
        }


def main() -> None:
    auto_layout()
    b = Builder()
    project = b.build()
    OUTPUT.write_text(json.dumps(project, indent="\t", ensure_ascii=False), encoding="utf-8")
    layers = compute_layers()
    print(f"Saved: {OUTPUT}")
    print(f"Entities: {len(ENTITIES)}, Relationships: {len(RELATIONSHIPS)}")
    print("Layout layers:")
    for layer in sorted(set(layers.values())):
        names = [k for k, lv in sorted(layers.items(), key=lambda x: ENTITIES[x[0]]["x"]) if lv == layer]
        print(f"  L{layer}: {', '.join(names)}")


if __name__ == "__main__":
    main()
