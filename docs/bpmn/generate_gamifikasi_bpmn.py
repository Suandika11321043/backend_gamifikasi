#!/usr/bin/env python3
"""Generate BPMN 2.0 sederhana (gaya dokumentasi TA) untuk Bizagi Modeler 4.x."""

import html
from pathlib import Path

TARGET_NS = "http://www.bizagi.com/gamifikasi/bpmn"


def esc(text: str) -> str:
    return html.escape(text, quote=True)


class SimpleBpmn:
    def __init__(self, process_id: str, pool_name: str, lane_name: str):
        self.process_id = process_id
        self.pool_name = pool_name
        self.lane_name = lane_name
        self.nodes: list[dict] = []
        self.flows: list[dict] = []

    def start(self, nid: str, name: str = "Mulai") -> str:
        self.nodes.append({"id": nid, "kind": "startEvent", "name": name})
        return nid

    def end(self, nid: str, name: str = "Selesai") -> str:
        self.nodes.append({"id": nid, "kind": "endEvent", "name": name})
        return nid

    def task(self, nid: str, name: str) -> str:
        self.nodes.append({"id": nid, "kind": "task", "name": name})
        return nid

    def gateway(self, nid: str, name: str = "", direction: str = "Diverging") -> str:
        """Exclusive Gateway (XOR) — Diverging = pecah, Converging = gabung."""
        self.nodes.append({
            "id": nid,
            "kind": "exclusiveGateway",
            "name": name,
            "direction": direction,
        })
        return nid

    def flow(self, fid: str, src: str, tgt: str, label: str = "") -> None:
        self.flows.append({"id": fid, "source": src, "target": tgt, "name": label})


def build_crud_management(
    process_id: str,
    pool_name: str,
    entity: str,
    menu_label: str,
    pre_steps: list[str] | None = None,
    extra_branches: list[tuple[str, str, list[str]]] | None = None,
) -> tuple[SimpleBpmn, dict[str, dict]]:
    """Pola: Akses website → Login → Menu → [pre] → CRUD → Tampilkan data."""
    m = SimpleBpmn(process_id, pool_name, "Admin (Guru)")
    pre_steps = pre_steps or []
    extra_branches = extra_branches or []

    m.start("Start")
    m.task("T_access", "Mengakses website")
    m.task("T_login", "Login")
    m.task("T_menu", menu_label)
    pre_ids = []
    for i, label in enumerate(pre_steps):
        pid = f"T_pre{i}"
        m.task(pid, label)
        pre_ids.append(pid)

    m.gateway("G_split", direction="Diverging")
    m.task("T_cari1", f"Mengklik kolom cari {entity.lower()}")
    m.task("T_cari2", "Memasukkan kriteria pencarian")
    m.task("T_add1", f"Memilih tombol tambah {entity.lower()}")
    m.task("T_add2", f"Mengisi data tambah {entity.lower()}")
    m.task("T_add3", "Memilih tombol simpan data")
    m.task("T_edit1", "Memilih tombol edit")
    m.task("T_edit2", f"Memperbarui data {entity.lower()}")
    m.task("T_edit3", "Pilih tombol simpan")
    m.task("T_del1", "Memilih tombol delete")
    m.gateway("G_del", direction="Diverging")
    m.task("T_del2", "Pilih tombol hapus")
    m.task("T_del3", "Pilih tombol batal")
    m.gateway("G_merge", direction="Converging")
    m.task("T_show", f"Menampilkan data {entity.lower()}")
    m.end("End")

    extra_task_ids: list[str] = []
    for flow_label, base_id, task_names in extra_branches:
        prev = None
        for i, tname in enumerate(task_names):
            tid = base_id if i == 0 else f"{base_id}_{i}"
            m.task(tid, tname)
            extra_task_ids.append(tid)
            if prev is None:
                m.flow(f"F_split_{base_id}", "G_split", tid, flow_label)
            else:
                m.flow(f"F_{prev}_{tid}", prev, tid)
            prev = tid
        if prev:
            m.flow(f"F_{prev}_merge", prev, "G_merge")

    chain = ["Start", "T_access", "T_login", "T_menu", *pre_ids, "G_split"]
    for a, b in zip(chain, chain[1:]):
        m.flow(f"F_{a}_{b}", a, b)

    m.flow("F_split_cari", "G_split", "T_cari1", "Cari")
    m.flow("F_cari1_cari2", "T_cari1", "T_cari2")
    m.flow("F_cari2_merge", "T_cari2", "G_merge")

    m.flow("F_split_add", "G_split", "T_add1", "Tambah")
    m.flow("F_add1_add2", "T_add1", "T_add2")
    m.flow("F_add2_add3", "T_add2", "T_add3")
    m.flow("F_add3_merge", "T_add3", "G_merge")

    m.flow("F_split_edit", "G_split", "T_edit1", "Edit")
    m.flow("F_edit1_edit2", "T_edit1", "T_edit2")
    m.flow("F_edit2_edit3", "T_edit2", "T_edit3")
    m.flow("F_edit3_merge", "T_edit3", "G_merge")

    m.flow("F_split_del", "G_split", "T_del1", "Hapus")
    m.flow("F_del1_gdel", "T_del1", "G_del")
    m.flow("F_gdel_hapus", "G_del", "T_del2", "Ya")
    m.flow("F_gdel_batal", "G_del", "T_del3", "Tidak")
    m.flow("F_del2_merge", "T_del2", "G_merge")
    m.flow("F_del3_merge", "T_del3", "G_merge")

    m.flow("F_merge_show", "G_merge", "T_show")
    m.flow("F_show_end", "T_show", "End")

    pos = layout_crud(pre_ids, extra_branches=extra_branches)
    return m, pos


