#!/usr/bin/env python3
import json
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

paths = [
    'doc/CASO SEMESTRAL F EFT.docx',
    'doc/Informe Parcial 1 - Darko Rodriguez.docx'
]
output = {}
for p in paths:
    fp = Path(p)
    if not fp.exists():
        output[p] = None
        continue
    try:
        with zipfile.ZipFile(fp, 'r') as z:
            with z.open('word/document.xml') as docxml:
                tree = ET.parse(docxml)
                root = tree.getroot()
                ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
                texts = []
                for para in root.findall('.//w:p', ns):
                    parts = [t.text for t in para.findall('.//w:t', ns) if t.text]
                    if parts:
                        texts.append(''.join(parts))
                output[p] = '\n'.join(texts)
    except Exception as e:
        output[p] = f'ERROR: {e}'

print(json.dumps(output, ensure_ascii=False))
