#!/usr/bin/env python3
"""Generate draw.io sequence diagram files (gaya akademik: View-Controller-Entity)."""

from __future__ import annotations

import html
from pathlib import Path

OUTPUT_DIR = Path(__file__).parent / "drawio" / "sequence"

# Participant kinds: actor | boundary (View) | control (Controller) | entity (Data)
# Message entries:
#   (frm, to, label, is_return)
#   ("FRAME", "alt"|"loop", "[kondisi]")  -> open frame
#   ("DIV", "[kondisi]")                  -> divider inside frame
#   ("END",)                              -> close frame
DIAGRAMS: list[dict] = [
    {
        "id": "SD_01_Login_Admin",
        "title": "Sequence Diagram Login Admin",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Login View", "boundary"),
            ("ctrl", "Auth Controller", "control"),
            ("user", "User", "entity"),
        ],
        "messages": [
            ("admin", "view", "Buka halaman login", False),
            ("view", "ctrl", "login (request)", False),
            ("ctrl", "view", "login view", True),
            ("admin", "view", "input username dan password", False),
            ("view", "ctrl", "authenticate", False),
            ("ctrl", "user", "get user", False),
            ("user", "ctrl", "data user", True),
            ("FRAME", "alt", "[Login berhasil]"),
            ("ctrl", "view", "respon sukses", True),
            ("view", "admin", "halaman dashboard", True),
            ("DIV", "[Login gagal]"),
            ("ctrl", "view", "response error", True),
            ("view", "admin", "pesan gagal", True),
            ("END",),
        ],
    },
    {
        "id": "SD_02_Logout_Admin",
        "title": "Sequence Diagram Logout Admin",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Dashboard View", "boundary"),
            ("ctrl", "Auth Controller", "control"),
        ],
        "messages": [
            ("admin", "view", "klik tombol keluar", False),
            ("view", "ctrl", "logout", False),
            ("ctrl", "ctrl", "hapus sesi login", False),
            ("ctrl", "view", "sesi berakhir", True),
            ("view", "admin", "halaman login", True),
        ],
    },
    {
        "id": "SD_03_Melihat_Dashboard",
        "title": "Sequence Diagram Melihat Dashboard",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Dashboard View", "boundary"),
            ("ctrl", "Dashboard Controller", "control"),
            ("db", "Data Gamifikasi", "entity"),
        ],
        "messages": [
            ("admin", "view", "buka halaman dashboard", False),
            ("view", "ctrl", "get data dashboard", False),
            ("ctrl", "db", "ambil total siswa, soal, dan tema", False),
            ("db", "ctrl", "data statistik", True),
            ("ctrl", "db", "ambil peringkat siswa", False),
            ("db", "ctrl", "data peringkat", True),
            ("ctrl", "view", "data dashboard", True),
            ("view", "admin", "tampilkan statistik dan peringkat", True),
        ],
    },
    {
        "id": "SD_04_Mengelola_Daftar_Siswa",
        "title": "Sequence Diagram Mengelola Daftar Siswa",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Siswa View", "boundary"),
            ("ctrl", "Student Controller", "control"),
            ("stu", "Student", "entity"),
        ],
        "messages": [
            ("admin", "view", "buka halaman daftar siswa", False),
            ("view", "ctrl", "get daftar siswa", False),
            ("ctrl", "stu", "ambil data siswa", False),
            ("stu", "ctrl", "data siswa", True),
            ("ctrl", "view", "daftar siswa", True),
            ("view", "admin", "tampilkan daftar siswa", True),
            ("FRAME", "alt", "[Tambah siswa]"),
            ("admin", "view", "input data siswa baru", False),
            ("view", "ctrl", "simpan siswa", False),
            ("ctrl", "stu", "tambah data siswa", False),
            ("stu", "ctrl", "data tersimpan", True),
            ("view", "admin", "siswa berhasil ditambah", True),
            ("DIV", "[Ubah siswa]"),
            ("admin", "view", "ubah data siswa", False),
            ("view", "ctrl", "perbarui siswa", False),
            ("ctrl", "stu", "ubah data siswa", False),
            ("stu", "ctrl", "data diperbarui", True),
            ("view", "admin", "siswa berhasil diubah", True),
            ("DIV", "[Hapus siswa]"),
            ("admin", "view", "pilih hapus dan konfirmasi", False),
            ("view", "ctrl", "hapus siswa", False),
            ("ctrl", "stu", "hapus data siswa", False),
            ("stu", "ctrl", "data terhapus", True),
            ("view", "admin", "siswa berhasil dihapus", True),
            ("END",),
        ],
    },
    {
        "id": "SD_05_Mengelola_Daftar_Tema",
        "title": "Sequence Diagram Mengelola Daftar Tema",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Tema View", "boundary"),
            ("ctrl", "Topic Controller", "control"),
            ("tema", "Tema", "entity"),
        ],
        "messages": [
            ("admin", "view", "buka halaman manajemen tema", False),
            ("view", "ctrl", "get daftar tema", False),
            ("ctrl", "tema", "ambil data tema", False),
            ("tema", "ctrl", "data tema", True),
            ("ctrl", "view", "daftar tema", True),
            ("view", "admin", "tampilkan daftar tema", True),
            ("FRAME", "alt", "[Tambah tema]"),
            ("admin", "view", "input data tema baru", False),
            ("view", "ctrl", "simpan tema", False),
            ("ctrl", "tema", "tambah data tema", False),
            ("tema", "ctrl", "data tersimpan", True),
            ("view", "admin", "tema berhasil ditambah", True),
            ("DIV", "[Ubah tema]"),
            ("admin", "view", "ubah data tema", False),
            ("view", "ctrl", "perbarui tema", False),
            ("ctrl", "tema", "ubah data tema", False),
            ("tema", "ctrl", "data diperbarui", True),
            ("view", "admin", "tema berhasil diubah", True),
            ("DIV", "[Aktivasi / nonaktivasi tema]"),
            ("admin", "view", "ubah status aktif tema", False),
            ("view", "ctrl", "ubah status tema", False),
            ("ctrl", "tema", "perbarui status aktif", False),
            ("tema", "ctrl", "status diperbarui", True),
            ("view", "admin", "status tema berubah", True),
            ("DIV", "[Hapus tema]"),
            ("admin", "view", "pilih hapus dan konfirmasi", False),
            ("view", "ctrl", "hapus tema", False),
            ("ctrl", "tema", "hapus data tema", False),
            ("tema", "ctrl", "data terhapus", True),
            ("view", "admin", "tema berhasil dihapus", True),
            ("END",),
        ],
    },
    {
        "id": "SD_06_Mengelola_Soal_Harian",
        "title": "Sequence Diagram Mengelola Daftar Soal Harian",
        "participants": [
            ("admin", "Admin", "actor"),
            ("view", "Soal View", "boundary"),
            ("ctrl", "Question Controller", "control"),
            ("soal", "Questions", "entity"),
        ],
        "messages": [
            ("admin", "view", "pilih tema dan tanggal belajar", False),
            ("view", "ctrl", "get daftar soal", False),
            ("ctrl", "soal", "ambil soal per tanggal", False),
            ("soal", "ctrl", "data soal", True),
            ("ctrl", "view", "daftar soal harian", True),
            ("view", "admin", "tampilkan daftar soal", True),
            ("FRAME", "alt", "[Tambah soal]"),
            ("admin", "view", "input soal baru dan opsinya", False),
            ("view", "ctrl", "simpan soal", False),
            ("ctrl", "soal", "tambah soal dan opsi", False),
            ("soal", "ctrl", "data tersimpan", True),
            ("view", "admin", "soal berhasil ditambah", True),
            ("DIV", "[Aktifkan soal harian]"),
            ("admin", "view", "aktifkan soal pada tanggal", False),
            ("view", "ctrl", "ubah ketersediaan soal", False),
            ("ctrl", "soal", "perbarui status tersedia", False),
            ("soal", "ctrl", "status diperbarui", True),
            ("view", "admin", "soal siap dikerjakan siswa", True),
            ("DIV", "[Ubah / duplikat soal]"),
            ("admin", "view", "ubah atau duplikat soal", False),
            ("view", "ctrl", "perbarui / salin soal", False),
            ("ctrl", "soal", "simpan perubahan", False),
            ("soal", "ctrl", "data diperbarui", True),
            ("view", "admin", "perubahan tersimpan", True),
            ("DIV", "[Hapus soal]"),
            ("admin", "view", "pilih hapus dan konfirmasi", False),
            ("view", "ctrl", "hapus soal", False),
            ("ctrl", "soal", "hapus soal dan opsi", False),
            ("soal", "ctrl", "data terhapus", True),
            ("view", "admin", "soal berhasil dihapus", True),
            ("END",),
        ],
    },
    {
        "id": "SD_07_Memulai_Soal_Harian",
        "title": "Sequence Diagram Memulai Soal Harian",
        "participants": [
            ("siswa", "Siswa", "actor"),
            ("view", "Belajar View", "boundary"),
            ("ctrl", "Quiz Controller", "control"),
            ("jawab", "Student Answer", "entity"),
        ],
        "messages": [
            ("siswa", "view", "pilih profil, tema, dan tanggal", False),
            ("view", "ctrl", "get soal harian", False),
            ("ctrl", "jawab", "ambil soal yang tersedia", False),
            ("jawab", "ctrl", "daftar soal", True),
            ("ctrl", "view", "soal siap dikerjakan", True),
            ("view", "siswa", "tampilkan soal pertama", True),
            ("FRAME", "loop", "[Setiap soal]"),
            ("siswa", "view", "kerjakan dan kirim jawaban", False),
            ("view", "ctrl", "periksa jawaban", False),
            ("ctrl", "jawab", "simpan jawaban dan nilai", False),
            ("jawab", "ctrl", "hasil penilaian", True),
            ("ctrl", "view", "benar / salah dan poin", True),
            ("view", "siswa", "tampilkan umpan balik", True),
            ("END",),
            ("siswa", "view", "selesai soal terakhir", False),
            ("view", "ctrl", "akhiri sesi belajar", False),
            ("ctrl", "jawab", "hitung skor dan bintang", False),
            ("jawab", "ctrl", "total skor dan bintang", True),
            ("ctrl", "view", "ringkasan hasil", True),
            ("view", "siswa", "tampilkan hasil belajar", True),
        ],
    },
    {
        "id": "SD_08_Review_Hasil_Belajar",
        "title": "Sequence Diagram Review Hasil Belajar",
        "participants": [
            ("siswa", "Siswa", "actor"),
            ("view", "Review View", "boundary"),
            ("ctrl", "Quiz Controller", "control"),
            ("jawab", "Student Answer", "entity"),
        ],
        "messages": [
            ("siswa", "view", "buka halaman review", False),
            ("view", "ctrl", "get riwayat jawaban", False),
            ("ctrl", "jawab", "ambil jawaban dan kunci", False),
            ("jawab", "ctrl", "data jawaban siswa", True),
            ("ctrl", "view", "detail jawaban per soal", True),
            ("view", "siswa", "tampilkan soal dan status benar/salah", True),
            ("FRAME", "loop", "[Navigasi antar soal]"),
            ("siswa", "view", "klik berikutnya / sebelumnya", False),
            ("view", "siswa", "tampilkan jawaban dan poin", True),
            ("END",),
            ("view", "siswa", "tampilkan ringkasan capaian belajar", True),
        ],
    },
]