def layout_crud(
    pre_ids: list[str],
    extra_branches: list[tuple[str, str, list[str]]] | None = None,
) -> dict[str, dict]:
    """Layout bercabang seperti diagram Mengelola Jemaat."""
    extra_branches = extra_branches or []
    extra_count = len(extra_branches)
    tw, th = 150, 70
    cy = 300
    x = 170
    pos: dict[str, dict] = {}

    pos["Start"] = {"x": x, "y": cy + 17, "w": 36, "h": 36}
    x += 80
    for nid in ["T_access", "T_login", "T_menu", *pre_ids]:
        pos[nid] = {"x": x, "y": cy, "w": tw, "h": th}
        x += tw + 30

    pos["G_split"] = {"x": x + 10, "y": cy - 5, "w": 50, "h": 50}
    bx = x + 90

    pos["T_cari1"] = {"x": bx, "y": 70, "w": tw, "h": th}
    pos["T_cari2"] = {"x": bx + tw + 25, "y": 70, "w": tw, "h": th}
    pos["T_add1"] = {"x": bx, "y": 155, "w": tw, "h": th}
    pos["T_add2"] = {"x": bx + tw + 25, "y": 155, "w": tw, "h": th}
    pos["T_add3"] = {"x": bx + 2 * (tw + 25), "y": 155, "w": tw, "h": th}
    pos["T_edit1"] = {"x": bx, "y": 240, "w": tw, "h": th}
    pos["T_edit2"] = {"x": bx + tw + 25, "y": 240, "w": tw, "h": th}
    pos["T_edit3"] = {"x": bx + 2 * (tw + 25), "y": 240, "w": tw, "h": th}

    del_y = 325 + extra_count * 85
    for i, (_, base_id, task_names) in enumerate(extra_branches):
        y = 325 + i * 85
        for j, _ in enumerate(task_names):
            tid = base_id if j == 0 else f"{base_id}_{j}"
            pos[tid] = {"x": bx + j * (tw + 25), "y": y, "w": tw, "h": th}

    pos["T_del1"] = {"x": bx, "y": del_y, "w": tw, "h": th}
    pos["G_del"] = {"x": bx + tw + 35, "y": del_y - 5, "w": 50, "h": 50}
    pos["T_del2"] = {"x": bx + tw + 110, "y": del_y - 30, "w": tw, "h": th}
    pos["T_del3"] = {"x": bx + tw + 110, "y": del_y + 40, "w": tw, "h": th}

    mx = bx + 3 * (tw + 25) + 60
    merge_y = cy + extra_count * 20
    pos["G_merge"] = {"x": mx, "y": merge_y - 5, "w": 50, "h": 50}
    pos["T_show"] = {"x": mx + 80, "y": merge_y, "w": tw, "h": th}
    pos["End"] = {"x": mx + tw + 110, "y": merge_y + 17, "w": 36, "h": 36}
    return pos


