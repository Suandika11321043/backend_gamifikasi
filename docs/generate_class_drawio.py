#!/usr/bin/env python3
"""Generate draw.io UML class diagram (Entity + Controller + Use) for Gamifikasi PAUD."""

from __future__ import annotations

import html
import re
import xml.etree.ElementTree as ET
from pathlib import Path

OUTPUT_DIR = Path(__file__).parent / "drawio" / "class"

CLASS_W = 200
ROW_H = 26
HEADER_H = 26

SWIMLANE = (
    "swimlane;fontStyle=1;align=center;verticalAlign=top;childLayout=stackLayout;"
    "horizontal=1;startSize=26;horizontalStack=0;resizeParent=1;resizeParentMax=0;"
    "resizeLast=0;collapsible=1;marginBottom=0;whiteSpace=wrap;html=1;"
)
MEMBER = (
    "text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;"
    "spacingLeft=4;spacingRight=4;overflow=hidden;rotatable=0;"
    "points=[[0,0.5],[1,0.5]];portConstraint=eastwest;whiteSpace=wrap;html=1;"
)
ASSOC = "endArrow=none;html=1;rounded=0;fontSize=10;strokeWidth=1;endFill=0;"
COMP = "endArrow=diamondThin;endFill=1;html=1;rounded=0;fontSize=10;strokeWidth=1;"
USE = "endArrow=open;endFill=0;dashed=1;html=1;rounded=0;fontSize=10;strokeWidth=1;"

# ── Entity definitions ───────────────────────────────────────────────
ENTITIES: dict[str, list[str]] = {
    "User": ["- id: Long", "- username: String", "- nama: String", "- role: String", "- password: String"],
    "Student": ["- id: Long", "- name: String", "- group: String", "- avatar: String"],
    "Tema": ["- id: Long", "- nameTopic: String", "- description: String", "- icon: String", "- isActive: Boolean"],
    "Questions": [
        "- id: Long", "- topic: Tema", "- learningDate: LocalDate", "- questionType: String",
        "- contentInstruction: String", "- contentImage: String", "- contentAudio: String",
        "- timeLimitMinutes: Integer", "- scorePoint: Integer", "- isAvailable: Boolean",
    ],
    "QuestionOptions": [
        "- id: Long", "- questions: Questions", "- teksOpsi: String", "- mediaOpsi: String",
        "- kunciJawaban: Boolean", "- urutanBenar: Integer", "- tipeItem: TipeItem",
    ],
    "MatchingRelation": [
        "- id: Long", "- questions: Questions", "- opsiPertanyaan: QuestionOptions",
        "- opsiJawaban: QuestionOptions",
    ],
    "JigsawPuzzle": ["- id: Long", "- question: Questions", "- imageUrl: String", "- gridRows: Integer", "- gridCols: Integer"],
    "JigsawPiece": ["- id: Long", "- puzzle: JigsawPuzzle", "- pieceIndex: Integer", "- correctPosition: Integer", "- pieceImageUrl: String"],
    "StudentAnswer": ["- id: Long", "- student: Student", "- questions: Questions", "- studentAnswer: String", "- isCorrect: Boolean", "- earnedScore: Integer"],
    "StudentScore": ["- id: Long", "- student: Student", "- topic: Tema", "- learningDate: LocalDate", "- correctCount: Integer", "- totalEarnedScore: Integer"],
    "QuestionTimerSession": ["- id: Long", "- studentId: Long", "- topicId: Long", "- questionId: Long", "- remainingSeconds: Integer", "- updatedAt: LocalDateTime"],
}

# ── Controller definitions (gaya referensi: CRUD + operasi khusus) ───
CONTROLLERS: dict[str, list[str]] = {
    "UserController": ["+ login(): ResponseEntity", "+ authenticate(): String", "+ logout(): void", "+ createUser(): ResponseEntity", "+ updateUser(): ResponseEntity", "+ deleteUser(): void"],
    "StudentController": ["+ index(): ResponseEntity", "+ createStudent(): ResponseEntity", "+ getAllStudents(): ResponseEntity", "+ show(id): ResponseEntity", "+ updateStudent(): ResponseEntity", "+ deleteStudent(): void"],
    "TopicController": ["+ index(): ResponseEntity", "+ createTopic(): ResponseEntity", "+ show(id): ResponseEntity", "+ updateTopic(): ResponseEntity", "+ activateTopic(): ResponseEntity", "+ deleteTopic(): void"],
    "QuestionsController": ["+ index(): ResponseEntity", "+ createQuestion(): ResponseEntity", "+ show(id): ResponseEntity", "+ updateQuestion(): ResponseEntity", "+ duplicateQuestion(): ResponseEntity", "+ deleteQuestion(): void"],
    "QuestionOptionsController": ["+ index(): ResponseEntity", "+ createOption(): ResponseEntity", "+ show(id): ResponseEntity", "+ updateOption(): ResponseEntity", "+ deleteOption(): void"],
    "MatchingRelationController": ["+ index(): ResponseEntity", "+ createRelation(): ResponseEntity", "+ show(id): ResponseEntity", "+ updateRelation(): ResponseEntity", "+ deleteRelation(): void"],
    "JigsawPuzzleController": ["+ createPuzzle(): ResponseEntity", "+ getPuzzleById(): ResponseEntity", "+ updatePuzzle(): ResponseEntity", "+ addPiece(): ResponseEntity", "+ deletePuzzle(): void"],
    "QuizController": ["+ getQuestions(): ResponseEntity", "+ submitSingleAnswer(): ResponseEntity", "+ finishQuiz(): ResponseEntity", "+ getStudentAnswer(): ResponseEntity", "+ getScoresByStudent(): ResponseEntity"],
    "QuizTimerController": ["+ getTimer(): ResponseEntity", "+ saveTimer(): ResponseEntity", "+ clearTimer(): void"],
    "DashboardController": ["+ getStats(): ResponseEntity", "+ getStudentsByTotalPoints(): ResponseEntity", "+ getStudentsByTopic(): ResponseEntity"],
}