MSG_STEP = 44
FRAME_OPEN_H = 40
DIV_H = 36
FRAME_END_H = 16
SPACING = 200
MARGIN = 100
LIFELINE_TOP = 110
MSG_START_Y = 170


def esc(text: str) -> str:
    return html.escape(text, quote=True)


def px(index: int) -> int:
    return MARGIN + index * SPACING


def diagram_height(messages: list) -> int:
    y = MSG_START_Y
    for m in messages:
        kind = m[0]
        if kind == "FRAME":
            y += FRAME_OPEN_H
        elif kind == "DIV":
            y += DIV_H
        elif kind == "END":
            y += FRAME_END_H
        else:
            y += MSG_STEP
    return y + 60


def build_drawio(diagram: dict) -> str:
    participants = diagram["participants"]
    messages = diagram["messages"]
    title = diagram["title"]

    pid_to_idx = {p[0]: i for i, p in enumerate(participants)}
    count = len(participants)
    total_h = diagram_height(messages)
    lifeline_bottom = total_h - 30

    frame_left = 40
    frame_right = px(count - 1) + 120

    cells: list[str] = []
    cid = 2

    def next_id() -> int:
        nonlocal cid
        c = cid
        cid += 1
        return c

    def vertex(value: str, style: str, x, y, w, h) -> None:
        cells.append(
            f'        <mxCell id="{next_id()}" value="{esc(value)}" style="{esc(style)}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
        )

    def edge(x1, y1, x2, y2, label="", dashed=False, arrow=True, waypoints=None) -> None:
        style = "html=1;verticalAlign=bottom;rounded=0;fontSize=11;"
        style += "endArrow=block;" if arrow else "endArrow=none;"
        if dashed:
            style += "dashed=1;"
        pts = ""
        if waypoints:
            pts = "<Array as=\"points\">" + "".join(
                f'<mxPoint x="{wx}" y="{wy}"/>' for wx, wy in waypoints
            ) + "</Array>"
        cells.append(
            f'        <mxCell id="{next_id()}" value="{esc(label)}" style="{style}" edge="1" parent="1">'
            f'<mxGeometry relative="1" as="geometry">'
            f'<mxPoint x="{x1}" y="{y1}" as="sourcePoint"/>'
            f'<mxPoint x="{x2}" y="{y2}" as="targetPoint"/>{pts}'
            f"</mxGeometry></mxCell>"
        )

    # Title
    vertex(
        title,
        "text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;fontSize=16;fontStyle=1",
        frame_left,
        10,
        frame_right - frame_left,
        30,
    )

    # Participants + lifelines
    for i, (pid, label, kind) in enumerate(participants):
        cx = px(i)
        if kind == "actor":
            vertex(
                label,
                "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;",
                cx - 15, 40, 30, 60,
            )
        elif kind == "boundary":
            vertex(
                label,
                "shape=umlBoundary;whiteSpace=wrap;html=1;verticalLabelPosition=bottom;verticalAlign=top;outlineConnect=0;",
                cx - 30, 45, 90, 50,
            )
        elif kind == "control":
            vertex(
                label,
                "shape=umlControl;whiteSpace=wrap;html=1;verticalLabelPosition=bottom;verticalAlign=top;outlineConnect=0;",
                cx - 25, 45, 50, 50,
            )
        else:  # entity
            vertex(
                label,
                "shape=umlEntity;whiteSpace=wrap;html=1;verticalLabelPosition=bottom;verticalAlign=top;outlineConnect=0;",
                cx - 25, 45, 50, 50,
            )
        # Lifeline (vertical dashed)
        edge(cx, LIFELINE_TOP, cx, lifeline_bottom, dashed=True, arrow=False)

    # Messages + frames
    y = MSG_START_Y
    frame_stack: list[dict] = []
    frames_done: list[dict] = []

    for m in messages:
        kind = m[0]
        if kind == "FRAME":
            _, ftype, cond = m
            frame_stack.append({"type": ftype, "cond": cond, "y0": y, "divs": []})
            y += FRAME_OPEN_H
        elif kind == "DIV":
            _, cond = m
            frame_stack[-1]["divs"].append((y, cond))
            y += DIV_H
        elif kind == "END":
            f = frame_stack.pop()
            f["y1"] = y
            frames_done.append(f)
            y += FRAME_END_H
        else:
            frm, to, label, is_return = m
            x1 = px(pid_to_idx[frm])
            x2 = px(pid_to_idx[to])
            if frm == to:
                edge(x1, y, x1, y + 22, label=label, dashed=is_return,
                     waypoints=[(x1 + 70, y), (x1 + 70, y + 22)])
            else:
                edge(x1, y, x2, y, label=label, dashed=is_return)
            y += MSG_STEP

    # Draw frames behind-ish (drawn after but transparent fill)
    for f in frames_done:
        fy0, fy1 = f["y0"], f["y1"]
        vertex(
            "",
            "rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#000000;verticalAlign=top;align=left;",
            frame_left, fy0, frame_right - frame_left, fy1 - fy0,
        )
        # frame type label (alt / loop)
        vertex(
            f["type"],
            "shape=umlFrame;whiteSpace=wrap;html=1;width=50;height=22;fontStyle=1;",
            frame_left, fy0, 60, 24,
        )
        # first condition
        vertex(
            f["cond"],
            "text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;fontSize=11;fontStyle=2",
            frame_left + 70, fy0 + 2, 240, 20,
        )
        # dividers
        for dy, cond in f["divs"]:
            edge(frame_left, dy + 8, frame_right, dy + 8, dashed=True, arrow=False)
            vertex(
                cond,
                "text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;fontSize=11;fontStyle=2",
                frame_left + 20, dy + 10, 240, 20,
            )

    page_w = frame_right + 80
    page_h = total_h + 40

    return f"""<mxfile host="app.diagrams.net" agent="generate_sequence_drawio.py" version="24.7.0" type="device">
  <diagram id="{diagram['id']}" name="{esc(title)}">
    <mxGraphModel dx="1422" dy="794" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{page_w}" pageHeight="{page_h}" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
{chr(10).join(cells)}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
"""


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    for diagram in DIAGRAMS:
        out = OUTPUT_DIR / f"{diagram['id']}.drawio"
        out.write_text(build_drawio(diagram), encoding="utf-8")
        print(f"Wrote {out}")

    combined = ['<mxfile host="app.diagrams.net" agent="generate_sequence_drawio.py" version="24.7.0">']
    for diagram in DIAGRAMS:
        single = build_drawio(diagram)
        start = single.index("<diagram ")
        end = single.rindex("</diagram>") + len("</diagram>")
        combined.append(single[start:end])
    combined.append("</mxfile>")
    combined_path = OUTPUT_DIR / "SD_Gamifikasi_PAUD_All.drawio"
    combined_path.write_text("\n  ".join(combined), encoding="utf-8")
    print(f"Wrote {combined_path}")


if __name__ == "__main__":
    main()