def layout_linear(nodes: list[dict], start_x=240, y=170, dx=150) -> dict[str, dict]:
    sizes = {
        "startEvent": (36, 36),
        "endEvent": (36, 36),
        "exclusiveGateway": (50, 50),
        "task": (140, 70),
    }
    pos = {}
    x = start_x
    for n in nodes:
        w, h = sizes[n["kind"]]
        ny = y - 5 if n["kind"] == "exclusiveGateway" else y
        pos[n["id"]] = {"x": x, "y": ny, "w": w, "h": h}
        x += dx
    return pos


def render(model: SimpleBpmn, positions: dict | None = None) -> str:
    pos = positions or layout_linear(model.nodes)
    participant_id = f"Pool_{model.process_id}"
    lane_id = f"Lane_{model.process_id}"

    xs = [p["x"] for p in pos.values()]
    ys = [p["y"] for p in pos.values()]
    ws = [p["x"] + p["w"] for p in pos.values()]
    hs = [p["y"] + p["h"] for p in pos.values()]
    pool_x, pool_y = 120, 60
    pool_w = max(ws) - min(xs) + 200
    pool_h = max(hs) - min(ys) + 140
    lane_x = pool_x + 30

    incoming: dict[str, list[str]] = {}
    outgoing: dict[str, list[str]] = {}
    for f in model.flows:
        outgoing.setdefault(f["source"], []).append(f["id"])
        incoming.setdefault(f["target"], []).append(f["id"])

    lines: list[str] = []
    a = lines.append

    a('<?xml version="1.0" encoding="UTF-8"?>')
    a(
        '<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
        'xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" '
        'xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" '
        'xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" '
        'xmlns:di="http://www.omg.org/spec/DD/20100524/DI" '
        f'id="Definitions_{model.process_id}" targetNamespace="{TARGET_NS}">'
    )

    a("  <bpmn:collaboration>")
    a(f'    <bpmn:participant id="{participant_id}" name="{esc(model.pool_name)}" processRef="{model.process_id}"/>')
    a("  </bpmn:collaboration>")

    a(f'  <bpmn:process id="{model.process_id}" isExecutable="false">')
    a("    <bpmn:laneSet>")
    a(f'      <bpmn:lane id="{lane_id}" name="{esc(model.lane_name)}">')
    for n in model.nodes:
        a(f'        <bpmn:flowNodeRef>{n["id"]}</bpmn:flowNodeRef>')
    a("      </bpmn:lane>")
    a("    </bpmn:laneSet>")

    for n in model.nodes:
        tag = n["kind"]
        name_attr = f' name="{esc(n["name"])}"' if n["name"] else ""
        dir_attr = ""
        if tag == "exclusiveGateway" and n.get("direction"):
            dir_attr = f' gatewayDirection="{n["direction"]}"'
        a(f'    <bpmn:{tag} id="{n["id"]}"{name_attr}{dir_attr}>')
        for fid in incoming.get(n["id"], []):
            a(f"      <bpmn:incoming>{fid}</bpmn:incoming>")
        for fid in outgoing.get(n["id"], []):
            a(f"      <bpmn:outgoing>{fid}</bpmn:outgoing>")
        a(f"    </bpmn:{tag}>")

    for f in model.flows:
        label = f' name="{esc(f["name"])}"' if f["name"] else ""
        a(f'    <bpmn:sequenceFlow id="{f["id"]}" sourceRef="{f["source"]}" targetRef="{f["target"]}"{label}/>')

    a("  </bpmn:process>")

    a(f'  <bpmndi:BPMNDiagram id="Diagram_{model.process_id}">')
    a(f'    <bpmndi:BPMNPlane id="Plane_{model.process_id}" bpmnElement="{participant_id}">')

    a(f'      <bpmndi:BPMNShape id="{participant_id}_di" bpmnElement="{participant_id}" isHorizontal="true">')
    a(f'        <dc:Bounds x="{pool_x}" y="{pool_y}" width="{pool_w}" height="{pool_h}"/>')
    a("      </bpmndi:BPMNShape>")

    a(f'      <bpmndi:BPMNShape id="{lane_id}_di" bpmnElement="{lane_id}" isHorizontal="true">')
    a(f'        <dc:Bounds x="{lane_x}" y="{pool_y}" width="{pool_w - 30}" height="{pool_h}"/>')
    a("      </bpmndi:BPMNShape>")

    for n in model.nodes:
        p = pos[n["id"]]
        a(f'      <bpmndi:BPMNShape id="{n["id"]}_di" bpmnElement="{n["id"]}">')
        a(f'        <dc:Bounds x="{p["x"]}" y="{p["y"]}" width="{p["w"]}" height="{p["h"]}"/>')
        a("      </bpmndi:BPMNShape>")

    for f in model.flows:
        sp, tp = pos[f["source"]], pos[f["target"]]
        sy = sp["y"] + sp["h"] // 2
        ty = tp["y"] + tp["h"] // 2

        # Loop ke kiri
        if tp["x"] < sp["x"]:
            top = min(sp["y"], tp["y"]) - 40
            a(f'      <bpmndi:BPMNEdge id="{f["id"]}_di" bpmnElement="{f["id"]}">')
            a(f'        <di:waypoint x="{sp["x"] + sp["w"] // 2}" y="{sp["y"]}"/>')
            a(f'        <di:waypoint x="{sp["x"] + sp["w"] // 2}" y="{top}"/>')
            a(f'        <di:waypoint x="{tp["x"] + tp["w"] // 2}" y="{top}"/>')
            a(f'        <di:waypoint x="{tp["x"] + tp["w"] // 2}" y="{tp["y"]}"/>')
            if f["name"]:
                a(f'        <bpmndi:BPMNLabel><dc:Bounds x="{(sp["x"]+tp["x"])//2}" y="{top - 18}" width="90" height="14"/></bpmndi:BPMNLabel>')
            a("      </bpmndi:BPMNEdge>")
        # Cabang vertikal menuju merge / gateway kanan
        elif abs(sp["y"] - ty) > 25 and tp["x"] > sp["x"]:
            ex = sp["x"] + sp["w"]
            mx = ex + 35
            ix = tp["x"]
            a(f'      <bpmndi:BPMNEdge id="{f["id"]}_di" bpmnElement="{f["id"]}">')
            a(f'        <di:waypoint x="{ex}" y="{sy}"/>')
            a(f'        <di:waypoint x="{mx}" y="{sy}"/>')
            a(f'        <di:waypoint x="{mx}" y="{ty}"/>')
            a(f'        <di:waypoint x="{ix}" y="{ty}"/>')
            if f["name"]:
                a(f'        <bpmndi:BPMNLabel><dc:Bounds x="{mx + 5}" y="{(sy + ty) // 2 - 8}" width="60" height="14"/></bpmndi:BPMNLabel>')
            a("      </bpmndi:BPMNEdge>")
        # Split gateway ke cabang atas/bawah
        elif f["source"] in ("G_split", "G_del") and abs(sp["y"] - ty) > 25:
            sx = sp["x"] + sp["w"]
            mid = sx + 25
            a(f'      <bpmndi:BPMNEdge id="{f["id"]}_di" bpmnElement="{f["id"]}">')
            a(f'        <di:waypoint x="{sx}" y="{sp["y"] + sp["h"] // 2}"/>')
            a(f'        <di:waypoint x="{mid}" y="{sp["y"] + sp["h"] // 2}"/>')
            a(f'        <di:waypoint x="{mid}" y="{ty}"/>')
            a(f'        <di:waypoint x="{tp["x"]}" y="{ty}"/>')
            if f["name"]:
                a(f'        <bpmndi:BPMNLabel><dc:Bounds x="{mid + 4}" y="{(sp["y"] + sp["h"] // 2 + ty) // 2 - 8}" width="50" height="14"/></bpmndi:BPMNLabel>')
            a("      </bpmndi:BPMNEdge>")
        else:
            a(f'      <bpmndi:BPMNEdge id="{f["id"]}_di" bpmnElement="{f["id"]}">')
            a(f'        <di:waypoint x="{sp["x"] + sp["w"]}" y="{sy}"/>')
            a(f'        <di:waypoint x="{tp["x"]}" y="{ty}"/>')
            if f["name"]:
                mx = (sp["x"] + sp["w"] + tp["x"]) // 2
                a(f'        <bpmndi:BPMNLabel><dc:Bounds x="{mx - 30}" y="{sy - 22}" width="60" height="14"/></bpmndi:BPMNLabel>')
            a("      </bpmndi:BPMNEdge>")

    a("    </bpmndi:BPMNPlane>")
    a("  </bpmndi:BPMNDiagram>")
    a("</bpmn:definitions>")
    return "\n".join(lines) + "\n"