# Controller -> Entity utama (relasi Use)
CONTROLLER_USES: list[tuple[str, str]] = [
    ("UserController", "User"),
    ("StudentController", "Student"),
    ("TopicController", "Tema"),
    ("QuestionsController", "Questions"),
    ("QuestionOptionsController", "QuestionOptions"),
    ("MatchingRelationController", "MatchingRelation"),
    ("JigsawPuzzleController", "JigsawPuzzle"),
    ("JigsawPuzzleController", "JigsawPiece"),
    ("QuizController", "StudentAnswer"),
    ("QuizController", "StudentScore"),
    ("QuizController", "Questions"),
    ("QuizController", "Student"),
    ("QuizTimerController", "QuestionTimerSession"),
    ("DashboardController", "Student"),
    ("DashboardController", "Tema"),
    ("DashboardController", "Questions"),
    ("DashboardController", "StudentScore"),
]

# Entity associations: (from, to, from_mult, to_mult, kind)
# kind: assoc | comp
ENTITY_RELS = [
    ("Tema", "Questions", "1", "1..*", "assoc"),
    ("Questions", "QuestionOptions", "1", "1..*", "assoc"),
    ("Questions", "MatchingRelation", "1", "1..*", "assoc"),
    ("Questions", "JigsawPuzzle", "1", "0..1", "assoc"),
    ("JigsawPuzzle", "JigsawPiece", "1", "1..*", "comp"),
    ("Student", "StudentAnswer", "1", "1..*", "assoc"),
    ("Questions", "StudentAnswer", "1", "1..*", "assoc"),
    ("Student", "StudentScore", "1", "1..*", "assoc"),
    ("Tema", "StudentScore", "1", "1..*", "assoc"),
    ("QuestionOptions", "MatchingRelation", "0..1", "1..*", "assoc"),
    ("Student", "QuestionTimerSession", "1", "1..*", "assoc"),
    ("Tema", "QuestionTimerSession", "1", "1..*", "assoc"),
    ("Questions", "QuestionTimerSession", "1", "1..*", "assoc"),
]

# Layout: (name, x, y, is_controller)
LAYOUT: list[tuple[str, int, int, bool]] = [
    # Baris 1 - entitas utama
    ("User", 40, 60, False),
    ("Student", 280, 60, False),
    ("Tema", 520, 60, False),
    ("Questions", 760, 60, False),
    ("QuestionOptions", 1020, 60, False),
    ("MatchingRelation", 1260, 60, False),
    ("JigsawPuzzle", 1500, 60, False),
    ("JigsawPiece", 1740, 60, False),
    # Baris 2 - entitas progres
    ("StudentAnswer", 280, 380, False),
    ("StudentScore", 520, 380, False),
    ("QuestionTimerSession", 760, 380, False),
    # Baris 3 - controller (di bawah entitas terkait)
    ("UserController", 40, 620, True),
    ("StudentController", 280, 620, True),
    ("TopicController", 520, 620, True),
    ("QuestionsController", 760, 620, True),
    ("QuestionOptionsController", 1020, 620, True),
    ("MatchingRelationController", 1260, 620, True),
    ("JigsawPuzzleController", 1500, 620, True),
    ("QuizController", 280, 920, True),
    ("QuizTimerController", 760, 920, True),
    ("DashboardController", 1020, 920, True),
]


def esc(text: str) -> str:
    return html.escape(text, quote=True)


def class_height(members: list[str]) -> int:
    return HEADER_H + len(members) * ROW_H + 6