# ── 8 Proses (gaya sederhana seperti contoh TA) ─────────────────────────────

def pb_login() -> SimpleBpmn:
    m = SimpleBpmn("ProsesLoginAdmin", "Proses Bisnis Login", "Admin (Guru)")
    m.start("Start")
    m.task("T1", "Mengakses website")
    m.task("T2", "Mengisi form login")
    m.task("T3", "Menekan button login")
    m.gateway("G1", direction="Diverging")
    m.task("T4", "Menampilkan dashboard")
    m.end("End")
    m.flow("F1", "Start", "T1")
    m.flow("F2", "T1", "T2")
    m.flow("F3", "T2", "T3")
    m.flow("F4", "T3", "G1")
    m.flow("F5", "G1", "T2", "data tidak valid")
    m.flow("F6", "G1", "T4", "data valid")
    m.flow("F7", "T4", "End")
    return m


def pb_logout() -> SimpleBpmn:
    m = SimpleBpmn("ProsesLogoutAdmin", "Proses Bisnis Logout", "Admin (Guru)")
    m.start("Start")
    m.task("T1", "Menekan tombol logout")
    m.task("T2", "Keluar dari sistem")
    m.task("T3", "Kembali ke halaman login")
    m.end("End")
    m.flow("F1", "Start", "T1")
    m.flow("F2", "T1", "T2")
    m.flow("F3", "T2", "T3")
    m.flow("F4", "T3", "End")
    return m


def pb_dashboard() -> SimpleBpmn:
    m = SimpleBpmn("ProsesDashboardAdmin", "Proses Bisnis Melihat Dashboard", "Admin (Guru)")
    m.start("Start")
    m.task("T1", "Membuka menu dashboard")
    m.task("T2", "Menampilkan statistik")
    m.task("T3", "Menampilkan leaderboard siswa")
    m.end("End")
    m.flow("F1", "Start", "T1")
    m.flow("F2", "T1", "T2")
    m.flow("F3", "T2", "T3")
    m.flow("F4", "T3", "End")
    return m


def pb_siswa() -> tuple[SimpleBpmn, dict]:
    return build_crud_management(
        "ProsesManajemenSiswa",
        "Mengelola Siswa",
        "Siswa",
        "Klik menu manajemen siswa",
        extra_branches=[
            ("Detail", "T_det1", ["Melihat detail siswa"]),
        ],
    )


def pb_tema() -> tuple[SimpleBpmn, dict]:
    return build_crud_management(
        "ProsesManajemenTema",
        "Mengelola Tema Pembelajaran",
        "Tema",
        "Klik menu manajemen tema",
        extra_branches=[
            ("Aktivasi", "T_akt1", ["Mengaktifkan / menonaktifkan tema"]),
            ("Detail", "T_det1", ["Melihat detail tema"]),
        ],
    )