class Builder:
    def __init__(self) -> None:
        self.cells: list[str] = []
        self._id = 2
        self.ids: dict[str, str] = {}

    def nid(self) -> str:
        c = self._id
        self._id += 1
        return str(c)

    def uml_class(
        self,
        key: str,
        name: str,
        stereotype: str,
        members: list[str],
        x: int,
        y: int,
        fill: str,
    ) -> str:
        cid = self.nid()
        self.ids[key] = cid
        h = class_height(members)
        label = f"{esc(name)}&#xa;&lt;&lt;{esc(stereotype)}&gt;&gt;"
        style = f"{SWIMLANE}fillColor={fill};"
        self.cells.append(
            f'        <mxCell id="{cid}" value="{label}" style="{esc(style)}" '
            f'vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{CLASS_W}" height="{h}" as="geometry"/></mxCell>'
        )
        oy = HEADER_H
        for m in members:
            mid = self.nid()
            self.cells.append(
                f'        <mxCell id="{mid}" value="{esc(m)}" style="{esc(MEMBER)}" '
                f'vertex="1" parent="{cid}">'
                f'<mxGeometry y="{oy}" width="{CLASS_W}" height="{ROW_H}" as="geometry"/></mxCell>'
            )
            oy += ROW_H
        return cid

    def title(self, text: str) -> None:
        tid = self.nid()
        self.cells.append(
            f'        <mxCell id="{tid}" value="{esc(text)}" '
            f'style="text;html=1;strokeColor=none;fillColor=none;align=center;'
            f'verticalAlign=middle;fontSize=16;fontStyle=1;" vertex="1" parent="1">'
            f'<mxGeometry x="40" y="10" width="1900" height="30" as="geometry"/></mxCell>'
        )

    def entity_assoc(self, src: str, tgt: str, m1: str, m2: str, kind: str = "assoc") -> None:
        style = COMP if kind == "comp" else ASSOC
        eid = self.nid()
        self.cells.append(
            f'        <mxCell id="{eid}" value="" style="{esc(style)}" edge="1" parent="1" '
            f'source="{self.ids[src]}" target="{self.ids[tgt]}">'
            f'<mxGeometry relative="1" as="geometry"/></mxCell>'
        )
        self._mult_label(eid, m1, "-0.15")
        self._mult_label(eid, m2, "0.85")

    def use_dep(self, controller: str, entity: str) -> None:
        eid = self.nid()
        self.cells.append(
            f'        <mxCell id="{eid}" value="Use" style="{esc(USE)}" edge="1" parent="1" '
            f'source="{self.ids[controller]}" target="{self.ids[entity]}">'
            f'<mxGeometry relative="1" as="geometry"/></mxCell>'
        )

    def _mult_label(self, edge_id: str, text: str, x_pos: str) -> None:
        lid = self.nid()
        self.cells.append(
            f'        <mxCell id="{lid}" value="{esc(text)}" '
            f'style="edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;'
            f'points=[];fontSize=10;labelBackgroundColor=#ffffff;" '
            f'vertex="1" connectable="0" parent="{edge_id}">'
            f'<mxGeometry x="{x_pos}" relative="1" as="geometry">'
            f'<mxPoint y="-8" as="offset"/></mxGeometry></mxCell>'
        )

    def wrap(self, diagram_id: str, name: str, page_w: int, page_h: int) -> str:
        body = "\n".join(self.cells)
        return (
            '<?xml version="1.0" encoding="UTF-8"?>\n'
            '<mxfile host="app.diagrams.net" modified="2026-07-04T00:00:00.000Z" '
            'agent="generate_class_drawio.py" version="24.7.0" type="device">\n'
            f'  <diagram id="{diagram_id}" name="{esc(name)}">\n'
            f'    <mxGraphModel dx="1800" dy="1000" grid="1" gridSize="10" guides="1" '
            f'tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" '
            f'pageWidth="{page_w}" pageHeight="{page_h}" math="0" shadow="0">\n'
            f"      <root>\n        <mxCell id=\"0\"/>\n        <mxCell id=\"1\" parent=\"0\"/>\n"
            f"{body}\n      </root>\n    </mxGraphModel>\n  </diagram>\n</mxfile>"
        )


def build_mvc_diagram() -> str:
    b = Builder()
    b.title("Class Diagram Sistem Gamifikasi Pembelajaran PAUD")

    for key, x, y, is_ctrl in LAYOUT:
        if is_ctrl:
            b.uml_class(key, key, "Controller", CONTROLLERS[key], x, y, "#fff2cc")
        else:
            b.uml_class(key, key, "Entity", ENTITIES[key], x, y, "#ffffff")

    for rel in ENTITY_RELS:
        b.entity_assoc(*rel)

    for ctrl, ent in CONTROLLER_USES:
        if ctrl in b.ids and ent in b.ids:
            b.use_dep(ctrl, ent)

    return b.wrap("CD_Gamifikasi_PAUD_MVC", "Class Diagram Gamifikasi PAUD", 2000, 1200)


def validate(path: Path) -> None:
    ET.parse(path)


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    mvc_xml = build_mvc_diagram()
    mvc_path = OUTPUT_DIR / "CD_Gamifikasi_PAUD_MVC.drawio"
    mvc_path.write_text(mvc_xml, encoding="utf-8")
    validate(mvc_path)
    print(f"Saved: {mvc_path}")

    # Salin juga sebagai file utama
    main_path = OUTPUT_DIR / "CD_Gamifikasi_PAUD.drawio"
    main_path.write_text(mvc_xml, encoding="utf-8")
    validate(main_path)
    print(f"Saved: {main_path}")

    print(f"Entities: {len(ENTITIES)}, Controllers: {len(CONTROLLERS)}")


if __name__ == "__main__":
    main()