def pb_soal() -> tuple[SimpleBpmn, dict]:
    return build_crud_management(
        "ProsesManajemenSoal",
        "Mengelola Soal",
        "Soal",
        "Klik menu manajemen soal",
        pre_steps=[
            "Memilih tema pembelajaran",
            "Memilih tanggal belajar",
        ],
    )


def pb_belajar() -> SimpleBpmn:
    m = SimpleBpmn("ProsesMemulaiPembelajaran", "Proses Bisnis Memulai Pembelajaran", "Siswa")
    m.start("Start")
    m.task("T1", "Menekan tombol mulai")
    m.task("T2", "Memilih profil siswa")
    m.task("T3", "Memilih tema pembelajaran")
    m.task("T4", "Memilih tanggal belajar")
    m.task("T5", "Menjawab soal")
    m.gateway("G1", direction="Diverging")
    m.task("T6", "Menampilkan hasil belajar")
    m.end("End")
    m.flow("F1", "Start", "T1")
    m.flow("F2", "T1", "T2")
    m.flow("F3", "T2", "T3")
    m.flow("F4", "T3", "T4")
    m.flow("F5", "T4", "T5")
    m.flow("F6", "T5", "G1")
    m.flow("F7", "G1", "T5", "Masih ada soal")
    m.flow("F8", "G1", "T6", "Selesai")
    m.flow("F9", "T6", "End")
    return m


def pb_review() -> SimpleBpmn:
    m = SimpleBpmn("ProsesReviewHasilBelajar", "Proses Bisnis Review Hasil Belajar", "Siswa")
    m.start("Start")
    m.task("T1", "Membuka tanggal yang sudah selesai")
    m.task("T2", "Meninjau jawaban per soal")
    m.gateway("G1", direction="Diverging")
    m.task("T3", "Menampilkan ringkasan skor")
    m.task("T4", "Kembali ke peta belajar")
    m.end("End")
    m.flow("F1", "Start", "T1")
    m.flow("F2", "T1", "T2")
    m.flow("F3", "T2", "G1")
    m.flow("F4", "G1", "T2", "Soal berikutnya")
    m.flow("F5", "G1", "T3", "Soal terakhir")
    m.flow("F6", "T3", "T4")
    m.flow("F7", "T4", "End")
    return m


BUILDERS = [
    ("PB_3211_Login_Admin.bpmn", pb_login, None),
    ("PB_3212_Logout_Admin.bpmn", pb_logout, None),
    ("PB_3213_Dashboard_Admin.bpmn", pb_dashboard, None),
    ("PB_3214_Manajemen_Siswa.bpmn", pb_siswa, "crud"),
    ("PB_3215_Manajemen_Tema.bpmn", pb_tema, "crud"),
    ("PB_3216_Manajemen_Soal.bpmn", pb_soal, "crud"),
    ("PB_3217_Memulai_Pembelajaran.bpmn", pb_belajar, None),
    ("PB_3218_Review_Hasil_Belajar.bpmn", pb_review, None),
]


def main() -> None:
    out = Path(__file__).parent
    for filename, builder, kind in BUILDERS:
        result = builder()
        if kind == "crud":
            model, positions = result
            xml = render(model, positions)
        else:
            model = result
            xml = render(model)
        path = out / filename
        path.write_text(xml, encoding="utf-8")
        print(f"Generated: {path}")


if __name__ == "__main__":
    main()
